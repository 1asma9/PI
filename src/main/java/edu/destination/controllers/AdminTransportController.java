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
import java.util.stream.Collectors;

public class AdminTransportController {

    @FXML private TableView<Transport> tableTransports;
    @FXML private TableColumn<Transport, Integer> colIdTransport;
    @FXML private TableColumn<Transport, String>  colTypeTransport;
    @FXML private TableColumn<Transport, Integer> colVoyageId;
    @FXML private ComboBox<String> comboTypeTransport;
    @FXML private TextField searchVoyageId;
    @FXML private Button btnReset;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;

    private final TransportService service = new TransportService();
    private final ObservableList<Transport> filteredList = FXCollections.observableArrayList();
    private List<Transport> allTransports;

    @FXML
    private void initialize() {

        // ── ID Badge violet ──
        colIdTransport.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIdTransport.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label("" + item);
                badge.setStyle("-fx-background-color:#1e1e2a;-fx-text-fill:#818cf8;-fx-background-radius:6;-fx-padding:3 9;-fx-font-size:12px;-fx-font-weight:700;");
                setGraphic(badge); setText(null);
            }
        });

        // ── Type Transport badge coloré selon type ──
        colTypeTransport.setCellValueFactory(new PropertyValueFactory<>("typeTransport"));
        colTypeTransport.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                String style = "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:3 9;-fx-font-size:11px;-fx-font-weight:600;";
                switch (item) {
                    case "Avion"   -> badge.setStyle(style + "-fx-background-color:#0a1a2e;-fx-text-fill:#7dd3fc;-fx-border-color:#0e2a4a;");
                    case "Train"   -> badge.setStyle(style + "-fx-background-color:#1a2e10;-fx-text-fill:#86efac;-fx-border-color:#1e4020;");
                    case "Voiture" -> badge.setStyle(style + "-fx-background-color:#2e1e06;-fx-text-fill:#fbbf24;-fx-border-color:#4a3010;");
                    case "Bus"     -> badge.setStyle(style + "-fx-background-color:#2e1506;-fx-text-fill:#fb923c;-fx-border-color:#4a2808;");
                    default        -> badge.setStyle(style + "-fx-background-color:#1e1e2a;-fx-text-fill:#94a3b8;-fx-border-color:#2a2a3a;");
                }
                setGraphic(badge); setText(null);
            }
        });

        // ── Voyage ID badge gris ──
        colVoyageId.setCellValueFactory(new PropertyValueFactory<>("voyageId"));
        colVoyageId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label("Voyage " + item);
                badge.setStyle("-fx-background-color:#1e1e2a;-fx-text-fill:#a78bfa;-fx-background-radius:6;-fx-padding:3 9;-fx-font-size:11px;-fx-font-weight:600;");
                setGraphic(badge); setText(null);
            }
        });

        tableTransports.setItems(filteredList);
        loadData();
        setupSearch();

        btnAjouter.setOnAction(e -> openForm(null));
        btnModifier.setOnAction(e -> {
            Transport selected = tableTransports.getSelectionModel().getSelectedItem();
            if (selected != null) openForm(selected);
            else showAlert("Veuillez sélectionner un transport à modifier.");
        });
        btnSupprimer.setOnAction(e -> {
            Transport selected = tableTransports.getSelectionModel().getSelectedItem();
            if (selected != null) { service.deleteEntity(selected); loadData(); }
            else showAlert("Veuillez sélectionner un transport à supprimer.");
        });
    }

    private void loadData() { allTransports = service.getData(); applyFilter(); }

    private void setupSearch() {
        comboTypeTransport.getItems().addAll("Tous", "Voiture", "Avion", "Train", "Bus");
        comboTypeTransport.getSelectionModel().selectFirst();
        comboTypeTransport.valueProperty().addListener((obs, o, n) -> applyFilter());
        searchVoyageId.textProperty().addListener((obs, o, n) -> applyFilter());
        btnReset.setOnAction(e -> { comboTypeTransport.getSelectionModel().selectFirst(); searchVoyageId.clear(); });
    }

    private void applyFilter() {
        if (allTransports == null) return;
        String type  = comboTypeTransport.getValue();
        String idStr = searchVoyageId.getText() == null ? "" : searchVoyageId.getText().trim();
        List<Transport> filtered = allTransports.stream()
                .filter(t -> {
                    boolean matchType = type == null || type.equals("Tous") || type.equalsIgnoreCase(t.getTypeTransport());
                    boolean matchId   = idStr.isEmpty() || String.valueOf(t.getVoyageId()).contains(idStr);
                    return matchType && matchId;
                })
                .collect(Collectors.toList());
        filteredList.setAll(filtered);
    }

    private void openForm(Transport transport) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminTransportForm.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            SceneUtil.applyCss(scene, "/app/transport.css");            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(transport == null ? "Ajouter Transport" : "Modifier Transport");
            AdminTransportFormController controller = loader.getController();
            controller.setTransport(transport);
            stage.showAndWait();
            loadData();
        } catch (IOException ex) { ex.printStackTrace(); showAlert("Erreur lors de l'ouverture du formulaire."); }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}