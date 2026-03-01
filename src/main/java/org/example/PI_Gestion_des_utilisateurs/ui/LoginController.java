package org.example.PI_Gestion_des_utilisateurs.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import org.example.PI_Gestion_des_utilisateurs.services.utilisateur_service;
import org.example.PI_Gestion_des_utilisateurs.tools.PasswordUtil;
import org.example.PI_Gestion_des_utilisateurs.ui.navigation.SceneNavigator;

import java.util.Optional;

/**
 * Contrôleur pour la page de connexion
 */
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final utilisateur_service service = new utilisateur_service();

    @FXML
    public void initialize() {
        // Focus sur le champ email au démarrage
        emailField.requestFocus();
        
        // Activer le bouton seulement si les champs sont remplis
        loginButton.disableProperty().bind(
            emailField.textProperty().isEmpty().or(passwordField.textProperty().isEmpty())
        );
    }

    @FXML
    private void onLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation de base
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        // Vérification de l'email
        if (!isValidEmail(email)) {
            showError("Format d'email invalide");
            return;
        }

        // Authentification
        Optional<utilisateur> userOpt = service.rechercherutilisateurParEmail(email);
        if (userOpt.isPresent()) {
            utilisateur user = userOpt.get();
            
            // Vérification du mot de passe (support hash et clair)
            boolean passwordValid = false;
            if (PasswordUtil.isHashedPassword(user.getPassword())) {
                // Le mot de passe stocké est hashé
                passwordValid = PasswordUtil.verifyPassword(password, user.getPassword());
            } else {
                // Le mot de passe stocké est en clair
                passwordValid = password.equals(user.getPassword());
            }
            
            if (passwordValid) {
                // Succès - navigation vers l'accueil
                SceneNavigator.goTo("/app/home.fxml", "Accueil");
            } else {
                // Échec - afficher un message d'erreur
                showError("Email ou mot de passe incorrect");
            }
        } else {
            // Échec - afficher un message d'erreur
            showError("Email ou mot de passe incorrect");
        }
    }

    @FXML
    private void onForgotPassword() {
        try {
            // Valider l'adresse email
            String email = emailField.getText().trim();
            if (isValidEmail(email)) {
                // Utiliser le service existant pour la réinitialisation
                boolean success = service.motDePasseOublie(email);
                
                if (success) {
                    showInfo("Mot de passe oublié", 
                            "Un email de réinitialisation a été envoyé à l'adresse :\n" +
                            email + "\n\n" +
                            "🔑 Veuillez consulter votre boîte de réception.\n\n" +
                            "⏰ Le mot de passe temporaire expirera dans 24 heures.\n\n" +
                            "📧 Veuillez le changer lors de votre prochaine connexion.");
                } else {
                    showError("Erreur", "Aucun utilisateur trouvé avec cet email.");
                }
            } else {
                showError("Email invalide", "Veuillez entrer une adresse email valide.");
            }
        } catch (Exception e) {
            showError("Erreur", "Une erreur est survenue lors de l'envoi du mot de passe temporaire.");
        }
    }

    /**
     * Affiche un message d'information
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche un message d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        
        // Masquer le message après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> errorLabel.setVisible(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Validation simple de l'email
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
    
    /**
     * Gère le clic sur le bouton "Annuler"
     */
    @FXML
    private void onCancel() {
        // Fermer l'application
        javafx.application.Platform.exit();
        System.exit(0);
    }
}
