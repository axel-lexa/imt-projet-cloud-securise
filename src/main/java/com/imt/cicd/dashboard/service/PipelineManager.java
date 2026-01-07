package com.imt.cicd.dashboard.service;

import com.imt.cicd.dashboard.model.PipelineExecution;
import com.imt.cicd.dashboard.model.PipelineStatus;
import com.imt.cicd.dashboard.repository.PipelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PipelineManager {

    private final GitService gitService;
    private final CommandService commandService;
    private final SshService sshService;
    private final PipelineRepository repository;
    private final QualityGateService qualityGateService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${sonar.url:http://localhost:9000}")
    private String sonarUrl;

    private void saveAndNotify(PipelineExecution execution) {
        repository.save(execution);
        messagingTemplate.convertAndSend("/topic/pipeline/" + execution.getId(), execution);
    }

    @Async
    public void runPipeline(Long executionId) {
        PipelineExecution execution = repository.findById(executionId).orElseThrow();
        File tempDir = new File("temp-workspace/" + executionId);

        try {
            execution.setStatus(PipelineStatus.RUNNING);
            execution.setStartTime(LocalDateTime.now());
            saveAndNotify(execution);

            // 1. GIT CLONE
            execution.appendLog("--- ÉTAPE 1: CLONAGE DU DÉPÔT ---");
            saveAndNotify(execution);
            gitService.cloneRepository(execution.getRepoUrl(), execution.getBranch(), tempDir);

            // 2. MAVEN BUILD
            execution.appendLog("--- ÉTAPE 2: BUILD & TEST MAVEN ---");
            saveAndNotify(execution);
            commandService.executeCommand("chmod +x mvnw", tempDir, execution);
            commandService.executeCommand("./mvnw clean package", tempDir, execution);

            // 2.5 ANALYSE SONARQUBE
            execution.appendLog("--- ÉTAPE 2.5: ANALYSE QUALITÉ ---");
            String repoUrl = execution.getRepoUrl();
            String projectKey = repoUrl.substring(repoUrl.lastIndexOf("/") + 1).replace(".git", "").replaceAll("[^a-zA-Z0-9-_]", "-");

            String sonarCmd = "./mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184:sonar " +
                    "-Dsonar.projectKey=" + projectKey + " " +
                    "-Dsonar.host.url=" + sonarUrl + " " +
                    "-Dsonar.login=admin " +
                    "-Dsonar.password=admin123 " +
                    "-Dorg.slf4j.simpleLogger.defaultLogLevel=warn";

            commandService.executeCommand(sonarCmd, tempDir, execution, true);
            qualityGateService.verifyQuality(projectKey);
            execution.appendLog("✅ Quality Gate validé par SonarQube.");

            // 3. DOCKER BUILD
            execution.appendLog("--- ÉTAPE 3: BUILD DOCKER ---");
            saveAndNotify(execution);
            String imageNameId = "mon-app-metier:" + execution.getId();
            String imageNameLatest = "mon-app-metier:latest";
            commandService.executeCommand("docker build -t " + imageNameId + " -t " + imageNameLatest + " .", tempDir, execution);

            execution.appendLog("--- SAUVEGARDE IMAGE ---");
            saveAndNotify(execution);
            commandService.executeCommand("docker save -o app.tar " + imageNameId + " " + imageNameLatest, tempDir, execution);

            // 4. TRANSFERT SSH
            execution.appendLog("--- TRANSFERT VERS LA VM ---");
            saveAndNotify(execution);
            File imageFile = new File(tempDir, "app.tar");
            File composeFile = new File(tempDir, "docker-compose.yml");
            String remoteHome = "/home/" + sshService.getUser();

            if (imageFile.exists()) sshService.transferFile(imageFile, remoteHome + "/app.tar");
            if (composeFile.exists()) sshService.transferFile(composeFile, remoteHome + "/docker-compose.yml");

            // 5. DÉPLOIEMENT
            // 5. DÉPLOIEMENT
            execution.appendLog("--- ÉTAPE 4: DÉPLOIEMENT SSH ---");
            saveAndNotify(execution);

            // CORRECTION ICI : On tue tout ce qui peut gêner (l'ancien nom ET le nouveau nom)
            String deployCmd =
                    // 1. On force la suppression de l'ancien conteneur s'il traîne
                    "docker rm -f app-metier || true && " +
                            // 2. On force la suppression du nouveau conteneur s'il existe déjà
                            "docker rm -f IMT-ArchitectureLogicielle-app || true && " +
                            // 3. On charge la nouvelle image
                            "docker load -i " + remoteHome + "/app.tar && " +
                            // 4. On redémarre proprement avec docker compose
                            // Le 'down' va nettoyer le réseau, le 'up' va tout recréer
                            "docker compose -f " + remoteHome + "/docker-compose.yml down || true && " +
                            "docker compose -f " + remoteHome + "/docker-compose.yml up -d";

            sshService.executeRemoteCommand(deployCmd, execution);

            // =================================================================
            // 6. TEST D'INTRUSION (PENTEST)
            // =================================================================
            // CORRECTION 1 : Le marqueur doit correspondre exactement à celui du Frontend ("--- PENTEST ---")
            execution.appendLog("--- PENTEST --- : LANCEMENT OWASP ZAP");
            saveAndNotify(execution);

            Thread.sleep(15000); // Attente démarrage app

            // CORRECTION 2 : Utilisation du vrai nom du conteneur (IMT-ArchitectureLogicielle-app)
            String zapCmd = "docker run --rm " +
                    "--network cicd-network " +
                    "ghcr.io/zaproxy/zaproxy:stable " +
                    "zap-baseline.py " +
                    "-t http://IMT-ArchitectureLogicielle-app:8080 " +
                    "-I";

            try {
                execution.appendLog("Exécution du scan de vulnérabilités...");

                // --- LIGNE À SUPPRIMER ---
                // sshService.executeRemoteCommand("docker network connect cicd-network IMT-ArchitectureLogicielle-app", execution);
                // -------------------------

                // On exécute DIRECTEMENT la commande ZAP
                sshService.executeRemoteCommand(zapCmd, execution);

                execution.appendLog("✅ Pentest validé : Aucune faille critique détectée.");
            } catch (Exception e) {
                // Si ZAP échoue (trouve des failles), on passe ici
                throw new RuntimeException("⛔ PENTEST ÉCHOUÉ : Failles de sécurité détectées ! " + e.getMessage());
            }
            // =================================================================

            execution.setStatus(PipelineStatus.SUCCESS);

        } catch (Exception e) {
            execution.setStatus(PipelineStatus.FAILED);
            execution.appendLog("ERREUR CRITIQUE: " + e.getMessage());

            // ROLLBACK
            execution.appendLog("--- TENTATIVE DE ROLLBACK ---");
            saveAndNotify(execution);
            var lastSuccessOpt = repository.findFirstByRepoUrlAndStatusOrderByStartTimeDesc(execution.getRepoUrl(), PipelineStatus.SUCCESS);

            if (lastSuccessOpt.isPresent()) {
                String previousImageTag = "mon-app-metier:" + lastSuccessOpt.get().getId();
                String remoteHome = "/home/" + sshService.getUser();
                String connectionString = "mongodb://user:pass@db:27017/carrentaldb?authSource=admin";

                try {
                    // Rollback plus agressif avec rm -f
                    String rollbackCmd =
                            "docker rm -f IMT-ArchitectureLogicielle-app || true && " +
                                    "docker rm -f app-metier || true && " +
                                    "docker run -d -p 8080:8080 " +
                                    "--name IMT-ArchitectureLogicielle-app " +
                                    "--network cicd-network " +
                                    "-e SPRING_DATA_MONGODB_URI='" + connectionString + "' " +
                                    previousImageTag;

                    sshService.executeRemoteCommand(rollbackCmd, execution);
                    execution.appendLog("Rollback effectué vers l'ID " + lastSuccessOpt.get().getId());
                } catch (Exception rollbackEx) {
                    execution.appendLog("Echec du rollback : " + rollbackEx.getMessage());
                }
            } else {
                execution.appendLog("Aucune version précédente stable trouvée.");
            }
        } finally {
            execution.setEndTime(LocalDateTime.now());
            saveAndNotify(execution);
            FileSystemUtils.deleteRecursively(tempDir);
        }
    }
}