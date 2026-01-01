package com.eduforge.platform.web.controller;

import com.eduforge.platform.service.auth.RegistrationService;
import com.eduforge.platform.web.dto.forms.RegisterForm;
import jakarta.validation.Valid;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final RegistrationService registration;

    public AuthController(RegistrationService registration) {
        this.registration = registration;
    }

    @GetMapping("/login")
    public String login(Authentication auth, Model model,
                        @RequestParam(value = "error", required = false) String error) {

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        model.addAttribute("pageTitle", "Connexion");
        model.addAttribute("hasError", error != null);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("pageTitle", "Créer un compte");
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("form") RegisterForm form,
                                 BindingResult br,
                                 Model model) {
        model.addAttribute("pageTitle", "Créer un compte");

        if (br.hasErrors()) {
            return "auth/register";
        }
        try {
            registration.register(form);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("formError", ex.getMessage());
            return "auth/register";
        }
    }
}
