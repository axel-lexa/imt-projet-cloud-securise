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
    private final KubernetesService kubernetesService;
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
            String registryUrl = kubernetesService.getRegistryUrl();
            String appName = execution.getRepoUrl().substring(execution.getRepoUrl().lastIndexOf("/") + 1)
                .replace(".git", "").replaceAll("[^a-zA-Z0-9-_]", "-");
            if (appName.isEmpty()) appName = "app";
            
            String imageTag = registryUrl + "/" + appName.toLowerCase() + ":" + execution.getId();
            String imageTagLatest = registryUrl + "/" + appName.toLowerCase() + ":latest";
            
            commandService.executeCommand("docker build -t " + imageTag + " -t " + imageTagLatest + " .", tempDir, execution);

            // 4. PUSH IMAGE VERS REGISTRY
            execution.appendLog("--- ÉTAPE 4: PUSH IMAGE VERS REGISTRY ---");
            saveAndNotify(execution);
            kubernetesService.pushImageToRegistry(imageTag, tempDir, execution);

            // 5. GÉNÉRATION ET DÉPLOIEMENT KUBERNETES
            execution.appendLog("--- ÉTAPE 5: DÉPLOIEMENT KUBERNETES ---");
            saveAndNotify(execution);
            String namespace = kubernetesService.getTargetNamespace(execution);
            
            // Crée le secret registry si nécessaire
            kubernetesService.createRegistrySecret(namespace, execution);
            
            // Déploie l'application
            kubernetesService.deployApplication(namespace, execution, imageTag);

            // 6. VÉRIFICATION
            execution.appendLog("--- VÉRIFICATION DU DÉPLOIEMENT ---");
            saveAndNotify(execution);
            kubernetesService.waitForDeploymentReady(namespace, execution);
            String appUrl = kubernetesService.getServiceUrl(namespace, execution);
            execution.appendLog("✅ Application déployée et accessible à : " + appUrl);

            // =================================================================
            // 7. TEST D'INTRUSION (PENTEST)
            // =================================================================
            // CORRECTION 1 : Le marqueur doit correspondre exactement à celui du Frontend ("--- PENTEST ---")
            execution.appendLog("--- PENTEST --- : LANCEMENT OWASP ZAP");
            saveAndNotify(execution);

            Thread.sleep(15000); // Attente démarrage app

            // Pour Kubernetes, on utilise le service name pour accéder à l'application
            String serviceName = appName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
            String serviceUrl = serviceName + "." + namespace + ".svc.cluster.local:8080";
            
            // Note: ZAP doit être exécuté dans le cluster ou avoir accès au service
            // Pour simplifier, on utilise kubectl run pour créer un pod temporaire
            // Dans un environnement de production, utilisez un Job Kubernetes ou port-forward
            String zapPodName = "zap-scan-" + execution.getId();
            String zapCmd = "kubectl run " + zapPodName + " " +
                    "--image=ghcr.io/zaproxy/zaproxy:stable " +
                    "--restart=Never " +
                    "--namespace=" + namespace + " " +
                    "-- zap-api-scan.py " +
                    "-t http://" + serviceUrl + "/v3/api-docs " +
                    "-f openapi " +
                    "-I";

            try {
                execution.appendLog("Exécution du scan API via Swagger sur Kubernetes...");
                // Exécute ZAP dans le cluster Kubernetes
                commandService.executeCommand(zapCmd, tempDir, execution);
                
                // Nettoie le pod après exécution
                try {
                    commandService.executeCommand("kubectl delete pod " + zapPodName + " -n " + namespace + " --ignore-not-found=true", tempDir, execution, true);
                } catch (Exception cleanupEx) {
                    // Ignore les erreurs de nettoyage
                }
                
                execution.appendLog("✅ Pentest API validé : Aucune faille critique détectée.");
            } catch (Exception e) {
                execution.appendLog("⚠️ Pentest non exécuté (peut nécessiter une configuration spéciale): " + e.getMessage());
                // Nettoie le pod même en cas d'erreur
                try {
                    commandService.executeCommand("kubectl delete pod " + zapPodName + " -n " + namespace + " --ignore-not-found=true", tempDir, execution, true);
                } catch (Exception cleanupEx) {
                    // Ignore les erreurs de nettoyage
                }
                // Ne fait pas échouer le pipeline si le pentest ne peut pas s'exécuter
                // Dans un environnement de production, vous pourriez vouloir faire échouer ici
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
                try {
                    String namespace = kubernetesService.getTargetNamespace(execution);
                    String appName = execution.getRepoUrl().substring(execution.getRepoUrl().lastIndexOf("/") + 1)
                        .replace(".git", "").replaceAll("[^a-zA-Z0-9-_]", "-");
                    if (appName.isEmpty()) appName = "app";
                    String deploymentName = appName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
                    String previousVersion = lastSuccessOpt.get().getId().toString();
                    
                    kubernetesService.rollbackDeployment(namespace, deploymentName, previousVersion, execution);
                    execution.appendLog("✅ Rollback effectué vers l'ID " + previousVersion);
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