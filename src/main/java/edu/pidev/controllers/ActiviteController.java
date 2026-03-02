package edu.pidev.controllers;

import edu.pidev.entities.Activite;
import edu.pidev.services.ActiviteService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ActiviteController {

    @FXML private TextField tfNom;
    @FXML private TextField tfType;
    @FXML private TextField tfLieu;
    @FXML private TextField tfPrix;
    @FXML private TextField tfDuree;
    @FXML private TextArea taDescription;

    // ✅ NEW (from your FXML)
    @FXML private TextField tfImageUrl;

    @FXML private Label lblMessage;

    // ✅ optional preview (only if you keep an ImageView in FXML)
    @FXML private ImageView imgPreview;

    private final ActiviteService service = new ActiviteService();

    @FXML
    public void initialize() {
        System.out.println("[DEBUG_LOG] ActiviteController (AJOUT) chargé");

        // ✅ live preview when user types/pastes a link
        if (tfImageUrl != null) {
            tfImageUrl.textProperty().addListener((obs, oldV, newV) -> previewImage(newV));
        }
    }

    private void previewImage(String url) {
        if (imgPreview == null) return;
        if (url == null || url.trim().isEmpty()) {
            imgPreview.setImage(null);
            return;
        }

        String u = url.trim();

        // Only preview direct image links or local path
        try {
            if (u.startsWith("http://") || u.startsWith("https://")) {
                imgPreview.setImage(new Image(u, true));
            } else if (u.matches("^[A-Za-z]:\\\\.*")) { // windows path
                String uri = new java.io.File(u).toURI().toString();
                imgPreview.setImage(new Image(uri, true));
            } else if (u.startsWith("/")) { // resource path
                var res = getClass().getResource(u);
                imgPreview.setImage(res != null ? new Image(res.toExternalForm(), true) : null);
            } else { // resource without /
                var res = getClass().getResource("/" + u);
                imgPreview.setImage(res != null ? new Image(res.toExternalForm(), true) : null);
            }
        } catch (Exception e) {
            imgPreview.setImage(null);
        }
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

            double prix = Double.parseDouble(tfPrix.getText().trim().replace(",", "."));
            int duree = Integer.parseInt(tfDuree.getText().trim());

            if (prix < 0) throw new IllegalArgumentException("Le prix doit être positif.");
            if (duree <= 0) throw new IllegalArgumentException("La durée doit être > 0.");

            Activite a = new Activite(nom, description, type, prix, duree, lieu);

            // ✅ store URL from user (NO API)
            String imageUrl = (tfImageUrl == null) ? null : tfImageUrl.getText();
            if (imageUrl != null) imageUrl = imageUrl.trim();
            a.setImageUrl((imageUrl == null || imageUrl.isBlank()) ? null : imageUrl);

            service.addActivite(a);

            new Alert(Alert.AlertType.INFORMATION,
                    "L’activité a été ajoutée avec succès !").showAndWait();

            switchTo(event, "/affichage_activites_back.fxml", "/affichage.css", "Affichage Activités");

        } catch (NumberFormatException e) {
            showError("Erreur de saisie", "Prix ou durée invalide",
                    "Veuillez entrer des valeurs numériques valides.");
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de l’ajout", e.getMessage());
        }
    }

    @FXML
    void viderChamps(ActionEvent event) {
        tfNom.clear();
        tfType.clear();
        tfLieu.clear();
        tfPrix.clear();
        tfDuree.clear();
        taDescription.clear();
        if (tfImageUrl != null) tfImageUrl.clear();
        if (imgPreview != null) imgPreview.setImage(null);
        if (lblMessage != null) lblMessage.setText("");
    }

    @FXML
    private void retourListe(ActionEvent event) {
        switchTo(event, "/affichage_activites_back.fxml", "/affichage.css", "Affichage Activités");
    }

    private void switchTo(ActionEvent event, String fxmlPath, String cssPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            scene.getStylesheets().clear();
            var css = getClass().getResource(cssPath);
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setTitle(title);
            stage.show();

            Platform.runLater(() -> {
                stage.setMaximized(true);
                stage.centerOnScreen();
            });

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur navigation : " + e.getMessage()).showAndWait();
        }
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}