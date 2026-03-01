package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import java.io.IOException;

public class UserMenuController {

    @FXML
    void retourMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/main_menu.fxml"));
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToReclamations(ActionEvent event) {
        loadInterface("/user_reclamations.fxml", event);
    }

    @FXML
    void goToAvis(ActionEvent event) {
        loadInterface("/user_avis.fxml", event);
    }

    @FXML
    void goToReclamationsClick(MouseEvent event) {
        loadInterfaceClick("/user_reclamations.fxml", event);
    }

    @FXML
    void goToAvisClick(MouseEvent event) {
        loadInterfaceClick("/user_avis.fxml", event);
    }

    private void loadInterface(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadInterfaceClick(String fxmlPath, MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
