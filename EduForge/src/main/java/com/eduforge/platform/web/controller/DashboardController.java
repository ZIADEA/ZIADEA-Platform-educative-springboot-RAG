package com.eduforge.platform.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isProf = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROF"));
        boolean isStudent = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ETUDIANT"));
        boolean isInst = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUTION_MANAGER"));

        if (isAdmin) return "redirect:/admin/dashboard";
        if (isInst) return "redirect:/institution/dashboard";
        if (isProf) return "redirect:/prof/dashboard";
        if (isStudent) return "redirect:/student/dashboard";
        
        return "redirect:/";
    }
}