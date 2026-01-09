package com.imt.cicd.dashboard.service;

import com.imt.cicd.dashboard.model.PipelineExecution;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KubernetesService {

    private final CommandService commandService;
    private KubernetesClient kubernetesClient;

    @Value("${kubernetes.config.path:${KUBECONFIG_PATH:/root/.kube/config}}")
    private String kubeconfigPath;

    @Value("${kubernetes.registry.url:${REGISTRY_URL:docker.io}}")
    private String registryUrl;

    @Value("${kubernetes.registry.username:${REGISTRY_USERNAME:}}")
    private String registryUsername;

    @Value("${kubernetes.registry.password:${REGISTRY_PASSWORD:}}")
    private String registryPassword;

    @Value("${kubernetes.namespace.default:${K8S_NAMESPACE:default}}")
    private String defaultNamespace;

    @Value("${kubernetes.namespace.create:true}")
    private boolean createNamespace;

    @Value("${kubernetes.resources.default.memory:256Mi}")
    private String defaultMemory;

    @Value("${kubernetes.resources.default.cpu:250m}")
    private String defaultCpu;

    @Value("${kubernetes.service.type:ClusterIP}")
    private String serviceType;

    @Value("${kubernetes.app.port:8080}")
    private int appPort;

    @Value("${kubernetes.deployment.timeout.minutes:15}")
    private int deploymentTimeoutMinutes;

    @Value("${kubernetes.healthcheck.enabled:true}")
    private boolean healthCheckEnabled;

    @Value("${kubernetes.healthcheck.path:/actuator/health}")
    private String healthCheckPath;

    @Value("${kubernetes.healthcheck.readiness.path:/actuator/health/readiness}")
    private String readinessCheckPath;

    public KubernetesService(CommandService commandService) {
        this.commandService = commandService;
    }

    /**
     * Initialise le client Kubernetes
     */
    private KubernetesClient getKubernetesClient() {
        if (kubernetesClient == null) {
            try {
                Config config;
                File kubeconfigFile = new File(kubeconfigPath);
                if (kubeconfigFile.exists()) {
                    config = Config.fromKubeconfig(null, java.nio.file.Files.readString(kubeconfigFile.toPath()), kubeconfigPath);
                } else {
                    // Utilise la configuration par défaut (in-cluster ou ~/.kube/config)
                    config = new ConfigBuilder().build();
                }
                kubernetesClient = new KubernetesClientBuilder().withConfig(config).build();
                log.info("Kubernetes client initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Kubernetes client", e);
                throw new RuntimeException("Failed to initialize Kubernetes client: " + e.getMessage(), e);
            }
        }
        return kubernetesClient;
    }

    /**
     * Pousse l'image Docker vers le registry
     */
    public void pushImageToRegistry(String imageTag, File dockerfileDir, PipelineExecution execution) throws Exception {
        execution.appendLog("Authentification au registry Docker...");

        // Login au registry si credentials fournis
        if (registryUsername != null && !registryUsername.isEmpty() &&
                registryPassword != null && !registryPassword.isEmpty()) {
            String loginCmd = String.format("docker login -u %s -p %s %s",
                    registryUsername, registryPassword, registryUrl);
            commandService.executeCommand(loginCmd, dockerfileDir, execution);
        }

        execution.appendLog("Push de l'image vers le registry: " + imageTag);
        String pushCmd = "docker push " + imageTag;
        commandService.executeCommand(pushCmd, dockerfileDir, execution);
        execution.appendLog("✅ Image poussée avec succès: " + imageTag);
    }

    /**
     * Obtient le namespace cible pour le déploiement
     */
    public String getTargetNamespace(PipelineExecution execution) {
        // Utilise le namespace par défaut ou crée un namespace par application
        String namespace = defaultNamespace;

        if (createNamespace) {
            // Crée un namespace unique par exécution si nécessaire
            String appName = getAppName(execution);
            namespace = "cicd-" + appName.toLowerCase().replaceAll("[^a-z0-9-]", "-");

            try {
                KubernetesClient client = getKubernetesClient();
                Namespace ns = client.namespaces().withName(namespace).get();
                if (ns == null) {
                    execution.appendLog("Création du namespace: " + namespace);
                    client.namespaces().resource(new NamespaceBuilder()
                                    .withNewMetadata()
                                    .withName(namespace)
                                    .addToLabels("managed-by", "cicd-dashboard")
                                    .endMetadata()
                                    .build())
                            .create();
                    execution.appendLog("✅ Namespace créé: " + namespace);
                }
            } catch (Exception e) {
                log.warn("Failed to create namespace, using default: " + e.getMessage());
                namespace = defaultNamespace;
            }
        }

        return namespace;
    }

    /**
     * Génère un manifest Deployment Kubernetes
     */
    public Deployment generateDeploymentManifest(PipelineExecution execution, String imageUrl) {
        String appName = getAppName(execution);
        String deploymentName = appName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
        String namespace = getTargetNamespace(execution);

        Map<String, String> labels = new HashMap<>();
        labels.put("app", deploymentName);
        labels.put("version", execution.getId().toString());
        labels.put("managed-by", "cicd-dashboard");
        labels.put("execution-id", execution.getId().toString());

        // Construit le container builder avec ou sans health checks
        io.fabric8.kubernetes.api.model.ContainerBuilder containerBuilder = new io.fabric8.kubernetes.api.model.ContainerBuilder()
            .withName(deploymentName)
            .withImage(imageUrl)
            .addNewPort()
                .withContainerPort(appPort)
                .withProtocol("TCP")
            .endPort()
            .withNewResources()
                .addToRequests("cpu", new Quantity(defaultCpu))
                .addToRequests("memory", new Quantity(defaultMemory))
                .addToLimits("cpu", new Quantity(defaultCpu))
                .addToLimits("memory", new Quantity(defaultMemory))
            .endResources();
        
        // Ajoute les health checks seulement si activés
        if (healthCheckEnabled) {
            containerBuilder
                .withNewLivenessProbe()
                    .withNewHttpGet()
                        .withPath(healthCheckPath)
                        .withPort(new IntOrString(appPort))
                    .endHttpGet()
                    .withInitialDelaySeconds(60)
                    .withPeriodSeconds(10)
                    .withTimeoutSeconds(5)
                    .withFailureThreshold(5)
                .endLivenessProbe()
                .withNewReadinessProbe()
                    .withNewHttpGet()
                        .withPath(readinessCheckPath)
                        .withPort(new IntOrString(appPort))
                    .endHttpGet()
                    .withInitialDelaySeconds(30)
                    .withPeriodSeconds(5)
                    .withTimeoutSeconds(3)
                    .withFailureThreshold(5)
                .endReadinessProbe();
        }
        
        DeploymentBuilder builder = new DeploymentBuilder()
                .withNewMetadata()
                .withName(deploymentName)
                .withNamespace(namespace)
                .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                .withReplicas(1)
                .withNewSelector()
                .addToMatchLabels("app", deploymentName)
                .addToMatchLabels("version", execution.getId().toString())
                .endSelector()
                .withNewTemplate()
                .withNewMetadata()
                .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                .addToContainers(containerBuilder.build())
                .addNewContainer()
                .withName("mongo-sidecar")
                .withImage("mongo:5.0")
                .addNewPort()
                .withContainerPort(27017)
                .endPort()
                .addNewEnv()
                .withName("MONGO_INITDB_ROOT_USERNAME").withValue("user")
                .endEnv()
                .addNewEnv()
                .withName("MONGO_INITDB_ROOT_PASSWORD").withValue("pass")
                .endEnv()
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec();

        // Ajoute les ImagePullSecrets si nécessaire
        addImagePullSecrets(builder);

        return builder.build();
    }

    /**
     * Génère un manifest Service Kubernetes
     */
    public io.fabric8.kubernetes.api.model.Service generateServiceManifest(PipelineExecution execution, int port) {
        String appName = getAppName(execution);
        String serviceName = appName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
        String deploymentName = serviceName;

        Map<String, String> labels = new HashMap<>();
        labels.put("app", deploymentName);
        labels.put("version", execution.getId().toString());
        labels.put("managed-by", "cicd-dashboard");

        ServiceBuilder serviceBuilder = new ServiceBuilder()
                .withNewMetadata()
                .withName(serviceName)
                .withNamespace(getTargetNamespace(execution))
                .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                .withType(serviceType)
                .addToSelector("app", deploymentName)
                .addToSelector("version", execution.getId().toString())
                .addNewPort()
                .withPort(port)
                .withTargetPort(new IntOrString(appPort))
                .withProtocol("TCP")
                .withName("http")
                .endPort()
                .endSpec();

        return serviceBuilder.build();
    }

    /**
     * Déploie l'application sur Kubernetes
     */
    public void deployApplication(String namespace, PipelineExecution execution, String imageTag) throws Exception {
        execution.appendLog("Génération des manifests Kubernetes...");

        KubernetesClient client = getKubernetesClient();

        // Génère les manifests
        Deployment deployment = generateDeploymentManifest(execution, imageTag);
        io.fabric8.kubernetes.api.model.Service service = generateServiceManifest(execution, appPort);

        String deploymentName = deployment.getMetadata().getName();
        String serviceName = service.getMetadata().getName();

        execution.appendLog("Application des manifests dans le namespace: " + namespace);

        // Supprime l'ancien déploiement s'il existe
        try {
            Deployment existing = client.apps().deployments().inNamespace(namespace).withName(deploymentName).get();
            if (existing != null) {
                execution.appendLog("Suppression de l'ancien déploiement...");
                client.apps().deployments().inNamespace(namespace).withName(deploymentName).delete();
                // Attendre la suppression
                client.apps().deployments().inNamespace(namespace).withName(deploymentName)
                        .waitUntilCondition(d -> d == null, 60, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.debug("No existing deployment to delete: " + e.getMessage());
        }

        // Crée le déploiement
        execution.appendLog("Création du Deployment: " + deploymentName);
        client.apps().deployments().inNamespace(namespace).resource(deployment).createOrReplace();

        // Crée ou met à jour le service
        execution.appendLog("Création du Service: " + serviceName);
        client.services().inNamespace(namespace).resource(service).createOrReplace();

        execution.appendLog("✅ Manifests appliqués avec succès");
    }

    /**
     * Attend que le déploiement soit prêt
     */
    public void waitForDeploymentReady(String namespace, PipelineExecution execution) throws Exception {
        KubernetesClient client = getKubernetesClient();
        String deploymentName = getAppName(execution).toLowerCase().replaceAll("[^a-z0-9-]", "-");

        execution.appendLog("Attente du déploiement: " + deploymentName + " (timeout: " + deploymentTimeoutMinutes + " minutes)");

        try {
            // Vérifie périodiquement le statut et affiche des informations
            long startTime = System.currentTimeMillis();
            long timeoutMs = deploymentTimeoutMinutes * 60 * 1000L;

            while (System.currentTimeMillis() - startTime < timeoutMs) {
                Deployment deployment = client.apps().deployments().inNamespace(namespace).withName(deploymentName).get();
                if (deployment == null) {
                    execution.appendLog("⚠️ Déploiement non trouvé");
                    throw new RuntimeException("Deployment not found: " + deploymentName);
                }

                // Vérifie le statut du déploiement
                var status = deployment.getStatus();
                if (status != null) {
                    Integer readyReplicas = status.getReadyReplicas();
                    Integer replicas = status.getReplicas();
                    Integer availableReplicas = status.getAvailableReplicas();

                    execution.appendLog(String.format("Statut: %d/%d replicas prêtes, %d disponibles",
                            readyReplicas != null ? readyReplicas : 0,
                            replicas != null ? replicas : 0,
                            availableReplicas != null ? availableReplicas : 0));

                    if (readyReplicas != null && readyReplicas > 0 &&
                            replicas != null && readyReplicas.equals(replicas)) {
                        execution.appendLog("✅ Déploiement prêt");
                        break;
                    }
                }

                // Vérifie les pods et leurs statuts
                var pods = client.pods().inNamespace(namespace)
                        .withLabel("app", deploymentName)
                        .list();

                if (pods != null && pods.getItems() != null && !pods.getItems().isEmpty()) {
                    for (var pod : pods.getItems()) {
                        String podName = pod.getMetadata().getName();
                        String phase = pod.getStatus().getPhase();
                        execution.appendLog("Pod " + podName + ": " + phase);

                        // Affiche les conditions du pod
                        if (pod.getStatus().getConditions() != null) {
                            for (var condition : pod.getStatus().getConditions()) {
                                if ("Ready".equals(condition.getType()) || "ContainersReady".equals(condition.getType())) {
                                    execution.appendLog("  - " + condition.getType() + ": " + condition.getStatus() +
                                            (condition.getMessage() != null ? " (" + condition.getMessage() + ")" : ""));
                                }
                            }
                        }

                        // Affiche les événements récents du pod
                        try {
                            var events = client.v1().events().inNamespace(namespace)
                                    .withField("involvedObject.name", podName)
                                    .list();
                            if (events != null && events.getItems() != null && !events.getItems().isEmpty()) {
                                var recentEvents = events.getItems().stream()
                                        .sorted((e1, e2) -> e2.getLastTimestamp().compareTo(e1.getLastTimestamp()))
                                        .limit(3)
                                        .toList();
                                for (var event : recentEvents) {
                                    execution.appendLog("  Événement: " + event.getReason() + " - " + event.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            // Ignore les erreurs de récupération d'événements
                        }
                    }
                } else {
                    execution.appendLog("⚠️ Aucun pod trouvé pour le déploiement");
                }

                // Attend 10 secondes avant la prochaine vérification
                Thread.sleep(10000);
            }

            // Vérification finale avec waitUntilReady pour s'assurer que c'est vraiment prêt
            try {
                client.apps().deployments().inNamespace(namespace).withName(deploymentName)
                        .waitUntilReady(1, TimeUnit.MINUTES);
                execution.appendLog("✅ Déploiement confirmé prêt");
            } catch (KubernetesClientException e) {
                // Si ça échoue encore, on récupère plus d'informations
                execution.appendLog("⚠️ Le déploiement n'est pas complètement prêt");
                logDiagnostics(namespace, deploymentName, execution, client);
                throw new RuntimeException("Deployment not ready after " + deploymentTimeoutMinutes + " minutes: " + e.getMessage(), e);
            }

        } catch (KubernetesClientException e) {
            execution.appendLog("⚠️ Timeout ou erreur lors de l'attente du déploiement: " + e.getMessage());
            logDiagnostics(namespace, deploymentName, execution, client);
            throw new RuntimeException("Deployment not ready: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for deployment", e);
        }
    }

    /**
     * Affiche des diagnostics détaillés en cas d'échec
     */
    private void logDiagnostics(String namespace, String deploymentName, PipelineExecution execution, KubernetesClient client) {
        try {
            execution.appendLog("--- DIAGNOSTICS ---");

            // Événements du déploiement
            var deploymentEvents = client.v1().events().inNamespace(namespace)
                    .withField("involvedObject.name", deploymentName)
                    .list();
            if (deploymentEvents != null && deploymentEvents.getItems() != null) {
                execution.appendLog("Événements du déploiement:");
                for (var event : deploymentEvents.getItems()) {
                    execution.appendLog("  - " + event.getReason() + ": " + event.getMessage());
                }
            }

            // Pods et leurs logs
            var pods = client.pods().inNamespace(namespace)
                    .withLabel("app", deploymentName)
                    .list();

            if (pods != null && pods.getItems() != null) {
                for (var pod : pods.getItems()) {
                    String podName = pod.getMetadata().getName();
                    execution.appendLog("Pod: " + podName);

                    // Logs du pod (dernières 20 lignes)
                    try {
                        String logs = client.pods().inNamespace(namespace).withName(podName).getLog(true);
                        if (logs != null && !logs.isEmpty()) {
                            String[] logLines = logs.split("\n");
                            int start = Math.max(0, logLines.length - 20);
                            execution.appendLog("  Derniers logs:");
                            for (int i = start; i < logLines.length; i++) {
                                execution.appendLog("    " + logLines[i]);
                            }
                        }
                    } catch (Exception e) {
                        execution.appendLog("  Impossible de récupérer les logs: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            execution.appendLog("Erreur lors de la récupération des diagnostics: " + e.getMessage());
        }
    }

    /**
     * Récupère l'URL d'accès au service
     */
    public String getServiceUrl(String namespace, PipelineExecution execution) {
        KubernetesClient client = getKubernetesClient();
        String serviceName = getAppName(execution).toLowerCase().replaceAll("[^a-z0-9-]", "-");

        try {
            io.fabric8.kubernetes.api.model.Service service = client.services().inNamespace(namespace).withName(serviceName).get();
            if (service == null) {
                return "Service non trouvé";
            }

            String type = service.getSpec().getType();

            if ("LoadBalancer".equals(type)) {
                String loadBalancerIp = service.getStatus().getLoadBalancer().getIngress().stream()
                        .findFirst()
                        .map(ingress -> ingress.getIp() != null ? ingress.getIp() : ingress.getHostname())
                        .orElse("En attente d'IP...");
                return "http://" + loadBalancerIp + ":" + appPort;
            } else if ("NodePort".equals(type)) {
                Integer nodePort = service.getSpec().getPorts().stream()
                        .findFirst()
                        .map(ServicePort::getNodePort)
                        .orElse(null);
                if (nodePort != null) {
                    return "http://<node-ip>:" + nodePort;
                }
            } else {
                // ClusterIP
                return String.format("Service %s.%s.svc.cluster.local:%d (ClusterIP)",
                        serviceName, namespace, appPort);
            }
        } catch (Exception e) {
            log.error("Failed to get service URL", e);
        }

        return "URL non disponible";
    }

    /**
     * Effectue un rollback vers une version précédente
     */
    public void rollbackDeployment(String namespace, String deploymentName, String previousVersion, PipelineExecution execution) throws Exception {
        KubernetesClient client = getKubernetesClient();

        execution.appendLog("Rollback vers la version: " + previousVersion);

        Deployment deployment = client.apps().deployments().inNamespace(namespace).withName(deploymentName).get();
        if (deployment == null) {
            throw new RuntimeException("Deployment not found: " + deploymentName);
        }

        // Met à jour l'image avec la version précédente
        String imageUrl = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage();
        String baseImage = imageUrl.substring(0, imageUrl.lastIndexOf(":"));
        String newImageUrl = baseImage + ":" + previousVersion;

        deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setImage(newImageUrl);
        deployment.getMetadata().getLabels().put("version", previousVersion);
        deployment.getSpec().getSelector().getMatchLabels().put("version", previousVersion);
        deployment.getSpec().getTemplate().getMetadata().getLabels().put("version", previousVersion);

        client.apps().deployments().inNamespace(namespace).resource(deployment).createOrReplace();

        execution.appendLog("✅ Rollback effectué vers: " + newImageUrl);

        // Attend que le rollback soit prêt
        waitForDeploymentReady(namespace, execution);
    }

    /**
     * Obtient le nom de l'application depuis l'URL du repo
     */
    private String getAppName(PipelineExecution execution) {
        String repoUrl = execution.getRepoUrl();
        String appName = repoUrl.substring(repoUrl.lastIndexOf("/") + 1)
                .replace(".git", "")
                .replaceAll("[^a-zA-Z0-9-_]", "-");
        return appName.isEmpty() ? "app" : appName;
    }

    /**
     * Ajoute les ImagePullSecrets au Deployment si nécessaire
     */
    private void addImagePullSecrets(DeploymentBuilder builder) {
        if (registryUsername != null && !registryUsername.isEmpty() &&
                registryPassword != null && !registryPassword.isEmpty()) {
            builder.editSpec()
                    .editTemplate()
                    .editSpec()
                    .addNewImagePullSecret()
                    .withName("registry-secret")
                    .endImagePullSecret()
                    .endSpec()
                    .endTemplate()
                    .endSpec();
        }
    }

    /**
     * Crée le secret pour le registry Docker
     */
    public void createRegistrySecret(String namespace, PipelineExecution execution) {
        try {
            if (registryUsername == null || registryUsername.isEmpty() ||
                    registryPassword == null || registryPassword.isEmpty()) {
                return; // Pas de credentials, pas besoin de secret
            }

            KubernetesClient client = getKubernetesClient();
            String secretName = "registry-secret";

            // Vérifie si le secret existe déjà
            Secret existing = client.secrets().inNamespace(namespace).withName(secretName).get();
            if (existing != null) {
                execution.appendLog("Secret registry existe déjà");
                return;
            }

            // Crée le secret Docker registry au format correct
            String auth = java.util.Base64.getEncoder().encodeToString((registryUsername + ":" + registryPassword).getBytes());
            String dockerConfigJson = String.format(
                    "{\"auths\":{\"%s\":{\"username\":\"%s\",\"password\":\"%s\",\"auth\":\"%s\"}}}",
                    registryUrl,
                    registryUsername,
                    registryPassword,
                    auth
            );

            Secret secret = new SecretBuilder()
                    .withNewMetadata()
                    .withName(secretName)
                    .withNamespace(namespace)
                    .endMetadata()
                    .withType("kubernetes.io/dockerconfigjson")
                    .addToData(".dockerconfigjson", java.util.Base64.getEncoder().encodeToString(dockerConfigJson.getBytes()))
                    .build();

            client.secrets().inNamespace(namespace).resource(secret).create();
            execution.appendLog("✅ Secret registry créé: " + secretName);
        } catch (Exception e) {
            log.error("Failed to create registry secret", e);
            execution.appendLog("⚠️ Échec de création du secret registry: " + e.getMessage());
        }
    }

    /**
     * Obtient l'URL du registry
     */
    public String getRegistryUrl() {
        return registryUrl;
    }
}
