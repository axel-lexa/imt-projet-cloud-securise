package com.imt.cicd.dashboard.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class QualityGateService {

    private final String SONAR_API = "http://localhost:9000/api/qualitygates/project_status?projectKey=";
    
    // Identifiants SonarQube (à externaliser idéalement)
    private final String USERNAME = "admin";
    private final String PASSWORD = "admin123"; // Mettez "admin" si vous n'avez pas changé le mdp

    public void verifyQuality(String projectKey) {
        RestTemplate restTemplate = new RestTemplate();
        
        // Création du header d'authentification Basic Auth
        String auth = USERNAME + ":" + PASSWORD;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Appel API avec authentification
            ResponseEntity<Map> response = restTemplate.exchange(
                    SONAR_API + projectKey, 
                    HttpMethod.GET, 
                    entity, 
                    Map.class
            );
            
            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new RuntimeException("Réponse vide de SonarQube");
            }

            Map<String, Object> projectStatus = (Map<String, Object>) body.get("projectStatus");
            String status = (String) projectStatus.get("status");

            if ("ERROR".equals(status)) {
                throw new RuntimeException("⛔ ROLLBACK : Quality Gate échoué ! Code trop sale.");
            }
            System.out.println("✅ Quality Gate validé.");
        } catch (Exception e) {
            throw new RuntimeException("Erreur SonarQube : " + e.getMessage());
        }
    }
}
