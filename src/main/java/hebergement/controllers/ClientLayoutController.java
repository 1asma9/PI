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
            if (url == null) {
                System.out.println("❌ INTROUVABLE: " + fxmlPath);
                return;
            }
            Parent page = FXMLLoader.load(url);
            contentPane.getChildren().setAll(page);
        } catch (Exception e) {
            System.out.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goDestination() {
        loadPage("/ClientDestinationListView.fxml");
    }

    @FXML
    public void goReservation() {
        loadPage("/app/reservation.fxml");
    }

    @FXML
    public void goChat() {
        loadPage("/app/chat.fxml");
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
    }
    @FXML
    public void goFrontendBlog() {
        loadPage("/fxml/FrontendBlogList.fxml");
    }

    @FXML
    public void goRatingStars() {
        loadPage("/fxml/RatingStars.fxml");
    }
    @FXML
    public void goMesReclamations() {
        loadPage("/mes_reclamations.fxml");
    }
}