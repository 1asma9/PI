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

public class AdminLayoutController implements Initializable {

    @FXML
    private StackPane contentArea;

    private static AdminLayoutController instance;

    public static AdminLayoutController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        // Charger le dashboard par défaut
        afficherDashboard();
    }

    @FXML
    void afficherDashboard() {
        chargerPage("/voyage/dashboard.fxml");
    }

    @FXML
    void afficherAjouter() {
        chargerPage("/voyage/admin/ajouter.fxml");
    }

    @FXML
    void afficherGererHebergements() {
        chargerPage("/voyage/admin/gerer_hebergements.fxml");
    }

    @FXML
    void afficherReservationsAdmin() {
        chargerPage("/voyage/admin/reservations.fxml");
    }

    @FXML
    void afficherUtilisateurs() {
        chargerPage("/voyage/admin/utilisateurs.fxml");
    }

    @FXML
    void afficherAdminReclamations() {
        chargerPage("/reclamations/admin_reclamations.fxml");
    }

    @FXML
    void afficherAdminAvis() {
        chargerPage("/avis/admin_avis.fxml");
    }

    @FXML
    void afficherStatistiques() {
        chargerPage("/statistiques/dashboard_stats.fxml");
    }

    @FXML
    void switchRole() {
        SessionManager.login(SessionManager.getCurrentUserId(), SessionManager.getUsername(), false);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user_layout.fxml"));
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

    public void chargerPageExterne(String fxmlPath) {
        chargerPage(fxmlPath);
    }

    public void chargerPageWithData(String fxmlPath, java.util.function.Consumer<Object> controllerConsumer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newContent = loader.load();

            if (controllerConsumer != null) {
                controllerConsumer.accept(loader.getController());
            }

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

    private void chargerPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newContent = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);
        } catch (IOException e) {
            Label errorLabel = new Label("Page non trouvée ou erreur : " + fxmlPath);
            errorLabel.setStyle("-fx-text-fill: red;");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(errorLabel);
            System.err.println("Erreur chargement " + fxmlPath + " : " + e.getMessage());
        }
    }
}
