package edu.destination.controllers;

import edu.destination.entities.Destination;
import edu.destination.services.DestinationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdminDestinationController {

    @FXML private TableView<Destination> tableDestinations;
    @FXML private TableColumn<Destination, Integer> colId;
    @FXML private TableColumn<Destination, String>  colNom;
    @FXML private TableColumn<Destination, String>  colPays;
    @FXML private TableColumn<Destination, String>  colDescription;
    @FXML private TableColumn<Destination, String>  colVideoPath;
    @FXML private TableColumn<Destination, Boolean> colStatut;
    @FXML private TableColumn<Destination, String>  colSaison;
    @FXML private TableColumn<Destination, Double>  colLatitude;
    @FXML private TableColumn<Destination, Double>  colLongitude;
    @FXML private TableColumn<Destination, Integer> colNbVisites;
    @FXML private TableColumn<Destination, Integer> colNbLikes;
    @FXML private TableColumn<Destination, Void>    colActions;

    @FXML private Button      btnAjouter, btnModifier, btnSupprimer;
    @FXML private TextField   searchPays;
    @FXML private ComboBox<String> comboStatut;
    @FXML private ComboBox<String> comboSaison;
    @FXML private Button      btnReset;

    private final DestinationService      service      = new DestinationService();
    private final ObservableList<Destination> filteredList = FXCollections.observableArrayList();
    private List<Destination> allDestinations;

    // ══════════════════════════════════════════════════════
    @FXML
    private void initialize() {
        // ── ID Badge violet ──────────────────────────────
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label("" + item);
                badge.setStyle(
                    "-fx-background-color:#1e1e2a;" +
                    "-fx-text-fill:#818cf8;" +
                    "-fx-background-radius:6;" +
                    "-fx-padding:3 9;" +
                    "-fx-font-size:12px;" +
                    "-fx-font-weight:700;"
                );
                setGraphic(badge);
                setText(null);
            }
        });

        // ── Nom en blanc ─────────────────────────────────
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNom.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                setStyle("-fx-text-fill:#e2e8f0;-fx-font-weight:600;-fx-font-size:13px;");
            }
        });

        // ── Pays badge gris ──────────────────────────────
        colPays.setCellValueFactory(new PropertyValueFactory<>("pays"));
        colPays.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                badge.setStyle(
                    "-fx-background-color:#1e1e2a;" +
                    "-fx-text-fill:#94a3b8;" +
                    "-fx-border-color:#2a2a3a;" +
                    "-fx-border-radius:6;" +
                    "-fx-background-radius:6;" +
                    "-fx-padding:3 9;" +
                    "-fx-font-size:12px;"
                );
                setGraphic(badge);
                setText(null);
            }
        });

        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // ── Statut badge actif/inactif ───────────────────
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item ? "Actif" : "Inactif");
                if (item) {
                    badge.setStyle(
                        "-fx-background-color:#0d2b1a;" +
                        "-fx-text-fill:#4ade80;" +
                        "-fx-border-color:#1a3d28;" +
                        "-fx-border-radius:20;" +
                        "-fx-background-radius:20;" +
                        "-fx-padding:3 10;" +
                        "-fx-font-size:11px;" +
                        "-fx-font-weight:700;"
                    );
                } else {
                    badge.setStyle(
                        "-fx-background-color:#2b0d0d;" +
                        "-fx-text-fill:#f87171;" +
                        "-fx-border-color:#3d1a1a;" +
                        "-fx-border-radius:20;" +
                        "-fx-background-radius:20;" +
                        "-fx-padding:3 10;" +
                        "-fx-font-size:11px;" +
                        "-fx-font-weight:700;"
                    );
                }
                setGraphic(badge);
                setText(null);
            }
        });

        // ── Saison badge coloré ──────────────────────────
        colSaison.setCellValueFactory(new PropertyValueFactory<>("meilleureSaison"));
        colSaison.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                String style = "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:3 9;-fx-font-size:11px;-fx-font-weight:600;";
                switch (item) {
                    case "Printemps" -> badge.setStyle(style + "-fx-background-color:#1a2e10;-fx-text-fill:#86efac;-fx-border-color:#1e4020;");
                    case "Ete"       -> badge.setStyle(style + "-fx-background-color:#2e1e06;-fx-text-fill:#fbbf24;-fx-border-color:#4a3010;");
                    case "Automne"   -> badge.setStyle(style + "-fx-background-color:#2e1506;-fx-text-fill:#fb923c;-fx-border-color:#4a2808;");
                    case "Hiver"     -> badge.setStyle(style + "-fx-background-color:#0a1a2e;-fx-text-fill:#7dd3fc;-fx-border-color:#0e2a4a;");
                    default          -> badge.setStyle(style + "-fx-background-color:#1e1e2a;-fx-text-fill:#94a3b8;-fx-border-color:#2a2a3a;");
                }
                setGraphic(badge);
                setText(null);
            }
        });

        colLatitude.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        colLongitude.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        colNbVisites.setCellValueFactory(new PropertyValueFactory<>("nbVisites"));
        colNbLikes.setCellValueFactory(new PropertyValueFactory<>("nbLikes"));

        colVideoPath.setCellValueFactory(new PropertyValueFactory<>("videoPath"));
        colVideoPath.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null || item.isBlank() ? "" : "Oui");
            }
        });

        // ── Colonne Actions ──────────────────────────────
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button btnVoyage = new Button("+ Voyage");
            private final Button btnImage  = new Button("+ Image");
            private final HBox   box       = new HBox(6, btnVoyage, btnImage);
            {
                btnVoyage.setStyle(
                    "-fx-background-color:#1e1e2a;" +
                    "-fx-text-fill:#a78bfa;" +
                    "-fx-border-color:#3a2060;" +
                    "-fx-border-width:1;" +
                    "-fx-border-radius:6;" +
                    "-fx-background-radius:6;" +
                    "-fx-font-size:11px;" +
                    "-fx-padding:4 10;" +
                    "-fx-cursor:hand;"
                );
                btnImage.setStyle(
                    "-fx-background-color:#1a3d28;" +
                    "-fx-text-fill:#4ade80;" +
                    "-fx-border-color:#1e4020;" +
                    "-fx-border-width:1;" +
                    "-fx-border-radius:6;" +
                    "-fx-background-radius:6;" +
                    "-fx-font-size:11px;" +
                    "-fx-padding:4 10;" +
                    "-fx-cursor:hand;"
                );

                btnVoyage.setOnMouseEntered(e -> btnVoyage.setStyle(
                    "-fx-background-color:#a78bfa;-fx-text-fill:white;" +
                    "-fx-border-radius:6;-fx-background-radius:6;" +
                    "-fx-font-size:11px;-fx-padding:4 10;-fx-cursor:hand;"
                ));
                btnVoyage.setOnMouseExited(e -> btnVoyage.setStyle(
                    "-fx-background-color:#1e1e2a;-fx-text-fill:#a78bfa;" +
                    "-fx-border-color:#3a2060;-fx-border-width:1;" +
                    "-fx-border-radius:6;-fx-background-radius:6;" +
                    "-fx-font-size:11px;-fx-padding:4 10;-fx-cursor:hand;"
                ));

                btnImage.setOnMouseEntered(e -> btnImage.setStyle(
                    "-fx-background-color:#4ade80;-fx-text-fill:#0d1a0d;" +
                    "-fx-border-radius:6;-fx-background-radius:6;" +
                    "-fx-font-size:11px;-fx-padding:4 10;-fx-cursor:hand;"
                ));
                btnImage.setOnMouseExited(e -> btnImage.setStyle(
                    "-fx-background-color:#1a3d28;-fx-text-fill:#4ade80;" +
                    "-fx-border-color:#1e4020;-fx-border-width:1;" +
                    "-fx-border-radius:6;-fx-background-radius:6;" +
                    "-fx-font-size:11px;-fx-padding:4 10;-fx-cursor:hand;"
                ));

                btnVoyage.setOnAction(e -> {
                    Destination dest = getTableView().getItems().get(getIndex());
                    openVoyageForm(dest.getId());
                });
                btnImage.setOnAction(e -> {
                    Destination dest = getTableView().getItems().get(getIndex());
                    openImageForm(dest.getId());
                });

                box.setStyle("-fx-alignment:center;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tableDestinations.setItems(filteredList);
        loadData();
        setupSearch();

        btnAjouter.setOnAction(e -> openForm(null));

        btnModifier.setOnAction(e -> {
            Destination selected = tableDestinations.getSelectionModel().getSelectedItem();
            if (selected != null) openForm(selected);
            else showAlert("Veuillez sélectionner une destination à modifier.");
        });

        btnSupprimer.setOnAction(e -> {
            Destination selected = tableDestinations.getSelectionModel().getSelectedItem();
            if (selected != null) { service.deleteEntity(selected); loadData(); applyFilter(); }
            else showAlert("Veuillez sélectionner une destination à supprimer.");
        });
    }

    // ══════════════════════════════════════════════════════
    @FXML
    private void openStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/DestinationStatistique.fxml"));
            if (loader.getLocation() == null) {
                showAlert("Fichier DestinationStatistique.fxml introuvable.");
                return;
            }
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("📊 Statistiques Destinations");
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur ouverture statistiques : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════
    private void openVoyageForm(int destinationId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminVoyageForm.fxml"));
            Parent root = loader.load();
            AdminVoyageFormController controller = loader.getController();
            controller.setDestinationId(destinationId);
            Scene scene = new Scene(root);
            applyCss(scene);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Ajouter Voyage — Destination #" + destinationId);
            stage.showAndWait();
            loadData();
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Erreur ouverture formulaire Voyage.");
        }
    }

    private void openImageForm(int destinationId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminImageForm.fxml"));
            Parent root = loader.load();
            AdminImageFormController controller = loader.getController();
            controller.setDestinationId(destinationId);
            Scene scene = new Scene(root);
            applyCss(scene);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Ajouter Image — Destination #" + destinationId);
            stage.showAndWait();
            loadData();
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Erreur ouverture formulaire Image.");
        }
    }

    private void openForm(Destination destination) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDestinationForm.fxml"));
            Parent formRoot = loader.load();
            Scene formScene = new Scene(formRoot, 680, 750);
            applyCss(formScene);
            Stage stage = new Stage();
            stage.setScene(formScene);
            stage.setTitle(destination == null ? "Ajouter Destination" : "Modifier Destination");
            stage.setMinWidth(650);
            stage.setMinHeight(600);
            AdminDestinationFormController controller = loader.getController();
            controller.setDestination(destination);
            stage.showAndWait();
            loadData();
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Erreur lors de l'ouverture du formulaire.");
        }
    }

    // ══════════════════════════════════════════════════════
    private void loadData() {
        allDestinations = service.getData();
        applyFilter();
    }

    private void setupSearch() {
        comboStatut.getItems().addAll("Tous", "Actif", "Inactif");
        comboStatut.getSelectionModel().selectFirst();

        comboSaison.getItems().add("Toutes les saisons");
        List<String> saisons = allDestinations.stream()
                .map(Destination::getMeilleureSaison)
                .filter(s -> s != null && !s.isBlank())
                .distinct().sorted().collect(Collectors.toList());
        comboSaison.getItems().addAll(saisons);
        comboSaison.getSelectionModel().selectFirst();

        searchPays.textProperty().addListener((obs, o, n) -> applyFilter());
        comboStatut.valueProperty().addListener((obs, o, n) -> applyFilter());
        comboSaison.valueProperty().addListener((obs, o, n) -> applyFilter());

        btnReset.setOnAction(e -> {
            searchPays.clear();
            comboStatut.getSelectionModel().selectFirst();
            comboSaison.getSelectionModel().selectFirst();
        });
    }

    private void applyFilter() {
        if (allDestinations == null) return;
        String pays   = searchPays.getText() == null ? "" : searchPays.getText().toLowerCase().trim();
        String statut = comboStatut.getValue();
        String saison = comboSaison.getValue();

        List<Destination> filtered = allDestinations.stream()
                .filter(d -> {
                    boolean matchPays   = pays.isEmpty()
                            || d.getPays().toLowerCase().contains(pays)
                            || d.getNom().toLowerCase().contains(pays);
                    boolean matchStatut = statut == null || statut.equals("Tous")
                            || (statut.equals("Actif")   &&  d.getStatut())
                            || (statut.equals("Inactif") && !d.getStatut());
                    boolean matchSaison = saison == null || saison.equals("Toutes les saisons")
                            || saison.equalsIgnoreCase(d.getMeilleureSaison());
                    return matchPays && matchStatut && matchSaison;
                })
                .collect(Collectors.toList());

        filteredList.setAll(filtered);
    }

    // ══════════════════════════════════════════════════════
    private void applyCss(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/app/destination.css")).toExternalForm());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}