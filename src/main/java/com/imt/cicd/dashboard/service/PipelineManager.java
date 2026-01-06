//package com.imt.cicd.dashboard.service;
//
//import com.imt.cicd.dashboard.model.PipelineExecution;
//import com.imt.cicd.dashboard.model.PipelineStatus;
//import com.imt.cicd.dashboard.repository.PipelineRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.util.FileSystemUtils;
//
//import java.io.File;
//import java.time.LocalDateTime;
//import java.util.Objects;
//
//@Service
//@RequiredArgsConstructor
//public class PipelineManager {
//
//    private final GitService gitService;
//    private final CommandService commandService;
//    private final SshService sshService;
//    private final PipelineRepository repository;
//
//    @Async
//    public void runPipeline(Long executionId) {
//        PipelineExecution execution = repository.findById(executionId).orElseThrow();
//        File tempDir = new File("temp-workspace/" + executionId);
//
//        try {
//            execution.setStatus(PipelineStatus.RUNNING);
//            execution.setStartTime(LocalDateTime.now());
//            repository.save(execution);
//
//            // Étape 1: Cloner le dépôt
//            execution.appendLog("--- ÉTAPE 1: CLONAGE DU DÉPÔT ---");
//            gitService.cloneRepository(execution.getRepoUrl(), execution.getBranch(), tempDir);
//
//            // Étape 2: MAVEN BUILD & TEST
//            execution.appendLog("--- ÉTAPE 2: BUILD & TEST MAVEN ---");
//            commandService.executeCommand("chmod +x mvnw ", tempDir, execution);
//            commandService.executeCommand("./mvnw clean package", tempDir, execution);
//
//            // Étape 2.5: ANALYSE SONARQUBE
//            // execution.appendLog("--- ÉTAPE 2.5: ANALYSE SONARQUBE ---");
//            // Nécessite un serveur SonarQube lancé (ex: sur localhost:9000)
//            // TODO: Remplacer VOTRE_TOKEN par un token valide
//            // commandService.executeCommand("./mvnw sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=VOTRE_TOKEN", tempDir, execution);
//
//            // Étape 3: DOCKER BUILD
//            execution.appendLog("--- ÉTAPE 3: BUILD DOCKER ---");
//            String imageName = "mon-app-metier:" + execution.getId(); // On utilise l'ID comme tag unique
//            commandService.executeCommand("docker build -t " + imageName + " .", tempDir, execution);
//
//            // TRANSFERT DE L'IMAGE
//            execution.appendLog("--- TRANSFERT VERS LA VM ---");
//            // 1. Sauvegarder l'image en fichier tar
//            commandService.executeCommand("docker save -o app.tar " + imageName, tempDir, execution);
//            File imageFile = new File(tempDir, "app.tar");
//            // 2. Envoyer le fichier (Nécessite le SshService complété ci-dessus)
//            sshService.transferFile(imageFile, "/home/" + sshService.getUser() + "/app.tar");
//
//            // Étape 4: DÉPLOIEMENT SSH
//            execution.appendLog("--- ÉTAPE 4: DÉPLOIEMENT SSH ---");
//            // On charge l'image sur la VM puis on lance
//            String deployCmd = "docker load -i /home/" + sshService.getUser() + "/app.tar && " +
//                    "docker run -d -p 8080:8080 --name app-metier " + imageName;
//            sshService.executeRemoteCommand(deployCmd, execution);
//
//            execution.setStatus(PipelineStatus.SUCCESS);
//        } catch (Exception e) {
//            execution.setStatus(PipelineStatus.FAILED);
//            execution.appendLog("Erreur: " + e.getMessage());
//
//            // --- Logique de Rollback ---
//            execution.appendLog("--- DÉBUT DU ROLLBACK ---");
//
//            // 1. Trouver le dernier déploiement réussi
//            var lastSuccess = repository.findAllByOrderByStartTimeDesc().stream()
//                    .filter(p -> p.getStatus() == PipelineStatus.SUCCESS && !Objects.equals(p.getId(), execution.getId()))
//                    .findFirst();
//
//            if (lastSuccess.isPresent()) {
//                String lastImage = "mon-app-metier:" + lastSuccess.get().getId();
//                execution.appendLog("Rollback vers l'image : " + lastImage);
//
//                try {
//                    // On relance simplement le conteneur avec l'ancienne image sur la VM
//                    String rollbackCmd = "docker stop app-metier || true && " +
//                            "docker rm app-metier || true && " +
//                            "docker run -d -p 8080:8080 --name app-metier " + lastImage;
//                    sshService.executeRemoteCommand(rollbackCmd, execution);
//                    execution.appendLog("Rollback effectué avec succès.");
//                } catch (Exception ex) {
//                    execution.appendLog("Echec du Rollback : " + ex.getMessage());
//                }
//            } else {
//                execution.appendLog("Aucune version précédente stable trouvée. Rollback impossible.");
//            }
//
//        } finally {
//            execution.setEndTime(LocalDateTime.now());
//            repository.save(execution);
//
//            // Implémentation du nettoyage
//            if (tempDir.exists()) {
//                boolean deleted = FileSystemUtils.deleteRecursively(tempDir);
//                if (!deleted) {
//                    System.err.println("Impossible de supprimer le dossier temporaire : " + tempDir.getAbsolutePath());
//                }
//            }
//        }
//    }
//}


