package com.imt.cicd.dashboard.controller;

import com.imt.cicd.dashboard.model.User;
import com.imt.cicd.dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:8081"})
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        Map<String, Object> userInfo = new HashMap<>();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof OAuth2User) {
                OAuth2User oauthUser = (OAuth2User) principal;
                Map<String, Object> attributes = oauthUser.getAttributes();

                String login = (String) attributes.get("login");
                String avatarUrl = (String) attributes.get("avatar_url");
                String name = (String) attributes.get("name");

                userInfo.put("login", login);
                userInfo.put("name", name != null ? name : login);
                userInfo.put("avatarUrl", avatarUrl);
                userInfo.put("authenticated", true);

                // Récupération du rôle depuis la BDD
                Optional<User> userOpt = userRepository.findByEmail(login);
                if (userOpt.isPresent()) {
                    userInfo.put("role", userOpt.get().getRole());
                } else {
                    userInfo.put("role", "USER");
                }
            }
        } else {
            userInfo.put("authenticated", false);
        }

        return userInfo;
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        return response;
    }
}
