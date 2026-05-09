package controllers;

import entities.Avis;
import entities.Reclamation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.AvisService;
import services.ReclamationService;
import tools.AlertHelper;
import tools.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class MonProfilController implements Initializable {

    // ── Onglets ──
    @FXML private VBox panelAvis;
    @FXML private VBox panelReclamations;
    @FXML private Button btnOngletAvis;
    @FXML private Button btnOngletReclamations;

    // ── Infos utilisateur ──
    @FXML private Label lblNomUtilisateur;
    @FXML private Label lblEmailUtilisateur;
    @FXML private Label lblInitiales;

    // ── Table Avis ──
    @FXML private TableView<Avis> tableAvis;
    @FXML private TableColumn<Avis, Integer> colAvisNote;
    @FXML private TableColumn<Avis, String>  colAvisCommentaire;
    @FXML private TableColumn<Avis, String>  colAvisStatut;
    @FXML private TableColumn<Avis, String>  colAvisReponse;
    @FXML private TableColumn<Avis, Date>    colAvisDate;
    @FXML private TableColumn<Avis, Void>    colAvisActions;
    @FXML private Label lblPageAvis;
    @FXML private Button btnPrevAvis;
    @FXML private Button btnNextAvis;

    // ── Table Réclamations ──
    @FXML private TableView<Reclamation> tableReclamations;
    @FXML private TableColumn<Reclamation, String>  colReclTitre;
    @FXML private TableColumn<Reclamation, String>  colReclPriorite;
    @FXML private TableColumn<Reclamation, String>  colReclStatut;
    @FXML private TableColumn<Reclamation, Date>    colReclDate;
    @FXML private TableColumn<Reclamation, String>  colReclReponse;
    @FXML private TableColumn<Reclamation, Void>    colReclActions;
    @FXML private Label lblPageRecl;
    @FXML private Button btnPrevRecl;
    @FXML private Button btnNextRecl;

    private AvisService avisService = new AvisService();
    private ReclamationService reclamationService = new ReclamationService();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    private List<Avis> listeAvis = new ArrayList<>();
    private List<Reclamation> listeReclamations = new ArrayList<>();
    private static final int PAGE_SIZE = 5;
    private int pageAvis = 1;
    private int pageRecl = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Infos utilisateur
        String username = SessionManager.getUsername();
        String email    = SessionManager.getEmail();
        if (lblNomUtilisateur  != null) lblNomUtilisateur.setText(username);
        if (lblEmailUtilisateur!= null) lblEmailUtilisateur.setText(email);
        if (lblInitiales != null && username != null && username.length() >= 2)
            lblInitiales.setText(("" + username.charAt(0) + username.charAt(username.lastIndexOf(' ') > 0 ? username.lastIndexOf(' ') + 1 : 1)).toUpperCase());

        setupTableAvis();
        setupTableReclamations();
        chargerDonnees();
    }

    // ══════════════ ONGLETS ══════════════

    @FXML void afficherOngletAvis() {
        panelAvis.setVisible(true);  panelAvis.setManaged(true);
        panelReclamations.setVisible(false); panelReclamations.setManaged(false);
        btnOngletAvis.setStyle("-fx-background-color:#c9a24a; -fx-text-fill:white; -fx-font-weight:800; -fx-font-size:13px; -fx-padding:10 24; -fx-background-radius:10 10 0 0; -fx-cursor:hand;");
        btnOngletReclamations.setStyle("-fx-background-color:rgba(255,255,255,0.1); -fx-text-fill:rgba(255,255,255,0.8); -fx-font-weight:700; -fx-font-size:13px; -fx-padding:10 24; -fx-background-radius:10 10 0 0; -fx-cursor:hand;");
    }

    @FXML void afficherOngletReclamations() {
        panelAvis.setVisible(false); panelAvis.setManaged(false);
        panelReclamations.setVisible(true); panelReclamations.setManaged(true);
        btnOngletReclamations.setStyle("-fx-background-color:#c9a24a; -fx-text-fill:white; -fx-font-weight:800; -fx-font-size:13px; -fx-padding:10 24; -fx-background-radius:10 10 0 0; -fx-cursor:hand;");
        btnOngletAvis.setStyle("-fx-background-color:rgba(255,255,255,0.1); -fx-text-fill:rgba(255,255,255,0.8); -fx-font-weight:700; -fx-font-size:13px; -fx-padding:10 24; -fx-background-radius:10 10 0 0; -fx-cursor:hand;");
    }

    // ══════════════ TABLE AVIS ══════════════

    private void setupTableAvis() {
        colAvisNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colAvisNote.setCellFactory(col -> new TableCell<Avis, Integer>() {
            @Override protected void updateItem(Integer note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) { setText(null); setGraphic(null); return; }
                String stars = "★".repeat(note) + "☆".repeat(5 - note);
                Label lbl = new Label(stars);
                lbl.setStyle("-fx-text-fill: #c9a24a; -fx-font-size: 14px;");
                setGraphic(lbl); setText(null);
            }
        });
        colAvisCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colAvisStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colAvisStatut.setCellFactory(col -> new TableCell<Avis, String>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val);
                String color = "En attente".equals(val) ? "#fff4df;-fx-text-fill:#9a6a11;-fx-border-color:#f0d4a0" : "#e8fff0;-fx-text-fill:#1c7a44;-fx-border-color:#b9efcf";
                badge.setStyle("-fx-background-color:" + color + "; -fx-padding:3 10; -fx-background-radius:999; -fx-border-radius:999; -fx-font-size:11px; -fx-font-weight:900;");
                setGraphic(badge); setText(null);
            }
        });
        colAvisReponse.setCellValueFactory(new PropertyValueFactory<>("reponseAdmin"));
        colAvisDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colAvisDate.setCellFactory(col -> new TableCell<Avis, Date>() {
            @Override protected void updateItem(Date d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "—" : sdf.format(d));
            }
        });
        colAvisActions.setCellFactory(col -> new TableCell<Avis, Void>() {
            final Button btnEdit = new Button("✏");
            final Button btnDel  = new Button("🗑");
            final HBox box = new HBox(6, btnEdit, btnDel);
            {
                btnEdit.setStyle("-fx-background-color:#c9a24a; -fx-text-fill:white; -fx-background-radius:7; -fx-padding:5 10; -fx-cursor:hand; -fx-font-size:13px;");
                btnDel.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; -fx-background-radius:7; -fx-padding:5 10; -fx-cursor:hand; -fx-font-size:13px;");
                box.setAlignment(javafx.geometry.Pos.CENTER);
                btnEdit.setOnAction(e -> {
                    Avis a = getTableView().getItems().get(getIndex());
                    modifierAvis(a);
                });
                btnDel.setOnAction(e -> {
                    Avis a = getTableView().getItems().get(getIndex());
                    if (AlertHelper.showConfirmation("Supprimer", "Supprimer cet avis ?")) {
                        try { avisService.deleteEntity(a.getId()); chargerDonnees(); }
                        catch (SQLException ex) { AlertHelper.showError("Erreur", ex.getMessage()); }
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ══════════════ TABLE RÉCLAMATIONS ══════════════

    private void setupTableReclamations() {
        colReclTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colReclPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colReclPriorite.setCellFactory(col -> new TableCell<Reclamation, String>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val);
                String color = "Urgente".equals(val) || "Haute".equals(val)
                    ? "#fff1f0;-fx-text-fill:#c0392b;-fx-border-color:#ffb3ae"
                    : "Moyenne".equals(val)
                    ? "#eff6ff;-fx-text-fill:#1d4ed8;-fx-border-color:#bfdbfe"
                    : "#f0fdf4;-fx-text-fill:#16a34a;-fx-border-color:#bbf7d0";
                badge.setStyle("-fx-background-color:" + color + "; -fx-padding:3 10; -fx-background-radius:999; -fx-border-radius:999; -fx-font-size:11px; -fx-font-weight:900;");
                setGraphic(badge); setText(null);
            }
        });
        colReclStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colReclStatut.setCellFactory(col -> new TableCell<Reclamation, String>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val);
                String color = "Résolue".equals(val) ? "#e8fff0;-fx-text-fill:#1c7a44;-fx-border-color:#b9efcf" : "#fff4df;-fx-text-fill:#9a6a11;-fx-border-color:#f0d4a0";
                badge.setStyle("-fx-background-color:" + color + "; -fx-padding:3 10; -fx-background-radius:999; -fx-border-radius:999; -fx-font-size:11px; -fx-font-weight:900;");
                setGraphic(badge); setText(null);
            }
        });
        colReclDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colReclDate.setCellFactory(col -> new TableCell<Reclamation, Date>() {
            @Override protected void updateItem(Date d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "—" : sdf.format(d));
            }
        });
        colReclReponse.setCellValueFactory(new PropertyValueFactory<>("reponseAdmin"));
        colReclActions.setCellFactory(col -> new TableCell<Reclamation, Void>() {
            final Button btnEdit = new Button("✏");
            final Button btnDel  = new Button("🗑");
            final HBox box = new HBox(6, btnEdit, btnDel);
            {
                btnEdit.setStyle("-fx-background-color:#c9a24a; -fx-text-fill:white; -fx-background-radius:7; -fx-padding:5 10; -fx-cursor:hand; -fx-font-size:13px;");
                btnDel.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; -fx-background-radius:7; -fx-padding:5 10; -fx-cursor:hand; -fx-font-size:13px;");
                box.setAlignment(javafx.geometry.Pos.CENTER);
                btnEdit.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    if (!"En attente".equals(r.getStatut())) {
                        AlertHelper.showInfo("Info", "Impossible de modifier une réclamation " + r.getStatut());
                        return;
                    }
                    modifierReclamation(r);
                });
                btnDel.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    if (AlertHelper.showConfirmation("Supprimer", "Supprimer cette réclamation ?")) {
                        try { reclamationService.deleteEntity(r.getId()); chargerDonnees(); }
                        catch (SQLException ex) { AlertHelper.showError("Erreur", ex.getMessage()); }
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ══════════════ CHARGEMENT DONNÉES ══════════════

    private void chargerDonnees() {
        try {
            listeAvis = avisService.getByUserId(SessionManager.getCurrentUserId());
            pageAvis = 1;
            afficherPageAvis();
        } catch (SQLException e) { AlertHelper.showError("Erreur", e.getMessage()); }
        try {
            listeReclamations = reclamationService.getByUserId(SessionManager.getCurrentUserId());
            pageRecl = 1;
            afficherPageRecl();
        } catch (SQLException e) { AlertHelper.showError("Erreur", e.getMessage()); }
    }

    private void afficherPageAvis() {
        int total = listeAvis.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int from = (pageAvis - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);
        tableAvis.setItems(FXCollections.observableArrayList(listeAvis.subList(from, to)));
        if (lblPageAvis != null) lblPageAvis.setText("Page " + pageAvis + " / " + totalPages);
        if (btnPrevAvis != null) btnPrevAvis.setDisable(pageAvis <= 1);
        if (btnNextAvis != null) btnNextAvis.setDisable(pageAvis >= totalPages);
    }

    private void afficherPageRecl() {
        int total = listeReclamations.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int from = (pageRecl - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);
        tableReclamations.setItems(FXCollections.observableArrayList(listeReclamations.subList(from, to)));
        if (lblPageRecl != null) lblPageRecl.setText("Page " + pageRecl + " / " + totalPages);
        if (btnPrevRecl != null) btnPrevRecl.setDisable(pageRecl <= 1);
        if (btnNextRecl != null) btnNextRecl.setDisable(pageRecl >= totalPages);
    }

    @FXML void pagePrevAvis() { if (pageAvis > 1) { pageAvis--; afficherPageAvis(); } }
    @FXML void pageNextAvis() {
        int tp = Math.max(1,(int)Math.ceil((double)listeAvis.size()/PAGE_SIZE));
        if (pageAvis < tp) { pageAvis++; afficherPageAvis(); }
    }
    @FXML void pagePrevRecl() { if (pageRecl > 1) { pageRecl--; afficherPageRecl(); } }
    @FXML void pageNextRecl() {
        int tp = Math.max(1,(int)Math.ceil((double)listeReclamations.size()/PAGE_SIZE));
        if (pageRecl < tp) { pageRecl++; afficherPageRecl(); }
    }

    // ══════════════ ACTIONS ══════════════

    @FXML void ajouterAvis() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user_ajouter_avis.fxml"));
            tableAvis.getScene().setRoot(root);
        } catch (IOException e) { AlertHelper.showError("Erreur", e.getMessage()); }
    }

    @FXML void ajouterReclamation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user_ajouter_reclamation.fxml"));
            tableReclamations.getScene().setRoot(root);
        } catch (IOException e) { AlertHelper.showError("Erreur", e.getMessage()); }
    }

    private void modifierAvis(Avis a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_modifier_avis.fxml"));
            Parent root = loader.load();
            ModifierAvisController ctrl = loader.getController();
            ctrl.setAvis(a);
            tableAvis.getScene().setRoot(root);
        } catch (IOException e) { AlertHelper.showError("Erreur", e.getMessage()); }
    }

    private void modifierReclamation(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_modifier_reclamation.fxml"));
            Parent root = loader.load();
            ModifierReclamationController ctrl = loader.getController();
            ctrl.setReclamation(r);
            tableReclamations.getScene().setRoot(root);
        } catch (IOException e) { AlertHelper.showError("Erreur", e.getMessage()); }
    }

    @FXML void ouvrirChatbotAvis() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/chatbot.fxml"));
            panelAvis.getScene().setRoot(root);
        } catch (IOException e) {
            AlertHelper.showError("Chatbot", "Assistant IA non disponible: " + e.getMessage());
        }
    }

    @FXML void ouvrirChatbotReclamation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/chatbot.fxml"));
            panelReclamations.getScene().setRoot(root);
        } catch (IOException e) {
            AlertHelper.showError("Chatbot", "Assistant IA non disponible: " + e.getMessage());
        }
    }

    @FXML void retourMenu() {
        hebergement.controllers.ClientLayoutController layout =
            hebergement.controllers.ClientLayoutController.getInstance();
        if (layout != null) {
            layout.goDestination();
        }
    }
}
