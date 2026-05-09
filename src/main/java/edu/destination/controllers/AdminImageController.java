package edu.destination.controllers;

import edu.destination.entities.Image;
import edu.destination.services.ImageService;
import edu.destination.tools.SceneUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AdminImageController {

    @FXML private TableView<Image> tableImages;
    @FXML private TableColumn<Image, Integer> colIdImage;
    @FXML private TableColumn<Image, String>  colUrlImage;
    @FXML private TableColumn<Image, Integer> colIdDestination;
    @FXML private TextField searchIdDestination;
    @FXML private Button btnReset;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;

    private final ImageService service = new ImageService();
    private final ObservableList<Image> filteredList = FXCollections.observableArrayList();
    private List<Image> allImages;

    @FXML
    private void initialize() {

        // ── ID Badge violet ──
        colIdImage.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIdImage.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label("" + item);
                badge.setStyle("-fx-background-color:#1e1e2a;-fx-text-fill:#818cf8;-fx-background-radius:6;-fx-padding:3 9;-fx-font-size:12px;-fx-font-weight:700;");
                setGraphic(badge); setText(null);
            }
        });

        // ── Destination ID badge ──
        colIdDestination.setCellValueFactory(new PropertyValueFactory<>("destinationId"));
        colIdDestination.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label("Dest. " + item);
                badge.setStyle("-fx-background-color:#1e1e2a;-fx-text-fill:#a78bfa;-fx-background-radius:6;-fx-padding:3 9;-fx-font-size:11px;-fx-font-weight:600;");
                setGraphic(badge); setText(null);
            }
        });

        // ── URL Image : aperçu ──
        colUrlImage.setCellValueFactory(new PropertyValueFactory<>("urlImage"));
        colUrlImage.setCellFactory(col -> new TableCell<Image, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(60);
                imageView.setFitWidth(90);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
            }
            @Override protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isBlank()) { setGraphic(null); setText(null); return; }
                javafx.scene.image.Image img = loadImage(url);
                if (img != null && !img.isError()) {
                    imageView.setImage(img);
                    setGraphic(imageView);
                    setText(null);
                } else {
                    setGraphic(null);
                    setText(url);
                    setStyle("-fx-font-size:10;-fx-text-fill:#94a3b8;");
                }
            }
        });

        tableImages.setRowFactory(tv -> {
            TableRow<Image> row = new TableRow<>();
            row.setPrefHeight(70);
            return row;
        });

        tableImages.setItems(filteredList);
        loadData();
        setupSearch();

        btnAjouter.setOnAction(e -> openForm(null));
        btnModifier.setOnAction(e -> {
            Image selected = tableImages.getSelectionModel().getSelectedItem();
            if (selected != null) openForm(selected);
            else showAlert("Sélectionnez une image.");
        });
        btnSupprimer.setOnAction(e -> {
            Image selected = tableImages.getSelectionModel().getSelectedItem();
            if (selected != null) { service.deleteEntity(selected); loadData(); }
            else showAlert("Sélectionnez une image à supprimer.");
        });
    }

    private javafx.scene.image.Image loadImage(String url) {
        try {
            if (url.startsWith("/uploads/")) {
                File f = new File("C:/xampp/htdocs/VOYAGE/public" + url);
                if (f.exists()) return new javafx.scene.image.Image(f.toURI().toString(), false);
            }
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return new javafx.scene.image.Image(url, true);
            }
            File file = new File(url);
            if (file.exists()) return new javafx.scene.image.Image(file.toURI().toString(), true);
            String[] bases = {
                    System.getProperty("user.dir") + "/src/main/resources",
                    System.getProperty("user.dir") + "/target/classes",
                    System.getProperty("user.dir")
            };
            for (String base : bases) {
                File f = new File(base + url);
                if (f.exists()) return new javafx.scene.image.Image(f.toURI().toString(), true);
            }
            var stream = getClass().getResourceAsStream(url);
            if (stream != null) return new javafx.scene.image.Image(stream);
        } catch (Exception e) { /* image non chargeable */ }
        return null;
    }

    private void loadData() { allImages = service.getData(); applyFilter(); }

    private void setupSearch() {
        searchIdDestination.textProperty().addListener((obs, o, n) -> applyFilter());
        btnReset.setOnAction(e -> searchIdDestination.clear());
    }

    private void applyFilter() {
        if (allImages == null) return;
        String idStr = searchIdDestination.getText() == null ? "" : searchIdDestination.getText().trim();
        List<Image> filtered = allImages.stream()
                .filter(img -> idStr.isEmpty() || String.valueOf(img.getDestinationId()).contains(idStr))
                .collect(Collectors.toList());
        filteredList.setAll(filtered);
    }

    private void openForm(Image image) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminImageForm.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            SceneUtil.applyCss(scene, "/app/image.css");            Stage stage = new Stage();
            stage.setScene(scene);
            AdminImageFormController controller = loader.getController();
            controller.setImage(image);
            stage.showAndWait();
            loadData();
        } catch (IOException e) { e.printStackTrace(); showAlert("Erreur lors de l'ouverture du formulaire."); }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}