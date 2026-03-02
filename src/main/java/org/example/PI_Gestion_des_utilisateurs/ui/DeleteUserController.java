package org.example.PI_Gestion_des_utilisateurs.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import org.example.PI_Gestion_des_utilisateurs.services.utilisateur_service;
import org.example.PI_Gestion_des_utilisateurs.ui.navigation.SceneNavigator;

import java.util.List;
import java.util.Optional;

public class DeleteUserController {

    @FXML private TextField idField;
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
        refreshTable();

        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) idField.setText(String.valueOf(newV.getId()));
        });
    }

    private void refreshTable() {
        users.setAll(service.afficherutilisateurs());
    }

    @FXML
    private void onDelete() {
        String text = idField.getText();
        if (text == null || text.trim().isEmpty()) {
            showError("Validation", "Saisis un ID ou sélectionne un utilisateur.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            showError("Validation", "ID invalide.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur ?");
        confirm.setContentText("ID: " + id);

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.OK) return;

        boolean ok = service.supprimerutilisateur(id);
        if (ok) {
            showInfo("Succès", "Utilisateur supprimé.");
            refreshTable();
            idField.clear();
        } else {
            showError("Suppression impossible", "Échec lors de la suppression.");
        }
    }

    @FXML
    private void onSearch() {
        String text = idField.getText();
        if (text == null || text.trim().isEmpty()) {
            refreshTable();
            return;
        }
        try {
            int id = Integer.parseInt(text.trim());
            List<utilisateur> allUsers = service.afficherutilisateurs();
            Optional<utilisateur> user = allUsers.stream()
                    .filter(u -> u.getId() == id)
                    .findFirst();
            if (user.isPresent()) {
                users.setAll(user.get());
            } else {
                users.clear();
                showError("Recherche", "Aucun utilisateur trouvé.");
            }
        } catch (NumberFormatException e) {
            showError("Validation", "ID invalide.");
            refreshTable();
        }
    }

    @FXML
    private void onBack() {
        SceneNavigator.goTo("/app/home.fxml", "Accueil");
    }

    @FXML
    private void onCancel() {
        SceneNavigator.goTo("/app/home.fxml", "Accueil");
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