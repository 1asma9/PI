package controllers;

import entities.Reclamation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ReclamationService;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import tools.AlertHelper;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class UserReclamationsController implements Initializable {

    @FXML private TableView<Reclamation> tableReclamations;
    @FXML private TableColumn<Reclamation, String> colTitre;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colStatut;
    @FXML private TableColumn<Reclamation, Date> colDate;
    @FXML private TableColumn<Reclamation, Date> colDateReponse;
    @FXML private TableColumn<Reclamation, Void> colActions;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboStatut;
    @FXML private Label lblPageTitle;
    @FXML private Button btnAdd;

    private ReclamationService reclamationService = new ReclamationService();
    private int currentUserId = tools.SessionManager.getCurrentUserId();

    private ObservableList<Reclamation> listeComplete;
    private FilteredList<Reclamation> listeFiltree;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        comboStatut.setItems(FXCollections.observableArrayList("Tous", "En attente", "Traitée"));
        comboStatut.setValue("Tous");
        loadData();
    }

    private void setupTable() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDateReponse.setCellValueFactory(new PropertyValueFactory<>("dateReponse"));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        if (colDate != null) {
            colDate.setCellFactory(column -> new TableCell<Reclamation, Date>() {
                @Override
                protected void updateItem(Date date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) setText(null);
                    else setText(dateFormat.format(date));
                }
            });
        }

        if (colDateReponse != null) {
            colDateReponse.setCellFactory(column -> new TableCell<Reclamation, Date>() {
                @Override
                protected void updateItem(Date date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty) { setText(null); setGraphic(null); }
                    else if (date == null) {
                        setText("En attente...");
                        setStyle("-fx-text-fill: #6a7a73; -fx-font-style: italic;");
                        setGraphic(null);
                    } else {
                        setText(dateFormat.format(date));
                        setStyle("-fx-text-fill: #0f2a2a; -fx-font-weight: bold;");
                        Label icon = new Label("✓ ");
                        icon.setStyle("-fx-text-fill: #c9a24a;");
                        setGraphic(icon);
                    }
                }
            });
        }

        if (colStatut != null) {
            colStatut.setCellFactory(param -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); }
                    else {
                        Label badge = new Label(item);
                        badge.getStyleClass().add("statusBadge");
                        if (item.equals("En attente")) badge.getStyleClass().add("status-En_attente");
                        else if (item.equals("Traitée") || item.equals("Traitee")) badge.getStyleClass().add("status-Traitee");
                        setGraphic(badge);
                    }
                }
            });
        }

        if (colActions != null) {
            colActions.setCellFactory(param -> new TableCell<>() {
                private final Button btnEdit = new Button("✏");
                private final Button btnDelete = new Button("🗑");
                {
                    btnEdit.getStyleClass().addAll("iconBtn", "btnEdit");
                    btnDelete.getStyleClass().addAll("iconBtn", "btnDelete");
                    btnEdit.setOnAction(e -> modifierReclamationSpecific(getTableView().getItems().get(getIndex())));
                    btnDelete.setOnAction(e -> supprimerReclamationSpecific(getTableView().getItems().get(getIndex())));
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); }
                    else {
                        HBox hbox = new HBox(8, btnEdit, btnDelete);
                        hbox.setAlignment(javafx.geometry.Pos.CENTER);
                        setGraphic(hbox);
                    }
                }
            });
        }
    }

    private void loadData() {
        try {
            List<Reclamation> list = reclamationService.getByUserId(currentUserId);
            listeComplete = FXCollections.observableArrayList(list);
            listeFiltree = new FilteredList<>(listeComplete, p -> true);
            SortedList<Reclamation> listeTrie = new SortedList<>(listeFiltree);
            listeTrie.comparatorProperty().bind(tableReclamations.comparatorProperty());
            tableReclamations.setItems(listeTrie);
        } catch (SQLException e) {
            AlertHelper.showError("Erreur", "Impossible de charger les réclamations : " + e.getMessage());
        }
    }

    @FXML
    void appliquerFiltres() {
        if (listeFiltree == null) return;
        listeFiltree.setPredicate(reclamation -> {
            String recherche = txtRecherche.getText() == null ? "" : txtRecherche.getText().toLowerCase().trim();
            boolean matchRecherche = recherche.isEmpty()
                    || (reclamation.getTitre() != null && reclamation.getTitre().toLowerCase().contains(recherche))
                    || (reclamation.getDescription() != null && reclamation.getDescription().toLowerCase().contains(recherche));
            String statutSelectionne = comboStatut.getValue();
            boolean matchStatut = statutSelectionne == null || statutSelectionne.equals("Tous")
                    || (reclamation.getStatut() != null && reclamation.getStatut().equals(statutSelectionne));
            return matchRecherche && matchStatut;
        });
    }

    @FXML
    void reinitialiser() {
        txtRecherche.clear();
        comboStatut.setValue("Tous");
        appliquerFiltres();
    }

    @FXML
    void ajouterReclamation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user_ajouter_reclamation.fxml"));
            tableReclamations.getScene().setRoot(root);
        } catch (IOException e) {
            AlertHelper.showError("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void modifierReclamationSpecific(Reclamation rec) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_modifier_reclamation.fxml"));
            Parent root = loader.load();
            ModifierReclamationController controller = loader.getController();
            controller.setReclamation(rec);
            tableReclamations.getScene().setRoot(root);
        } catch (IOException e) {
            AlertHelper.showError("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void supprimerReclamationSpecific(Reclamation rec) {
        if (AlertHelper.showConfirmation("Confirmation", "Êtes-vous sûr de vouloir supprimer cette réclamation ?")) {
            try {
                reclamationService.deleteEntity(rec.getId());
                listeComplete.remove(rec);
                AlertHelper.showSuccess("Succès", "Réclamation supprimée avec succès !");
            } catch (SQLException e) {
                AlertHelper.showError("Erreur", "Impossible de supprimer : " + e.getMessage());
            }
        }
    }

    @FXML
    void retourMenu(ActionEvent event) {
        // ✅ CORRIGÉ : utilise ClientLayoutController pour naviguer vers Destinations
        hebergement.controllers.ClientLayoutController layout =
                hebergement.controllers.ClientLayoutController.getInstance();
        if (layout != null) {
            layout.goDestination();
        } else {
            AlertHelper.showError("Erreur", "Layout introuvable.");
        }
    }

    @FXML
    void ouvrirChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chatbot_widget.fxml"));
            Parent chatbotWidget = loader.load();
            ChatbotController controller = loader.getController();
            controller.setChatbotType("reclamation");
            Stage stage = new Stage();
            stage.setTitle("Assistant Réclamations");
            stage.setScene(new Scene(chatbotWidget));
            stage.show();
        } catch (IOException e) {
            AlertHelper.showError("Erreur", "Impossible d'ouvrir le chatbot : " + e.getMessage());
        }
    }
}