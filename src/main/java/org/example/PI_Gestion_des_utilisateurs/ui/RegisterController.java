package org.example.PI_Gestion_des_utilisateurs.ui;

import hebergement.controllers.MainLayoutController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import org.example.PI_Gestion_des_utilisateurs.services.utilisateur_service;
import hebergement.tools.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private final utilisateur_service service = new utilisateur_service();

    @FXML
    public void initialize() {
    }

    private Integer findRoleIdByName(String roleName) {
        String sql = "SELECT id FROM role WHERE name = ?";
        try (Connection connection = MyConnection.getInstance().getCnx();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    private void onRegister() {
        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        String prenom = prenomField.getText() == null ? "" : prenomField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String tel = telephoneField.getText() == null ? "" : telephoneField.getText().trim();
        String pwd = passwordField.getText() == null ? "" : passwordField.getText();
        String confirmPwd = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || pwd.isEmpty() || confirmPwd.isEmpty()) {
            showInlineError("Veuillez remplir tous les champs obligatoires (*).");
            return;
        }

        if (!pwd.equals(confirmPwd)) {
            showInlineError("Les mots de passe ne correspondent pas.");
            return;
        }

        if (!service.verifierEmailUnique(email)) {
            showInlineError("Un compte avec cet email existe déjà.");
            return;
        }

        utilisateur u = new utilisateur(nom, prenom, email, pwd, tel);
        String err = service.validerDonneesutilisateurAvecMessage(u);
        if (err != null) {
            showInlineError(err);
            return;
        }

        // Add user
        if (service.ajouterutilisateur(u)) {
            // Assign ROLE_USER
            Integer roleId = findRoleIdByName("ROLE_USER");
            if (roleId != null) {
                Optional<utilisateur> added = service.rechercherutilisateurParEmail(u.getEmail());
                added.ifPresent(utilisateur -> service.associerRoleAutilisateur(utilisateur.getId(), roleId));
            }

            showPopupInfo("Inscription réussie", "Votre compte a été créé avec succès ! Veuillez vérifier votre adresse email avant de vous connecter.");
            onGoToLogin();
        } else {
            showInlineError(utilisateur_service.lastError != null ? utilisateur_service.lastError : "Erreur lors de l'inscription.");
        }
    }

    @FXML
    private void onGoToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/login.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInlineError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(4000);
                javafx.application.Platform.runLater(() -> errorLabel.setVisible(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void showPopupInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
