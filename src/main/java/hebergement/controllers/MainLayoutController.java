package hebergement.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;


public class MainLayoutController {

    @FXML private StackPane contentPane;
    @FXML private Label pageTitle;

    @FXML
    public void initialize() {
        goDashboard(); // charge dashboard au démarrage
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
        loadPage("/app/reservation.fxml", "Réserver");
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
}
