package com.eduforge.platform.security;

import com.eduforge.platform.domain.auth.User;
import com.eduforge.platform.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository users;

    public UserDetailsServiceImpl(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = users.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        return new UserDetailsImpl(u);
    }
}
