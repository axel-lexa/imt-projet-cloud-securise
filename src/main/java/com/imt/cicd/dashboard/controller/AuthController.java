package com.imt.cicd.dashboard.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:8081"})
public class AuthController {

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
