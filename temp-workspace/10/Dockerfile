# ----- Étape 1 : Build -----
# Utilise une image Maven 3.9 qui contient déjà le JDK 21
FROM maven:3.9-eclipse-temurin-21 AS builder

# Répertoire de travail
WORKDIR /workspace

# 1. Copier tous les pom.xml en premier pour le cache Docker
# Copie le pom.xml parent
COPY pom.xml .
# Copie les poms des modules
COPY domain/pom.xml domain/
COPY adapters-in-rest/pom.xml adapters-in-rest/
COPY adapters-in-scheduler/pom.xml adapters-in-scheduler/
COPY adapters-out-bdd/pom.xml adapters-out-bdd/
COPY application/pom.xml application/

# (Optionnel, mais recommandé) Télécharge les dépendances dans une couche séparée
# RUN mvn dependency:go-offline

# 2. Copier tout le code source
COPY domain/ domain/
COPY adapters-in-rest/ adapters-in-rest/
COPY adapters-in-scheduler/ adapters-in-scheduler/
COPY adapters-out-bdd/ adapters-out-bdd/
COPY application/ application/

# 3. Lancer le build ciblé du module `application` et de ses dépendances d'agrégat
# Utilise le pom racine mais ne build que le module `application` et ses dépendances internes
RUN mvn -f pom.xml -pl application -am clean package -DskipTests

# ----- Étape 2 : Run -----
# Utilise une image JRE 21 (plus légère) pour exécuter l'application
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 4. MODIFICATION CLÉ : Copier le .jar depuis le module 'application' (bootstrap)
# Le .jar se trouve maintenant dans /workspace/application/target/
# Remplacez 'application-0.0.1-SNAPSHOT.jar' par le nom réel de votre artifactId et version
COPY --from=builder /workspace/application/target/application-0.0.1-SNAPSHOT.jar app.jar

# Expose le port par défaut de Spring Boot
EXPOSE 8080

# Commande pour lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]