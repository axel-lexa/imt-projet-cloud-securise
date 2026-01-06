package com.imt.cicd.dashboard.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@Service
public class QualityGateService {

    private final String SONAR_API = "http://localhost:9000/api/qualitygates/project_status?projectKey=";

    public void verifyQuality(String projectKey) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(SONAR_API + projectKey, Map.class);
            Map<String, Object> projectStatus = (Map<String, Object>) response.getBody().get("projectStatus");
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
