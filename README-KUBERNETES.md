# Guide d'Intégration Kubernetes

Ce document explique comment configurer et utiliser le déploiement Kubernetes dans le Dashboard CI/CD.

## Vue d'ensemble

Le Dashboard CI/CD a été modifié pour déployer les applications cibles sur **Kubernetes** au lieu de Docker Compose via SSH. Le pipeline effectue maintenant :

1. Clone du dépôt Git
2. Build et tests Maven
3. Analyse SonarQube
4. Build de l'image Docker
5. **Push de l'image vers un registry Docker**
6. **Déploiement sur Kubernetes** (Deployment, Service)
7. Vérification du déploiement
8. Test de sécurité (OWASP ZAP)

## Prérequis

### 1. Cluster Kubernetes

Vous devez avoir accès à un cluster Kubernetes. Options possibles :

- **Minikube** (local) : `minikube start`
- **Kind** (local) : `kind create cluster`
- **k3d** (local) : `k3d cluster create`
- **Cluster cloud** (GKE, EKS, AKS, etc.)

### 2. Configuration kubectl

Assurez-vous que `kubectl` est configuré et peut accéder à votre cluster :

```bash
kubectl cluster-info
kubectl get nodes
```

### 3. Registry Docker

Vous devez configurer un registry Docker pour stocker les images. Options :

- **Docker Hub** : `docker.io` (public ou privé)
- **GitHub Container Registry (GHCR)** : `ghcr.io`
- **Registry privé** : Harbor, Nexus, etc.
- **Registry cloud** : ECR, GCR, ACR

## Configuration

### Variables d'environnement

Les valeurs utilisées dans les exemples ci‑dessous proviennent **de votre environnement système**. Vous pouvez les fournir de plusieurs façons selon le contexte :

- **En local (sans Docker)** : exporter les variables dans votre shell avant de lancer l'application :

  ```bash
  export KUBECONFIG_PATH=/chemin/vers/votre/kubeconfig
  export K8S_NAMESPACE=default
  export REGISTRY_URL=docker.io
  export REGISTRY_USERNAME=votre-username
  export REGISTRY_PASSWORD=votre-password
  ./mvnw spring-boot:run
  ```

- **Avec Docker / docker-compose** : les définir dans un fichier `.env` ou dans la section `environment:` de votre service, elles seront injectées dans le conteneur.
- **Dans un pipeline CI/CD (GitHub Actions, GitLab, etc.)** : les créer comme **secrets**/variables protégées et les mapper en variables d’environnement pour le job qui lance l’application.
- **Dans Kubernetes** : vous pouvez aussi créer un `Secret` / `ConfigMap` et les exposer comme variables d’environnement dans le `Deployment` (option avancée si vous déployez directement le dashboard dans Kubernetes).

Une fois ces valeurs disponibles dans l’environnement, Spring Boot les récupère automatiquement via la syntaxe `${NOM_VAR:valeurParDefaut}` utilisée dans `application.properties`.

Ajoutez ensuite les variables suivantes dans votre fichier `.env` ou configurez-les dans votre environnement :

```bash
# Kubernetes Configuration
KUBECONFIG_PATH=/root/.kube/config  # Chemin vers le fichier kubeconfig
K8S_NAMESPACE=default                # Namespace par défaut

# Registry Docker
REGISTRY_URL=docker.io                # URL du registry (docker.io, ghcr.io, etc.)
REGISTRY_USERNAME=votre-username      # Username pour le registry
REGISTRY_PASSWORD=votre-password      # Password/token pour le registry
```

### Configuration dans application.properties

Les paramètres suivants peuvent être configurés dans `application.properties` :

```properties
# Kubernetes Configuration
kubernetes.config.path=${KUBECONFIG_PATH:/root/.kube/config}
kubernetes.registry.url=${REGISTRY_URL:docker.io}
kubernetes.registry.username=${REGISTRY_USERNAME:}
kubernetes.registry.password=${REGISTRY_PASSWORD:}
kubernetes.namespace.default=${K8S_NAMESPACE:default}
kubernetes.namespace.create=true
kubernetes.resources.default.memory=256Mi
kubernetes.resources.default.cpu=250m
kubernetes.service.type=ClusterIP
kubernetes.app.port=8080
```

### Explication des paramètres

- **kubernetes.config.path** : Chemin vers le fichier kubeconfig. Si non spécifié, utilise la configuration par défaut.
- **kubernetes.registry.url** : URL du registry Docker (ex: `docker.io`, `ghcr.io`, `registry.example.com`).
- **kubernetes.registry.username/password** : Credentials pour s'authentifier au registry.
- **kubernetes.namespace.default** : Namespace par défaut pour les déploiements.
- **kubernetes.namespace.create** : Si `true`, crée un namespace unique par application.
- **kubernetes.resources.default.memory/cpu** : Ressources par défaut pour les pods.
- **kubernetes.service.type** : Type de Service Kubernetes (`ClusterIP`, `NodePort`, `LoadBalancer`).
- **kubernetes.app.port** : Port de l'application (par défaut 8080).

