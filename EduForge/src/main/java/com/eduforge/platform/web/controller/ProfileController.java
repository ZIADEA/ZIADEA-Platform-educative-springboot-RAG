package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.auth.User;
import com.eduforge.platform.repository.InstitutionRepository;
import com.eduforge.platform.repository.UserRepository;
import com.eduforge.platform.util.SecurityUtil;
import com.eduforge.platform.web.dto.forms.UserProfileForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InstitutionRepository institutionRepository;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder, InstitutionRepository institutionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.institutionRepository = institutionRepository;
    }

    @GetMapping
    public String showProfile(Authentication auth, Model model) {
        Long userId = SecurityUtil.userId(auth);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Charger nom institution si affilié
        if (user.getInstitutionId() != null) {
            institutionRepository.findById(user.getInstitutionId())
                .ifPresent(inst -> user.setInstitutionName(inst.getName()));
        }

        UserProfileForm form = new UserProfileForm();
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setBio(user.getBio());

        model.addAttribute("pageTitle", "Mon profil");
        model.addAttribute("user", user);
        model.addAttribute("form", form);
        return "profile/view";
    }

    @PostMapping
    public String updateProfile(Authentication auth,
                                @Valid @ModelAttribute("form") UserProfileForm form,
                                BindingResult br,
                                Model model,
                                RedirectAttributes ra) {
        Long userId = SecurityUtil.userId(auth);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Vérification changement de mot de passe
        boolean changingPassword = form.getNewPassword() != null && !form.getNewPassword().isBlank();
        
        if (changingPassword) {
            // Vérifier mot de passe actuel
            if (form.getCurrentPassword() == null || form.getCurrentPassword().isBlank()) {
                br.rejectValue("currentPassword", "error.form", "Le mot de passe actuel est requis");
            } else if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPasswordHash())) {
                br.rejectValue("currentPassword", "error.form", "Mot de passe actuel incorrect");
            }

            // Vérifier confirmation
            if (!form.getNewPassword().equals(form.getConfirmPassword())) {
                br.rejectValue("confirmPassword", "error.form", "Les mots de passe ne correspondent pas");
            }
        }

        // Vérifier unicité email si changé
        if (!user.getEmail().equalsIgnoreCase(form.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(form.getEmail())) {
                br.rejectValue("email", "error.form", "Cet email est déjà utilisé");
            }
        }

        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Mon profil");
            model.addAttribute("user", user);
            return "profile/view";
        }

        // Mise à jour des champs
        user.setFullName(form.getFullName());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setBio(form.getBio());

        if (changingPassword) {
            user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        }

        userRepository.save(user);

        ra.addFlashAttribute("flashSuccess", "Profil mis à jour avec succès");
        return "redirect:/profile";
    }
}
