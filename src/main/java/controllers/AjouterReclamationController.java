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

    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;

    private ReclamationService reclamationService = new ReclamationService();

    // ✅ CORRIGÉ : utilise la session au lieu de 1 codé en dur
    private int currentUserId = tools.SessionManager.getCurrentUserId();

    @FXML
    void enregistrer() {
        String titre = txtTitre.getText();
        String desc = txtDescription.getText();

        if (titre == null || titre.isEmpty() || desc == null || desc.isEmpty()) {
            showAlert("Attention", "Veuillez remplir tous les champs");
            return;
        }

        try {
            Reclamation r = new Reclamation(currentUserId, titre, desc);
            reclamationService.addEntity(r);
            showAlert("Succès", "Réclamation ajoutée avec succès !");
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
            Parent root = FXMLLoader.load(getClass().getResource("/user_reclamations.fxml"));
            txtTitre.getScene().setRoot(root);
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