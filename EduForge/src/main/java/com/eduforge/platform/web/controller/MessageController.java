package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.auth.Role;
import com.eduforge.platform.domain.auth.User;
import com.eduforge.platform.domain.institution.Institution;
import com.eduforge.platform.domain.institution.MembershipStatus;
import com.eduforge.platform.domain.messaging.*;
import com.eduforge.platform.repository.InstitutionMembershipRepository;
import com.eduforge.platform.service.institution.InstitutionService;
import com.eduforge.platform.service.messaging.*;
import com.eduforge.platform.repository.UserRepository;
import com.eduforge.platform.util.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final NotificationService notificationService;
    private final UserRepository users;
    private final InstitutionService institutionService;
    private final InstitutionMembershipRepository membershipRepository;

    public MessageController(MessageService messageService,
                             NotificationService notificationService,
                             UserRepository users,
                             InstitutionService institutionService,
                             InstitutionMembershipRepository membershipRepository) {
        this.messageService = messageService;
        this.notificationService = notificationService;
        this.users = users;
        this.institutionService = institutionService;
        this.membershipRepository = membershipRepository;
    }

    @GetMapping
    public String inbox(Authentication auth, Model model) {
        return "redirect:/messages/inbox";
    }

    @GetMapping("/inbox")
    public String inboxList(Authentication auth, Model model) {
        Long userId = SecurityUtil.userId(auth);
        
        var messages = messageService.getInbox(userId);
        long unreadCount = messageService.getUnreadCount(userId);
        
        // Charger les noms des expéditeurs
        Map<Long, String> senderNames = new HashMap<>();
        for (var msg : messages) {
            if (msg.getSenderId() != null && !senderNames.containsKey(msg.getSenderId())) {
                users.findById(msg.getSenderId())
                    .ifPresent(u -> senderNames.put(msg.getSenderId(), u.getFullName()));
            }
        }

        model.addAttribute("pageTitle", "Messages reçus");
        model.addAttribute("messages", messages);
        model.addAttribute("senderNames", senderNames);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("activeTab", "inbox");
        return "messages/inbox";
    }

    @GetMapping("/sent")
    public String sentList(Authentication auth, Model model) {
        Long userId = SecurityUtil.userId(auth);
        
        var messages = messageService.getSent(userId);
        
        // Charger les noms des destinataires
        Map<Long, String> recipientNames = new HashMap<>();
        for (var msg : messages) {
            if (msg.getRecipientId() != null && !recipientNames.containsKey(msg.getRecipientId())) {
                users.findById(msg.getRecipientId())
                    .ifPresent(u -> recipientNames.put(msg.getRecipientId(), u.getFullName()));
            }
        }

        model.addAttribute("pageTitle", "Messages envoyés");
        model.addAttribute("messages", messages);
        model.addAttribute("recipientNames", recipientNames);
        model.addAttribute("activeTab", "sent");
        return "messages/sent";
    }

    @GetMapping("/compose")
    public String composeForm(Authentication auth,
                              @RequestParam(required = false) Long replyTo,
                              Model model) {
        Long userId = SecurityUtil.userId(auth);
        User currentUser = users.findById(userId).orElseThrow();
        
        // Récupérer les destinataires disponibles selon le rôle
        List<User> recipients = getAvailableRecipients(currentUser);
        
        model.addAttribute("pageTitle", "Nouveau message");
        model.addAttribute("recipients", recipients);
        model.addAttribute("currentUserRole", currentUser.getRole().name());
        
        if (replyTo != null) {
            // Pré-remplir pour réponse
            messageService.getInbox(userId).stream()
                .filter(m -> m.getId().equals(replyTo))
                .findFirst()
                .ifPresent(original -> {
                    model.addAttribute("replyToSubject", "Re: " + original.getSubject());
                    model.addAttribute("replyToId", original.getSenderId());
                });
        }

        return "messages/compose";
    }
    
    private List<User> getAvailableRecipients(User currentUser) {
        if (currentUser.getRole() == Role.INSTITUTION_MANAGER) {
            // Institution: peut contacter les profs et étudiants avec affiliation APPROVED
            try {
                Institution inst = institutionService.getOrCreateByOwner(currentUser.getId());
                var memberships = membershipRepository.findByInstitutionIdAndStatus(inst.getId(), MembershipStatus.APPROVED);
                List<Long> memberIds = memberships.stream()
                    .map(m -> m.getUserId())
                    .collect(Collectors.toList());
                if (memberIds.isEmpty()) return List.of();
                return users.findAllById(memberIds);
            } catch (Exception e) {
                return List.of();
            }
        } else if (currentUser.getRole() == Role.PROF) {
            // Prof: peut contacter ses étudiants et les autres profs
            return users.findByRoleIn(List.of(Role.PROF, Role.ETUDIANT));
        } else if (currentUser.getRole() == Role.ETUDIANT) {
            // Étudiant: peut contacter ses profs
            return users.findByRole(Role.PROF);
        } else if (currentUser.getRole() == Role.ADMIN) {
            // Admin: peut contacter tout le monde
            return users.findAll();
        }
        return List.of();
    }

    @PostMapping("/compose")
    public String send(Authentication auth,
                       @RequestParam Long receiverId,
                       @RequestParam String subject,
                       @RequestParam String content,
                       RedirectAttributes ra) {
        Long userId = SecurityUtil.userId(auth);
        
        try {
            messageService.sendDirect(userId, receiverId, subject, content);
            
            // Notifier le destinataire
            users.findById(userId).ifPresent(sender -> 
                    notificationService.notifyNewMessage(receiverId, sender.getFullName()));

            ra.addFlashAttribute("flashSuccess", "Message envoyé avec succès!");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Erreur lors de l'envoi: " + e.getMessage());
        }
        
        return "redirect:/messages/sent";
    }

    @GetMapping("/view/{messageId}")
    public String view(Authentication auth,
                       @PathVariable Long messageId,
                       Model model) {
        Long userId = SecurityUtil.userId(auth);
        
        // Marquer comme lu
        messageService.markAsRead(messageId, userId);

        var messages = messageService.getInbox(userId);
        var message = messages.stream()
                .filter(m -> m.getId().equals(messageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Message introuvable."));
        
        // Récupérer le nom de l'expéditeur
        String senderName = "Système";
        if (message.getSenderId() != null) {
            senderName = users.findById(message.getSenderId())
                    .map(User::getFullName)
                    .orElse("Utilisateur inconnu");
        }

        model.addAttribute("pageTitle", message.getSubject());
        model.addAttribute("message", message);
        model.addAttribute("senderName", senderName);
        return "messages/view";
    }

    @PostMapping("/delete/{messageId}")
    public String delete(Authentication auth,
                         @PathVariable Long messageId) {
        Long userId = SecurityUtil.userId(auth);
        messageService.delete(messageId, userId);
        return "redirect:/messages/inbox";
    }

    @PostMapping("/mark-all-read")
    public String markAllRead(Authentication auth) {
        Long userId = SecurityUtil.userId(auth);
        messageService.markAllAsRead(userId);
        return "redirect:/messages/inbox";
    }

    @GetMapping("/search")
    public String search(Authentication auth,
                         @RequestParam String q,
                         Model model) {
        Long userId = SecurityUtil.userId(auth);
        
        var results = messageService.searchMessages(userId, q);

        model.addAttribute("pageTitle", "Recherche: " + q);
        model.addAttribute("messages", results);
        model.addAttribute("searchQuery", q);
        return "messages/search";
    }

    // --- Notifications ---

    @GetMapping("/notifications")
    public String notifications(Authentication auth, Model model) {
        Long userId = SecurityUtil.userId(auth);
        
        var notifications = notificationService.getAll(userId);
        long unreadCount = notificationService.getUnreadCount(userId);

        model.addAttribute("pageTitle", "Notifications");
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("activeTab", "notifications");
        return "messages/notifications";
    }

    @PostMapping("/notifications/{notifId}/read")
    public String markNotificationRead(Authentication auth,
                                       @PathVariable Long notifId) {
        Long userId = SecurityUtil.userId(auth);
        notificationService.markAsRead(notifId, userId);
        return "redirect:/messages/notifications";
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsRead(Authentication auth) {
        Long userId = SecurityUtil.userId(auth);
        notificationService.markAllAsRead(userId);
        return "redirect:/messages/notifications";
    }

    @PostMapping("/notifications/clear")
    public String clearNotifications(Authentication auth) {
        Long userId = SecurityUtil.userId(auth);
        notificationService.deleteAll(userId);
        return "redirect:/messages/notifications";
    }

    // --- API endpoints pour AJAX (header notifications) ---

    @GetMapping("/api/unread-count")
    @ResponseBody
    public Map<String, Long> getUnreadCounts(Authentication auth) {
        Long userId = SecurityUtil.userId(auth);
        return Map.of(
                "messages", messageService.getUnreadCount(userId),
                "notifications", notificationService.getUnreadCount(userId)
        );
    }
}
