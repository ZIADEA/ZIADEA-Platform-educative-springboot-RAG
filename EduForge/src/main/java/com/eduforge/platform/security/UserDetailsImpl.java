package com.eduforge.platform.security;

import com.eduforge.platform.domain.auth.AccountStatus;
import com.eduforge.platform.domain.auth.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String fullName;
    private final String email;
    private final String passwordHash;
    private final String role;
    private final AccountStatus status;

    public UserDetailsImpl(User u) {
        this.id = u.getId();
        this.fullName = u.getFullName();
        this.email = u.getEmail();
        this.passwordHash = u.getPasswordHash();
        this.role = u.getRole().name();
        this.status = u.getStatus();
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security attend souvent ROLE_*
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return status != AccountStatus.DISABLED; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return status == AccountStatus.ACTIVE; }
}
