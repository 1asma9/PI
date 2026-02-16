package edu.pidev.controllers;

import edu.pidev.entities.Activite;
import edu.pidev.services.ActiviteService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ModifierActiviteController {

    @FXML private TextField tfId; // caché
    @FXML private TextField tfNom;
    @FXML private TextField tfType;
    @FXML private TextField tfLieu;
    @FXML private TextField tfPrix;
    @FXML private TextField tfDuree;
    @FXML private TextArea taDescription;
    @FXML private Label lblMessage;

    private final ActiviteService service = new ActiviteService();
    private Activite activiteSelectionnee;

    public void setActivite(Activite a) {
        this.activiteSelectionnee = a;

        tfId.setText(String.valueOf(a.getIdActivite()));
        tfNom.setText(a.getNom());
        tfType.setText(a.getType());
        tfLieu.setText(a.getLieu());
        tfPrix.setText(String.valueOf(a.getPrix()));
        tfDuree.setText(String.valueOf(a.getDuree()));
        taDescription.setText(a.getDescription() == null ? "" : a.getDescription());
    }

    @FXML
    private void mettreAJour(ActionEvent event) {
        try {
            if (tfNom.getText().isEmpty() || tfType.getText().isEmpty() || tfLieu.getText().isEmpty()
                    || tfPrix.getText().isEmpty() || tfDuree.getText().isEmpty()) {
                lblMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                lblMessage.setText("❌ Veuillez remplir tous les champs obligatoires.");
                return;
            }

            double prix = Double.parseDouble(tfPrix.getText().trim());
            int duree = Integer.parseInt(tfDuree.getText().trim());

            activiteSelectionnee.setNom(tfNom.getText().trim());
            activiteSelectionnee.setType(tfType.getText().trim());
            activiteSelectionnee.setLieu(tfLieu.getText().trim());
            activiteSelectionnee.setPrix(prix);
            activiteSelectionnee.setDuree(duree);
            activiteSelectionnee.setDescription(taDescription.getText().trim());

            service.updateActivite(activiteSelectionnee);

            lblMessage.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            lblMessage.setText("✅ Activité mise à jour avec succès !");

            // ✅ Optionnel: retourner automatiquement à l'affichage après mise à jour
            // retour(null);

        } catch (NumberFormatException e) {
            lblMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            lblMessage.setText("❌ Prix/Durée doivent être numériques.");
        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            lblMessage.setText("❌ Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void retour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/affichage_activites.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // ✅ CSS safe (ONLY if affichage_activites.fxml does NOT already include stylesheets="@affichage.css")
            var css = getClass().getResource("/affichage.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.out.println("❌ CSS not found: /affichage.css");
            }

            Stage stage = (Stage) tfNom.getScene().getWindow();

            // ✅ FIX: prevents stuck window after switching scenes
            boolean wasMax = stage.isMaximized();
            stage.setMaximized(false);

            stage.setScene(scene);
            stage.setTitle("Affichage Activités");

            // ✅ force layout recalculation
            root.applyCss();
            root.layout();

            stage.setMaximized(wasMax || true); // keep maximized
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Retour impossible", e.getMessage());
        }
    }

    private void showError(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
