package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.auth.AffiliationStatus;
import com.eduforge.platform.domain.auth.User;
import com.eduforge.platform.domain.institution.Institution;
import com.eduforge.platform.domain.institution.InstitutionMembership;
import com.eduforge.platform.domain.institution.MembershipStatus;
import com.eduforge.platform.repository.InstitutionMembershipRepository;
import com.eduforge.platform.repository.InstitutionRepository;
import com.eduforge.platform.repository.UserRepository;
import com.eduforge.platform.util.SecurityUtil;
import com.eduforge.platform.web.dto.forms.AffiliationRequestForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/affiliation")
public class AffiliationController {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final InstitutionMembershipRepository membershipRepository;

    public AffiliationController(UserRepository userRepository,
                                  InstitutionRepository institutionRepository,
                                  InstitutionMembershipRepository membershipRepository) {
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.membershipRepository = membershipRepository;
    }

    @GetMapping("/request")
    public String showRequestForm(Authentication auth, Model model) {
        Long userId = SecurityUtil.userId(auth);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Vérifier si l'utilisateur a déjà une demande en cours
        List<InstitutionMembership> pendingRequests = membershipRepository.findByUserId(userId);
        boolean hasPendingRequest = pendingRequests.stream()
                .anyMatch(m -> m.getStatus() == MembershipStatus.PENDING);

        if (user.isAffiliated()) {
            model.addAttribute("flashError", "Vous êtes déjà affilié à une institution.");
            return "redirect:/profile";
        }

        List<Institution> institutions = institutionRepository.findAll();

        model.addAttribute("pageTitle", "Demande d'affiliation");
        model.addAttribute("user", user);
        model.addAttribute("institutions", institutions);
        model.addAttribute("hasPendingRequest", hasPendingRequest);
        model.addAttribute("pendingRequests", pendingRequests.stream()
                .filter(m -> m.getStatus() == MembershipStatus.PENDING)
                .toList());
        model.addAttribute("form", new AffiliationRequestForm());
        return "profile/affiliation-request";
    }

    @PostMapping("/request")
    public String submitRequest(Authentication auth,
                                @Valid @ModelAttribute("form") AffiliationRequestForm form,
                                BindingResult br,
                                Model model,
                                RedirectAttributes ra) {
        Long userId = SecurityUtil.userId(auth);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (user.isAffiliated()) {
            ra.addFlashAttribute("flashError", "Vous êtes déjà affilié à une institution.");
            return "redirect:/profile";
        }

        // Vérifier si demande déjà existante pour cette institution
        if (membershipRepository.existsByInstitutionIdAndUserIdAndStatus(
                form.getInstitutionId(), userId, MembershipStatus.PENDING)) {
            ra.addFlashAttribute("flashError", "Vous avez déjà une demande en attente pour cette institution.");
            return "redirect:/affiliation/request";
        }

        if (br.hasErrors()) {
            List<Institution> institutions = institutionRepository.findAll();
            model.addAttribute("pageTitle", "Demande d'affiliation");
            model.addAttribute("user", user);
            model.addAttribute("institutions", institutions);
            model.addAttribute("hasPendingRequest", false);
            return "profile/affiliation-request";
        }

        // Créer la demande d'affiliation
        InstitutionMembership membership = new InstitutionMembership(
                form.getInstitutionId(), userId, MembershipStatus.PENDING);
        membershipRepository.save(membership);

        // Mettre à jour le statut de l'utilisateur
        user.setAffiliationStatus(AffiliationStatus.PENDING);
        user.setInstitutionId(form.getInstitutionId());
        userRepository.save(user);

        ra.addFlashAttribute("flashSuccess", "Votre demande d'affiliation a été envoyée.");
        return "redirect:/profile";
    }

    @PostMapping("/cancel/{id}")
    public String cancelRequest(Authentication auth,
                                @PathVariable Long id,
                                RedirectAttributes ra) {
        Long userId = SecurityUtil.userId(auth);
        
        InstitutionMembership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        if (!membership.getUserId().equals(userId)) {
            ra.addFlashAttribute("flashError", "Action non autorisée.");
            return "redirect:/profile";
        }

        if (membership.getStatus() != MembershipStatus.PENDING) {
            ra.addFlashAttribute("flashError", "Cette demande ne peut pas être annulée.");
            return "redirect:/profile";
        }

        membershipRepository.delete(membership);

        // Réinitialiser le statut utilisateur si c'était la seule demande
        User user = userRepository.findById(userId).orElseThrow();
        List<InstitutionMembership> remaining = membershipRepository.findByUserId(userId);
        if (remaining.stream().noneMatch(m -> m.getStatus() == MembershipStatus.PENDING)) {
            user.setAffiliationStatus(AffiliationStatus.INDEPENDENT);
            user.setInstitutionId(null);
            userRepository.save(user);
        }

        ra.addFlashAttribute("flashSuccess", "Demande d'affiliation annulée.");
        return "redirect:/affiliation/request";
    }
}
