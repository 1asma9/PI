package edu.destination.controllers;

import edu.destination.entities.Destination;
import edu.destination.entities.DestinationImage;
import edu.destination.services.DestinationService;
import edu.destination.services.ImageService;
import edu.destination.tools.SceneUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class AdminImageFormController {

    @FXML private TextField txtUrlImage;
    @FXML private ComboBox<Destination> comboDestination;
    @FXML private Button btnSave, btnCancel;
    @FXML private Label errUrl, errDestination;

    private final ImageService service = new ImageService();
    private final DestinationService destinationService = new DestinationService();
    private DestinationImage image;

    public void setImage(DestinationImage image) {
        this.image = image;

        if (image != null) {
            txtUrlImage.setText(image.getUrlImage());

            // sélectionner la destination
            for (Destination d : comboDestination.getItems()) {
                if (d.getIdDestination() == image.getIdDestination()) {
                    comboDestination.getSelectionModel().select(d);
                    break;
                }
            }
        }
    }

    @FXML
    private void initialize() {
        // remplir combo destinations
        List<Destination> destinations = destinationService.getData();
        comboDestination.getItems().setAll(destinations);

        btnSave.setOnAction(e -> save());
        btnCancel.setOnAction(e -> closeWindow());
    }

    private void closeWindow() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }

    @FXML
    private void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");

        // ✅ CORRECT - avec "*.extension"
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        // ✅ Dossier initial = Images de l'utilisateur
        File initialDir = new File(System.getProperty("user.home") + "/Pictures");
        if (!initialDir.exists()) initialDir = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(initialDir);

        File selectedFile = fileChooser.showOpenDialog(txtUrlImage.getScene().getWindow());
        if (selectedFile != null) {
            txtUrlImage.setText(selectedFile.getAbsolutePath());
        }
    }

    private void save() {
        clearErrors();
        if (!validateFields()) return;

        try {
            if (image == null) image = new DestinationImage();

            image.setUrlImage(txtUrlImage.getText().trim());

            Destination selectedDest = comboDestination.getSelectionModel().getSelectedItem();
            image.setIdDestination(selectedDest.getIdDestination());

            if (image.getIdImage() == 0) service.addEntity(image);
            else service.update(image.getIdImage(), image);

            closeWindow();

        } catch (SQLException ex) {
            showAlert("Erreur base de données", ex.getMessage());
        } catch (Exception ex) {
            showAlert("Erreur", ex.getMessage());
        }
    }

    private boolean validateFields() {
        boolean valid = true;

        String url = txtUrlImage.getText().trim();
        if (url.isEmpty()) {
            setError(txtUrlImage, errUrl, "Veuillez choisir une image");
            valid = false;
        } else {
            File f = new File(url);
            if (!f.exists() || !f.isFile()) {
                setError(txtUrlImage, errUrl, "Fichier invalide");
                valid = false;
            }
        }

        Destination selectedDest = comboDestination.getSelectionModel().getSelectedItem();
        if (selectedDest == null) {
            comboDestination.getStyleClass().add("input-error");
            errDestination.setText("Veuillez choisir une destination");
            valid = false;
        }

        return valid;
    }

    private void setError(Control field, Label label, String message) {
        if (field != null && !field.getStyleClass().contains("input-error")) {
            field.getStyleClass().add("input-error");
        }
        if (label != null) label.setText(message);
    }

    private void clearErrors() {
        txtUrlImage.getStyleClass().remove("input-error");
        comboDestination.getStyleClass().remove("input-error");

        errUrl.setText("");
        errDestination.setText("");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}