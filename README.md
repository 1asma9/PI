# Gestion des Utilisateurs - Application Java/MySQL

## Description
Application console de gestion des utilisateurs développée en Java avec connexion à une base de données MySQL.

## Structure du projet
```
src/main/java/org/example/PI_Gestion_des_utilisateurs/
├── entities/
│   ├── utilisateur.java      # Entité Utilisateur
│   └── role.java             # Entité Rôle
├── interfaces/
│   └── utilisateur_interface.java  # Interface des services
├── services/
│   └── utilisateur_service.java    # Implémentation des services
├── tools/
│   └── MaConnection.java    # Singleton de connexion à la BDD
├── tests/
│   └── Main.java            # Classe de test
└── Application.java         # Classe principale de l'application
```

## Prérequis
- Java 17 ou supérieur
- MySQL Server 8.0 ou supérieur
- Maven 3.6 ou supérieur

## Installation

### 1. Configuration de la base de données
1. Démarrez votre serveur MySQL
2. Exécutez le script SQL `database_schema.sql` pour créer la base de données et les tables :
   ```sql
   mysql -u root -p < database_schema.sql
   ```

### 2. Configuration de la connexion
Modifiez les paramètres de connexion dans le fichier `src/main/java/org/example/PI_Gestion_des_utilisateurs/tools/MaConnection.java` si nécessaire :
```java
private final String URL = "jdbc:mysql://localhost:3306/projet intégré";
private final String USER = "root";
private final String PASS = "";  // Mettez votre mot de passe MySQL
```

### 3. Compilation et exécution
1. Compilez le projet avec Maven :
   ```bash
   mvn clean compile
   ```

2. Exécutez l'application :
   ```bash
   mvn exec:java -Dexec.mainClass="org.example.PI_Gestion_des_utilisateurs.Application"
   ```

## Fonctionnalités

L'application propose un menu console avec les options suivantes :

1. **Ajouter un utilisateur** : Création d'un nouvel utilisateur avec validation des données
2. **Afficher tous les utilisateurs** : Liste de tous les utilisateurs enregistrés
3. **Rechercher un utilisateur par email** : Recherche spécifique d'un utilisateur
4. **Modifier un utilisateur** : Mise à jour des informations d'un utilisateur
5. **Supprimer un utilisateur** : Suppression d'un utilisateur par son ID
6. **Quitter** : Fermeture de l'application

## Validation des données
- **Nom et Prénom** : Ne doivent pas être vides
- **Email** : Doit contenir le symbole @ et être unique
- **Mot de passe** : Minimum 6 caractères

## Architecture

### Entités
- **utilisateur** : Représente un utilisateur avec ses attributs (id, nom, prénom, email, password, dateCreation, roles)
- **role** : Représente un rôle (id, nom, description)

### Services
L'interface `utilisateur_interface` définit les contrats suivants :
- `ajouterutilisateur()` : Ajoute un nouvel utilisateur
- `afficherutilisateurs()` : Retourne la liste des utilisateurs
- `modifierutilisateur()` : Modifie un utilisateur existant
- `supprimerutilisateur()` : Supprime un utilisateur
- `rechercherutilisateurParEmail()` : Recherche par email
- `associerRoleAutilisateur()` : Associe un rôle à un utilisateur
- `verifierEmailUnique()` : Vérifie l'unicité de l'email
- `validerDonneesutilisateur()` : Valide les données utilisateur

### Connexion à la base de données
La classe `MaConnection` implémente le pattern Singleton pour garantir une seule connexion à la base de données.

## Tests
Pour tester l'application, vous pouvez utiliser la classe `Main.java` dans le package `tests` qui effectue des tests de base.

## Dépannage

### Problèmes courants
1. **Erreur de connexion** : Vérifiez que MySQL est démarré et que les identifiants dans `MaConnection.java` sont corrects
2. **Erreur de compilation** : Assurez-vous d'avoir Java 17 et Maven correctement installés
3. **Base de données introuvable** : Exécutez bien le script `database_schema.sql` avant de lancer l'application

### Logs
L'application affiche des messages d'erreur dans la console en cas de problème avec la base de données ou la validation des données.

## Auteur
Projet développé dans le cadre d'un exercice de gestion des utilisateurs en Java.
