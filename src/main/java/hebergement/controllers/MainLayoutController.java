package hebergement.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;

public class MainLayoutController {

    @FXML private StackPane contentPane;
    @FXML private Label pageTitle;

    // ✅ Utilisateur connecté
    private static utilisateur currentUser;

    public static void setCurrentUser(utilisateur user) {
        currentUser = user;
    }

    public static utilisateur getCurrentUser() {
        return currentUser;
    }

    @FXML
    public void initialize() {
        goDashboard();
    }

    @FXML
    void goDashboard() {
        loadPage("/app/dashboard.fxml", "Dashboard");
    }

    @FXML
    void goAdd() {
        loadPage("/app/add.fxml", "Ajouter");
    }

    @FXML
    void goList() {
        loadPage("/app/list.fxml", "Gérer");
    }

    @FXML
    void goReservation() {
        loadPage("/app/reservation_gallery.fxml", "Réserver");
    }

    @FXML
    void goReservationsAdmin() {
        loadPage("/app/reservations.fxml", "Réservations");
    }

    private void loadPage(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Node view = loader.load();
            contentPane.getChildren().setAll(view);
            pageTitle.setText(title);
        } catch (Exception e) {
            e.printStackTrace();
            contentPane.getChildren().setAll(new Label("Erreur chargement: " + fxml));
        }
    }
    @FXML
    void goChat() {
        loadPage("/app/chat.fxml", "Chat");
    }
    @FXML
    void goUtilisateurs() {
        loadPage("/app/home.fxml", "Utilisateurs");
    }
}