package hebergement.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;

public class AdminLayoutController {

    @FXML private StackPane contentPane;
    @FXML private Label pageTitle;
    @FXML private Label connectedLabel;

    private static AdminLayoutController instance;

    public static AdminLayoutController getInstance() { return instance; }

    @FXML
    public void initialize() {
        instance = this;
        utilisateur user = MainLayoutController.getCurrentUser();
        if (user != null && connectedLabel != null) {
            connectedLabel.setText(user.getNom() != null ? user.getNom() : "Admin");
        }
        goDashboard();
    }

    public void loadPage(String fxmlPath, String title) {
        try {
            var url = getClass().getResource(fxmlPath);
            if (url == null) {
                System.out.println("❌ INTROUVABLE: " + fxmlPath);
                return;
            }
            Parent page = FXMLLoader.load(url);
            contentPane.getChildren().setAll(page);
            pageTitle.setText(title);
        } catch (Exception e) {
            System.out.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadPageWithCss(String fxmlPath, String cssPath, String title) {
        try {
            var url = getClass().getResource(fxmlPath);
            if (url == null) {
                System.out.println("❌ INTROUVABLE: " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent page = loader.load();

            if (cssPath != null) {
                var css = getClass().getResource(cssPath);
                if (css != null) page.getStylesheets().add(css.toExternalForm());
            }

            contentPane.getChildren().setAll(page);
            pageTitle.setText(title);
        } catch (Exception e) {
            System.out.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goDashboard() {
        loadPage("/app/dashboard.fxml", "Dashboard");
    }

    @FXML
    public void goReservationsAdmin() {
        loadPage("/app/reservations.fxml", "Réservations");
    }

    @FXML
    public void goList() {
        loadPage("/app/list.fxml", "Hébergements");
    }

    @FXML
    public void goAdd() {
        loadPage("/app/add.fxml", "Ajouter Hébergement");
    }

    @FXML
    public void goUtilisateurs() {
        loadPage("/app/home.fxml", "Utilisateurs");
    }

    @FXML
    public void goDestination() {
        loadPage("/AdminDestinationView.fxml", "Destinations");
    }

    @FXML
    public void goChat() {
        loadPage("/app/chat.fxml", "Chat");
    }

    @FXML
    public void goBlogList() {
        loadPage("/fxml/BlogList.fxml", "Gestion Blog");
    }

    @FXML
    public void goBlogDashboard() {
        loadPage("/fxml/Dashboard.fxml", "Dashboard Blog");
    }

    @FXML
    public void goAdminReclamations() {
        loadPage("/admin_reclamations.fxml", "Gestion Réclamations");
    }



    @FXML
    public void goActiviteBack() {
        loadPageWithCss("/affichage_activites_back.fxml", "/affichage_activites_back.css", "Gestion Activités");
    }

    @FXML
    public void goLogout() {
        try {
            MainLayoutController.setCurrentUser(null);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/main_layout.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/app/app.css").toExternalForm());
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadPageWithRoot(Parent root) {
        contentPane.getChildren().setAll(root);
        pageTitle.setText("");
    }
}