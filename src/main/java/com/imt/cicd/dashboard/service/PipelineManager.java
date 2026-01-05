package com.imt.cicd.dashboard.service;

import com.imt.cicd.dashboard.model.PipelineExecution;
import com.imt.cicd.dashboard.model.PipelineStatus;
import com.imt.cicd.dashboard.repository.PipelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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

            // Étape 1: Cloner le dépôt
            execution.appendLog("--- ÉTAPE 1: CLONAGE DU DÉPÔT ---");
            gitService.cloneRepository(execution.getRepoUrl(), execution.getBranch(), tempDir);

            // Étape 2: MAVEN BUILD & TEST
            execution.appendLog("--- ÉTAPE 2: BUILD & TEST MAVEN ---");
            commandService.executeCommand("chmod +x mvnw ", tempDir, execution);
            commandService.executeCommand("./mvnw clean package", tempDir, execution);

            // Étape 2.5: ANALYSE SONARQUBE
            execution.appendLog("--- ÉTAPE 2.5: ANALYSE SONARQUBE ---");
            // Nécessite un serveur SonarQube lancé (ex: sur localhost:9000)
            // TODO: Remplacer VOTRE_TOKEN par un token valide
            commandService.executeCommand("./mvnw sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=VOTRE_TOKEN", tempDir, execution);

            // Étape 3: DOCKER BUILD
            execution.appendLog("--- ÉTAPE 3: BUILD DOCKER ---");
            String imageName = "mon-app-metier:latest";
            commandService.executeCommand("docker build -t " + imageName + " .", tempDir, execution);

            // TRANSFERT DE L'IMAGE
            execution.appendLog("--- TRANSFERT VERS LA VM ---");
            // 1. Sauvegarder l'image en fichier tar
            commandService.executeCommand("docker save -o app.tar " + imageName, tempDir, execution);
            File imageFile = new File(tempDir, "app.tar");
            // 2. Envoyer le fichier (Nécessite le SshService complété ci-dessus)
            sshService.transferFile(imageFile, "/home/" + sshService.getUser() + "/app.tar");

            // Étape 4: DÉPLOIEMENT SSH
            execution.appendLog("--- ÉTAPE 4: DÉPLOIEMENT SSH ---");
            // On charge l'image sur la VM puis on lance
            String deployCmd = "docker load -i /home/" + sshService.getUser() + "/app.tar && " +
                    "docker run -d -p 8080:8080 --name app-metier " + imageName;
            sshService.executeRemoteCommand(deployCmd, execution);

            execution.setStatus(PipelineStatus.SUCCESS);
        } catch (Exception e) {
            execution.setStatus(PipelineStatus.FAILED);
            execution.appendLog("Erreur: " + e.getMessage());

            // TODO: Implémenter ici la logique de Rollback
        } finally {
            execution.setEndTime(LocalDateTime.now());
            repository.save(execution);

            // TODO: Nettoyage du répertoire temporaire
        }
    }
}
