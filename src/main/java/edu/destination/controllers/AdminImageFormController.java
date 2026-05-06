package edu.destination.controllers;

import edu.destination.entities.Destination;
import edu.destination.entities.Image;
import edu.destination.services.DestinationService;
import edu.destination.services.ImageService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class AdminImageFormController {

    @FXML private TextField txtUrlImage;
    @FXML private ComboBox<Destination> comboDestination;
    @FXML private Button btnSave, btnCancel;
    @FXML private Label errUrl, errDestination;

    // ✅ Chemin vers le dossier public de Symfony
    private static final String SYMFONY_UPLOAD_DIR =
            "C:/xampp/htdocs/VOYAGE/public/uploads/images/";

    private final ImageService       service            = new ImageService();
    private final DestinationService destinationService = new DestinationService();
    private Image  image;
    private File   selectedFile; // fichier sélectionné avant sauvegarde

    public void setImage(Image image) {
        this.image = image;
        if (image != null) {
            txtUrlImage.setText(image.getUrlImage());
            for (Destination d : comboDestination.getItems()) {
                if (d.getId() == image.getDestinationId()) {
                    comboDestination.getSelectionModel().select(d);
                    break;
                }
            }
        }
    }

    @FXML
    private void initialize() {
        List<Destination> destinations = destinationService.getData();
        comboDestination.getItems().setAll(destinations);
        btnSave.setOnAction(e -> save());
        btnCancel.setOnAction(e -> closeWindow());
    }

    @FXML
    private void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        File initialDir = new File(System.getProperty("user.home") + "/Pictures");
        if (!initialDir.exists()) initialDir = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(initialDir);

        selectedFile = fileChooser.showOpenDialog(txtUrlImage.getScene().getWindow());
        if (selectedFile != null) {
            // Affiche juste le nom du fichier dans le champ
            txtUrlImage.setText(selectedFile.getName());
        }
    }

    private void save() {
        clearErrors();
        if (!validateFields()) return;

        try {
            if (image == null) image = new Image();

            // ✅ Copie l'image dans le dossier Symfony et sauvegarde le chemin relatif
            if (selectedFile != null) {
                String ext      = getExtension(selectedFile.getName());
                String fileName = "img_" + UUID.randomUUID().toString().substring(0, 8) + ext;

                File uploadDir = new File(SYMFONY_UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                File dest = new File(SYMFONY_UPLOAD_DIR + fileName);
                Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Chemin relatif pour Symfony
                image.setUrlImage("/uploads/images/" + fileName);
            } else {
                // Modification sans changer l'image
                image.setUrlImage(txtUrlImage.getText().trim());
            }

            Destination selectedDest = comboDestination.getSelectionModel().getSelectedItem();
            image.setDestinationId(selectedDest.getId());

            if (image.getId() == 0) service.addEntity(image);
            else service.update(image.getId(), image);

            closeWindow();

        } catch (SQLException ex) {
            showAlert("Erreur base de données", ex.getMessage());
        } catch (IOException ex) {
            showAlert("Erreur copie image", ex.getMessage());
        } catch (Exception ex) {
            showAlert("Erreur", ex.getMessage());
        }
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot) : ".jpg";
    }

    private boolean validateFields() {
        boolean valid = true;

        String url = txtUrlImage.getText().trim();
        if (url.isEmpty()) {
            setError(txtUrlImage, errUrl, "Veuillez choisir une image");
            valid = false;
        }

        if (comboDestination.getSelectionModel().getSelectedItem() == null) {
            comboDestination.getStyleClass().add("input-error");
            errDestination.setText("Veuillez choisir une destination");
            valid = false;
        }

        return valid;
    }

    private void setError(Control field, Label label, String message) {
        if (field != null && !field.getStyleClass().contains("input-error"))
            field.getStyleClass().add("input-error");
        if (label != null) label.setText(message);
    }

    private void clearErrors() {
        txtUrlImage.getStyleClass().remove("input-error");
        comboDestination.getStyleClass().remove("input-error");
        errUrl.setText("");
        errDestination.setText("");
    }

    private void closeWindow() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void setDestinationId(int destinationId) {
        for (Destination d : comboDestination.getItems()) {
            if (d.getId() == destinationId) {
                comboDestination.getSelectionModel().select(d);
                break;
            }
        }
        comboDestination.setDisable(true);
    }
}