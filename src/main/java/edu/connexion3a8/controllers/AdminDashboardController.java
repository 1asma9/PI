package edu.connexion3a8.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Pane content_area;
    @FXML private HBox dashboardBtn;
    @FXML private HBox blogBtn;
    @FXML private HBox navBarLogout;
    @FXML private Text navFullname;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Charger le dashboard par défaut
        try {
            loadDashboard();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Configurer le nom de l'utilisateur
        navFullname.setText("Admin");
    }

    @FXML
    public void open_Dashboard(MouseEvent event) {
        try {
            loadDashboard();
            setActiveButton(dashboardBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void open_BlogList(MouseEvent event) {
        try {
            loadBlogList();
            setActiveButton(blogBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout(MouseEvent event) {
        System.exit(0);
    }

    private void loadDashboard() throws IOException {
        // Créer un pane simple pour le dashboard
        Pane dashboardPane = new Pane();
        Text welcomeText = new Text(50, 100, "Bienvenue sur le Dashboard !");
        welcomeText.setStyle("-fx-font-size: 24px; -fx-font-family: 'Montserrat Bold';");
        dashboardPane.getChildren().add(welcomeText);

        content_area.getChildren().clear();
        content_area.getChildren().add(dashboardPane);
    }

    private void loadBlogList() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BlogList.fxml"));
        Pane blogListPane = loader.load();

        content_area.getChildren().clear();
        content_area.getChildren().add(blogListPane);
    }

    private void setActiveButton(HBox activeButton) {
        // Retirer la classe active de tous les boutons
        dashboardBtn.getStyleClass().remove("activeLink");
        blogBtn.getStyleClass().remove("activeLink");

        // Ajouter la classe active au bouton cliqué
        if (!activeButton.getStyleClass().contains("activeLink")) {
            activeButton.getStyleClass().add("activeLink");
        }
    }
}