## Processus de Déploiement

### 1. Build et Push de l'Image

Le pipeline construit l'image Docker et la pousse vers le registry configuré :

```bash
docker build -t registry.example.com/app-name:123 .
docker push registry.example.com/app-name:123
```

L'image est taguée avec l'ID d'exécution du pipeline pour permettre le versioning.

### 2. Génération des Manifests Kubernetes

Le service `KubernetesService` génère dynamiquement :

#### Deployment

- **Image** : URL complète du registry avec tag
- **Replicas** : 1 (configurable)
- **Ressources** : CPU/Memory requests et limits
- **Health Checks** :
  - `livenessProbe` : `/actuator/health`
  - `readinessProbe` : `/actuator/health/readiness`
- **Labels** : `app`, `version`, `managed-by: cicd-dashboard`

#### Service

- **Type** : ClusterIP par défaut (ou LoadBalancer/NodePort si configuré)
- **Port** : Port de l'application (8080 par défaut)
- **Sélecteur** : Correspond aux labels du Deployment

### 3. Application des Manifests

Les manifests sont appliqués via l'API Kubernetes :

```bash
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```

### 4. Vérification

Le pipeline attend que :
- Le Deployment soit prêt
- Les pods soient en état `Running`
- Les health checks répondent

## Exemples de Manifests Générés

### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app-name
  namespace: cicd-app-name
  labels:
    app: app-name
    version: "123"
    managed-by: cicd-dashboard
spec:
  replicas: 1
  selector:
    matchLabels:
      app: app-name
      version: "123"
  template:
    metadata:
      labels:
        app: app-name
        version: "123"
    spec:
      containers:
      - name: app-name
        image: registry.example.com/app-name:123
        ports:
        - containerPort: 8080
        resources:
          requests:
            cpu: 250m
            memory: 256Mi
          limits:
            cpu: 250m
            memory: 256Mi
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
```

### Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: app-name
  namespace: cicd-app-name
  labels:
    app: app-name
    version: "123"
    managed-by: cicd-dashboard
spec:
  type: ClusterIP
  selector:
    app: app-name
    version: "123"
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
    name: http
```

## Accès aux Applications

### ClusterIP (par défaut)

Les applications sont accessibles via le DNS Kubernetes :

```
http://service-name.namespace.svc.cluster.local:8080
```

Pour accéder depuis l'extérieur, utilisez `kubectl port-forward` :

```bash
kubectl port-forward -n cicd-app-name svc/app-name 8080:8080
```

Puis accédez à `http://localhost:8080`.

### LoadBalancer

Si vous configurez `kubernetes.service.type=LoadBalancer`, Kubernetes créera un LoadBalancer externe (si supporté par votre cluster).

### NodePort

Si vous configurez `kubernetes.service.type=NodePort`, l'application sera accessible via un port sur chaque node.

## Rollback

Le pipeline supporte le rollback automatique en cas d'échec. Il :

1. Identifie la dernière version stable
2. Met à jour le Deployment avec l'image précédente
3. Attend que le rollback soit prêt

Vous pouvez aussi effectuer un rollback manuel via l'interface ou l'API.

## Commandes kubectl Utiles

### Vérifier les déploiements

```bash
# Lister les déploiements
kubectl get deployments -n cicd-app-name

# Détails d'un déploiement
kubectl describe deployment app-name -n cicd-app-name

# Historique des déploiements
kubectl rollout history deployment/app-name -n cicd-app-name
```

### Vérifier les pods

```bash
# Lister les pods
kubectl get pods -n cicd-app-name

# Logs d'un pod
kubectl logs -n cicd-app-name <pod-name>

# Détails d'un pod
kubectl describe pod <pod-name> -n cicd-app-name
```

### Vérifier les services

```bash
# Lister les services
kubectl get services -n cicd-app-name

# Détails d'un service
kubectl describe service app-name -n cicd-app-name
```

### Rollback manuel

```bash
# Rollback vers la version précédente
kubectl rollout undo deployment/app-name -n cicd-app-name

# Rollback vers une version spécifique
kubectl rollout undo deployment/app-name -n cicd-app-name --to-revision=2
```

### Port-forward pour accès local

```bash
# Forwarder le port du service
kubectl port-forward -n cicd-app-name svc/app-name 8080:8080
```

## Troubleshooting

### L'image ne peut pas être pullée

**Problème** : `ErrImagePull` ou `ImagePullBackOff`

