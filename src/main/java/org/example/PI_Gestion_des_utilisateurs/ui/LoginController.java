package org.example.PI_Gestion_des_utilisateurs.ui;

import hebergement.controllers.MainLayoutController;
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

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final utilisateur_service service = new utilisateur_service();

    @FXML
    public void initialize() {
        emailField.requestFocus();
        loginButton.disableProperty().bind(
                emailField.textProperty().isEmpty()
                        .or(passwordField.textProperty().isEmpty())
        );
    }

    @FXML
    private void onLogin() {
        System.out.println("🔵 onLogin() appelé");

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
        System.out.println("🔵 userOpt: " + userOpt);
        if (userOpt.isEmpty()) {
            showInlineError("Email ou mot de passe incorrect");
            return;
        }

        utilisateur user = userOpt.get();
        System.out.println("🔵 user: " + user.getEmail() + " role: " + user.getRoleName());

        boolean passwordValid;
        if (PasswordUtil.isHashedPassword(user.getPassword())) {
            passwordValid = PasswordUtil.verifyPassword(password, user.getPassword());
        } else {
            passwordValid = password.equals(user.getPassword());
        }

        System.out.println("🔵 passwordValid: " + passwordValid);
        if (!passwordValid) {
            showInlineError("Email ou mot de passe incorrect");
            return;
        }

        // ✅ Stocker utilisateur
        MainLayoutController.setCurrentUser(user);

        try {
            String role = user.getRoleName();
            System.out.println("🔵 role: " + role);

            String layoutPath = "CLIENT".equals(role)
                    ? "/app/main_layout_client.fxml"
                    : "/app/main_layout_admin.fxml";

            System.out.println("🔵 chargement layout: " + layoutPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(layoutPath));
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/app/app.css").toExternalForm());

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("Voyage & Découverte");
            stage.setScene(scene);
            stage.show();

            System.out.println("✅ Navigation réussie vers: " + layoutPath);

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
}