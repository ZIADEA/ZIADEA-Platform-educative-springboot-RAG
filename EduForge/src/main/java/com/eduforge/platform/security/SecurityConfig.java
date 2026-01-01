package com.eduforge.platform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthSuccessHandler successHandler;

    public SecurityConfig(AuthSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                // Public
                .requestMatchers("/", "/catalog", "/catalog/**", "/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/favicon.ico", "/favicon.svg").permitAll()
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/error/**").permitAll()
                .requestMatchers("/api/institutions/search").permitAll()

                // Rôles spécifiques
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/institution/**").hasRole("INSTITUTION_MANAGER")
                .requestMatchers("/prof/**").hasRole("PROF")
                .requestMatchers("/student/{studentId}/profile").hasAnyRole("PROF", "ETUDIANT", "INSTITUTION_MANAGER", "ADMIN")
                .requestMatchers("/student/**").hasRole("ETUDIANT")
                .requestMatchers("/stats/admin/**").hasRole("ADMIN")
                .requestMatchers("/stats/prof/**").hasRole("PROF")
                
                // Routes communes authentifiées
                .requestMatchers("/dashboard").authenticated()
                .requestMatchers("/course/**").authenticated()
                .requestMatchers("/messages/**").authenticated()
                .requestMatchers("/certificates/**").authenticated()
                .requestMatchers("/my-institutions/**").hasAnyRole("PROF", "ETUDIANT")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // NoOpPasswordEncoder pour le développement - mots de passe en clair
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }
}