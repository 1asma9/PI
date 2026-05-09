package controllers;

import entities.Reclamation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.ReclamationService;
import tools.AlertHelper;
import tools.MyConnection;
import tools.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdminReclamationsController implements Initializable {

    @FXML private TableView<Reclamation> tableReclamations;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, Integer> colUser;
    @FXML private TableColumn<Reclamation, String>  colTitre;
    @FXML private TableColumn<Reclamation, String>  colDescription;
    @FXML private TableColumn<Reclamation, String>  colStatut;
    @FXML private TableColumn<Reclamation, String>  colReponse;
    @FXML private TableColumn<Reclamation, String>  colPriorite;
    @FXML private TableColumn<Reclamation, String>  colAvisLie;
    @FXML private TableColumn<Reclamation, Date>    colDate;
    @FXML private TableColumn<Reclamation, Void>    colActions;

    @FXML private TextArea         txtReponse;
    @FXML private TextField        txtSearch;
    @FXML private ComboBox<String> comboFilter;
    @FXML private ComboBox<String> comboPriorite;
    @FXML private ComboBox<String> comboTri;
    @FXML private Label            lblCount;
    @FXML private Label            lblPageInfo;
    @FXML private Button           btnPrev;
    @FXML private Button           btnNext;
    @FXML private Button           btn1;

    private ReclamationService reclamationService = new ReclamationService();
    private List<Reclamation>  listeComplete  = new ArrayList<>();
    private List<Reclamation>  listeFiltree   = new ArrayList<>();
    private static final int   PAGE_SIZE = 10;
    private int currentPage = 1;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboFilter.setItems(FXCollections.observableArrayList("Tous les statuts", "En attente", "Résolue", "Rejetée", "Traitée"));
        comboFilter.setValue("Tous les statuts");
        comboPriorite.setItems(FXCollections.observableArrayList("Toutes priorités", "Basse", "Moyenne", "Haute", "Urgente"));
        comboPriorite.setValue("Toutes priorités");
        comboTri.setItems(FXCollections.observableArrayList("Trier par...", "Date ↓", "Date ↑", "Priorité", "Statut"));
        comboTri.setValue("Trier par...");

        txtSearch.textProperty().addListener((o, a, b) -> appliquerFiltres());
        comboFilter.valueProperty().addListener((o, a, b) -> appliquerFiltres());
        comboPriorite.valueProperty().addListener((o, a, b) -> appliquerFiltres());
        comboTri.valueProperty().addListener((o, a, b) -> appliquerFiltres());

        setupTable();
        loadData();
    }

    private void setupTable() {
        // Style table dark
        tableReclamations.setStyle("-fx-background-color:#1f2937; -fx-text-fill:white;");

        // #ID
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setCellFactory(col -> new TableCell<Reclamation, Integer>() {
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                setStyle("-fx-background-color:#1f2937; -fx-alignment:CENTER;");
                if (empty || v == null) { setText(null); return; }
                Label lbl = new Label("#" + v);
                lbl.setStyle("-fx-background-color:#3730a3; -fx-text-fill:white; -fx-padding:3 10; -fx-background-radius:8; -fx-font-weight:900; -fx-font-size:12px;");
                setGraphic(lbl); setText(null);
            }
        });

        // CLIENT (userId → nom)
        colUser.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUser.setCellFactory(col -> new TableCell<Reclamation, Integer>() {
            @Override protected void updateItem(Integer userId, boolean empty) {
                super.updateItem(userId, empty);
                setStyle("-fx-background-color:#1f2937; -fx-alignment:CENTER;");
                if (empty || userId == null) { setText(null); return; }
                String nom = getNomUser(userId);
                HBox box = new HBox(6);
                box.setAlignment(Pos.CENTER_LEFT);
                Label avatar = new Label("👤");
                avatar.setStyle("-fx-font-size:14px;");
                Label name = new Label(nom);
                name.setStyle("-fx-text-fill:#d1d5db; -fx-font-size:12px;");
                box.getChildren().addAll(avatar, name);
                setGraphic(box); setText(null);
            }
        });

        // TITRE
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colTitre.setCellFactory(col -> new TableCell<Reclamation, String>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setStyle("-fx-background-color:#1f2937; -fx-text-fill:#f9fafb; -fx-font-weight:600;");
                setText(empty || v == null ? null : v.length() > 30 ? v.substring(0,30)+"..." : v);
            }
        });

        // CONTENU
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDescription.setCellFactory(col -> new TableCell<Reclamation, String>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setStyle("-fx-background-color:#1f2937; -fx-text-fill:#9ca3af; -fx-font-size:12px;");
                setText(empty || v == null ? null : v.length() > 40 ? v.substring(0,40)+"..." : v);
            }
        });

        // DATE
        if (colDate != null) {
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
            colDate.setCellFactory(col -> new TableCell<Reclamation, Date>() {
                @Override protected void updateItem(Date d, boolean empty) {
                    super.updateItem(d, empty);
                    setStyle("-fx-background-color:#1f2937; -fx-text-fill:#6b7280; -fx-alignment:CENTER;");
                    setText(empty || d == null ? "—" : sdf.format(d));
                }
            });
        }

        // PRIORITÉ badge
        if (colPriorite != null) {
            colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
            colPriorite.setCellFactory(col -> new TableCell<Reclamation, String>() {
                @Override protected void updateItem(String v, boolean empty) {
                    super.updateItem(v, empty);
                    setStyle("-fx-background-color:#1f2937; -fx-alignment:CENTER;");
                    if (empty || v == null) { setGraphic(null); return; }
                    Label badge = new Label(v);
                    if ("Urgente".equals(v))
                        badge.setStyle("-fx-background-color:rgba(239,68,68,0.15);-fx-text-fill:#ef4444;-fx-border-color:#ef4444;-fx-border-width:1;-fx-padding:3 10;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                    else if ("Haute".equals(v))
                        badge.setStyle("-fx-background-color:rgba(245,158,11,0.15);-fx-text-fill:#f59e0b;-fx-border-color:#f59e0b;-fx-border-width:1;-fx-padding:3 10;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                    else if ("Moyenne".equals(v))
                        badge.setStyle("-fx-background-color:rgba(59,130,246,0.15);-fx-text-fill:#60a5fa;-fx-border-color:#60a5fa;-fx-border-width:1;-fx-padding:3 10;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                    else
                        badge.setStyle("-fx-background-color:rgba(107,114,128,0.15);-fx-text-fill:#9ca3af;-fx-border-color:#9ca3af;-fx-border-width:1;-fx-padding:3 10;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                    setGraphic(badge); setText(null);
                }
            });
        }

        // STATUT avec ComboBox inline
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(col -> new TableCell<Reclamation, String>() {
            final ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(
                "En attente", "Résolue", "Rejetée", "Traitée"));
            {
                combo.setStyle("-fx-background-color:#374151; -fx-border-color:#4b5563; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:12px;");
                combo.setPrefWidth(130);
                combo.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    if (r != null && combo.getValue() != null) {
                        try {
                            r.setStatut(combo.getValue());
                            reclamationService.updateEntity(r);
                            tableReclamations.refresh();
                        } catch (Exception ex) { AlertHelper.showError("Erreur", ex.getMessage()); }
                    }
                });
            }
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setStyle("-fx-background-color:#1f2937; -fx-alignment:CENTER;");
                if (empty) { setGraphic(null); return; }
                combo.setValue(v);
                setGraphic(combo);
            }
        });

        // AVIS LIÉ
        if (colAvisLie != null) {
            colAvisLie.setCellFactory(col -> new TableCell<Reclamation, String>() {
                @Override protected void updateItem(String v, boolean empty) {
                    super.updateItem(v, empty);
                    setStyle("-fx-background-color:#1f2937; -fx-alignment:CENTER;");
                    if (empty) { setGraphic(null); return; }
                    Reclamation r = getTableView().getItems().get(getIndex());
                    // Vérifier si c'est une réclamation auto générée depuis un avis
                    if (r != null && r.getTitre() != null && r.getTitre().startsWith("Avis négatif")) {
                        Label lbl = new Label("Auto ⭐");
                        lbl.setStyle("-fx-text-fill:#f59e0b; -fx-font-size:11px; -fx-font-weight:700;");
                        setGraphic(lbl);
                    } else {
                        Label lbl = new Label("Manuel");
                        lbl.setStyle("-fx-text-fill:#6b7280; -fx-font-size:11px;");
                        setGraphic(lbl);
                    }
                    setText(null);
                }
            });
        }

        // RÉPONSE
        colReponse.setCellValueFactory(new PropertyValueFactory<>("reponseAdmin"));
        colReponse.setCellFactory(col -> new TableCell<Reclamation, String>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setStyle("-fx-background-color:#1f2937;");
                if (empty || v == null || v.isEmpty()) {
                    Label dash = new Label("—");
                    dash.setStyle("-fx-text-fill:#4b5563;");
                    setGraphic(dash); setText(null); return;
                }
                Label lbl = new Label("💬 " + (v.length() > 25 ? v.substring(0,25)+"..." : v));
                lbl.setStyle("-fx-text-fill:#34d399; -fx-font-size:12px; -fx-font-style:italic;");
                setGraphic(lbl); setText(null);
            }
        });

        // ACTIONS
        colActions.setCellFactory(col -> new TableCell<Reclamation, Void>() {
            final Button btnView = new Button("👁");
            final Button btnReply = new Button("💬");
            final Button btnDel  = new Button("🗑");
            final HBox box = new HBox(4, btnView, btnReply, btnDel);
            {
                btnView.setStyle("-fx-background-color:#1d4ed8; -fx-text-fill:white; -fx-background-radius:6; -fx-padding:5 8; -fx-cursor:hand; -fx-font-size:12px;");
                btnReply.setStyle("-fx-background-color:#7c3aed; -fx-text-fill:white; -fx-background-radius:6; -fx-padding:5 8; -fx-cursor:hand; -fx-font-size:12px;");
                btnDel.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white; -fx-background-radius:6; -fx-padding:5 8; -fx-cursor:hand; -fx-font-size:12px;");
                box.setAlignment(Pos.CENTER);
                btnView.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    AlertHelper.showInfo("Détails #" + r.getId(),
                        "👤 User ID: " + r.getUserId() + "\n" +
                        "📌 Titre: " + r.getTitre() + "\n" +
                        "🎯 Priorité: " + r.getPriorite() + "\n" +
                        "📋 Statut: " + r.getStatut() + "\n\n" +
                        "📝 Description:\n" + r.getDescription() +
                        (r.getReponseAdmin() != null && !r.getReponseAdmin().isEmpty()
                            ? "\n\n✅ Réponse:\n" + r.getReponseAdmin() : ""));
                });
                btnReply.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    tableReclamations.getSelectionModel().select(r);
                    if (txtReponse != null) {
                        txtReponse.requestFocus();
                        if (r.getReponseAdmin() != null) txtReponse.setText(r.getReponseAdmin());
                    }
                });
                btnDel.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    if (AlertHelper.showConfirmation("Supprimer", "Supprimer la réclamation #" + r.getId() + " ?")) {
                        try { reclamationService.deleteEntity(r.getId()); loadData(); }
                        catch (Exception ex) { AlertHelper.showError("Erreur", ex.getMessage()); }
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setStyle("-fx-background-color:#1f2937; -fx-alignment:CENTER;");
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadData() {
        try {
            listeComplete = reclamationService.getAllEntities();
            appliquerFiltres();
        } catch (SQLException e) {
            AlertHelper.showError("Erreur", e.getMessage());
        }
    }

    private void appliquerFiltres() {
        String search   = txtSearch != null ? txtSearch.getText().toLowerCase().trim() : "";
        String statut   = comboFilter != null ? comboFilter.getValue() : "Tous les statuts";
        String priorite = comboPriorite != null ? comboPriorite.getValue() : "Toutes priorités";
        String tri      = comboTri != null ? comboTri.getValue() : "";

        listeFiltree = new ArrayList<>();
        for (Reclamation r : listeComplete) {
            boolean ok = true;
            if (!search.isEmpty())
                ok = (String.valueOf(r.getId()).contains(search))
                  || (r.getTitre() != null && r.getTitre().toLowerCase().contains(search))
                  || (r.getDescription() != null && r.getDescription().toLowerCase().contains(search));
            if (ok && !"Tous les statuts".equals(statut) && statut != null)
                ok = statut.equals(r.getStatut());
            if (ok && !"Toutes priorités".equals(priorite) && priorite != null)
                ok = priorite.equals(r.getPriorite());
            if (ok) listeFiltree.add(r);
        }

        // Tri
        if ("Date ↓".equals(tri)) listeFiltree.sort((a,b) -> b.getDateCreation() != null && a.getDateCreation() != null ? b.getDateCreation().compareTo(a.getDateCreation()) : 0);
        else if ("Date ↑".equals(tri)) listeFiltree.sort((a,b) -> a.getDateCreation() != null && b.getDateCreation() != null ? a.getDateCreation().compareTo(b.getDateCreation()) : 0);

        currentPage = 1;
        afficherPage();
        if (lblCount != null) lblCount.setText(listeFiltree.size() + " réclamation(s)");
    }

    private void afficherPage() {
        int total = listeFiltree.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);
        tableReclamations.setItems(FXCollections.observableArrayList(listeFiltree.subList(from, to)));
        if (lblPageInfo != null) lblPageInfo.setText("Page " + currentPage + " sur " + totalPages + " — " + total + " réclamation(s)");
        if (btnPrev != null) btnPrev.setDisable(currentPage <= 1);
        if (btnNext != null) btnNext.setDisable(currentPage >= totalPages);
        if (btn1 != null) btn1.setText(String.valueOf(currentPage));
    }

    @FXML void pagePrecedente() { if (currentPage > 1) { currentPage--; afficherPage(); } }
    @FXML void pageSuivante()   { int tp = Math.max(1,(int)Math.ceil((double)listeFiltree.size()/PAGE_SIZE)); if (currentPage < tp) { currentPage++; afficherPage(); } }
    @FXML void goPage1()        { currentPage = 1; afficherPage(); }

    @FXML void validerReponse() {
        Reclamation r = tableReclamations.getSelectionModel().getSelectedItem();
        String rep = txtReponse != null ? txtReponse.getText().trim() : "";
        if (r == null) { AlertHelper.showWarning("Attention", "Sélectionnez une réclamation."); return; }
        if (rep.isEmpty()) { AlertHelper.showWarning("Attention", "Saisissez une réponse."); return; }
        if (AlertHelper.showConfirmation("Confirmation", "Envoyer cette réponse ?")) {
            try {
                reclamationService.repondreReclamation(r.getId(), rep);
                r.setReponseAdmin(rep);
                r.setStatut("Résolue");
                reclamationService.updateEntity(r);
                if (txtReponse != null) txtReponse.clear();
                loadData();
                AlertHelper.showSuccess("Succès", "Réponse envoyée !");
            } catch (SQLException e) { AlertHelper.showError("Erreur", e.getMessage()); }
        }
    }

    @FXML void clearSearch() {
        if (txtSearch != null) txtSearch.clear();
        if (comboFilter != null) comboFilter.setValue("Tous les statuts");
        if (comboPriorite != null) comboPriorite.setValue("Toutes priorités");
        if (comboTri != null) comboTri.setValue("Trier par...");
        appliquerFiltres();
    }

    private String getNomUser(int userId) {
        try {
            String q = "SELECT CONCAT(prenom, ' ', nom) FROM users WHERE id = ?";
            try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(q)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString(1);
                }
            }
        } catch (SQLException e) {}
        return String.valueOf(userId);
    }

    @FXML void searchData()   { appliquerFiltres(); }
    @FXML void applyFilter()  { appliquerFiltres(); }

    @FXML void afficherAdminReclamations() {}
    @FXML void afficherAdminAvis() {
        try { Parent r = FXMLLoader.load(getClass().getResource("/admin_avis.fxml")); txtSearch.getScene().setRoot(r); }
        catch (Exception e) { e.printStackTrace(); }
    }
    @FXML void afficherStatistiques() {
        try { Parent r = FXMLLoader.load(getClass().getResource("/statistiques/dashboard_stats.fxml")); txtSearch.getScene().setRoot(r); }
        catch (Exception e) { e.printStackTrace(); }
    }
    @FXML void switchRole() {
        try { Parent r = FXMLLoader.load(getClass().getResource("/user_layout.fxml")); txtSearch.getScene().setRoot(r); }
        catch (Exception e) { e.printStackTrace(); }
    }
    @FXML void deconnexion() { SessionManager.logout(); System.exit(0); }
    @FXML void retourMenu()  { afficherAdminReclamations(); }
}
