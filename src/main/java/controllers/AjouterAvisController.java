package controllers;

import entities.Avis;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.AvisService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AjouterAvisController implements Initializable {

    @FXML
    private ComboBox<Integer> comboNote;
    @FXML
    private TextArea txtCommentaire;

    private AvisService avisService = new AvisService();
    private int currentUserId = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboNote.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
    }

    @FXML
    void enregistrer() {
        Integer note = comboNote.getValue();
        String comment = txtCommentaire.getText();

        if (note == null || comment.isEmpty()) {
            showAlert("Warning", "Please fill all fields");
            return;
        }

        try {
            Avis a = new Avis(currentUserId, note, comment);
            avisService.addEntity(a);
            showAlert("Success", "Review added!");
            annuler();
        } catch (SQLException e) {
            showAlert("Error", "Could not add: " + e.getMessage());
        }
    }

    @FXML
    void annuler() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user_avis.fxml"));
            txtCommentaire.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Error", "Could not return: " + e.getMessage());
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
