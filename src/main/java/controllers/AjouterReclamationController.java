package controllers;

import entities.Reclamation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import services.ReclamationService;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterReclamationController {

    @FXML
    private TextField txtTitre;
    @FXML
    private TextArea txtDescription;

    private ReclamationService reclamationService = new ReclamationService();
    private int currentUserId = 1;

    @FXML
    void enregistrer() {
        String titre = txtTitre.getText();
        String desc = txtDescription.getText();

        if (titre.isEmpty() || desc.isEmpty()) {
            showAlert("Warning", "Please fill all fields");
            return;
        }

        try {
            Reclamation r = new Reclamation(currentUserId, titre, desc);
            reclamationService.addEntity(r);
            showAlert("Success", "Complaint added!");
            annuler();
        } catch (SQLException e) {
            showAlert("Error", "Could not add complaint: " + e.getMessage());
        }
    }

    @FXML
    void annuler() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user_reclamations.fxml"));
            txtTitre.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Error", "Could not return to list: " + e.getMessage());
        }
    }

    @FXML
    void retourMenu() {
        annuler();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
