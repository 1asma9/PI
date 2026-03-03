package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import tools.AlertHelper;
import tools.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class UserLayoutController implements Initializable {

    @FXML
    private StackPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Charger les réclamations par défaut (le dashboard a été supprimé)
        afficherMesReclamations();
    }

    @FXML
    void afficherMesReclamations() {
        chargerPage("/reclamations/mes_reclamations.fxml");
    }

    @FXML
    void afficherMesAvis() {
        chargerPage("/avis/mes_avis.fxml");
    }

    @FXML
    void afficherAvisPublics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/avis/avis_publics.fxml"));
            Parent newContent = loader.load();

            UserAvisController controller = loader.getController();
            controller.setPublicView(true);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);

        } catch (IOException e) {
            System.err.println("Impossible de charger les avis publics : " + e.getMessage());
        }
    }

    @FXML
    void switchRole() {
        SessionManager.login(SessionManager.getCurrentUserId(), SessionManager.getUsername(), true);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_layout.fxml"));
            contentArea.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void deconnexion() {
        if (AlertHelper.showConfirmation("Déconnexion", "Êtes-vous sûr de vouloir vous déconnecter ?")) {
            SessionManager.logout();
            System.exit(0);
        }
    }

    private void chargerPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newContent = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);
        } catch (IOException e) {
            Label errorLabel = new Label("Erreur de chargement : " + fxmlPath);
            errorLabel.setStyle("-fx-text-fill: red;");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(errorLabel);
            e.printStackTrace();
        }
    }
}
