package controllers;

import entities.Avis;
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
import services.AvisService;
import tools.AlertHelper;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

public class AdminAvisController implements Initializable {

    @FXML
    private TableView<Avis> tableAvis;
    @FXML
    private TableColumn<Avis, Integer> colId;
    @FXML
    private TableColumn<Avis, Integer> colUser;
    @FXML
    private TableColumn<Avis, Integer> colNote;
    @FXML
    private TableColumn<Avis, String> colCommentaire;
    @FXML
    private TableColumn<Avis, String> colReponse;
    @FXML
    private TableColumn<Avis, Date> colDateReponse;

    @FXML
    private TextArea txtReponse;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> comboFilter;

    private AvisService avisService = new AvisService();
    private ObservableList<Avis> listeComplete;
    private FilteredList<Avis> listeFiltree;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();

        // Initialiser les filtres
        comboFilter.setItems(FXCollections.observableArrayList("Toutes", "5 étoiles", "4 étoiles", "3 étoiles",
                "2 étoiles", "1 étoile"));
        comboFilter.setValue("Toutes");

        // Ajouter des listeners pour le filtrage en temps réel
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        comboFilter.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());

        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colReponse.setCellValueFactory(new PropertyValueFactory<>("reponseAdmin"));
        colDateReponse.setCellValueFactory(new PropertyValueFactory<>("dateReponse"));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        colDateReponse.setCellFactory(column -> new TableCell<Avis, Date>() {
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
    }

    private void loadData() {
        try {
            List<Avis> list = avisService.getAllEntities();
            listeComplete = FXCollections.observableArrayList(list);

            listeFiltree = new FilteredList<>(listeComplete, p -> true);
            SortedList<Avis> listeTrie = new SortedList<>(listeFiltree);
            listeTrie.comparatorProperty().bind(tableAvis.comparatorProperty());

            tableAvis.setItems(listeTrie);
        } catch (SQLException e) {
            AlertHelper.showError("Erreur", "Impossible de charger les données : " + e.getMessage());
        }
    }

    private void appliquerFiltres() {
        if (listeFiltree == null)
            return;

        listeFiltree.setPredicate(avis -> {
            // Filtre par recherche
            String search = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
            boolean matchSearch = search.isEmpty()
                    || (avis.getCommentaire() != null && avis.getCommentaire().toLowerCase().contains(search))
                    || (avis.getReponseAdmin() != null && avis.getReponseAdmin().toLowerCase().contains(search));

            // Filtre par note
            String statusFilter = comboFilter.getValue();
            boolean matchNote = true;
            if (statusFilter != null && !statusFilter.equals("Toutes")) {
                int noteAttendue = Integer.parseInt(statusFilter.substring(0, 1));
                matchNote = (avis.getNote() == noteAttendue);
            }

            return matchSearch && matchNote;
        });
    }

    @FXML
    void validerReponse() {
        Avis selected = tableAvis.getSelectionModel().getSelectedItem();
        String reponse = txtReponse.getText().trim();

        if (selected == null) {
            AlertHelper.showWarning("Attention", "Veuillez sélectionner un avis dans le tableau.");
            return;
        }

        if (reponse.isEmpty()) {
            AlertHelper.showWarning("Attention", "Veuillez saisir une réponse.");
            txtReponse.requestFocus();
            return;
        }

        if (AlertHelper.showConfirmation("Confirmation", "Mettre à jour la réponse pour cet avis ?")) {
            try {
                avisService.repondreAvis(selected.getId(), reponse);
                selected.setReponseAdmin(reponse);
                selected.setDateReponse(new Date()); // Update local object

                txtReponse.clear();
                tableAvis.refresh();
                AlertHelper.showSuccess("Succès", "Réponse mise à jour avec succès !");
            } catch (SQLException e) {
                AlertHelper.showError("Erreur", "Impossible de mettre à jour la réponse : " + e.getMessage());
            }
        }
    }

    @FXML
    void clearSearch() {
        txtSearch.clear();
        comboFilter.setValue("Toutes");
        appliquerFiltres();
    }
    @FXML
    private void retourMenu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_reclamations.fxml"));
            // remplacez "txtSearch" par n'importe quel fx:id qui existe dans AdminAvisController
            txtSearch.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
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
    void applySorting() {
        // Tri géré automatiquement par SortedList et les en-têtes de colonnes
    }
}
