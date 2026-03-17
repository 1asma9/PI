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

    @FXML private ComboBox<Integer> comboNote;
    @FXML private TextArea txtCommentaire;

    private AvisService avisService = new AvisService();

    // ✅ CORRIGÉ : utilise la session au lieu de 1 codé en dur
    private int currentUserId = tools.SessionManager.getCurrentUserId();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboNote.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
    }

    @FXML
    void enregistrer() {
        Integer note = comboNote.getValue();
        String comment = txtCommentaire.getText();

        if (note == null || comment == null || comment.isEmpty()) {
            showAlert("Attention", "Veuillez remplir tous les champs");
            return;
        }

        try {
            Avis a = new Avis(currentUserId, note, comment);
            avisService.addEntity(a);
            showAlert("Succès", "Avis ajouté avec succès !");
            retourListe();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible d'ajouter : " + e.getMessage());
        }
    }

    @FXML
    void annuler() {
        retourListe();
    }

    @FXML
    void retourMenu() {
        // ✅ CORRIGÉ : retour via ClientLayoutController
        hebergement.controllers.ClientLayoutController layout =
                hebergement.controllers.ClientLayoutController.getInstance();
        if (layout != null) {
            layout.goDestination();
        } else {
            retourListe();
        }
    }

    private void retourListe() {
        // ✅ CORRIGÉ : retour direct via setRoot
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user_avis.fxml"));
            txtCommentaire.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de retourner : " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}