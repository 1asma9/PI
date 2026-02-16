package edu.pidev.controllers;

import edu.pidev.entities.Activite;
import edu.pidev.services.ActiviteService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ActiviteController {

    @FXML private TextField tfNom;
    @FXML private TextField tfType;
    @FXML private TextField tfLieu;
    @FXML private TextField tfPrix;
    @FXML private TextField tfDuree;
    @FXML private TextArea taDescription;
    @FXML private Label lblMessage;

    private final ActiviteService service = new ActiviteService();

    @FXML
    public void initialize() {
        System.out.println("[DEBUG_LOG] ActiviteController (AJOUT) chargé");
        // ✅ Nothing else here (keep it clean)
    }

    @FXML
    void ajouterActivite(ActionEvent event) {
        try {
            String nom = tfNom.getText();
            String type = tfType.getText();
            String lieu = tfLieu.getText();
            String description = taDescription.getText();

            if (isBlank(nom) || isBlank(type) || isBlank(lieu)
                    || isBlank(tfPrix.getText()) || isBlank(tfDuree.getText())) {
                throw new IllegalArgumentException("Veuillez remplir tous les champs obligatoires.");
            }

            double prix = Double.parseDouble(tfPrix.getText().trim());
            int duree = Integer.parseInt(tfDuree.getText().trim());

            Activite a = new Activite(nom, description, type, prix, duree, lieu);
            service.addActivite(a);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Activité ajoutée");
            alert.setContentText("L’activité a été ajoutée avec succès !");
            alert.showAndWait();

            goToAffichage();

        } catch (NumberFormatException e) {
            showError("Erreur de saisie", "Prix ou durée invalide",
                    "Veuillez entrer des valeurs numériques valides.");
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de l’ajout", e.getMessage());
        }
    }

    @FXML
    void viderChamps() {
        tfNom.clear();
        tfType.clear();
        tfLieu.clear();
        tfPrix.clear();
        tfDuree.clear();
        taDescription.clear();
        if (lblMessage != null) lblMessage.setText("");
    }

    @FXML
    private void retourListe() {
        goToAffichage();
    }

    private void goToAffichage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/affichage_activites.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // CSS safe
            var css = getClass().getResource("/affichage.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            else System.out.println("❌ CSS not found: /affichage.css");

            Stage stage = (Stage) tfNom.getScene().getWindow();

            // ✅ IMPORTANT: reset maximize before switching scene (fix "stuck window")
            boolean wasMax = stage.isMaximized();
            stage.setMaximized(false);

            stage.setScene(scene);
            stage.setTitle("Affichage Activités");

            // ✅ force layout recalculation
            root.applyCss();
            root.layout();

            // ✅ restore maximize after layout
            stage.setMaximized(wasMax || true); // always maximize if you want
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir l'affichage", e.getMessage());
        }
    }


    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
