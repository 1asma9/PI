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
        if (userOpt.isEmpty()) {
            showInlineError("Email ou mot de passe incorrect");
            return;
        }

        utilisateur user = userOpt.get();

        boolean passwordValid;
        if (PasswordUtil.isHashedPassword(user.getPassword())) {
            passwordValid = PasswordUtil.verifyPassword(password, user.getPassword());
        } else {
            passwordValid = password.equals(user.getPassword());
        }

        if (!passwordValid) {
            showInlineError("Email ou mot de passe incorrect");
            return;
        }

        // ✅ stocker l'utilisateur connecté
        MainLayoutController.setCurrentUser(user);

        // ✅ navigation vers main_layout.fxml (fiable)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/main_layout.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            scene.getStylesheets().add(getClass().getResource("/app/app.css").toExternalForm());

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("Accueil");
            stage.setScene(scene);
            stage.show();
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

    // ✅ message d'erreur dans la page
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