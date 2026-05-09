package org.example.PI_Gestion_des_utilisateurs.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import org.example.PI_Gestion_des_utilisateurs.services.utilisateur_service;
import hebergement.controllers.AdminLayoutController;
import hebergement.controllers.ClientLayoutController;

import java.util.Collections;
import java.util.Optional;

public class EditUserController {

    @FXML private TextField searchIdField;
    @FXML private TextField searchEmailField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField telephoneField;
    @FXML private TableView<utilisateur> usersTable;
    @FXML private TableColumn<utilisateur, Number> idCol;
    @FXML private TableColumn<utilisateur, String> nomCol;
    @FXML private TableColumn<utilisateur, String> prenomCol;
    @FXML private TableColumn<utilisateur, String> emailCol;

    private final utilisateur_service service = new utilisateur_service();
    private final ObservableList<utilisateur> users = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getId()));
        nomCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getNom()));
        prenomCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getPrenom()));
        emailCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getEmail()));

        usersTable.setItems(users);
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) fillForm(newV);
        });
        refreshTable();
    }

    private void refreshTable() {
        users.setAll(service.afficherutilisateurs());
    }

    private void fillForm(utilisateur u) {
        nomField.setText(u.getNom());
        prenomField.setText(u.getPrenom());
        emailField.setText(u.getEmail());
        passwordField.clear();
        telephoneField.setText(u.getTelephone());
    }

    private String trimmedOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @FXML
    private void onSearch() {
        String email = trimmedOrNull(searchEmailField.getText());
        String idText = trimmedOrNull(searchIdField.getText());

        if (email == null && idText == null) {
            refreshTable();
            return;
        }

        if (email != null) {
            Optional<utilisateur> found = service.rechercherutilisateurParEmail(email);
            if (found.isPresent()) {
                users.setAll(Collections.singletonList(found.get())); // ← corrigé
                usersTable.getSelectionModel().select(found.get());
            } else {
                users.clear();
                showInfo("Aucun résultat", "Aucun utilisateur trouvé.");
            }
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            utilisateur found = null;
            for (utilisateur u : service.afficherutilisateurs()) {
                if (u.getId() == id) { found = u; break; }
            }
            if (found != null) {
                users.setAll(Collections.singletonList(found)); // ← corrigé
                usersTable.getSelectionModel().select(found);
            } else {
                users.clear();
                showInfo("Aucun résultat", "Aucun utilisateur trouvé.");
            }
        } catch (NumberFormatException e) {
            showError("Validation", "ID invalide.");
        }
    }

    @FXML
    private void onUpdate() {
        utilisateur selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Sélectionne un utilisateur.");
            return;
        }

        String password = trimmedOrNull(passwordField.getText());
        if (password == null) {
            showError("Validation", "Le mot de passe est requis.");
            return;
        }

        try {
            utilisateur u = new utilisateur(
                    nomField.getText(),
                    prenomField.getText(),
                    emailField.getText(),
                    password,
                    telephoneField.getText()
            );
            u.setId(selected.getId());

            String erreur = service.validerDonneesutilisateurAvecMessage(u);
            if (erreur != null) {
                showError("Erreur de validation", erreur);
                return;
            }

            boolean ok = service.modifierutilisateur(u);
            if (ok) {
                showInfo("Succès", "Utilisateur modifié avec succès.");
                refreshTable();
            } else {
                showError("Modification impossible", "Échec lors de la modification.");
            }
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        loadInMainLayout("/app/home.fxml");
    }

    @FXML
    private void onCancel() {
        loadInMainLayout("/app/home.fxml");
    }

    private void loadInMainLayout(String fxmlPath) {
        try {
            AdminLayoutController admin = AdminLayoutController.getInstance();
            if (admin != null) {
                admin.loadPage(fxmlPath, "Utilisateurs");
                return;
            }
            ClientLayoutController client = ClientLayoutController.getInstance();
            if (client != null) {
                client.loadPage(fxmlPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
}