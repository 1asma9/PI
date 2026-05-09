package hebergement.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;

public class AdminLayoutController {

    @FXML private StackPane contentPane;
    @FXML private Label pageTitle;
    @FXML private Label connectedLabel;
    @FXML private VBox destinationSubMenu;

    private static AdminLayoutController instance;
    private boolean destinationMenuOpen = true;

    public static AdminLayoutController getInstance() { return instance; }

    @FXML
    public void initialize() {
        instance = this;
        utilisateur user = MainLayoutController.getCurrentUser();
        if (user != null && connectedLabel != null) {
            connectedLabel.setText(
                    user.getNom() != null ? user.getNom() : "Admin"
            );
        }
        goDashboard();
    }

    // ✅ loadPage standard
    public void loadPage(String fxmlPath, String title) {
        try {
            var url = getClass().getResource(fxmlPath);
            if (url == null) { System.out.println("❌ INTROUVABLE: " + fxmlPath); return; }

            Parent page = FXMLLoader.load(url);

            var css = getClass().getResource("/app/app.css");
            if (css != null) {
                page.getStylesheets().add(css.toExternalForm());
                // ✅ Ajoute aussi à la scène pour les popups DatePicker
                if (contentPane.getScene() != null) {
                    if (!contentPane.getScene().getStylesheets().contains(css.toExternalForm()))
                        contentPane.getScene().getStylesheets().add(css.toExternalForm());
                }
            }

            contentPane.getChildren().setAll(page);
            if (pageTitle != null) pageTitle.setText(title);

        } catch (Exception e) {
            System.out.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ loadPageWithRoot — utilisé par modifierHebergement() pour passer un controller pré-configuré
    public void loadPageWithRoot(Parent root, String title) {
        var css = getClass().getResource("/app/app.css");
        if (css != null && !root.getStylesheets().contains(css.toExternalForm())) {
            root.getStylesheets().add(css.toExternalForm());
        }
        contentPane.getChildren().setAll(root);
        if (pageTitle != null) pageTitle.setText(title);
    }

    // ✅ loadPageWithRoot sans titre (compatibilité)
    public void loadPageWithRoot(Parent root) {
        loadPageWithRoot(root, "");
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

            var appCss = getClass().getResource("/app/app.css");
            if (appCss != null) page.getStylesheets().add(appCss.toExternalForm());

            if (cssPath != null) {
                var css = getClass().getResource(cssPath);
                if (css != null) page.getStylesheets().add(css.toExternalForm());
            }

            contentPane.getChildren().setAll(page);
            if (pageTitle != null) pageTitle.setText(title);

        } catch (Exception e) {
            System.out.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== NAVIGATION =====

    @FXML public void goDashboard() {
        loadPage("/app/dashboard.fxml", "Dashboard");
    }

    @FXML public void goReservationsAdmin() {
        loadPage("/app/reservations.fxml", "Réservations");
    }

    @FXML public void goAdd() {
        loadPage("/app/add.fxml", "Ajouter Hébergement");
    }

    @FXML public void goGestionHebergement() {
        try {
            var url = getClass().getResource("/app/GestionHebergement.fxml");
            System.out.println("URL = " + url);
            loadPage("/app/GestionHebergement.fxml", "Hébergements");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML public void goList() {
        loadPage("/app/hebergement_gallery.fxml", "Hébergements");
    }

    @FXML public void goToChambres() {
        loadPage("/app/AddChambre.fxml", "Chambres");
    }

    @FXML public void goUtilisateurs() {
        loadPage("/app/home.fxml", "Utilisateurs");
    }

    @FXML public void goDestination() {
        loadPage("/AdminDestinationView.fxml", "Destinations");
    }

    @FXML public void goClientHebergement() {
        loadPage("/hebergement/views/HebergementClientView.fxml", "Hébergements");
    }

    @FXML public void goVoyage() {
        loadPage("/AdminVoyageView.fxml", "Voyages");
    }

    @FXML public void goTransport() {
        loadPage("/AdminTransportView.fxml", "Transports");
    }

    @FXML public void goImage() {
        loadPage("/AdminImageView.fxml", "Images");
    }

    @FXML public void toggleDestinationMenu() {
        if (destinationSubMenu != null) {
            destinationMenuOpen = !destinationMenuOpen;
            destinationSubMenu.setVisible(destinationMenuOpen);
            destinationSubMenu.setManaged(destinationMenuOpen);
        }
    }

    @FXML public void goChat() {
        loadPage("/app/chat.fxml", "Chat");
    }

    @FXML public void goBlogList() {
        loadPage("/fxml/BlogList.fxml", "Gestion Blog");
    }

    @FXML public void goBlogDashboard() {
        loadPage("/fxml/Dashboard.fxml", "Dashboard Blog");
    }

    @FXML public void goAdminReclamations() {
        loadPage("/admin_reclamations.fxml", "Gestion Réclamations");
    }

    @FXML public void goActiviteBack() {
        loadPageWithCss(
                "/affichage_activites_back.fxml",
                "/affichage_activites_back.css",
                "Gestion Activités"
        );
    }

    @FXML public void goLogout() {
        try {
            MainLayoutController.setCurrentUser(null);
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/app/main_layout.fxml")
            );
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(
                    getClass().getResource("/app/app.css").toExternalForm()
            );
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}