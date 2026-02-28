package edu.destination.controllers;

import edu.destination.tools.SceneUtil;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class AdminDashboardController {

    @FXML private Button btnDestination;
    @FXML private Button btnTransport;
    @FXML private Button btnImage;
    @FXML private Button btnClientHome;

    @FXML
    public void initialize() {
        btnDestination.setOnAction(e -> openView("/AdminDestinationView.fxml"));
        btnTransport.setOnAction(e -> openView("/AdminTransportView.fxml"));
        btnImage.setOnAction(e -> openView("/AdminImageView.fxml"));
        btnClientHome.setOnAction(e -> openView("/ClientDestinationListView.fxml"));
    }

    private void openView(String fxml) {
        Parent root = SceneUtil.load(fxml);
        Stage stage = (Stage) btnDestination.getScene().getWindow();
        SceneUtil.setScene(stage, root, 1200, 800);
    }
} // Test commit pour push