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
                                "/api/users/debug",  // Pour déboguer l'état de la BDD
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

                System.out.println("📍 SUCCESS HANDLER - Authentification OAuth2 reçue");
                System.out.println("📊 Attributs GitHub reçus: " + attributes);

                // Identifiants GitHub : login est toujours présent, email peut être null/privé, id est un fallback sûr
                String login = (String) attributes.get("login");
                String email = (String) attributes.get("email");
                String githubId = attributes.get("id") != null ? String.valueOf(attributes.get("id")) : null;

                // Identifiant unique pour notre User : login, sinon email, sinon id GitHub
                String identifier = login != null ? login : (email != null ? email : githubId);

                System.out.println("👤 Login: " + login);
                System.out.println("📧 Email: " + email);
                System.out.println("🆔 GitHub ID: " + githubId);
                System.out.println("🔑 Identifiant retenu: " + identifier);

                if (identifier != null) {
                    long before = userRepository.count();

                    User user = userRepository.findByEmail(identifier).orElse(new User());
                    user.setEmail(identifier); // on stocke l’identifiant unique dans le champ email
                    user.setName(login != null ? login : (identifier != null ? identifier : "Unknown"));
                    if (user.getRole() == null) user.setRole("DEV"); // rôle par défaut

                    // saveAndFlush pour forcer l’écriture immédiate en BDD
                    User savedUser = userRepository.saveAndFlush(user);
                    long after = userRepository.count();

                    System.out.println("✅ Utilisateur sauvegardé/mis à jour: " + savedUser.getName()
                            + " (ID: " + savedUser.getId() + ", Rôle: " + savedUser.getRole() + ")");
                    System.out.println("📈 Compteur users avant/after: " + before + " -> " + after);
                } else {
                    System.out.println("❌ Impossible de récupérer un identifiant (login/email/id vides)");
                }

                // Redirection vers le front
                response.sendRedirect(frontendUrl);
            } catch (Exception e) {
                System.err.println("❌ Erreur dans successHandler: " + e.getMessage());
                e.printStackTrace();
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
