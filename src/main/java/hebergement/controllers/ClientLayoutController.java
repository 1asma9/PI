package hebergement.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;

public class ClientLayoutController {

    @FXML private StackPane contentPane;
    @FXML private Label connectedLabel;

    private static ClientLayoutController instance;

    public static ClientLayoutController getInstance() { return instance; }

    @FXML
    public void initialize() {
        instance = this;
        utilisateur user = MainLayoutController.getCurrentUser();
        if (user != null && connectedLabel != null) {
            connectedLabel.setText(user.getNom() != null ? user.getNom() : "Client");
        }
        goDestination();
    }

    public void loadPage(String fxmlPath) {
        try {
            var url = getClass().getResource(fxmlPath);
            System.out.println("🔎 loadPage url=" + url + " path=" + fxmlPath);

            if (url == null) {
                System.out.println("❌ INTROUVABLE: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent page = loader.load();
            contentPane.getChildren().setAll(page);

        } catch (Exception e) {
            System.out.println("❌ ERREUR CHARGEMENT: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public void loadPageWithCss(String fxmlPath, String cssPath) {
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
        } catch (Exception e) {
            System.out.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadPageWithRoot(Parent root) {
        contentPane.getChildren().setAll(root);
    }

    @FXML
    public void goDestination() {
        loadPage("/ClientDestinationListView.fxml");
    }

    @FXML
    public void goChat() {
        loadPage("/app/chat.fxml");
    }

    @FXML
    public void goFrontendBlog() {
        loadPage("/fxml/FrontendBlogList.fxml");
    }

    @FXML
    public void goRatingStars() {
        // ✅ CORRIGÉ : était "/fxml/RatingStars.fxml"
        loadPage("/user_avis.fxml");
    }

    @FXML
    public void goMesReclamations() {
        // ✅ CORRIGÉ : était "/mes_reclamations.fxml"
        loadPage("/user_reclamations.fxml");
    }

    @FXML
    public void goActiviteFront() {
        loadPageWithCss("/affichage_activites_front.fxml", "/affichage.css");
    }

    @FXML
    public void goAjoutActivite() {
        loadPageWithCss("/ajout_activite.fxml", "/form_activite.css");
    }

    @FXML
    public void goModifierActivite() {
        loadPageWithCss("/modifier_activite.fxml", "/affichage.css");
    }

    @FXML
    public void goReservation() {
        // ✅ chemin corrigé
        loadPage("/app/hebergement_gallery.fxml");
    }
    @FXML
    public void goMonEspace() {
        System.out.println("✅ CLICK Mon Espace");
        loadPage("/app/MonEspace.fxml");  // ✅ nom EXACT comme dans resources
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
}