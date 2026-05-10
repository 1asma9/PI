package org.example.PI_Gestion_des_utilisateurs.ui;

import hebergement.controllers.MainLayoutController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import org.example.PI_Gestion_des_utilisateurs.services.utilisateur_service;
import org.example.PI_Gestion_des_utilisateurs.tools.PasswordUtil;

import java.util.Optional;

public class LoginController {

    // DEV MODE ONLY: bypass authentication and open admin dashboard directly.
    private static final boolean DEV_MODE_BYPASS_LOGIN = false;

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final utilisateur_service service = new utilisateur_service();

    @FXML
    public void initialize() {
        if (DEV_MODE_BYPASS_LOGIN) {
            Platform.runLater(() -> openDashboard("/app/main_layout_client.fxml"));
            return;
        }

        emailField.requestFocus();
        loginButton.disableProperty().bind(
                emailField.textProperty().isEmpty()
                        .or(passwordField.textProperty().isEmpty())
        );
    }

    @FXML
    private void onLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showInlineError("Veuillez remplir tous les champs");
            return;
        }

        if (!isValidEmail(email)) {
            showInlineError("Format d'email invalide");
            return;
        }

        Optional<utilisateur> userOpt = service.rechercherutilisateurParEmail(email);
        if (!userOpt.isPresent()) {
            showInlineError("Email ou mot de passe incorrect");
            return;
        }

        utilisateur user = userOpt.get();
        boolean passwordValid = PasswordUtil.verifyPassword(password, user.getPassword());

        if (!passwordValid) {
            showInlineError("Email ou mot de passe incorrect");
            return;
        }

        if (!user.isActive()) {
            showInlineError("Ce compte est suspendu ou inactif.");
            return;
        }

        // ✅ Stocker utilisateur
        MainLayoutController.setCurrentUser(user);

        // ✅ Sauvegarder dans SessionManager pour MonEspaceController et autres
        int userId    = user.getId();
        String prenom = user.getPrenom() != null ? user.getPrenom() : "";
        String nom    = user.getNom()    != null ? user.getNom()    : "";
        String email2 = user.getEmail()  != null ? user.getEmail()  : "";
        tools.SessionManager.login(userId, prenom + " " + nom, email2, "ROLE_ADMIN".equals(user.getRoleName()));
        System.out.println("✅ Login OK : " + prenom + " " + nom + " → userId=" + userId + " | role=" + user.getRoleName());

        try {
            String role = user.getRoleName();

            String layoutPath = "ROLE_ADMIN".equals(role)
                    ? "/app/main_layout_admin.fxml"
                    : "/app/main_layout_client.fxml";
            openDashboard(layoutPath);

        } catch (Exception e) {
            e.printStackTrace();
            showPopupError("Erreur", "Impossible d'ouvrir l'accueil: " + e.getMessage());
        }

    }

    @FXML
    private void onForgotPassword() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        if (!isValidEmail(email)) {
            showPopupError("Email invalide", "Veuillez entrer une adresse email valide.");
            return;
        }
        boolean success = service.motDePasseOublie(email);
        if (success) {
            showPopupInfo("Mot de passe oublié", "Un email de réinitialisation a été envoyé à : " + email);
        } else {
            showPopupError("Erreur", "Aucun utilisateur trouvé avec cet email, ou envoi email échoué.");
        }
    }

    @FXML
    private void onCancel() {
        javafx.application.Platform.exit();
        System.exit(0);
    }

    @FXML
    private void onGoToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/register.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showPopupError("Erreur", "Impossible d'ouvrir la page d'inscription: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    private void showInlineError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> errorLabel.setVisible(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void showPopupError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showPopupInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openDashboard(String layoutPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(layoutPath));
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/app/app.css").toExternalForm());

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("Voyage & Découverte");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showPopupError("Erreur", "Impossible d'ouvrir l'accueil: " + e.getMessage());
        }
    }
}
