package org.example.PI_Gestion_des_utilisateurs.ui;

import hebergement.controllers.AdminLayoutController;
import hebergement.controllers.ClientLayoutController;
import javafx.fxml.FXML;

public class HomeController {

    @FXML
    private void onAddUser() {
        loadPage("/app/add_user.fxml", "Ajouter Utilisateur");
    }

    @FXML
    private void onEditUser() {
        loadPage("/app/edit_user.fxml", "Modifier Utilisateur");
    }

    @FXML
    private void onDeleteUser() {
        loadPage("/app/delete_user.fxml", "Supprimer Utilisateur");
    }

    @FXML
    private void onListUsers() {
        loadPage("/app/list.fxml", "Liste Utilisateurs");
    }

    @FXML
    private void onLogout() {
        loadPage("/app/login.fxml", "Connexion");
    }

    private void loadPage(String fxmlPath, String title) {
        AdminLayoutController admin = AdminLayoutController.getInstance();
        if (admin != null) {
            admin.loadPage(fxmlPath, title);
            return;
        }
        ClientLayoutController client = ClientLayoutController.getInstance();
        if (client != null) {
            client.loadPage(fxmlPath);
        }
    }
}