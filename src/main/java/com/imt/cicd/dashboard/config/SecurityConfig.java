package com.imt.cicd.dashboard.config;

import com.imt.cicd.dashboard.model.User;
import com.imt.cicd.dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/pipelines/webhook").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .successHandler(successHandler()) // <--- AJOUT
                );
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            String email = oauthUser.getAttribute("email");
            String name = oauthUser.getAttribute("login"); // "login" pour GitHub

            // Sauvegarde ou mise à jour de l'utilisateur
            if (email != null) {
                User user = userRepository.findByEmail(email).orElse(new User());
                user.setEmail(email);
                user.setName(name);
                if (user.getRole() == null) user.setRole("DEV"); // Rôle par défaut
                userRepository.save(user);
            }

            // Redirection vers le front
            response.sendRedirect("http://localhost:3000");
        };
    }
}
