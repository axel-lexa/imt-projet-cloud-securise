package com.imt.cicd.dashboard.config;

import com.imt.cicd.dashboard.model.User;
import com.imt.cicd.dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Configuration CORS pour autoriser le frontend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Désactivation CSRF (souvent nécessaire pour les APIs stateless/SPA simples)
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Gestion des accès par URL
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/index.html",
                                "/assets/**",
                                "/*.ico",
                                "/*.json",
                                "/error",
                                "/webjars/**",
                                "/api/pipelines/webhook",
                                "/api/health/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // 4. Gestion des erreurs API (403 au lieu de redirection Login)
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.FORBIDDEN),
                                request -> request.getServletPath().startsWith("/api/")
                        )
                )

                // 5. Configuration OAuth2 (Login GitHub)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("http://localhost:8081/login") // Redirection explicite si non connecté
                        .successHandler(successHandler())
                )

                // 6. Configuration du Logout (Nettoyage complet)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        // On remplace logoutSuccessUrl par ceci :
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Autoriser le port de développement (Vite) et de prod
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8081", "http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            try {
                OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                Map<String, Object> attributes = oauthUser.getAttributes();

                String login = (String) attributes.get("login");
                String email = (String) attributes.get("email");
                String githubId = attributes.get("id") != null ? String.valueOf(attributes.get("id")) : null;
                String identifier = login != null ? login : (email != null ? email : githubId);

                if (identifier != null) {
                    User user = userRepository.findByEmail(identifier).orElse(new User());
                    user.setEmail(identifier);
                    user.setName(login != null ? login : identifier);
                    // Rôle par défaut
                    if (user.getRole() == null) user.setRole("DEV");
                    userRepository.saveAndFlush(user);
                }

                // Redirection vers le frontend après login réussi
                response.sendRedirect(frontendUrl);
            } catch (Exception e) {
                response.sendError(500, "Erreur lors de la sauvegarde de l'utilisateur");
            }
        };
    }
}