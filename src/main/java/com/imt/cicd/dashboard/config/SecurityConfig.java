package com.imt.cicd.dashboard.config;

import com.imt.cicd.dashboard.model.User;
import com.imt.cicd.dashboard.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/error").permitAll()
                        // Seul le rôle ADMIN en base de données peut déployer
                        .requestMatchers("/api/deploy/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userAuthoritiesMapper(userAuthoritiesMapper())
                        )
                );
        return http.build();
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