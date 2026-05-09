package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tools.AlertHelper;
import tools.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminLayoutController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private VBox destinationSubMenu;
    @FXML private Button btnToggleDestination;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chargerPage("/admin_reclamations.fxml");
    }

    // =============== DROPDOWN ===============
    @FXML
    void toggleDestinationMenu() {
        boolean visible = destinationSubMenu.isVisible();
        destinationSubMenu.setVisible(!visible);
        destinationSubMenu.setManaged(!visible);
        btnToggleDestination.setText(visible ? "🌍 Gestion Destination ▶" : "🌍 Gestion Destination ▼");
    }

    // =============== DESTINATION ===============
    @FXML
    void goDestination() {
        chargerPage("/AdminDestinationView.fxml");
    }

    @FXML
    void goVoyage() {
        chargerPage("/AdminVoyageView.fxml");
    }

    @FXML
    void goTransport() {
        chargerPage("/AdminTransportView.fxml");
    }

    @FXML
    void goImage() {
        chargerPage("/AdminImageView.fxml");
    }

    // =============== NAVIGATION ===============
    @FXML
    void goDashboard() {
        chargerPage("/app/dashboard.fxml");
    }

    @FXML
    void goReservationsAdmin() {
        chargerPage("/app/reservations.fxml");
    }

    @FXML
    void goList() {
        chargerPage("/app/list.fxml");
    }

    @FXML
    void goAdd() {
        chargerPage("/app/add.fxml");
    }

    @FXML
    void goUtilisateurs() {
        chargerPage("/app/add_user.fxml");
    }

    @FXML
    void goChat() {
        chargerPage("/app/chat.fxml");
    }

    @FXML
    void goBlogList() {
        chargerPage("/app/home.fxml");
    }

    @FXML
    void goBlogDashboard() {
        chargerPage("/app/dashboard.fxml");
    }

    @FXML
    void goAdminReclamations() {
        chargerPage("/admin_reclamations.fxml");
    }

    @FXML
    void goActiviteBack() {
        chargerPage("/affichage_activites_back.fxml");
    }

    @FXML
    void goLogout() {
        if (AlertHelper.showConfirmation("Déconnexion", "Êtes-vous sûr de vouloir vous déconnecter ?")) {
            SessionManager.logout();
            System.exit(0);
        }
    }

    // =============== ANCIENS NOMS (compatibilité) ===============
    @FXML
    void afficherDashboard() {
        chargerPage("/app/dashboard.fxml");
    }

    @FXML
    void afficherAjouter() {
        chargerPage("/app/add.fxml");
    }

    @FXML
    void afficherGererHebergements() {
        chargerPage("/app/list.fxml");
    }

    @FXML
    void afficherReservationsAdmin() {
        chargerPage("/app/reservations.fxml");
    }

    @FXML
    void afficherUtilisateurs() {
        chargerPage("/app/add_user.fxml");
    }

    @FXML
    void afficherAdminReclamations() {
        chargerPage("/admin_reclamations.fxml");
    }

    @FXML
    void afficherAdminAvis() {
        chargerPage("/admin_avis.fxml");
    }

    @FXML
    void afficherStatistiques() {
        chargerPage("/app/dashboard.fxml");
    }

    @FXML
    void switchRole() {
        SessionManager.login(SessionManager.getCurrentUserId(), SessionManager.getUsername(), false);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/app/main_layout.fxml"));
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

    // =============== CHARGEMENT ===============
    private void chargerPage(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                resource = getClass().getClassLoader().getResource(
                        fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath
                );
            }
            if (resource == null) {
                Label errorLabel = new Label("❌ Page introuvable: " + fxmlPath);
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                contentArea.getChildren().setAll(errorLabel);
                System.err.println("❌ Introuvable: " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent newContent = loader.load();
            contentArea.getChildren().setAll(newContent);
        } catch (IOException e) {
            Label errorLabel = new Label("Erreur chargement: " + fxmlPath + "\n" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            contentArea.getChildren().setAll(errorLabel);
            System.err.println("Erreur: " + fxmlPath + " → " + e.getMessage());
        }
    }
}