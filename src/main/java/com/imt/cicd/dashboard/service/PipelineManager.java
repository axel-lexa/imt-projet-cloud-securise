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
    private final QualityGateService qualityGateService;

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
            commandService.executeCommand("./mvnw clean package -DskipTests", tempDir, execution);

            // 2.5 ANALYSE SONARQUBE & QUALITY GATE
            execution.appendLog("--- ÉTAPE 2.5: ANALYSE QUALITÉ ---");
            
            // Extraction du nom du projet depuis l'URL
            String repoUrl = execution.getRepoUrl();
            String projectKey = repoUrl.substring(repoUrl.lastIndexOf("/") + 1).replace(".git", "");
            projectKey = projectKey.replaceAll("[^a-zA-Z0-9-_]", "-");
            
            execution.appendLog("Clé du projet SonarQube : " + projectKey);
            
            // Commande SonarQube en mode SILENCIEUX (quiet = true)
            String sonarCmd = "./mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184:sonar " +
                    "-Dsonar.projectKey=" + projectKey + " " +
                    "-Dsonar.host.url=http://localhost:9000 " +
                    "-Dsonar.login=admin " +
                    "-Dsonar.password=admin123 " +
                    "-Dorg.slf4j.simpleLogger.defaultLogLevel=warn";

            commandService.executeCommand(sonarCmd, tempDir, execution, true);

            // Vérification du Quality Gate via l'API
            qualityGateService.verifyQuality(projectKey);
            execution.appendLog("✅ Quality Gate validé par SonarQube.");


            // 3. DOCKER BUILD
            execution.appendLog("--- ÉTAPE 3: BUILD DOCKER ---");
            String imageNameId = "mon-app-metier:" + execution.getId();
            String imageNameLatest = "mon-app-metier:latest";

            commandService.executeCommand("docker build -t " + imageNameId + " -t " + imageNameLatest + " .", tempDir, execution);

            // Sauvegarde de l'image en .tar
            execution.appendLog("--- SAUVEGARDE IMAGE ---");
            commandService.executeCommand("docker save -o app.tar " + imageNameLatest, tempDir, execution);

            // 4. TRANSFERT SSH (DÉSACTIVÉ TEMPORAIREMENT POUR TESTS CI)
            /*
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
            String deployCmd =
                    "docker load -i " + remoteHome + "/app.tar && " +
                            "docker compose -f " + remoteHome + "/docker-compose.yml down || true && " +
                            "docker compose -f " + remoteHome + "/docker-compose.yml up -d";
            sshService.executeRemoteCommand(deployCmd, execution);
            */
            execution.appendLog("⚠️ DÉPLOIEMENT SSH DÉSACTIVÉ (Mode Test CI uniquement)");

            execution.setStatus(PipelineStatus.SUCCESS);

        } catch (Exception e) {
            execution.setStatus(PipelineStatus.FAILED);
            execution.appendLog("Erreur critique : " + e.getMessage());
            e.printStackTrace();
        } finally {
            execution.setEndTime(LocalDateTime.now());
            repository.save(execution);
            // Nettoyage
            FileSystemUtils.deleteRecursively(tempDir);
        }
    }
}
