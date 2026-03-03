package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import entities.Avis;
import services.AvisService;
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

public class UserAvisController implements Initializable {

    @FXML
    private TableView<Avis> tableAvis;
    @FXML
    private TableColumn<Avis, Integer> colNote;
    @FXML
    private TableColumn<Avis, String> colCommentaire;
    @FXML
    private TableColumn<Avis, String> colReponse;
    @FXML
    private TableColumn<Avis, Date> colDateReponse;
    @FXML
    private TableColumn<Avis, Void> colActions;

    @FXML
    private TextField txtRecherche;
    @FXML
    private ComboBox<String> comboNote;

    private AvisService avisService = new AvisService();
    private int currentUserId = tools.SessionManager.getCurrentUserId();
    private boolean isPublicView = false;

    private ObservableList<Avis> listeComplete;
    private FilteredList<Avis> listeFiltree;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();

        // Initialiser les filtres
        comboNote.setItems(FXCollections.observableArrayList("Toutes", "5", "4", "3", "2", "1"));
        comboNote.setValue("Toutes");

        // Ajouter des listeners pour les filtres
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());
        comboNote.valueProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());

        loadData();
    }

    public void setPublicView(boolean isPublic) {
        this.isPublicView = isPublic;
        if (colActions != null) {
            colActions.setVisible(!isPublic);
        }
        loadData();
    }

    private void setupTable() {
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colReponse.setCellValueFactory(new PropertyValueFactory<>("reponseAdmin"));
        colDateReponse.setCellValueFactory(new PropertyValueFactory<>("dateReponse"));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        if (colDateReponse != null) {
            colDateReponse.setCellFactory(column -> new TableCell<Avis, Date>() {
                @Override
                protected void updateItem(Date date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else if (date == null) {
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

        // Actions Column
        if (colActions != null) {
            colActions.setCellFactory(param -> new TableCell<>() {

                private final Button btnEdit = new Button("✏");
                private final Button btnDelete = new Button("🗑");

                {
                    btnEdit.getStyleClass().addAll("iconBtn", "btnEdit");
                    btnDelete.getStyleClass().addAll("iconBtn", "btnDelete");
                    btnEdit.setOnAction(e -> {
                        Avis av = getTableView().getItems().get(getIndex());
                        modifierAvisSpecific(av);
                    });
                    btnDelete.setOnAction(e -> {
                        Avis av = getTableView().getItems().get(getIndex());
                        supprimerAvisSpecific(av);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
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
            List<Avis> list;
            if (isPublicView) {
                list = avisService.getAllEntities();
            } else {
                list = avisService.getByUserId(currentUserId);
            }
            listeComplete = FXCollections.observableArrayList(list);

            // Configuration du filtrage
            listeFiltree = new FilteredList<>(listeComplete, p -> true);

            // Configuration du tri
            SortedList<Avis> listeTrie = new SortedList<>(listeFiltree);
            listeTrie.comparatorProperty().bind(tableAvis.comparatorProperty());

            tableAvis.setItems(listeTrie);
        } catch (SQLException e) {
            AlertHelper.showError("Erreur", "Impossible de charger les avis : " + e.getMessage());
        }
    }

    @FXML
    void appliquerFiltres() {
        if (listeFiltree == null)
            return;

        listeFiltree.setPredicate(avis -> {
            // Filtre par recherche (commentaire)
            String recherche = txtRecherche.getText() == null ? "" : txtRecherche.getText().toLowerCase().trim();
            boolean matchRecherche = recherche.isEmpty()
                    || (avis.getCommentaire() != null && avis.getCommentaire().toLowerCase().contains(recherche));

            // Filtre par note
            String noteSelectionnee = comboNote.getValue();
            boolean matchNote = noteSelectionnee == null
                    || noteSelectionnee.equals("Toutes")
                    || String.valueOf(avis.getNote()).equals(noteSelectionnee);

            return matchRecherche && matchNote;
        });
    }

    @FXML
    void reinitialiser() {
        txtRecherche.clear();
        comboNote.setValue("Toutes");
        appliquerFiltres();
    }

    @FXML
    void ajouterAvis() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user_ajouter_avis.fxml"));
            tableAvis.getScene().setRoot(root);
        } catch (IOException e) {
            AlertHelper.showError("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void modifierAvisSpecific(Avis av) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_modifier_avis.fxml"));
            Parent root = loader.load();
            ModifierAvisController controller = loader.getController();
            controller.setAvis(av);
            tableAvis.getScene().setRoot(root);
        } catch (IOException e) {
            AlertHelper.showError("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void supprimerAvisSpecific(Avis av) {
        if (AlertHelper.showConfirmation("Confirmation", "Êtes-vous sûr de vouloir supprimer cet avis ?")) {
            try {
                avisService.deleteEntity(av.getId());
                listeComplete.remove(av);
                AlertHelper.showSuccess("Succès", "Avis supprimé avec succès !");
            } catch (SQLException e) {
                AlertHelper.showError("Erreur", "Impossible de supprimer : " + e.getMessage());
            }
        }
    }

    @FXML
    void switchToReclamations() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/reclamations/mes_reclamations.fxml"));
            tableAvis.getScene().setRoot(root);
        } catch (IOException e) {
            AlertHelper.showError("Erreur", "Could not switch: " + e.getMessage());
        }
    }

    @FXML
    void switchToAdmin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_layout.fxml"));
            tableAvis.getScene().setRoot(root);
        } catch (IOException e) {
            AlertHelper.showError("Erreur", "Could not switch to Admin: " + e.getMessage());
        }
    }

    @FXML
    void goToMainMenu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/main_menu.fxml"));
            tableAvis.getScene().setRoot(root);
        } catch (IOException e) {
            AlertHelper.showError("Erreur", "Could not go to main menu: " + e.getMessage());
        }
    }

    @FXML
    void ouvrirChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chatbot_widget.fxml"));
            Parent chatbotWidget = loader.load();
            ChatbotController controller = loader.getController();
            controller.setChatbotType("avis");
            Stage stage = new Stage();
            stage.setTitle("Assistant Avis");
            stage.setScene(new Scene(chatbotWidget));
            stage.show();
        } catch (IOException e) {
            AlertHelper.showError("Erreur", "Impossible d'ouvrir le chatbot : " + e.getMessage());
        }
    }

    @FXML
    void retourMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user_layout.fxml"));
            tableAvis.getScene().setRoot(root);
        } catch (IOException e) {
            AlertHelper.showError("Erreur", "Impossible de retourner au menu : " + e.getMessage());
        }
    }
}
