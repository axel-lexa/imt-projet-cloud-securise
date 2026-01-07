package com.imt.cicd.dashboard.config;

import com.imt.cicd.dashboard.model.User;
import com.imt.cicd.dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // MODIFICATION ICI : Ajoutez index.html et les assets statiques
                        .requestMatchers(
                                "/",
                                "/login",
                                "/index.html",       // INDISPENSABLE pour briser la boucle
                                "/assets/**",        // Pour les fichiers JS/CSS buildés par Vite
                                "/*.ico",            // Favicon
                                "/*.json",           // Manifests etc
                                "/error",
                                "/webjars/**",
                                "/api/pipelines/webhook",
                                "/api/health/**"     // Pour vérifier la santé de l'app et la BDD
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(successHandler())
                        // La redirection est gérée dans successHandler (response.sendRedirect(frontendUrl))
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("http://localhost:8081/")
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8081", "http://localhost:3000")); // URL du Frontend
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Important pour les cookies de session/OAuth2

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
                    if (user.getRole() == null) user.setRole("DEV");
                    userRepository.saveAndFlush(user);
                }

                response.sendRedirect(frontendUrl);
            } catch (Exception e) {
                response.sendError(500, "Erreur lors de la sauvegarde de l'utilisateur");
            }
        };
    }

    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<SimpleGrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (authority instanceof OAuth2UserAuthority oauth2User) {
                    Map<String, Object> attributes = oauth2User.getAttributes();

                    // Avec GitHub, l'email est souvent privé/null.
                    // On utilise le 'login' (pseudo) comme identifiant unique qu'on mappera sur le champ 'email' de notre User.
                    String gitHubLogin = (String) attributes.get("login");

                    // Recherche en BDD via le login
                    Optional<User> userOpt = userRepository.findByEmail(gitHubLogin);

                    if (userOpt.isPresent()) {
                        String role = userOpt.get().getRole();
                        mappedAuthorities.add(new SimpleGrantedAuthority(role));
                    } else {
                        // Rôle par défaut si pas en BDD
                        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    }
                }
            });
            return mappedAuthorities;
        };
    }
}
