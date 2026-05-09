package edu.destination.controllers;

import edu.destination.entities.Voyage;
import edu.destination.services.VoyageService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdminVoyageController {

    @FXML private TableView<Voyage>          tableVoyages;
    @FXML private TableColumn<Voyage, Integer> colId;
    @FXML private TableColumn<Voyage, String>  colDateDepart;
    @FXML private TableColumn<Voyage, String>  colDateArrivee;
    @FXML private TableColumn<Voyage, String>  colPointDepart;
    @FXML private TableColumn<Voyage, String>  colPointArrivee;
    @FXML private TableColumn<Voyage, Double>  colPrix;
    @FXML private TableColumn<Voyage, Integer> colDestinationId;
    @FXML private TableColumn<Voyage, Integer> colPaid;
    @FXML private TableColumn<Voyage, Void>    colActions;

    @FXML private Button    btnAjouter, btnModifier, btnSupprimer;
    @FXML private TextField searchPointDepart;
    @FXML private Button    btnReset;

    private final VoyageService          service      = new VoyageService();
    private final ObservableList<Voyage> filteredList = FXCollections.observableArrayList();
    private List<Voyage> allVoyages;

    @FXML
    private void initialize() {

        // ── ID Badge violet ──
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label("" + item);
                badge.setStyle("-fx-background-color:#1e1e2a;-fx-text-fill:#818cf8;-fx-background-radius:6;-fx-padding:3 9;-fx-font-size:12px;-fx-font-weight:700;");
                setGraphic(badge); setText(null);
            }
        });

        // ── Point départ badge gris ──
        colPointDepart.setCellValueFactory(new PropertyValueFactory<>("pointDepart"));
        colPointDepart.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                badge.setStyle("-fx-background-color:#1e1e2a;-fx-text-fill:#94a3b8;-fx-border-color:#2a2a3a;-fx-border-radius:6;-fx-background-radius:6;-fx-padding:3 9;-fx-font-size:12px;");
                setGraphic(badge); setText(null);
            }
        });

        // ── Point arrivée badge gris ──
        colPointArrivee.setCellValueFactory(new PropertyValueFactory<>("pointArrivee"));
        colPointArrivee.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                badge.setStyle("-fx-background-color:#1e1e2a;-fx-text-fill:#94a3b8;-fx-border-color:#2a2a3a;-fx-border-radius:6;-fx-background-radius:6;-fx-padding:3 9;-fx-font-size:12px;");
                setGraphic(badge); setText(null);
            }
        });

        // ── Prix badge orange ──
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colPrix.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(String.format("%.0f €", item));
                badge.setStyle("-fx-background-color:#2e1e06;-fx-text-fill:#fbbf24;-fx-border-color:#4a3010;-fx-border-radius:6;-fx-background-radius:6;-fx-padding:3 9;-fx-font-size:12px;-fx-font-weight:600;");
                setGraphic(badge); setText(null);
            }
        });

        // ── Paid badge ──
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        colPaid.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                boolean paid = item == 1;
                Label badge = new Label(paid ? "✅ Payé" : "⏳ Non payé");
                if (paid) {
                    badge.setStyle("-fx-background-color:#0d2b1a;-fx-text-fill:#4ade80;-fx-border-color:#1a3d28;-fx-border-radius:20;-fx-background-radius:20;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:700;");
                } else {
                    badge.setStyle("-fx-background-color:#2b0d0d;-fx-text-fill:#f87171;-fx-border-color:#3d1a1a;-fx-border-radius:20;-fx-background-radius:20;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:700;");
                }
                setGraphic(badge); setText(null);
            }
        });

        colDateDepart.setCellValueFactory(new PropertyValueFactory<>("dateDepart"));
        colDateArrivee.setCellValueFactory(new PropertyValueFactory<>("dateArrivee"));
        colDestinationId.setCellValueFactory(new PropertyValueFactory<>("destinationId"));

        // ── Colonne Actions ──
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button btnTransport = new Button("+ Transport");
            private final Button btnDetail    = new Button("👁 Détail");
            private final HBox   box          = new HBox(6, btnTransport, btnDetail);
            {
                box.setAlignment(Pos.CENTER);
                btnTransport.setStyle("-fx-background-color:#1e1e2a;-fx-text-fill:#a78bfa;-fx-border-color:#3a2060;-fx-border-width:1;-fx-border-radius:6;-fx-background-radius:6;-fx-font-size:11px;-fx-padding:4 10;-fx-cursor:hand;");
                btnTransport.setOnMouseEntered(e -> btnTransport.setStyle("-fx-background-color:#a78bfa;-fx-text-fill:white;-fx-border-radius:6;-fx-background-radius:6;-fx-font-size:11px;-fx-padding:4 10;-fx-cursor:hand;"));
                btnTransport.setOnMouseExited(e -> btnTransport.setStyle("-fx-background-color:#1e1e2a;-fx-text-fill:#a78bfa;-fx-border-color:#3a2060;-fx-border-width:1;-fx-border-radius:6;-fx-background-radius:6;-fx-font-size:11px;-fx-padding:4 10;-fx-cursor:hand;"));

                btnDetail.setStyle("-fx-background-color:transparent;-fx-border-color:#f97316;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1.5;-fx-text-fill:#f97316;-fx-font-size:11px;-fx-font-weight:700;-fx-padding:4 10;-fx-cursor:hand;");
                btnDetail.setOnMouseEntered(e -> btnDetail.setStyle("-fx-background-color:rgba(249,115,22,0.15);-fx-border-color:#f97316;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1.5;-fx-text-fill:#f97316;-fx-font-size:11px;-fx-font-weight:700;-fx-padding:4 10;-fx-cursor:hand;"));
                btnDetail.setOnMouseExited(e -> btnDetail.setStyle("-fx-background-color:transparent;-fx-border-color:#f97316;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1.5;-fx-text-fill:#f97316;-fx-font-size:11px;-fx-font-weight:700;-fx-padding:4 10;-fx-cursor:hand;"));

                btnTransport.setOnAction(e -> openTransportForm(getTableView().getItems().get(getIndex()).getId()));
                btnDetail.setOnAction(e -> openDetail(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tableVoyages.setItems(filteredList);
        loadData();
        setupSearch();

        btnAjouter.setOnAction(e -> openForm(null));
        btnModifier.setOnAction(e -> {
            Voyage selected = tableVoyages.getSelectionModel().getSelectedItem();
            if (selected != null) openForm(selected);
            else showAlert("Veuillez sélectionner un voyage à modifier.");
        });
        btnSupprimer.setOnAction(e -> {
            Voyage selected = tableVoyages.getSelectionModel().getSelectedItem();
            if (selected != null) { service.deleteEntity(selected); loadData(); }
            else showAlert("Veuillez sélectionner un voyage à supprimer.");
        });
    }

    private void openDetail(Voyage voyage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminVoyageDetail.fxml"));
            Parent root = loader.load();
            AdminVoyageDetailController ctrl = loader.getController();
            ctrl.setVoyage(voyage);
            Scene scene = new Scene(root);
            applyCss(scene);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Détail Voyage " + voyage.getId());
            stage.setMinWidth(760); stage.setMinHeight(600);
            stage.showAndWait();
            loadData();
        } catch (IOException ex) { ex.printStackTrace(); showAlert("Erreur ouverture détail voyage."); }
    }

    private void openTransportForm(int voyageId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminTransportForm.fxml"));
            Parent root = loader.load();
            AdminTransportFormController controller = loader.getController();
            controller.setVoyageId(voyageId);
            Scene scene = new Scene(root);
            applyCss(scene);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Ajouter Transport — Voyage #" + voyageId);
            stage.showAndWait();
            loadData();
        } catch (IOException ex) { ex.printStackTrace(); showAlert("Erreur ouverture formulaire Transport."); }
    }

    private void loadData() { allVoyages = service.getData(); applyFilter(); }

    private void setupSearch() {
        searchPointDepart.textProperty().addListener((obs, o, n) -> applyFilter());
        btnReset.setOnAction(e -> searchPointDepart.clear());
    }

    private void applyFilter() {
        if (allVoyages == null) return;
        String search = searchPointDepart.getText() == null ? "" : searchPointDepart.getText().toLowerCase().trim();
        List<Voyage> filtered = allVoyages.stream()
                .filter(v -> search.isEmpty()
                        || (v.getPointDepart()  != null && v.getPointDepart().toLowerCase().contains(search))
                        || (v.getPointArrivee() != null && v.getPointArrivee().toLowerCase().contains(search)))
                .collect(Collectors.toList());
        filteredList.setAll(filtered);
    }

    private void openForm(Voyage voyage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminVoyageForm.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            applyCss(scene);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(voyage == null ? "Ajouter Voyage" : "Modifier Voyage");
            AdminVoyageFormController controller = loader.getController();
            controller.setVoyage(voyage);
            stage.showAndWait();
            loadData();
        } catch (IOException ex) { ex.printStackTrace(); showAlert("Erreur lors de l'ouverture du formulaire."); }
    }

    private void applyCss(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/app/voyage.css")).toExternalForm());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}