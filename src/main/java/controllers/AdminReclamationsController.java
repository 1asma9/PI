package controllers;

import entities.Reclamation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.ReclamationService;
import tools.AlertHelper;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

public class AdminReclamationsController implements Initializable {

    @FXML
    private TableView<Reclamation> tableReclamations;
    @FXML
    private TableColumn<Reclamation, Integer> colId;
    @FXML
    private TableColumn<Reclamation, Integer> colUser;
    @FXML
    private TableColumn<Reclamation, String> colTitre;
    @FXML
    private TableColumn<Reclamation, String> colDescription;
    @FXML
    private TableColumn<Reclamation, String> colStatut;
    @FXML
    private TableColumn<Reclamation, String> colReponse;
    @FXML
    private TableColumn<Reclamation, Date> colDateReponse;
    @FXML
    private TableColumn<Reclamation, Void> colActions;

    @FXML
    private TextArea txtReponse;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> comboFilter;

    private ReclamationService reclamationService = new ReclamationService();
    private ObservableList<Reclamation> listeComplete;
    private FilteredList<Reclamation> listeFiltree;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();

        // Initialiser le filtre de statut
        comboFilter.setItems(FXCollections.observableArrayList("Tous", "En_attente", "Traitee"));
        comboFilter.setValue("Tous");

        // Ajouter des listeners pour le filtrage en temps réel
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        comboFilter.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());

        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colReponse.setCellValueFactory(new PropertyValueFactory<>("reponseAdmin"));
        colDateReponse.setCellValueFactory(new PropertyValueFactory<>("dateReponse"));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        colDateReponse.setCellFactory(column -> new TableCell<Reclamation, Date>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (date == null) {
                    setText("Non répondu");
                    setStyle("-fx-text-fill: #6a7a73; -fx-font-style: italic;");
                    setGraphic(new Label("⏳ "));
                } else {
                    setText(dateFormat.format(date));
                    setStyle("-fx-text-fill: #0f2a2a; -fx-font-weight: bold;");
                    setGraphic(new Label("✓ "));
                }
            }
        });

        // Status Column with Badges
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item.replace("_", " "));
                    label.getStyleClass().add("statusBadge");
                    if (item.equalsIgnoreCase("En_attente")) {
                        label.getStyleClass().add("status-En_attente");
                    } else if (item.equalsIgnoreCase("Traitee")) {
                        label.getStyleClass().add("status-Traitee");
                    }
                    setGraphic(label);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        // Actions Column for Status Change
        colActions.setCellFactory(param -> new TableCell<>() {
            private final ComboBox<String> comboStatus = new ComboBox<>(
                    FXCollections.observableArrayList("En_attente", "Traitee"));
            {
                comboStatus.setPrefWidth(120);
                comboStatus.setOnAction(e -> {
                    Reclamation rec = getTableView().getItems().get(getIndex());
                    String newStatus = comboStatus.getValue();
                    if (rec != null && newStatus != null && !newStatus.equals(rec.getStatut())) {
                        changerStatut(rec, newStatus);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reclamation rec = getTableView().getItems().get(getIndex());
                    if (rec != null) {
                        comboStatus.setValue(rec.getStatut());
                        setGraphic(comboStatus);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadData() {
        try {
            List<Reclamation> list = reclamationService.getAllEntities();
            listeComplete = FXCollections.observableArrayList(list);

            listeFiltree = new FilteredList<>(listeComplete, p -> true);
            SortedList<Reclamation> listeTrie = new SortedList<>(listeFiltree);
            listeTrie.comparatorProperty().bind(tableReclamations.comparatorProperty());

            tableReclamations.setItems(listeTrie);
        } catch (SQLException e) {
            AlertHelper.showError("Erreur", "Impossible de charger les données : " + e.getMessage());
        }
    }

    private void appliquerFiltres() {
        if (listeFiltree == null)
            return;

        listeFiltree.setPredicate(rec -> {
            // Filtre par recherche
            String search = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
            boolean matchSearch = search.isEmpty()
                    || String.valueOf(rec.getId()).contains(search)
                    || (rec.getTitre() != null && rec.getTitre().toLowerCase().contains(search))
                    || (rec.getDescription() != null && rec.getDescription().toLowerCase().contains(search));

            // Filtre par statut
            String statusFilter = comboFilter.getValue();
            boolean matchStatus = statusFilter == null || statusFilter.equals("Tous")
                    || rec.getStatut().equals(statusFilter);

            return matchSearch && matchStatus;
        });
    }

    private void changerStatut(Reclamation rec, String newStatus) {
        try {
            // Ici on réutilise la méthode du service si elle existe, ou on en crée une
            // Pour l'instant, on met à jour localement et on tente un update
            rec.setStatut(newStatus);
            reclamationService.updateEntity(rec);
            AlertHelper.showSuccess("Succès", "Statut mis à jour pour la réclamation #" + rec.getId());
            tableReclamations.refresh();
        } catch (SQLException e) {
            AlertHelper.showError("Erreur", "Impossible de changer le statut : " + e.getMessage());
            loadData(); // Recharger pour annuler le changement visuel
        }
    }

    @FXML
    void validerReponse() {
        Reclamation selected = tableReclamations.getSelectionModel().getSelectedItem();
        String reponse = txtReponse.getText().trim();

        if (selected == null) {
            AlertHelper.showWarning("Attention", "Veuillez sélectionner une réclamation dans le tableau.");
            return;
        }

        if (reponse.isEmpty()) {
            AlertHelper.showWarning("Attention", "Veuillez saisir une réponse.");
            txtReponse.requestFocus();
            return;
        }

        if (AlertHelper.showConfirmation("Confirmation", "Envoyer cette réponse à l'utilisateur ?")) {
            try {
                reclamationService.repondreReclamation(selected.getId(), reponse);
                selected.setReponseAdmin(reponse);
                selected.setStatut("Traitee");
                selected.setDateReponse(new Date()); // Update local object for immediate display

                reclamationService.updateEntity(selected);

                txtReponse.clear();
                tableReclamations.refresh();
                AlertHelper.showSuccess("Succès", "Réponse envoyée avec succès !");
            } catch (SQLException e) {
                AlertHelper.showError("Erreur", "Impossible d'envoyer la réponse : " + e.getMessage());
            }
        }
    }

    @FXML
    void retourMenu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_menu.fxml"));
            tableReclamations.getScene().setRoot(root);
        } catch (IOException e) {
            AlertHelper.showError("Erreur", "Impossible de retourner au menu : " + e.getMessage());
        }
    }

    @FXML
    void searchData() {
        appliquerFiltres();
    }

    @FXML
    void applyFilter() {
        appliquerFiltres();
    }

    @FXML
    void clearSearch() {
        txtSearch.clear();
        comboFilter.setValue("Tous");
        appliquerFiltres();
    }
}
