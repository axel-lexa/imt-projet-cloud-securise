# Étape 1 : Construction du Frontend (React/Vite)
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Étape 2 : Construction du Backend (Spring Boot)
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Copie des fichiers statiques du frontend vers le dossier static de Spring Boot
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn clean package -DskipTests

# Étape 3 : Image finale pour l'exécution
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Installation de Docker CLI et Git
RUN apt-get update && apt-get install -y docker.io git && rm -rf /var/lib/apt/lists/*

COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
