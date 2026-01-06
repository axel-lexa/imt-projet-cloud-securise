# 🚗 IMT-Architecture-Logiciel — Gestion de location automobile

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Database-green.svg)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-blue.svg)](https://www.docker.com/)
[![Architecture](https://img.shields.io/badge/Architecture-Hexagonale-purple.svg)](https://alistair.cockburn.us/hexagonal-architecture/)

---

## 📋 Description

Projet Spring Boot de **gestion de location automobile**, réalisé dans le cadre du TP d'Architecture Logicielle à l'IMT.
L'objectif est de mettre en œuvre une **Architecture Hexagonale (Ports & Adapters)** stricte via une approche **Multi-Modules Maven** pour garantir l'isolation du domaine métier.

---

## 📖 Contexte du Projet

Ce projet a été développé suite à l'obtention du budget pour la refonte du système de gestion des locations automobiles ("BFB").

**Le besoin métier :**
L'objectif est de gérer trois entités principales : **Clients**, **Véhicules** et **Contrats**. Le système doit respecter des règles métier strictes définies par la direction :
- Unicité des clients et des véhicules.
- Gestion des états de véhicules (Disponible, En location, En panne).
- Annulation automatique des contrats si un véhicule tombe en panne.
- Gestion des retards et annulations en cascade pour les locations suivantes.

**Le défi technique :**
Le comité d'architecture a imposé une contrainte forte : **"Apporter un soin particulier à l'architecture de l'application"**. Pour répondre à cette exigence et garantir la maintenabilité, nous avons opté pour une **Architecture Hexagonale (Ports & Adapters)** stricte, isolant totalement le code métier des frameworks.

---

## 📑 Table des matières

1. [Contexte du Projet](#-contexte-du-projet)
2. [Fonctionnalités principales](#-fonctionnalités-principales)
3. [Architecture Hexagonale](#%EF%B8%8F-architecture--hexagonale-ports--adapters)
4. [Pourquoi l'Architecture Hexagonale ?](#-pourquoi-larchitecture-hexagonale-)
5. [Design Patterns utilisés](#-design-patterns-utilisés)
   - [Chain of Responsibility](#1--chain-of-responsibility-validation)
   - [Ports & Adapters](#2--ports--adapters-hexagonal-architecture)
   - [Builder Pattern](#3--builder-pattern-modèles-immuables)
   - [Mapper Pattern](#4-%EF%B8%8F-mapper-pattern-dto--domain--entity)
   - [Decorator Pattern](#5--decorator-pattern-services-avec-validation)
   - [Dependency Injection](#6-%EF%B8%8F-dependency-injection-configuration-spring)
   - [Proxy Pattern](#7--proxy-pattern-abstraction-de-la-persistance)
6. [Structure des modules](#-structure-des-modules)
7. [Choix technologiques](#-choix-technologiques)
8. [Démarrage rapide](#-démarrage-rapide)
9. [API Endpoints](#-api-endpoints)
10. [Tests](#-tests)

---

## ✨ Fonctionnalités principales

### 👤 Clients
- Création et gestion des clients
- Validation d'unicité (nom, prénom, date de naissance)
- Validation des formats (permis de conduire, nom, prénom)

### 🚙 Véhicules
- Gestion du parc automobile
- Validation des plaques d'immatriculation (format FR : `AA-123-AA`)
- Gestion des états : `AVAILABLE`, `IN_RENTAL`, `BROKEN`
- Gestion des motorisations (essence, diesel, électrique, hybride)

### 📝 Contrats de location
- Cycle de vie complet : `PENDING` → `IN_PROGRESS` → `COMPLETED`
- Gestion des retards (`LATE`) et annulations (`CANCELLED`)
- Règles métier complexes :
    - Annulation automatique si véhicule en panne
    - Gestion des retards via tâches planifiées (scheduler)
    - Cascade d'annulation sur les contrats futurs en cas de retard

---

## 🏗️ Architecture — Hexagonale (Ports & Adapters)

### Vue d'ensemble

```plaintext
            ┌─────────────────────────────────────────────────┐
            │        🔌 ADAPTATEURS PRIMAIRES (IN)            │
            │      (Déclenchent les actions métier)           │
            ├─────────────────────────────────────────────────┤
            │  adapters-in-rest      │  adapters-in-scheduler │
            │  (API REST/HTTP)       │  (Tâches CRON)         │
            └──────────────┬─────────┴───────────┬────────────┘
                           │  Ports IN           │
                           ▼                     ▼
                     ┌─────────────────────────────────┐
                     │       📦 APPLICATION            │
                     │    (Composition & Config)       │
                     │    - BeanConfiguration          │
                     │    - Point d'entrée Spring      │
                     └────────────┬────────────────────┘
                                  │
                                  ▼
                     ┌─────────────────────────────────┐
                     │         🎯 DOMAIN               │
                     │    (Logique métier pure)        │
                     │                                 │
                     │  ✓ Zéro dépendance technique    │
                     │  ✓ Java pur + Jakarta Valid.   │
                     │  ✓ Testable en isolation        │
                     │                                 │
                     │  Services → Validators → Models │
                     └────────────┬────────────────────┘
                                  │  Ports OUT
                                  ▼
                     ┌─────────────────────────────────┐
                     │   🔌 ADAPTATEURS SECONDAIRES    │
                     │        (OUT - Driven)           │
                     ├─────────────────────────────────┤
                     │      adapters-out-bdd           │
                     │      (Persistance MongoDB)      │
                     │                                 │
                     │  Implémente les interfaces      │
                     │  définies dans le Domain        │
                     └─────────────────────────────────┘
```

### Flux de données type (Création d'un client)

```plaintext
HTTP POST /api/v1/clients
         │
         ▼
┌─────────────────────────┐
│   ClientsController     │  ← Adaptateur IN (REST)
│   (adapters-in-rest)    │
└──────────┬──────────────┘
           │ ClientInput.convert()
           ▼
┌─────────────────────────┐
│ ClientsServiceValidator │  ← Port IN (Domain)
│      (domain)           │
│                         │
│ → ConstraintValidatorStep
│ → ClientUnicityValidatorStep
│ → ClientUnicityLicenseValidatorStep
└──────────┬──────────────┘
           │ Client (Domain Model)
           ▼
┌─────────────────────────┐
│  ClientStorageProvider  │  ← Port OUT (Interface)
│      (domain)           │
└──────────┬──────────────┘
           │ (Implémentation)
           ▼
┌─────────────────────────┐
│   ClientsBddService     │  ← Adaptateur OUT (MongoDB)
│   (adapters-out-bdd)    │
│                         │
│ → ClientBddMapper.to()
│ → ClientRepository.save()
└─────────────────────────┘
```

---

## 🤔 Pourquoi l'Architecture Hexagonale ?

### Comparaison avec les autres architectures

| Critère | Architecture en Couches (Layered) | RESTful/MVC | Hexagonale (Ports & Adapters) |
|---------|-----------------------------------|-------------|-------------------------------|
| **Isolation du domaine** | ❌ Faible - Le domaine dépend souvent des couches techniques | ❌ Faible - Logique mélangée dans les contrôleurs | ✅ **Forte** - Domaine sans aucune dépendance |
| **Testabilité** | ⚠️ Moyenne - Tests nécessitent souvent un contexte Spring | ⚠️ Moyenne - Tests d'intégration lourds | ✅ **Excellente** - Tests unitaires purs sur le domaine |
| **Changement de BDD** | ❌ Difficile - Code métier couplé aux repositories | ❌ Difficile | ✅ **Facile** - Changer l'adaptateur suffit |
| **Changement d'API** | ⚠️ Moyen | ❌ Impact sur tout le code | ✅ **Facile** - Seul l'adaptateur REST change |
| **Maintenabilité** | ⚠️ Décline avec la taille | ⚠️ Décline rapidement | ✅ **Stable** - Modules indépendants |
| **Évolutivité** | ⚠️ Restructuration nécessaire | ❌ Refactoring majeur | ✅ **Native** - Ajouter un adaptateur suffit |
| **Courbe d'apprentissage** | ✅ Simple | ✅ Simple | ⚠️ Plus complexe initialement |

### Avantages concrets dans notre projet

#### 1. **Domaine métier protégé**
```java
// domain/pom.xml - Aucune dépendance Spring !
<dependencies>
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <!-- C'est tout ! Pas de Spring, pas de MongoDB -->
</dependencies>
```

#### 2. **Inversion de dépendance**
Le domaine définit les **interfaces** (Ports OUT), les adaptateurs les **implémentent** :

```java
// Port OUT (dans domain) - Le domaine définit le contrat
public interface ClientStorageProvider {
    Client save(Client client);
    Optional<Client> get(String id);
    // ...
}

// Adaptateur OUT (dans adapters-out-bdd) - Implémentation concrète
@Service
public class ClientsBddService implements ClientStorageProvider {
    private final ClientRepository repository;  // MongoDB
    // ...
}
```

#### 3. **Remplacement facile des technologies**
Besoin de passer de MongoDB à PostgreSQL ?
- ✅ Créer un nouveau module `adapters-out-postgres`
- ✅ Implémenter `ClientStorageProvider`
- ✅ Changer la configuration dans `BeanConfiguration`
- ❌ **Zéro modification du domaine métier**

#### 4. **Multi-points d'entrée**
Notre application a deux adaptateurs IN :
- `adapters-in-rest` : API HTTP pour les clients externes
- `adapters-in-scheduler` : Tâches CRON pour les règles métier automatiques

Les deux utilisent **le même domaine métier** sans duplication de code.

---

## 🎨 Design Patterns utilisés

### 1. 🔗 Chain of Responsibility (Validation)

**Localisation** : `domain/src/main/java/com/imt/common/validators/`

**Problème résolu** : Valider un objet avec plusieurs règles séquentielles, chaque règle pouvant arrêter la chaîne.

**Implémentation** :

```java
// Classe abstraite définissant la chaîne
public abstract class AbstractValidatorStep<T> {
    private AbstractValidatorStep<T> nextStep;
    
    public abstract void check(T toValidate) throws ImtException;
    
    public ValidatorResult validate(T toValidate) {
        try {
            this.check(toValidate);
        } catch (ImtException e) {
            return ValidatorResult.invalid(e);
        }
        
        if (Objects.nonNull(this.nextStep)) {
            return this.nextStep.validate(toValidate);
        }
        return ValidatorResult.valid();
    }
    
    public AbstractValidatorStep<T> linkWith(AbstractValidatorStep<T> nextStep) {
        // Lie les maillons de la chaîne
        if (Objects.isNull(this.nextStep)) {
            this.nextStep = nextStep;
        } else {
            this.nextStep.linkWith(nextStep);
        }
        return this;
    }
}
```

**Utilisation** :

```java
// Dans ClientsServiceValidator
public Client create(final Client client) throws ImtException {
    new ConstraintValidatorStep<Client>()           // 1. Valide @NotNull, @Pattern...
        .linkWith(new ClientUnicityValidatorStep(service))      // 2. Vérifie unicité nom/prénom/date
        .linkWith(new ClientUnicityLicenseValidatorStep(service)) // 3. Vérifie unicité permis
        .validate(client)
        .throwIfInvalid();
    
    return super.create(client);
}
```

**Avantages** :
- ✅ Chaque règle est isolée dans sa propre classe
- ✅ Ajout/suppression de règles sans modifier le code existant
- ✅ Ordre d'exécution contrôlé
- ✅ Réutilisation des validateurs entre les entités

---

### 2. 🔌 Ports & Adapters (Hexagonal Architecture)

**Localisation** : Structure globale du projet

**Ports IN (Interfaces d'entrée)** :
```java
// ClientsServiceValidator - Point d'entrée pour la création de clients
public class ClientsServiceValidator extends ClientsService {
    public Client create(final Client client) throws ImtException { ... }
}
```

**Ports OUT (Interfaces de sortie)** :
```java
// Interface définie dans le domaine
public interface ClientStorageProvider {
    Client save(Client client);
    Optional<Client> get(String id);
    Collection<Client> getAll();
    // ...
}
```

**Adaptateur IN (REST)** :
```java
@RestController
@RequestMapping("/api/v1/clients")
public class ClientsController {
    private final ClientsServiceValidator clientsServiceValidator;
    
    @PostMapping
    public ResponseEntity<ClientOutput> create(@Valid @RequestBody ClientInput input) {
        return new ResponseEntity<>(
            ClientOutput.from(clientsServiceValidator.create(ClientInput.convert(input))),
            HttpStatus.CREATED
        );
    }
}
```

**Adaptateur OUT (MongoDB)** :
```java
@Service
public class ClientsBddService implements ClientStorageProvider {
    private final ClientRepository repository;
    private final ClientBddMapper mapper;
    
    @Override
    public Client save(Client client) {
        ClientEntity entity = mapper.to(client);
        return mapper.from(repository.save(entity));
    }
}
```

---

### 3. 🏭 Builder Pattern (Modèles immuables)

**Localisation** : `domain/src/main/java/com/imt/*/model/`

**Utilisation** :
```java
@Getter
@Builder(toBuilder = true)  // ← Permet de cloner et modifier
@EqualsAndHashCode(of = "id")
public class Client {
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    private String lastName;
    private String firstName;
    // ...
}
```

**Avantages** :
- ✅ Objets immuables (thread-safe)
- ✅ Construction lisible et fluide
- ✅ `toBuilder()` permet des copies modifiées sans mutation

```java
// Modification d'un contrat sans mutation
Contract lateContract = contract.toBuilder()
    .state(ContractStateEnum.LATE)
    .build();
```

---

### 4. 🗺️ Mapper Pattern (DTO ↔ Domain ↔ Entity)

**Localisation** :
- `adapters-in-rest/.../model/` (DTO → Domain)
- `adapters-out-bdd/.../mappers/` (Domain ↔ Entity)

**Flux de transformation** :
```plaintext
ClientInput (DTO)  →  Client (Domain)  →  ClientEntity (MongoDB)
     ↑                     ↕                      ↓
ClientOutput (DTO) ←  Client (Domain)  ←  ClientEntity (MongoDB)
```

**Implémentation** :
```java
// DTO → Domain (dans adapters-in-rest)
public class ClientInput {
    public static Client convert(final ClientInput input) {
        return Client.builder()
            .lastName(input.getLastName())
            .firstName(input.getFirstName())
            .build();
    }
}

// Domain ↔ Entity (dans adapters-out-bdd)
@Component
public class ClientBddMapper extends AbstractBddMapper<Client, ClientEntity> {
    @Override
    public Client from(ClientEntity input) {
        return Client.builder()
            .id(input.getId())
            .firstName(input.getFirstName())
            // ...
            .build();
    }
    
    @Override
    public ClientEntity to(Client object) {
        ClientEntity entity = new ClientEntity();
        entity.setId(object.getId());
        // ...
        return entity;
    }
}
```

**Avantages** :
- ✅ Isolation totale entre les couches
- ✅ Le domaine ne connaît ni les DTOs HTTP ni les entités MongoDB
- ✅ Évolution indépendante de l'API et de la BDD

---

### 5. 🎭 Decorator Pattern (Services avec validation)

**Localisation** : `domain/src/main/java/com/imt/*/`

**Problème résolu** : Ajouter dynamiquement des responsabilités (validation) à un objet sans modifier son code original.

**Implémentation** :
```java
// Service de base (Component concret)
public class ClientsService {
    protected ClientStorageProvider service;
    
    public Client create(final Client client) throws ImtException {
        return this.service.save(client);
    }
}

// Décorateur qui ajoute la validation
public class ClientsServiceValidator extends ClientsService {
    
    public Client create(final Client client) throws ImtException {
        // 🎨 DÉCORATION : Ajout de comportement AVANT
        new ConstraintValidatorStep<Client>()
            .linkWith(new ClientUnicityValidatorStep(service))
            .linkWith(new ClientUnicityLicenseValidatorStep(service))
            .validate(client)
            .throwIfInvalid();
        
        // Délégation au composant de base
        return super.create(client);
    }
}
```

**Pourquoi c'est un Decorator et non un Template Method ?**
- **Template Method** : La classe parente définit un squelette d'algorithme avec des "hooks" abstraits que les sous-classes implémentent.
- **Decorator** : La sous-classe **enveloppe** le comportement existant en ajoutant des responsabilités avant/après l'appel au parent.

Ici, `ClientsServiceValidator` **décore** `ClientsService` en ajoutant une couche de validation tout en préservant l'interface originale.

**Avantages** :
- ✅ Séparation des préoccupations (CRUD vs Validation)
- ✅ Possibilité d'utiliser `ClientsService` sans validation si besoin
- ✅ Composition de décorateurs possible

---

### 6. 🏗️ Dependency Injection (Configuration Spring)

**Localisation** : `application/src/main/java/com/imt/config/BeanConfiguration.java`

**Problème** : Le module `domain` n'a pas de dépendance Spring, donc pas d'annotations `@Service`.

**Solution** :
```java
@Configuration
public class BeanConfiguration {
    
    @Bean
    public ClientsServiceValidator clientsServiceValidator(
            final ClientStorageProvider clientStorageProvider) {
        // Injection manuelle - le domaine reste pur
        return new ClientsServiceValidator(clientStorageProvider);
    }
    
    @Bean
    public VehicleServiceValidator vehicleServiceValidator(
            final VehicleStorageProvider vehicleStorageProvider) {
        return new VehicleServiceValidator(vehicleStorageProvider);
    }
}
```

---

### 7. 🔀 Proxy Pattern (Abstraction de la persistance)

**Localisation** : 
- Interface : `domain/src/main/java/com/imt/clients/ClientStorageProvider.java`
- Implémentation : `adapters-out-bdd/src/main/java/com/imt/adaptersoutbdd/clients/ClientsBddService.java`

**Problème résolu** : Permettre au domaine d'accéder à la persistance sans connaître l'implémentation concrète (MongoDB, PostgreSQL, mémoire...).

**Implémentation** :
```java
// Interface Proxy (dans le Domain)
public interface ClientStorageProvider {
    Client save(Client client);
    Optional<Client> get(String id);
    Collection<Client> getAll();
    void delete(String id);
    // ...
}

// Sujet Réel (dans adapters-out-bdd) - Le "vrai" accès aux données
@Service
public class ClientsBddService implements ClientStorageProvider {
    
    private final ClientRepository repository;  // MongoDB
    private final ClientBddMapper mapper;
    
    @Override
    public Client save(Client client) {
        // Conversion Domain → Entity
        ClientEntity entity = mapper.to(client);
        // Accès réel à MongoDB
        ClientEntity saved = repository.save(entity);
        // Conversion Entity → Domain
        return mapper.from(saved);
    }
    
    @Override
    public Optional<Client> get(String id) {
        return repository.findById(id)
                .map(mapper::from);
    }
}
```

**Flux avec le Proxy** :
```plaintext
Domain                          Proxy Interface                  Implémentation Réelle
┌───────────────┐              ┌───────────────────┐             ┌───────────────────┐
│ClientsService │ ──────────►  │ClientStorageProvider│ ────────► │ ClientsBddService │
│               │   appelle    │   (Interface)     │   délègue   │   (MongoDB)       │
└───────────────┘              └───────────────────┘             └───────────────────┘
                                                                          │
                                                                          ▼
                                                                 ┌───────────────────┐
                                                                 │  ClientRepository │
                                                                 │ (MongoRepository) │
                                                                 └───────────────────┘
```

**Pourquoi c'est un Proxy ?**
- Le domaine utilise `ClientStorageProvider` comme s'il accédait directement aux données
- En réalité, l'interface **intercepte** les appels et les **délègue** à l'implémentation concrète
- Le domaine ne sait pas (et n'a pas besoin de savoir) si les données viennent de MongoDB, PostgreSQL ou d'un mock en mémoire

**Avantages** :
- ✅ **Découplage total** : Le domaine ne dépend pas de la technologie de persistance
- ✅ **Interchangeabilité** : Changer de BDD = créer une nouvelle implémentation du proxy
- ✅ **Testabilité** : Facile de créer un mock/stub pour les tests unitaires
- ✅ **Lazy loading possible** : Le proxy peut différer le chargement réel des données

---

## 📁 Structure des modules

### Pourquoi des modules Maven séparés ?

| Raison | Explication |
|--------|-------------|
| **Isolation des dépendances** | Maven empêche physiquement d'importer Spring dans le domaine |
| **Compilation indépendante** | Chaque module peut être compilé et testé seul |
| **Déploiement flexible** | Possibilité de déployer les modules séparément (microservices) |
| **Clarté architecturale** | La structure du projet reflète l'architecture |
| **Gestion des versions** | Chaque module peut évoluer indépendamment |

### Détail des modules

```plaintext
IMT-Architecture-Logiciel/
│
├── 📦 domain/                          # 🎯 CŒUR MÉTIER
│   ├── pom.xml                         # Dépendances minimales (jakarta.validation, lombok)
│   └── src/main/java/com/imt/
│       ├── clients/
│       │   ├── model/
│       │   │   └── Client.java         # Entité métier immutable
│       │   ├── validators/
│       │   │   ├── ClientUnicityValidatorStep.java
│       │   │   └── ClientUnicityLicenseValidatorStep.java
│       │   ├── ClientStorageProvider.java   # [PORT OUT] Interface repository
│       │   ├── ClientsService.java          # Service CRUD de base
│       │   └── ClientsServiceValidator.java # [PORT IN] Service avec validation
│       │
│       ├── vehicle/
│       │   ├── model/
│       │   │   ├── Vehicle.java
│       │   │   ├── EngineTypeEnum.java
│       │   │   └── VehicleStateEnum.java
│       │   ├── validators/
│       │   ├── VehicleStorageProvider.java  # [PORT OUT]
│       │   ├── VehicleService.java
│       │   └── VehicleServiceValidator.java # [PORT IN]
│       │
│       ├── contracts/
│       │   ├── model/
│       │   │   ├── Contract.java
│       │   │   └── ContractStateEnum.java
│       │   ├── validators/
│       │   │   ├── ContractPeriodValidatorStep.java
│       │   │   ├── ContractStateValidatorStep.java
│       │   │   ├── ContractVehicleAvailabilityValidatorStep.java
│       │   │   └── ContractVehicleReadinessValidatorStep.java
│       │   ├── ContractStorageProvider.java # [PORT OUT]
│       │   ├── ContractsService.java        # Inclut logique métier complexe
│       │   └── ContractsServiceValidator.java
│       │
│       └── common/
│           ├── exceptions/
│           │   ├── ImtException.java        # Exception de base
│           │   ├── BadRequestException.java # HTTP 400
│           │   ├── ConflictException.java   # HTTP 409
│           │   └── ResourceNotFoundException.java # HTTP 404
│           ├── model/
│           │   └── ValidatorResult.java
│           └── validators/
│               ├── AbstractValidatorStep.java    # Pattern Chain of Responsibility
│               └── ConstraintValidatorStep.java  # Validation Jakarta
│
├── 📦 adapters-in-rest/                # 🌐 API REST
│   ├── pom.xml                         # spring-boot-starter-web, springdoc-openapi
│   └── src/main/java/com/imt/adaptersinrest/
│       ├── clients/
│       │   ├── ClientsController.java  # @RestController
│       │   └── model/
│       │       ├── input/
│       │       │   ├── ClientInput.java      # DTO création (POST)
│       │       │   └── ClientUpdateInput.java # DTO mise à jour (PATCH)
│       │       └── output/
│       │           └── ClientOutput.java     # DTO réponse
│       ├── vehicle/
│       ├── contracts/
│       └── common/
│           └── model/                  # DTOs partagés
│
├── 📦 adapters-out-bdd/                # 💾 PERSISTANCE MONGODB
│   ├── pom.xml                         # spring-boot-starter-data-mongodb
│   └── src/main/java/com/imt/adaptersoutbdd/
│       ├── clients/
│       │   ├── ClientsBddService.java  # Implémente ClientStorageProvider
│       │   └── repositories/
│       │       ├── ClientRepository.java    # Interface MongoRepository
│       │       ├── entities/
│       │       │   └── ClientEntity.java    # Document MongoDB
│       │       └── mappers/
│       │           └── ClientBddMapper.java # Domain ↔ Entity
│       ├── vehicle/
│       ├── contracts/
│       └── common/
│
├── 📦 adapters-in-scheduler/           # ⏰ TÂCHES PLANIFIÉES
│   ├── pom.xml
│   └── src/main/java/com/imt/adaptersinscheduler/
│       └── jobs/
│           └── ContractJob.java        # @Scheduled - Gestion des retards
│
└── 📦 application/                     # 🚀 BOOTSTRAP
    ├── pom.xml                         # Agrège tous les modules
    └── src/main/java/com/imt/
        ├── application/
        │   └── Application.java        # @SpringBootApplication
        └── config/
            └── BeanConfiguration.java  # Injection des services du domaine
```

### Graphe de dépendances Maven

```plaintext
                    ┌─────────────────────┐
                    │     application     │
                    │   (Point d'entrée)  │
                    └──────────┬──────────┘
                               │
           ┌───────────────────┼───────────────────┐
           │                   │                   │
           ▼                   ▼                   ▼
┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│ adapters-in-rest │ │adapters-in-sched.│ │ adapters-out-bdd │
└────────┬─────────┘ └────────┬─────────┘ └────────┬─────────┘
         │                    │                    │
         └───────────────────┬┴────────────────────┘
                             │
                             ▼
                    ┌─────────────────────┐
                    │       domain        │
                    │   (Aucune dép. ext) │
                    └─────────────────────┘
```

---

## 🏗️ Architecture Logicielle

L'application est structurée en **multi-modules Maven** pour forcer physiquement le respect de l'architecture hexagonale.

### 1. Le Noyau (Core Domain) - `domain`
C'est le cœur de l'application. Il contient la logique métier pure et ne dépend d'aucun framework (pas de Spring, pas de Mongo).
- **Modèles** : Objets riches (`Client`, `Vehicle`, `Contract`).
- **Ports (Interfaces)** : Définissent comment le domaine communique avec l'extérieur (ex: `ClientStorageProvider`).
- **Services** : Orchestration de la logique (`ClientsService`).

### 2. Les Adaptateurs (Adapters)
Ils font le lien entre le monde extérieur et le domaine.
- **Adapters-IN (Primaires)** : Pilotent l'application.
    - `adapters-in-rest` : Contrôleurs REST exposant l'API.
    - `adapters-in-scheduler` : Tâches planifiées (Batchs) pour la détection des retards.
- **Adapters-OUT (Secondaires)** : Pilotés par l'application.
    - `adapters-out-bdd` : Implémentation de la persistance avec MongoDB.

### 3. L'Assemblage - `application`
Le point d'entrée (`Main`) qui configure Spring Boot, scanne les modules et injecte les dépendances (Inversion de contrôle).

---

## 🔧 Choix technologiques

### 🐳 Pourquoi Docker ?

| Avantage | Description |
|----------|-------------|
| **Environnement reproductible** | "It works on my machine" n'est plus un problème |
| **Isolation** | L'application et MongoDB dans des conteneurs séparés |
| **Déploiement simplifié** | `docker-compose up` et tout fonctionne |
| **CI/CD ready** | Images identiques en dev, test et production |
| **Pas d'installation locale** | Pas besoin d'installer MongoDB sur la machine |

**Architecture Docker** :
```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - db
    environment:
      - SPRING_DATA_MONGODB_URI=mongodb://user:pass@db:27017/carrentaldb

  db:
    image: mongo
    ports:
      - "27017:27017"
    volumes:
      - mongo-data:/data/db  # Persistance des données
```

**Multi-stage build (Dockerfile)** :
```dockerfile
# Stage 1: Build avec Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
COPY . .
RUN mvn -pl application -am clean package -DskipTests

# Stage 2: Runtime léger
FROM eclipse-temurin:21-jre-jammy
COPY --from=builder /workspace/application/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### 🍃 Pourquoi MongoDB ?

| Avantage | Description |
|----------|-------------|
| **Schéma flexible** | Pas de migrations SQL, évolution facile des modèles |
| **Documents JSON** | Mapping naturel avec les objets Java |
| **Scalabilité horizontale** | Sharding natif pour la montée en charge |
| **Performance lectures** | Excellent pour les requêtes de lecture fréquentes |
| **Spring Data MongoDB** | Intégration simplifiée avec Spring Boot |

**Pourquoi pas SQL ?**
- Notre domaine métier est centré sur des **agrégats** (Client, Vehicle, Contract)
- Pas de jointures complexes nécessaires
- Les relations sont gérées par **références** (UUID), pas par clés étrangères
- Flexibilité pour l'évolution du schéma sans migrations

**Exemple d'entité MongoDB** :
```java
@Document(collection = "clients")
public class ClientEntity {
    @Id
    private String id;
    private String lastName;
    private String firstName;
    private LocalDate dateOfBirth;
    private String licenseNumber;
    private String address;
}
```

---

### ☕ Pourquoi Java 21 + Spring Boot 3.x ?

| Technologie | Justification |
|-------------|---------------|
| **Java 21 LTS** | Support long terme, Records, Pattern Matching, Virtual Threads |
| **Spring Boot 3.5** | Jakarta EE 10, performances améliorées, AOT compilation |
| **Jakarta Validation** | Annotations de validation standards (`@NotNull`, `@Pattern`) |
| **Lombok** | Réduction du boilerplate (getters, builders, constructors) |
| **SpringDoc OpenAPI** | Documentation Swagger automatique |

---

## 🚀 Démarrage rapide

### Prérequis
- Docker & Docker Compose
- (Optionnel) Java 21 & Maven 3.9+ pour le développement local

### Lancement avec Docker (recommandé)

```bash
# Cloner le projet
git clone <url-du-repo>
cd IMT-Architecture-Logiciel

# Lancer l'application et MongoDB
docker-compose up --build

# L'API est accessible sur http://localhost:8080
# Swagger UI : http://localhost:8080/swagger-ui.html
```

### Lancement local (développement)

```bash
# Démarrer MongoDB seul
docker-compose up db

# Dans un autre terminal, lancer l'application
./mvnw -pl application -am spring-boot:run
```

### Compilation seule

```bash
# Compiler tous les modules
./mvnw clean package

# Compiler sans les tests
./mvnw clean package -DskipTests
```

---

## 🌐 API Endpoints

### Clients

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/v1/clients` | Liste tous les clients |
| `GET` | `/api/v1/clients/{id}` | Récupère un client par ID |
| `POST` | `/api/v1/clients` | Crée un nouveau client |
| `PATCH` | `/api/v1/clients/{id}` | Met à jour un client |
| `DELETE` | `/api/v1/clients/{id}` | Supprime un client |

### Véhicules

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/v1/vehicles` | Liste tous les véhicules |
| `GET` | `/api/v1/vehicles/{id}` | Récupère un véhicule par ID |
| `POST` | `/api/v1/vehicles` | Crée un nouveau véhicule |
| `PATCH` | `/api/v1/vehicles/{id}` | Met à jour un véhicule |
| `DELETE` | `/api/v1/vehicles/{id}` | Supprime un véhicule |

### Contrats

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/v1/contracts` | Liste tous les contrats |
| `GET` | `/api/v1/contracts/{id}` | Récupère un contrat par ID |
| `POST` | `/api/v1/contracts` | Crée un nouveau contrat |
| `PATCH` | `/api/v1/contracts/{id}` | Met à jour un contrat |
| `DELETE` | `/api/v1/contracts/{id}` | Supprime un contrat |

### Exemple de requête

```bash
# Créer un client
curl -X POST http://localhost:8080/api/v1/clients \
  -H "Content-Type: application/json" \
  -d '{
    "lastName": "Dupont",
    "firstName": "Jean",
    "dateOfBirth": "1990-05-15",
    "licenseNumber": "ABC123456789",
    "address": "123 Rue de Paris, 75001 Paris"
  }'
```

---

## 🧪 Tests

### Exécution des tests

```bash
# Tous les tests
./mvnw test

# Tests d'un module spécifique
./mvnw -pl domain test

# Tests avec couverture
./mvnw test jacoco:report
```

### Stratégie de test

| Couche | Type de test | Outils |
|--------|--------------|--------|
| **Domain** | Tests unitaires purs | JUnit 5, Mockito, AssertJ |
| **Adapters-in-rest** | Tests d'intégration | Spring MockMvc |
| **Adapters-out-bdd** | Tests d'intégration | Testcontainers MongoDB |

**Avantage de l'architecture hexagonale** : Le domaine peut être testé **sans Spring Boot**, avec des mocks simples des interfaces.

---

## 📄 Licence

Projet réalisé dans le cadre du TP d'Architecture Logicielle - IMT IMT Nord Europe

---

## 👥 Auteurs

- Étudiants IMT Nord Europe - Promotion 27 :

-> Thomas DUBOT

-> Théo LEBIEZ

-> Axel ELIAS
