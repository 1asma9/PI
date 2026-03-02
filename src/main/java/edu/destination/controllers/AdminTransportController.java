package edu.destination.controllers;

import edu.destination.entities.Transport;
import edu.destination.services.TransportService;
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

public class AdminTransportController {

    @FXML private TableView<Transport> tableTransports;
    @FXML private TableColumn<Transport, Integer> colIdTransport;
    @FXML private TableColumn<Transport, String> colTypeTransport;
    @FXML private TableColumn<Transport, Integer> colIdDestination;

    // ==============================
    // FXML — Recherche (à ajouter dans le FXML)
    // ==============================
    @FXML private ComboBox<String> comboTypeTransport;  // Tous / Voiture / Avion / Train / Vélo / Piéton
    @FXML private TextField searchIdDestination;         // Recherche par id destination
    @FXML private Button btnReset;

    // Sidebar navigation
    @FXML private Button navDashboard;
    @FXML private Button navDestinations;
    @FXML private Button navTransports;
    @FXML private Button navImages;
    @FXML private Button navClient;

    // Table buttons
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;

    private final TransportService service = new TransportService();
    private final ObservableList<Transport> filteredList = FXCollections.observableArrayList();

    // ✅ Liste complète en mémoire
    private List<Transport> allTransports;

    @FXML
    private void initialize() {
        colIdTransport.setCellValueFactory(new PropertyValueFactory<>("idTransport"));
        colTypeTransport.setCellValueFactory(new PropertyValueFactory<>("typeTransport"));
        colIdDestination.setCellValueFactory(new PropertyValueFactory<>("idDestination"));

        tableTransports.setItems(filteredList);

        loadData();
        setupSearch();

        // Actions table
        btnAjouter.setOnAction(e -> openForm(null));
        btnModifier.setOnAction(e -> {
            Transport selected = tableTransports.getSelectionModel().getSelectedItem();
            if (selected != null) openForm(selected);
            else showAlert("Veuillez sélectionner un transport à modifier.");
        });
        btnSupprimer.setOnAction(e -> {
            Transport selected = tableTransports.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.deleteEntity(selected);
                loadData();
            } else showAlert("Veuillez sélectionner un transport à supprimer.");
        });

        // Sidebar
        navDashboard.setOnAction(e -> openView("/AdminDashboard.fxml"));
        navDestinations.setOnAction(e -> openView("/AdminDestinationView.fxml"));
        navTransports.setOnAction(e -> openView("/AdminTransportView.fxml"));
        navImages.setOnAction(e -> openView("/AdminImageView.fxml"));
        navClient.setOnAction(e -> openView("/ClientDestinationListView.fxml"));
    }

    // ==============================
    // CHARGEMENT DONNÉES
    // ==============================
    private void loadData() {
        allTransports = service.getData();
        applyFilter();
    }

    // ==============================
    // RECHERCHE EN MÉMOIRE
    // ==============================
    private void setupSearch() {
        // ComboBox types de transport
        comboTypeTransport.getItems().addAll("Tous", "Voiture", "Avion", "Train", "Vélo", "Piéton");
        comboTypeTransport.getSelectionModel().selectFirst();

        // Listeners temps réel
        comboTypeTransport.valueProperty().addListener((obs, o, n) -> applyFilter());
        searchIdDestination.textProperty().addListener((obs, o, n) -> applyFilter());

        // Reset
        btnReset.setOnAction(e -> {
            comboTypeTransport.getSelectionModel().selectFirst();
            searchIdDestination.clear();
        });
    }

    private void applyFilter() {
        if (allTransports == null) return;

        String type = comboTypeTransport.getValue();
        String idStr = searchIdDestination.getText() == null ? "" : searchIdDestination.getText().trim();

        List<Transport> filtered = allTransports.stream()
                .filter(t -> {
                    // Filtre type transport
                    boolean matchType = type == null || type.equals("Tous")
                            || type.equalsIgnoreCase(t.getTypeTransport());

                    // Filtre id destination (contient)
                    boolean matchId = idStr.isEmpty()
                            || String.valueOf(t.getIdDestination()).contains(idStr);

                    return matchType && matchId;
                })
                .collect(Collectors.toList());

        filteredList.setAll(filtered);
    }

    // ==============================
    // FORMULAIRE
    // ==============================
    private void openForm(Transport transport) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminTransportForm.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            SceneUtil.applyCss(scene);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(transport == null ? "Ajouter Transport" : "Modifier Transport");

            AdminTransportFormController controller = loader.getController();
            controller.setTransport(transport);

            stage.showAndWait();
            loadData();

        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Erreur lors de l'ouverture du formulaire.");
        }
    }

    private void openView(String fxml) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            Stage stage = (Stage) navDashboard.getScene().getWindow();
            SceneUtil.setScene(stage, root, 1200, 800);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement de la page.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}