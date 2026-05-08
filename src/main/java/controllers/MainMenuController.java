package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

import java.io.IOException;

public class MainMenuController {

    @FXML
    private Button btnUser;

    @FXML
    private Button btnAdmin;

    @FXML
    void goToUserSpace(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user_menu.fxml"));
            btnUser.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Error loading user space: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void goToAdminSpace(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_menu.fxml"));
            btnAdmin.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Error loading admin space: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void exitApp(ActionEvent event) {
        javafx.application.Platform.exit();
    }
}
