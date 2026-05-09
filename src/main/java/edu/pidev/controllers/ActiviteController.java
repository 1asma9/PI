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

    @FXML private TextField tfImageUrl;
    @FXML private Label lblMessage;

    @FXML private ImageView imgPreview;

    private final ActiviteService service = new ActiviteService();

    @FXML
    public void initialize() {
        System.out.println("[DEBUG_LOG] ActiviteController (AJOUT) chargé");

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

        try {
            if (u.startsWith("http://") || u.startsWith("https://")) {
                imgPreview.setImage(new Image(u, true));
                return;
            }

            // Windows local path
            if (u.matches("^[A-Za-z]:\\\\.*")) {
                String uri = new java.io.File(u).toURI().toString();
                imgPreview.setImage(new Image(uri, true));
                return;
            }

            // Resource path
            var res = u.startsWith("/") ? getClass().getResource(u) : getClass().getResource("/" + u);
            imgPreview.setImage(res != null ? new Image(res.toExternalForm(), true) : null);

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

            String imageUrl = (tfImageUrl == null) ? null : tfImageUrl.getText();
            if (imageUrl != null) imageUrl = imageUrl.trim();
            a.setImageUrl((imageUrl == null || imageUrl.isBlank()) ? null : imageUrl);

            service.addActivite(a);

            new Alert(Alert.AlertType.INFORMATION,
                    "L’activité a été ajoutée avec succès !").showAndWait();

            // ✅ back office + back css
            switchTo(event, "/affichage_activites_back.fxml", "/back_admin.css", "Back Office");

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
        // ✅ FIX: back css (NOT affichage.css)
        switchTo(event, "/affichage_activites_back.fxml", "/back_admin.css", "Back Office");
    }

    private void switchTo(ActionEvent event, String fxmlPath, String cssPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            boolean wasMax = stage.isMaximized();

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            // ✅ IMPORTANT: apply css after root swap
            scene.getStylesheets().clear();
            var css = getClass().getResource(cssPath);
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.out.println("❌ CSS introuvable: " + cssPath);
            }

            stage.setTitle(title);
            stage.show();

            // ✅ FORCE layout refresh (NO centerOnScreen)
            Platform.runLater(() -> {
                root.applyCss();
                root.layout();

                if (wasMax) {
                    stage.setMaximized(false);
                    stage.setMaximized(true);
                }
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