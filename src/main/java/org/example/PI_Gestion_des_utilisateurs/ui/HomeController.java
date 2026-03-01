package org.example.PI_Gestion_des_utilisateurs.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import org.example.PI_Gestion_des_utilisateurs.services.utilisateur_service;
import org.example.PI_Gestion_des_utilisateurs.ui.navigation.SceneNavigator;

public class HomeController {

    @FXML
    private void onAddUser() {
        SceneNavigator.goTo("/app/add_user.fxml", "Ajouter un utilisateur");
    }

    @FXML
    private void onEditUser() {
        SceneNavigator.goTo("/app/edit_user.fxml", "Modifier un utilisateur");
    }

    @FXML
    private void onDeleteUser() {
        SceneNavigator.goTo("/app/delete_user.fxml", "Supprimer un utilisateur");
    }

    @FXML
    private void onListUsers() {
        showUsersList();
    }

    @FXML
    private void onLogout() {
        SceneNavigator.goTo("/app/login.fxml", "Connexion");
    }

    private void showUsersList() {
        utilisateur_service service = new utilisateur_service();
        ObservableList<utilisateur> users = FXCollections.observableArrayList(service.afficherutilisateurs());

        // Créer une nouvelle fenêtre pour afficher la liste
        Stage stage = new Stage();
        stage.setTitle("Liste des Utilisateurs");
        
        // Créer un TableView
        TableView<utilisateur> tableView = new TableView<>(users);
        
        TableColumn<utilisateur, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()));
        
        TableColumn<utilisateur, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNom()));
        
        TableColumn<utilisateur, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPrenom()));
        
        TableColumn<utilisateur, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));
        
        TableColumn<utilisateur, String> telephoneCol = new TableColumn<>("Téléphone");
        telephoneCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTelephone()));
        
        TableColumn<utilisateur, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoleName()));
        
        // Ajouter les colonnes au TableView (correction du varargs)
        @SuppressWarnings("unchecked")
        TableColumn<utilisateur, ?>[] columns = new TableColumn[]{idCol, nomCol, prenomCol, emailCol, telephoneCol, roleCol};
        tableView.getColumns().addAll(columns);
        
        // Utiliser une approche simple sans dépréciation
        // Les colonnes s'adapteront automatiquement à la taille du TableView
        for (TableColumn<?, ?> col : tableView.getColumns()) {
            col.setPrefWidth(100); // Largeur initiale
        }
        
        // Créer un bouton fermer
        Button closeButton = new Button("Fermer");
        closeButton.setOnAction(e -> stage.close());
        closeButton.setStyle("-fx-background-color: #C49A28; -fx-text-fill: white; -fx-background-radius: 8px; -fx-padding: 8px 16px;");
        
        // Créer le layout principal
        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #E5E0D8; -fx-padding: 20px;");
        layout.getChildren().addAll(tableView, closeButton);
        
        // Créer la scène
        Scene scene = new Scene(layout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/app/styles.css").toExternalForm());
        
        stage.setScene(scene);
        stage.show();
    }
}
