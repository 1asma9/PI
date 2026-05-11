# ✈ Voyage — Travel & Accommodation Management Platform

> Application desktop **JavaFX 21** de gestion de voyages, hébergements, activités et services de voyage avec fonctionnalités IA et paiements intégrés.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=flat-square)
![Hibernate](https://img.shields.io/badge/Hibernate-6-green?style=flat-square&logo=hibernate)
![Maven](https://img.shields.io/badge/Maven-3.9-purple?style=flat-square&logo=apachemaven)
![MySQL](https://img.shields.io/badge/MySQL-8-yellow?style=flat-square&logo=mysql)

---

## 🎯 Aperçu du projet

Voyage est une plateforme desktop complète qui permet aux utilisateurs de :

- 🏨 Parcourir et gérer des hébergements (**Hébergement / Chambre**)
- 🧗 Découvrir et réserver des activités (**Activités**)
- 🗓️ Planifier des voyages et séjours
- 📋 Gérer des réservations (hébergements & activités)
- ✍️ Écrire et interagir avec des blogs de voyage
- ⭐ Noter et évaluer des hébergements et expériences
- 🎫 Communiquer avec le support via un système de tickets

La plateforme inclut des interfaces **admin** et **client** avec double authentification, des fonctionnalités IA, et des intégrations externes pour les paiements, traductions et notifications.

---

## 🏗️ Architecture

Application desktop **JavaFX 21** en architecture MVC avec :

### Composants principaux

| Composant | Technologie JavaFX | Équivalent Symfony |
|---|---|---|
| Framework UI | JavaFX 21 | Symfony 6.4 |
| ORM | Hibernate 6 + JPA | Doctrine ORM |
| Migrations BDD | Flyway | Doctrine Migrations |
| Templating | FXML + SceneBuilder | Twig |
| Build | Maven 3.9 | Composer |

### Interfaces

**Interface Admin** (`fxml/admin/`)
- Dashboard administratif et gestion complète
- Protégé par authentification admin
- Accès réservé aux rôles administrateurs

**Interface Client** (`fxml/client/`)
- Application publique orientée utilisateur
- Authentification client séparée

### Modules métier

| Module | Description |
|---|---|
| `Activite` | Activités et expériences |
| `Hebergement` | Logements et chambres (Chambre) |
| `Destination` | Destinations de voyage |
| `Reservation` | Gestion des bookings (activités & hébergements) |
| `Blog` | Publication de contenus avec engagement |
| `Avis` | Notes et évaluations |
| `Reclamation` | Tickets et réclamations support |
| `Users / Role` | Authentification et autorisations |
| `Transport` | Informations de transport |
| `Voyage` | Planification de séjours |

---

## 🔐 Fonctionnalités clés

### Authentification & Autorisation
- Double authentification : chemins de connexion admin et client séparés
- Contrôle d'accès basé sur les rôles via l'entité `Users` et la collection `Role`
- Vérification de compte actif via `UserSession`
- Gestion des sessions JavaFX

### Fonctionnalités Blog
- Workflow brouillon / publication
- Traduction de contenu assistée par IA
- Suivi d'engagement (notes, commentaires, vues)
- Modération des commentaires avec filtre de mots interdits
- Tableau Vision Board (`VisionBoardService`)
- Système de vues (`BlogViews`) et de notes (`BlogRating`)

### Intégrations externes

| Service | Librairie Java | Usage |
|---|---|---|
| Firebase | `firebase-admin` SDK | Notifications temps réel |
| Stripe | `stripe-java` | Traitement des paiements |
| Google OAuth2 | `google-oauth-client` | Connexion sociale |
| Mailjet | `mailjet-apiv3-java` | Envoi d'emails |
| Translation API | `OkHttp` / REST | Traduction de blogs |

### Fonctionnalités supplémentaires
- 📄 Génération de PDF (`iText` / `OpenPDF`)
- 📃 Pagination (`TableView` JavaFX natif)
- ✉️ Vérification email
- 💬 Système de commentaires et signalements admin
- ❤️ Favoris et liste de souhaits utilisateurs
- 📅 Gestion de disponibilité des hébergements

---

## 📋 Prérequis

- **Java** : 21 LTS ou supérieur (Temurin recommandé)
- **Maven** : 3.9 ou supérieur
- **Base de données** : MySQL 8 / MariaDB
- **JavaFX SDK** : 21 (inclus via Maven `javafx-maven-plugin`)

---

## 🚀 Installation & Lancement

### 1. Cloner le projet

```bash
git clone https://github.com/ton-org/voyage-javafx.git
cd voyage-javafx
```

### 2. Configurer la base de données

Éditer `src/main/resources/hibernate.cfg.xml` :

```xml
<property name="connection.url">
    jdbc:mysql://localhost:3306/voyage
</property>
<property name="connection.username">user</property>
<property name="connection.password">password</property>
```

### 3. Variables d'environnement

Créer `src/main/resources/config.properties` :

```properties
DATABASE_URL=jdbc:mysql://localhost:3306/voyage
STRIPE_SECRET_KEY=sk_test_...
FCM_PROJECT_ID=your_firebase_project_id
TRANSLATION_API_URL=https://api.translation-provider.com
TRANSLATION_API_KEY=your_api_key
MAILJET_API_KEY=your_mailjet_key
MAILJET_SECRET_KEY=your_mailjet_secret
```

### 4. Lancer l'application

```bash
# Compiler et lancer directement
mvn clean javafx:run

# Ou créer un JAR exécutable
mvn clean package
java -jar target/voyage-desktop.jar
```

### 5. Migrations base de données

```bash
# Flyway applique les migrations automatiquement au démarrage
# Ou manuellement :
mvn flyway:migrate
```

---

## 🧪 Tests

```bash
# Lancer tous les tests
mvn test

# Lancer un test spécifique
mvn test -Dtest=HebergementServiceTest

# Lancer une méthode spécifique
mvn test -Dtest=HebergementServiceTest#testCreateHebergement
```

Les tests sont configurés avec **JUnit 5** et **Mockito**.

---

## 📁 Structure du projet

```
voyage-javafx/
├── src/
│   ├── main/
│   │   ├── java/tn/voyage/
│   │   │   ├── controller/          # Contrôleurs FXML
│   │   │   │   ├── admin/           # Interface admin
│   │   │   │   └── client/          # Interface client
│   │   │   ├── model/               # Entités JPA
│   │   │   │   ├── Hebergement.java
│   │   │   │   ├── Activite.java
│   │   │   │   ├── Reservation.java
│   │   │   │   ├── Blog.java
│   │   │   │   ├── Users.java
│   │   │   │   └── Role.java
│   │   │   ├── repository/          # Couche d'accès aux données (DAO)
│   │   │   ├── service/             # Logique métier
│   │   │   ├── security/            # Authentification & session
│   │   │   ├── util/                # Helpers, PDF, session
│   │   │   └── MainApp.java         # Point d'entrée
│   │   └── resources/
│   │       ├── fxml/                # Vues FXML
│   │       │   ├── admin/
│   │       │   └── client/
│   │       ├── css/                 # Styles JavaFX
│   │       ├── images/              # Ressources graphiques
│   │       ├── hibernate.cfg.xml    # Configuration Hibernate
│   │       └── config.properties    # Variables d'environnement
│   └── test/
│       └── java/tn/voyage/          # Tests JUnit 5
├── db/migration/                    # Scripts Flyway (V1__, V2__...)
├── pom.xml                          # Dépendances Maven
└── README.md
```

---

## 🛠️ Conventions de développement

### Organisation des contrôleurs
- Contrôleurs et vues FXML organisés par module métier
- Nommage : `HebergementController.java` ↔ `hebergement.fxml`

### Sécurité
- Accès contrôlé via `SessionManager` et vérification de rôle
- Toujours vérifier les permissions avant d'afficher une vue admin

### Rôles utilisateurs
- Utilisateurs liés aux rôles via la collection `Role`
- `getAuthorities()` inclut toujours `ROLE_USER` par défaut
- Assignation des rôles via la relation `Users`

### Validation des données
- Entités avec contraintes Jakarta Bean Validation
- Contrôleurs effectuent une validation explicite avec messages localisés
- Maintenir les deux couches de validation

### Configuration des services
- Paramètres injectés via `config.properties`
- Éviter de coder en dur les valeurs de configuration

---

## 📦 Dépendances principales (`pom.xml`)

### Framework UI
```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21</version>
</dependency>
```

### Données
```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.4.0.Final</version>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>
```

### Services externes
```xml
<!-- Stripe -->
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>25.0.0</version>
</dependency>
<!-- Firebase -->
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
<!-- PDF -->
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf</artifactId>
    <version>1.3.30</version>
</dependency>
```

---

## 📝 Schéma de base de données

| Entité | Description |
|---|---|
| `Users` | Comptes utilisateurs avec rôles |
| `Role` | Rôles et permissions |
| `Hebergement` | Logements avec chambres (Chambre) |
| `Activite` | Activités et expériences |
| `Destination` | Destinations géographiques |
| `Voyage` | Enregistrements de séjours |
| `Reservation` | Bookings hébergements |
| `ReservationActivite` | Bookings activités |
| `Blog` | Articles avec suivi auteur |
| `Commentaire` | Commentaires sur articles |
| `BlogRating` | Notes sur articles |
| `Avis` | Évaluations avec classification TypeAvis |
| `Reclamation` | Tickets et réclamations |
| `Transport` | Options de transport |

---

## 🔄 Workflows courants

### Déployer une nouvelle version

```bash
# 1. Mettre à jour et committer
git add . && git commit -m "feat: nouvelle fonctionnalité"

# 2. Lancer les tests
mvn test

# 3. Vérifier la qualité du code
mvn checkstyle:check

# 4. Build final
mvn clean package
java -jar target/voyage-desktop.jar
```

### Ajouter une nouvelle fonctionnalité

1. Créer l'entité dans `src/main/java/tn/voyage/model/`
2. Créer le script de migration dans `db/migration/V{N}__description.sql`
3. Créer le contrôleur dans `src/main/java/tn/voyage/controller/`
4. Créer la vue FXML dans `src/main/resources/fxml/`
5. Créer le service dans `src/main/java/tn/voyage/service/`
6. Ajouter les tests dans `src/test/`

### Gérer les permissions

1. Définir les rôles en base via l'interface admin
2. Lier les utilisateurs aux rôles via l'entité `Users`
3. Vérifier les permissions dans les contrôleurs avec `SessionManager`

---

## 🤝 Contribuer

- Suivre l'organisation modulaire établie
- Inclure la validation au niveau entité ET contrôleur
- Mettre à jour les tests pour toute nouvelle fonctionnalité
- Lancer `mvn checkstyle:check` avant de committer
- Garder la configuration dans `config.properties`, jamais codée en dur
