package com.imt.cicd.dashboard.service;

import com.imt.cicd.dashboard.model.PipelineExecution;
import com.imt.cicd.dashboard.model.PipelineStatus;
import com.imt.cicd.dashboard.repository.PipelineRepository;
import lombok.RequiredArgsConstructor;
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
    private final SimpMessagingTemplate messagingTemplate; // Injection du WebSocket

    // Méthode utilitaire pour Sauvegarder ET Notifier le Frontend
    private void saveAndNotify(PipelineExecution execution) {
        repository.save(execution);
        // Envoie l'objet execution sur le topic spécifique /topic/pipeline/{id}
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
            saveAndNotify(execution); // Notification intermédiaire pour voir le log

            gitService.cloneRepository(execution.getRepoUrl(), execution.getBranch(), tempDir);

            // 2. MAVEN BUILD
            execution.appendLog("--- ÉTAPE 2: BUILD & TEST MAVEN ---");
            saveAndNotify(execution);

            commandService.executeCommand("chmod +x mvnw", tempDir, execution);
            commandService.executeCommand("./mvnw clean package", tempDir, execution); // Note: -DskipTests retiré

            // 3. DOCKER BUILD
            execution.appendLog("--- ÉTAPE 3: BUILD DOCKER ---");
            saveAndNotify(execution);

            String imageNameId = "mon-app-metier:" + execution.getId();
            String imageNameLatest = "mon-app-metier:latest";

            commandService.executeCommand("docker build -t " + imageNameId + " -t " + imageNameLatest + " .", tempDir, execution);

            // Sauvegarde de l'image en .tar
            execution.appendLog("--- SAUVEGARDE IMAGE ---");
            saveAndNotify(execution);

            // Correction Rollback: on sauvegarde les deux tags dans le tar
            commandService.executeCommand("docker save -o app.tar " + imageNameId + " " + imageNameLatest, tempDir, execution);

            // 4. TRANSFERT SSH
            execution.appendLog("--- TRANSFERT VERS LA VM ---");
            saveAndNotify(execution);

            File imageFile = new File(tempDir, "app.tar");
            File composeFile = new File(tempDir, "docker-compose.yml");

            String remoteHome = "/home/" + sshService.getUser();

            if (imageFile.exists()) {
                sshService.transferFile(imageFile, remoteHome + "/app.tar");
            } else {
                throw new RuntimeException("Fichier app.tar non généré !");
            }

            if (composeFile.exists()) {
                sshService.transferFile(composeFile, remoteHome + "/docker-compose.yml");
            } else {
                execution.appendLog("ATTENTION: docker-compose.yml introuvable à la racine !");
            }

            // 5. DÉPLOIEMENT
            execution.appendLog("--- ÉTAPE 4: DÉPLOIEMENT SSH ---");
            saveAndNotify(execution);

            // Commande robuste qui nettoie aussi les résidus de rollback (app-metier)
            String deployCmd =
                    "docker stop app-metier || true && " +
                            "docker rm app-metier || true && " +
                            "docker load -i " + remoteHome + "/app.tar && " +
                            "docker compose -f " + remoteHome + "/docker-compose.yml down || true && " +
                            "docker compose -f " + remoteHome + "/docker-compose.yml up -d";

            sshService.executeRemoteCommand(deployCmd, execution);

            execution.setStatus(PipelineStatus.SUCCESS);

        } catch (Exception e) {
            execution.setStatus(PipelineStatus.FAILED);
            execution.appendLog("ERREUR CRITIQUE: " + e.getMessage());

            // --- LOGIQUE ROLLBACK ---
            execution.appendLog("--- TENTATIVE DE ROLLBACK ---");
            saveAndNotify(execution); // Important pour voir que le rollback commence

            var lastSuccessOpt = repository.findFirstByRepoUrlAndStatusOrderByStartTimeDesc(
                    execution.getRepoUrl(),
                    PipelineStatus.SUCCESS
            );

            if (lastSuccessOpt.isPresent()) {
                PipelineExecution lastSuccess = lastSuccessOpt.get();
                String previousImageTag = "mon-app-metier:" + lastSuccess.getId();
                String remoteHome = "/home/" + sshService.getUser();
                String connectionString = "mongodb://user:pass@db:27017/carrentaldb?authSource=admin";

                execution.appendLog("Version précédente trouvée : " + previousImageTag);

                try {
                    // Rollback corrigé avec réseau fixe (cicd-network)
                    String rollbackCmd =
                            "docker stop IMT-ArchitectureLogicielle-app || true && " +
                                    "docker rm IMT-ArchitectureLogicielle-app || true && " +
                                    "docker compose -f " + remoteHome + "/docker-compose.yml up -d db && " +
                                    "docker stop app-metier || true && " +
                                    "docker rm app-metier || true && " +
                                    "docker run -d -p 8080:8080 " +
                                    "--name app-metier " +
                                    "--network cicd-network " + // Nom de réseau fixe
                                    "-e SPRING_DATA_MONGODB_URI='" + connectionString + "' " +
                                    previousImageTag;

                    sshService.executeRemoteCommand(rollbackCmd, execution);
                    execution.appendLog("Rollback effectué avec succès vers l'ID " + lastSuccess.getId());
                } catch (Exception rollbackEx) {
                    execution.appendLog("Echec du rollback : " + rollbackEx.getMessage());
                }
            } else {
                execution.appendLog("Aucune version précédente stable trouvée. Impossible de rollback.");
            }
        } finally {
            execution.setEndTime(LocalDateTime.now());
            saveAndNotify(execution); // Notification finale
            // Nettoyage
            FileSystemUtils.deleteRecursively(tempDir);
        }
    }
}