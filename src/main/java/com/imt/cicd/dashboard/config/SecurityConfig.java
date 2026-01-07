package com.imt.cicd.dashboard.config;

import com.imt.cicd.dashboard.model.User;
import com.imt.cicd.dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
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

import java.util.Arrays;
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
                // 1. Activation de CORS pour autoriser le Frontend (port 5173)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. Désactivation CSRF (pour simplifier les appels API, à réactiver en prod idéalement)
                .csrf(csrf -> csrf.disable())
                // 3. Configuration des autorisations
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/error", "/webjars/**").permitAll() // Pages publiques
                        .requestMatchers("/api/pipelines/webhook").permitAll() // Webhook GitHub doit être public
                        .anyRequest().authenticated() // Tout le reste nécessite d'être connecté
                )
                // 4. Configuration OAuth2
                .oauth2Login(oauth2 -> oauth2
                        // Après un login réussi via GitHub, on redirige vers le dashboard React
                        .defaultSuccessUrl("http://localhost:8081/dashboard", true)
                )
                // 5. Gestion de la déconnexion
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
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauthUser.getAttributes();

            // GitHub 'login' is the username. 'email' might be null if private.
            String login = (String) attributes.get("login");
            String email = (String) attributes.get("email");

            // Use login as the identifier to match userAuthoritiesMapper logic
            // Fallback to email if login is somehow null
            String identifier = (login != null) ? login : email;

            if (identifier != null) {
                User user = userRepository.findByEmail(identifier).orElse(new User());
                user.setEmail(identifier); // Storing identifier (login) in email field
                user.setName(login != null ? login : "Unknown");
                if (user.getRole() == null) user.setRole("DEV"); // Rôle par défaut
                userRepository.save(user);
            }

            // Redirection vers le front
            response.sendRedirect(frontendUrl);
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

                    System.out.println("🔍 Connexion GitHub : " + gitHubLogin);

                    // Recherche en BDD via le login
                    Optional<User> userOpt = userRepository.findByEmail(gitHubLogin);

                    if (userOpt.isPresent()) {
                        String role = userOpt.get().getRole();
                        mappedAuthorities.add(new SimpleGrantedAuthority(role));
                        System.out.println("✅ Utilisateur trouvé. Rôle : " + role);
                    } else {
                        // Rôle par défaut si pas en BDD
                        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                        System.out.println("⚠️ Utilisateur inconnu en BDD -> ROLE_USER");
                    }
                }
            });
            return mappedAuthorities;
        };
    }
}
