package edu.destination.controllers;

import edu.destination.entities.DestinationImage;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdminImageController {

    @FXML private TableView<DestinationImage> tableImages;
    @FXML private TableColumn<DestinationImage, Integer> colIdImage;
    @FXML private Button btnRetour;

    @FXML private TableColumn<DestinationImage, String> colUrlImage;
    @FXML private TableColumn<DestinationImage, Integer> colIdDestination;

    @FXML private TextField searchIdDestination;
    @FXML private Button btnReset;

    @FXML private Button navDashboard, navDestinations, navTransports, navImages, navClient;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;

    private final ImageService service = new ImageService();
    private final ObservableList<DestinationImage> filteredList = FXCollections.observableArrayList();
    private List<DestinationImage> allImages;

    @FXML
    private void initialize() {
        colIdImage.setCellValueFactory(new PropertyValueFactory<>("idImage"));
        colUrlImage.setCellValueFactory(new PropertyValueFactory<>("urlImage"));
        colIdDestination.setCellValueFactory(new PropertyValueFactory<>("idDestination"));

        tableImages.setItems(filteredList);

        loadData();
        setupSearch();

        btnAjouter.setOnAction(e -> openForm(null));
        btnModifier.setOnAction(e -> {
            DestinationImage selected = tableImages.getSelectionModel().getSelectedItem();
            if (selected != null) openForm(selected);
            else showAlert("Sélectionnez une image.");
        });
        btnRetour.setOnAction(e -> {
            try {
                Parent root = FXMLLoader.load(
                        getClass().getResource("/main_layout_admin.fxml")
                );
                Stage stage = (Stage) tableImages.getScene().getWindow();                SceneUtil.setScene(stage, root, 1200, 800);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        btnSupprimer.setOnAction(e -> {
            DestinationImage selected = tableImages.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.deleteEntity(selected);
                loadData();
            } else showAlert("Sélectionnez une image à supprimer.");
        });

        navDashboard.setOnAction(e -> openView("/AdminDashboard.fxml"));
        navDestinations.setOnAction(e -> openView("/AdminDestinationView.fxml"));
        navTransports.setOnAction(e -> openView("/AdminTransportView.fxml"));
        navImages.setOnAction(e -> openView("/AdminImageView.fxml"));
        navClient.setOnAction(e -> openView("/ClientDestinationListView.fxml"));
    }

    private void loadData() {
        allImages = service.getData();
        applyFilter();
    }

    private void setupSearch() {
        searchIdDestination.textProperty().addListener((obs, o, n) -> applyFilter());
        btnReset.setOnAction(e -> searchIdDestination.clear());
    }

    private void applyFilter() {
        if (allImages == null) return;

        String idStr = searchIdDestination.getText() == null ? "" : searchIdDestination.getText().trim();

        List<DestinationImage> filtered = allImages.stream()
                .filter(img -> idStr.isEmpty()
                        || String.valueOf(img.getIdDestination()).contains(idStr))
                .collect(Collectors.toList());

        filteredList.setAll(filtered);
    }

    private void openForm(DestinationImage image) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminImageForm.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            SceneUtil.applyCss(scene);

            Stage stage = new Stage();
            stage.setScene(scene);

            AdminImageFormController controller = loader.getController();
            controller.setImage(image);

            stage.showAndWait();
            loadData();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture du formulaire.");
        }
    }

    private void openView(String fxml) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            Stage stage = (Stage) tableImages.getScene().getWindow(); // ✅ utilise la table
            SceneUtil.setScene(stage, root, 1200, 800);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement de la page.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}