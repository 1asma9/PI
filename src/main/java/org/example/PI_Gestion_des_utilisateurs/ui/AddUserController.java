package org.example.PI_Gestion_des_utilisateurs.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import org.example.PI_Gestion_des_utilisateurs.services.utilisateur_service;
import org.example.PI_Gestion_des_utilisateurs.tools.MaConnection;
import org.example.PI_Gestion_des_utilisateurs.ui.navigation.SceneNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AddUserController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField telephoneField;
    @FXML private ComboBox<String> roleCombo;

    private final utilisateur_service service = new utilisateur_service();

    @FXML
    public void initialize() {
        loadRoles();
    }

    private void loadRoles() {
        ObservableList<String> roles = FXCollections.observableArrayList();
        String sql = "SELECT name FROM role ORDER BY name";

        try (Connection connection = MaConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString(1);
                if (name != null && !name.trim().isEmpty()) {
                    roles.add(name.trim());
                }
            }
        } catch (SQLException e) {
            showError("Chargement rôles", "Impossible de charger les rôles: " + e.getMessage());
        }

        roleCombo.setItems(roles);
        if (!roles.isEmpty()) {
            roleCombo.getSelectionModel().selectFirst();
        }
    }

    private Integer findRoleIdByName(String roleName) throws SQLException {
        String sql = "SELECT id FROM role WHERE name = ?";
        try (Connection connection = MaConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }

    private String trimmedOrNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean validateRequiredFields() {
        if (trimmedOrNull(nomField.getText()) == null) {
            showError("Validation", "Le champ 'Nom' est obligatoire.");
            return false;
        }
        if (trimmedOrNull(prenomField.getText()) == null) {
            showError("Validation", "Le champ 'Prénom' est obligatoire.");
            return false;
        }
        if (trimmedOrNull(emailField.getText()) == null) {
            showError("Validation", "Le champ 'Email' est obligatoire.");
            return false;
        }
        if (trimmedOrNull(passwordField.getText()) == null) {
            showError("Validation", "Le champ 'Mot de passe' est obligatoire.");
            return false;
        }
        String tel = trimmedOrNull(telephoneField.getText());
        if (tel != null && !tel.matches("\\d{8,}")) {
            showError("Validation", "Téléphone invalide: uniquement des chiffres (min 8). Ou laisser vide.");
            return false;
        }
        if (roleCombo.getSelectionModel().getSelectedItem() == null) {
            showError("Validation", "Choisis un rôle.");
            return false;
        }
        return true;
    }

    @FXML
    private void onAdd() {
        try {
            if (!validateRequiredFields()) {
                return;
            }

            utilisateur u = new utilisateur(
                    nomField.getText(),
                    prenomField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    telephoneField.getText()
            );

            // Valider les données avec messages d'erreur détaillés
            String erreurValidation = service.validerDonneesutilisateurAvecMessage(u);
            if (erreurValidation != null) {
                showError("Erreur de validation", erreurValidation);
                return;
            }

            boolean ok = service.ajouterutilisateur(u);
            if (!ok) {
                showError("Ajout impossible", "Échec lors de l'ajout. Vérifie les champs et si l'email est déjà utilisé.");
                return;
            }

            String roleName = roleCombo.getSelectionModel().getSelectedItem();
            Integer roleId = roleName == null ? null : findRoleIdByName(roleName);

            if (roleId != null) {
                Optional<utilisateur> added = service.rechercherutilisateurParEmail(u.getEmail());
                if (added.isPresent()) {
                    boolean roleOk = service.associerRoleAutilisateur(added.get().getId(), roleId);
                    if (!roleOk) {
                        showError("Association rôle", "Utilisateur ajouté, mais l'association du rôle a échoué.");
                        return;
                    }
                }
            }

            showInfo("Succès", "Utilisateur ajouté avec succès.");
            clearForm();
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        SceneNavigator.goTo("/app/home.fxml", "Accueil");
    }

    private void clearForm() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        telephoneField.clear();
        if (!roleCombo.getItems().isEmpty()) {
            roleCombo.getSelectionModel().selectFirst();
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onCancel() {
        SceneNavigator.goTo("/app/home.fxml", "Gestion des Utilisateurs");
    }
}
