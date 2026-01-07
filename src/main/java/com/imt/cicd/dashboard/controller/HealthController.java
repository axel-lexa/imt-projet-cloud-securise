package com.imt.cicd.dashboard.controller;

import com.imt.cicd.dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/db")
    public Map<String, Object> checkDatabase() {
        try {
            Connection connection = dataSource.getConnection();
            String dbUrl = connection.getMetaData().getURL();
            String dbName = connection.getMetaData().getDatabaseProductName();
            connection.close();

            long userCount = userRepository.count();

            System.out.println("✅ Connexion à la base de données réussie");
            System.out.println("   📊 URL: " + dbUrl);
            System.out.println("   🗄️  Type: " + dbName);
            System.out.println("   👥 Nombre d'utilisateurs: " + userCount);

            return Map.of(
                "status", "CONNECTED",
                "database_url", dbUrl,
                "database_type", dbName,
                "user_count", userCount
            );
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion à la base de données: " + e.getMessage());
            e.printStackTrace();
            return Map.of(
                "status", "ERROR",
                "error", e.getMessage()
            );
        }
    }
}

