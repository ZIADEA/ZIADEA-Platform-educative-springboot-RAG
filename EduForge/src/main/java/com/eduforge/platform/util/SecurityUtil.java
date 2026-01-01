package com.eduforge.platform.util;

import com.eduforge.platform.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;

public final class SecurityUtil {
    private SecurityUtil() {}

    public static Long userId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl p)) {
            throw new IllegalStateException("Utilisateur non authentifié");
        }
        return p.getId();
    }

    public static String fullName(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl p)) {
            return "";
        }
        return p.getFullName();
    }
}
