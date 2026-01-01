package com.eduforge.platform.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws ServletException, IOException {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isProf = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROF"));
        boolean isStudent = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ETUDIANT"));
        boolean isInst = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUTION_MANAGER"));

        if (isAdmin) {
            getRedirectStrategy().sendRedirect(request, response, "/admin/dashboard");
            return;
        }
        if (isInst) {
            getRedirectStrategy().sendRedirect(request, response, "/institution/dashboard");
            return;
        }
        if (isProf) {
            getRedirectStrategy().sendRedirect(request, response, "/prof/dashboard");
            return;
        }
        if (isStudent) {
            getRedirectStrategy().sendRedirect(request, response, "/student/dashboard");
            return;
        }
        getRedirectStrategy().sendRedirect(request, response, "/");
    }
}