**Solutions** :
1. Vérifiez que l'image existe dans le registry
2. Vérifiez les credentials du registry (secret `registry-secret`)
3. Vérifiez que le registry est accessible depuis le cluster

```bash
# Vérifier le secret
kubectl get secret registry-secret -n cicd-app-name

# Tester le pull manuellement
kubectl run test-pull --image=registry.example.com/app-name:123 --rm -it --restart=Never
```

### Les pods ne démarrent pas

**Problème** : Pods en état `Pending` ou `CrashLoopBackOff`

**Solutions** :
1. Vérifiez les logs : `kubectl logs <pod-name> -n cicd-app-name`
2. Vérifiez les ressources disponibles : `kubectl describe node`
3. Vérifiez les health checks (liveness/readiness probes)

### Le service n'est pas accessible

**Problème** : Impossible d'accéder à l'application via le service

**Solutions** :
1. Vérifiez que le service existe : `kubectl get svc -n cicd-app-name`
2. Vérifiez les sélecteurs du service correspondent aux labels des pods
3. Utilisez `kubectl port-forward` pour tester l'accès

### Erreur de connexion au cluster

**Problème** : `Failed to initialize Kubernetes client`

**Solutions** :
1. Vérifiez que le fichier kubeconfig existe et est valide
2. Vérifiez les permissions : `kubectl cluster-info`
3. Vérifiez que le contexte Kubernetes est correct : `kubectl config current-context`

### Erreur lors du push vers le registry

**Problème** : `denied: requested access to the resource is denied`

**Solutions** :
1. Vérifiez les credentials du registry
2. Vérifiez que vous êtes authentifié : `docker login registry.example.com`
3. Vérifiez les permissions sur le registry

## Sécurité

### Secrets Kubernetes

Les credentials du registry sont stockés dans un Secret Kubernetes :

```bash
# Vérifier le secret
kubectl get secret registry-secret -n cicd-app-name -o yaml
```

### Permissions RBAC

Le Dashboard CI/CD doit avoir les permissions suivantes :

- `create`, `get`, `update`, `delete` sur `deployments`
- `create`, `get`, `update`, `delete` sur `services`
- `create`, `get`, `update`, `delete` sur `secrets`
- `create`, `get`, `update`, `delete` sur `namespaces` (si `namespace.create=true`)

Exemple de Role et RoleBinding :

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: cicd-deployer
  namespace: default
rules:
- apiGroups: ["apps"]
  resources: ["deployments"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
- apiGroups: [""]
  resources: ["services", "secrets", "namespaces"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
```

## Multi-tenancy

### Namespaces par application

Si `kubernetes.namespace.create=true`, un namespace unique est créé pour chaque application :

- Format : `cicd-{app-name}`
- Isolation complète entre applications
- Facilite la gestion des ressources et quotas

### Labels et sélecteurs

Tous les ressources sont étiquetées avec :
- `app` : Nom de l'application
- `version` : ID d'exécution du pipeline
- `managed-by: cicd-dashboard`

Cela permet de :
- Grouper les ressources par application
- Identifier les versions
- Faciliter le nettoyage

## Nettoyage

### Supprimer un déploiement

```bash
# Supprimer le déploiement et le service
kubectl delete deployment app-name -n cicd-app-name
kubectl delete service app-name -n cicd-app-name

# Supprimer le namespace entier (si créé)
kubectl delete namespace cicd-app-name
```

### Nettoyer les anciennes versions

Les anciennes versions peuvent être identifiées par le label `version`. Pour nettoyer :

```bash
# Lister les déploiements par version
kubectl get deployments -n cicd-app-name -l managed-by=cicd-dashboard

# Supprimer les anciennes versions (garder les N dernières)
# (Script à implémenter selon vos besoins)
```

## Intégration avec le Frontend

Le frontend affiche maintenant :
- Le namespace Kubernetes où l'application est déployée
- L'URL d'accès (Service ou Ingress)
- Le statut des pods (Running, Pending, Failed)
- Un bouton de rollback si disponible

## Limitations et Améliorations Futures

### Limitations actuelles

- Pas de support pour les Ingress (peut être ajouté)
- Pas de support pour les PersistentVolumes
- Pentest ZAP nécessite une configuration spéciale pour Kubernetes
- Pas de support pour les applications multi-services

### Améliorations possibles

- Support des Ingress pour exposer les applications
- Support des ConfigMaps et Secrets depuis les fichiers de config
- Support des PersistentVolumes pour les applications avec stockage
- Intégration avec Prometheus pour le monitoring
- Support des applications multi-services (microservices)
- Auto-scaling basé sur les métriques
- Blue/Green ou Canary deployments

## Support

Pour toute question ou problème, consultez :
- Les logs du Dashboard CI/CD
- Les logs Kubernetes : `kubectl logs ...`
- La documentation Kubernetes : https://kubernetes.io/docs/