package com.imt.cicd.dashboard.service;

import com.imt.cicd.dashboard.model.PipelineExecution;
import com.imt.cicd.dashboard.model.PipelineStatus;
import com.imt.cicd.dashboard.repository.PipelineRepository;
import lombok.RequiredArgsConstructor;
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

    @Async
    public void runPipeline(Long executionId) {
        PipelineExecution execution = repository.findById(executionId).orElseThrow();
        File tempDir = new File("temp-workspace/" + executionId);

        try {
            execution.setStatus(PipelineStatus.RUNNING);
            execution.setStartTime(LocalDateTime.now());
            repository.save(execution);

            // 1. GIT CLONE
            execution.appendLog("--- ÉTAPE 1: CLONAGE DU DÉPÔT ---");
            gitService.cloneRepository(execution.getRepoUrl(), execution.getBranch(), tempDir);

            // 2. MAVEN BUILD
            execution.appendLog("--- ÉTAPE 2: BUILD & TEST MAVEN ---");
            commandService.executeCommand("chmod +x mvnw", tempDir, execution);
            commandService.executeCommand("./mvnw clean package ", tempDir, execution);

            // 3. DOCKER BUILD
            execution.appendLog("--- ÉTAPE 3: BUILD DOCKER ---");
            // On tag avec l'ID pour l'historique ET 'latest' pour le déploiement facile
            String imageNameId = "mon-app-metier:" + execution.getId();
            String imageNameLatest = "mon-app-metier:latest";

            commandService.executeCommand("docker build -t " + imageNameId + " -t " + imageNameLatest + " .", tempDir, execution);

            // Sauvegarde de l'image en .tar
            execution.appendLog("--- SAUVEGARDE IMAGE ---");
            commandService.executeCommand("docker save -o app.tar " + imageNameLatest, tempDir, execution);

            // 4. TRANSFERT SSH
            execution.appendLog("--- TRANSFERT VERS LA VM ---");
            File imageFile = new File(tempDir, "app.tar");
            File composeFile = new File(tempDir, "docker-compose.yml");

            String remoteHome = "/home/" + sshService.getUser();

            // Envoi de l'image
            if (imageFile.exists()) {
                sshService.transferFile(imageFile, remoteHome + "/app.tar");
            } else {
                throw new RuntimeException("Fichier app.tar non généré !");
            }

            // Envoi du docker-compose.yml (S'il existe)
            if (composeFile.exists()) {
                sshService.transferFile(composeFile, remoteHome + "/docker-compose.yml");
            } else {
                execution.appendLog("ATTENTION: docker-compose.yml introuvable à la racine !");
            }

            // 5. DÉPLOIEMENT
            execution.appendLog("--- ÉTAPE 4: DÉPLOIEMENT SSH ---");
            // La commande charge l'image PUIS lance docker-compose
            // On ajoute '|| true' pour éviter que le script plante si le conteneur n'existait pas
            String deployCmd =
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

            // 1. Chercher la dernière version stable pour ce repo
            var lastSuccessOpt = repository.findFirstByRepoUrlAndStatusOrderByStartTimeDesc(
                    execution.getRepoUrl(),
                    PipelineStatus.SUCCESS
            );

            if (lastSuccessOpt.isPresent()) {
                PipelineExecution lastSuccess = lastSuccessOpt.get();
                // On suppose que l'image est taguée avec l'ID de l'exécution (ex: mon-app:12)
                String previousImageTag = "mon-app-metier:" + lastSuccess.getId();

                execution.appendLog("Version précédente trouvée : " + previousImageTag);

                try {
                    // 2. Commande SSH pour relancer l'ancien conteneur
                    // Note: Adaptez selon votre docker-compose ou votre commande docker run habituelle
                    String rollbackCmd =
                            "docker stop app-metier || true && " +
                                    "docker rm app-metier || true && " +
                                    "docker run -d -p 8080:8080 --name app-metier " + previousImageTag;

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
            repository.save(execution);
            // Nettoyage
            FileSystemUtils.deleteRecursively(tempDir);
        }
    }
}