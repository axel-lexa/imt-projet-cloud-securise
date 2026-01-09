## ⚡ Quick Start – CI/CD Dashboard

Ce guide résume **le minimum à faire pour lancer le projet rapidement**, en utilisant Docker.

---

### 1. Prérequis

- **Git**
- **Docker** + **Docker Compose**
- Accès Internet pour télécharger les images Docker

Optionnel (pour le déploiement d’applications cibles) :
- Une VM / machine distante accessible en **SSH**
- Un **cluster Kubernetes** et un **registry Docker** (voir `README-KUBERNETES.md`)

---

### 2. Récupérer le projet

```bash
git clone https://github.com/axel-lexa/imt-projet-cloud-securise.git
cd imt-projet-cloud-securise
```

---

### 3. Configurer les variables d’environnement

1. Copier le fichier d’exemple :

```bash
cp .env.example .env        # Linux / macOS
REM copy .env.example .env  # Windows (PowerShell / CMD)
```

2. Ouvrir `.env` et adapter au besoin :
   - Accès **SonarQube** (login / mot de passe)
   - Paramètres de connexion **SSH** à la VM cible (si utilisés)
   - Paramètres **Kubernetes / registry Docker** si vous déployez sur k8s  
     → voir la section dédiée dans `README-KUBERNETES.md`.

Pour un simple test local, les valeurs par défaut du `.env.example` suffisent en général.

---

### 4. Démarrer toute la stack avec Docker

Depuis la racine du projet :

```bash
docker compose up -d --build
```

Ce que fait cette commande :
- Build et lance le **backend Spring Boot**
- Build et lance le **frontend React**
- Démarre **PostgreSQL**
- Démarre **SonarQube**

Pour suivre les logs :

```bash
docker compose logs -f
```

Pour tout arrêter :

```bash
docker compose down
```

---

### 5. Accéder aux interfaces

- **Dashboard CI/CD** : `http://localhost:8081`
- **SonarQube** : `http://localhost:9000`  
  - Identifiants par défaut : `admin / admin`  
  - On vous demandera de changer le mot de passe (par exemple `admin123`).

---

### 6. (Optionnel) Exposer le Dashboard via ngrok

Si vous voulez recevoir des webhooks GitHub / GitLab, exposez le port 8081 :

```bash
ngrok http 8081
```

Renseignez ensuite l’URL publique ngrok dans la configuration de vos webhooks.

---

### 7. (Optionnel) Déploiement sur Kubernetes

Pour activer le déploiement sur Kubernetes (au lieu d’une VM Docker/SSH) :

1. Configurez votre **cluster Kubernetes** et votre **registry Docker**.
2. Remplissez les variables liées à Kubernetes et au registry dans `.env`.
3. Vérifiez la configuration dans `application.properties` comme décrit dans `README-KUBERNETES.md`.

Tous les détails se trouvent dans `README-KUBERNETES.md`.

