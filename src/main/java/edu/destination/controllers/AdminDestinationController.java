package edu.destination.controllers;

import edu.destination.entities.Destination;
import edu.destination.services.DestinationService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdminDestinationController {

    // Table des destinations
    @FXML private TableView<Destination> tableDestinations;
    @FXML private TableColumn<Destination, Integer> colId;
    @FXML private TableColumn<Destination, String> colNom;
    @FXML private TableColumn<Destination, String> colPays;
    @FXML private TableColumn<Destination, String> colDescription;
    @FXML private TableColumn<Destination, Boolean> colStatut;
    @FXML private TableColumn<Destination, String> colSaison;
    @FXML private TableColumn<Destination, Double> colLatitude;
    @FXML private TableColumn<Destination, Double> colLongitude;
    @FXML private TableColumn<Destination, Integer> colNbVisites;
    @FXML private TableColumn<Destination, Double> colPrix;
    @FXML private TableColumn<Destination, String> colDateDepart;
    @FXML private TableColumn<Destination, String> colDateArrivee;

    // Actions table
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;

    // ==============================
    // FXML — Recherche (à ajouter dans le FXML)
    // ==============================
    @FXML private TextField searchPays;          // Recherche par pays
    @FXML private ComboBox<String> comboStatut;  // Tous / Actif / Inactif
    @FXML private ComboBox<String> comboSaison;  // Toutes les saisons / ...
    @FXML private DatePicker dateDepart;         // Filtrer par date de départ
    @FXML private Button btnReset;               // Réinitialiser les filtres

    // Sidebar navigation
    @FXML private Button navDashboard, navDestinations, navTransports, navImages, navClient;

    private final DestinationService service = new DestinationService();
    private final ObservableList<Destination> filteredList = FXCollections.observableArrayList();

    // ✅ Liste complète en mémoire — zéro requête SQL supplémentaire
    private List<Destination> allDestinations;

    @FXML
    private void initialize() {

        // Colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("idDestination"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPays.setCellValueFactory(new PropertyValueFactory<>("pays"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colSaison.setCellValueFactory(new PropertyValueFactory<>("meilleureSaison"));
        colLatitude.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        colLongitude.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        colNbVisites.setCellValueFactory(new PropertyValueFactory<>("nbVisites"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colDateDepart.setCellValueFactory(new PropertyValueFactory<>("dateDepart"));
        colDateArrivee.setCellValueFactory(new PropertyValueFactory<>("dateArrivee"));

        tableDestinations.setItems(filteredList);

        loadData();
        setupSearch();

        // Actions table
        btnAjouter.setOnAction(e -> openForm(null));
        btnModifier.setOnAction(e -> {
            Destination selected = tableDestinations.getSelectionModel().getSelectedItem();
            if (selected != null) openForm(selected);
            else showAlert("Veuillez sélectionner une destination à modifier.");
        });
        btnSupprimer.setOnAction(e -> {
            Destination selected = tableDestinations.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.deleteEntity(selected);
                loadData();
                applyFilter();
            } else showAlert("Veuillez sélectionner une destination à supprimer.");
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
        allDestinations = service.getData();
        applyFilter();
    }

    // ==============================
    // RECHERCHE EN MÉMOIRE
    // ==============================
    private void setupSearch() {
        // ComboBox Statut
        comboStatut.getItems().addAll("Tous", "Actif", "Inactif");
        comboStatut.getSelectionModel().selectFirst();

        // ComboBox Saison — peuplé depuis les données
        comboSaison.getItems().add("Toutes les saisons");
        List<String> saisons = allDestinations.stream()
                .map(Destination::getMeilleureSaison)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        comboSaison.getItems().addAll(saisons);
        comboSaison.getSelectionModel().selectFirst();

        // Listeners — filtre en temps réel
        searchPays.textProperty().addListener((obs, o, n) -> applyFilter());
        comboStatut.valueProperty().addListener((obs, o, n) -> applyFilter());
        comboSaison.valueProperty().addListener((obs, o, n) -> applyFilter());
        dateDepart.valueProperty().addListener((obs, o, n) -> applyFilter());

        // Reset
        btnReset.setOnAction(e -> {
            searchPays.clear();
            comboStatut.getSelectionModel().selectFirst();
            comboSaison.getSelectionModel().selectFirst();
            dateDepart.setValue(null);
        });
    }

    private void applyFilter() {
        if (allDestinations == null) return;

        String pays       = searchPays.getText() == null ? "" : searchPays.getText().toLowerCase().trim();
        String statut     = comboStatut.getValue();
        String saison     = comboSaison.getValue();
        LocalDate dateDep = dateDepart.getValue();

        List<Destination> filtered = allDestinations.stream()
                .filter(d -> {
                    // Filtre pays
                    boolean matchPays = pays.isEmpty()
                            || d.getPays().toLowerCase().contains(pays)
                            || d.getNom().toLowerCase().contains(pays);

                    // Filtre statut
                    boolean matchStatut = statut == null || statut.equals("Tous")
                            || (statut.equals("Actif")   &&  d.getStatut())
                            || (statut.equals("Inactif") && !d.getStatut());

                    // Filtre saison
                    boolean matchSaison = saison == null || saison.equals("Toutes les saisons")
                            || saison.equalsIgnoreCase(d.getMeilleureSaison());

                    // Filtre date de départ (exact)
                    boolean matchDate = dateDep == null
                            || dateDep.equals(d.getDateDepart());

                    return matchPays && matchStatut && matchSaison && matchDate;
                })
                .collect(Collectors.toList());

        filteredList.setAll(filtered);
    }

    // ==============================
    // FORMULAIRE
    // ==============================
    private void openForm(Destination destination) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDestinationForm.fxml"));
            Parent formRoot = loader.load();

            Scene formScene = new Scene(formRoot);
            applyCss(formScene);

            Stage stage = new Stage();
            stage.setScene(formScene);
            stage.setTitle(destination == null ? "Ajouter Destination" : "Modifier Destination");

            AdminDestinationFormController controller = loader.getController();
            controller.setDestination(destination);

            stage.showAndWait();

            // Recharger et réappliquer le filtre après modification
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

    private void applyCss(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/app.css"), "app.css introuvable")
                        .toExternalForm()
        );
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}