package com.imt.cicd.dashboard.controller;

import com.imt.cicd.dashboard.model.User;
import com.imt.cicd.dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:8081")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        System.out.println("📋 GET /api/users - Nombre d'utilisateurs: " + users.size());
        for (User user : users) {
            System.out.println("   👤 " + user.getName() + " (" + user.getEmail() + ") - Rôle: " + user.getRole());
        }
        return users;
    }

    @GetMapping("/debug")
    public List<User> debugAllUsers() {
        long count = userRepository.count();
        List<User> users = userRepository.findAll();
        System.out.println("🐛 [DEBUG] GET /api/users/debug - Total utilisateurs en BDD (count) : " + count);
        System.out.println("🐛 [DEBUG] GET /api/users/debug - Total utilisateurs en BDD (list size): " + users.size());
        users.forEach(u -> System.out.println("   👤 ID=" + u.getId() + ", Email=" + u.getEmail() + ", Name=" + u.getName() + ", Role=" + u.getRole()));
        return users;
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🔐 GET /api/users/me - Authentification: " + authentication);

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauthUser.getAttributes();
            System.out.println("📊 Utilisateur actuel (OAuth2): " + attributes);
            return Map.of(
                "authenticated", true,
                "principal", attributes,
                "authorities", authentication.getAuthorities()
            );
        }

        System.out.println("❌ Aucun utilisateur authentifié");
        return Map.of("authenticated", false);
    }

    // (Bonus) Pour changer le rôle d'un user plus tard
    @PostMapping("/{id}/role")
    public User updateUserRole(@PathVariable Long id, @RequestBody String newRole) {
        System.out.println("📝 POST /api/users/{id}/role - ID: " + id + ", Nouveau rôle: " + newRole);
        return userRepository.findById(id).map(user -> {
            user.setRole(newRole);
            User updatedUser = userRepository.save(user);
            System.out.println("✅ Rôle de " + user.getName() + " mis à jour vers " + newRole);
            return updatedUser;
        }).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}