package com.imt.cicd.dashboard.config;

import com.imt.cicd.dashboard.model.User;
import com.imt.cicd.dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                // Désactive la protection CSRF pour faciliter les tests via Postman/Curl
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/pipelines/webhook", "/api/pipelines/run").permitAll() // Autoriser /run sans auth pour test
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userAuthoritiesMapper(userAuthoritiesMapper())
                        )
                        .successHandler(successHandler())
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
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

            // Redirection vers le front (Vite default port is 5173)
            response.sendRedirect("http://localhost:5173");
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
                        System.out.println("✅ Utilisateur trouvé. Rôle : "+ role);
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
