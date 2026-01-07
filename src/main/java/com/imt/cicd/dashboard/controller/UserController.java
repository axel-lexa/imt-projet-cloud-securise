package com.imt.cicd.dashboard.controller;

import com.imt.cicd.dashboard.model.User;
import com.imt.cicd.dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:8081")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // (Bonus) Pour changer le rôle d'un user plus tard
    @PostMapping("/{id}/role")
    public User updateUserRole(@PathVariable Long id, @RequestBody String newRole) {
        return userRepository.findById(id).map(user -> {
            user.setRole(newRole);
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}