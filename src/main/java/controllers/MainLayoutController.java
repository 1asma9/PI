package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tools.AlertHelper;
import tools.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {

    @FXML
    private StackPane contentArea;
    @FXML
    private VBox adminVoyageSection;
    @FXML
    private VBox adminServicesSection;
    @FXML
    private Label lblUsername;

    private boolean isAdmin = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Récupérer les infos de session
        isAdmin = SessionManager.isAdmin();

        // Afficher/masquer les sections admin
        adminVoyageSection.setVisible(isAdmin);
        adminVoyageSection.setManaged(isAdmin);
        adminServicesSection.setVisible(isAdmin);
        adminServicesSection.setManaged(isAdmin);

        // Mettre à jour le nom d'utilisateur
        String username = SessionManager.getUsername();
        String role = isAdmin ? "ADMIN" : "USER";
        lblUsername.setText(username + " (" + role + ")");

        // Charger le dashboard par défaut
        afficherDashboard();
    }

    // ===== VOYAGE =====

    @FXML
    void afficherDashboard() {
        chargerPage("/voyage/dashboard.fxml");
    }

    @FXML
    void afficherReserver() {
        chargerPage("/voyage/reserver.fxml");
    }

    @FXML
    void afficherChat() {
        chargerPage("/voyage/chat.fxml");
    }

    // ===== MES SERVICES (USER) =====

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

    // ===== ADMIN VOYAGE =====

    @FXML
    void afficherAjouter() {
        if (!isAdmin)
            return;
        chargerPage("/voyage/admin/ajouter.fxml");
    }

    @FXML
    void afficherGererHebergements() {
        if (!isAdmin)
            return;
        chargerPage("/voyage/admin/gerer_hebergements.fxml");
    }

    @FXML
    void afficherReservationsAdmin() {
        if (!isAdmin)
            return;
        chargerPage("/voyage/admin/reservations.fxml");
    }

    @FXML
    void afficherUtilisateurs() {
        if (!isAdmin)
            return;
        chargerPage("/voyage/admin/utilisateurs.fxml");
    }

    // ===== ADMIN SERVICES =====

    @FXML
    void afficherAdminReclamations() {
        if (!isAdmin)
            return;
        chargerPage("/reclamations/admin_reclamations.fxml");
    }

    @FXML
    void afficherAdminAvis() {
        if (!isAdmin)
            return;
        chargerPage("/avis/admin_avis.fxml");
    }

    @FXML
    void afficherStatistiques() {
        if (!isAdmin)
            return;
        chargerPage("/statistiques/dashboard_stats.fxml");
    }

    // ===== AUTRES =====

    @FXML
    void afficherParametres() {
        chargerPage("/parametres.fxml");
    }

    @FXML
    void deconnexion() {
        if (AlertHelper.showConfirmation("Déconnexion",
                "Êtes-vous sûr de vouloir vous déconnecter ?")) {
            SessionManager.logout();
            System.exit(0);
        }
    }

    // ===== MÉTHODE UTILITAIRE =====

    private void chargerPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newContent = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);

        } catch (IOException e) {
            // Placeholder label if page not found
            Label errorLabel = new Label("Page non trouvée : " + fxmlPath);
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(errorLabel);

            System.err.println("Impossible de charger la page : " + fxmlPath + "\n" + e.getMessage());
        }
    }
}
