package controllers;

import entities.Avis;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.AvisService;
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

public class AdminAvisController implements Initializable {

    @FXML private TableView<Avis> tableAvis;
    @FXML private TableColumn<Avis, Integer> colId;
    @FXML private TableColumn<Avis, Integer> colUser;
    @FXML private TableColumn<Avis, String>  colCategorie;
    @FXML private TableColumn<Avis, Integer> colNote;
    @FXML private TableColumn<Avis, String>  colCommentaire;
    @FXML private TableColumn<Avis, Date>    colDate;
    @FXML private TableColumn<Avis, String>  colStatut;
    @FXML private TableColumn<Avis, String>  colSentiment;
    @FXML private TableColumn<Avis, String>  colReponse;
    @FXML private TableColumn<Avis, Void>    colActions;

    @FXML private TextArea         txtReponse;
    @FXML private TextField        txtSearch;
    @FXML private ComboBox<String> comboFilter;
    @FXML private ComboBox<String> comboNoteFilter;
    @FXML private ComboBox<String> comboTri;
    @FXML private Label            lblCount;
    @FXML private Label            lblPageInfo;
    @FXML private Button           btnPrev;
    @FXML private Button           btnNext;

    private AvisService avisService = new AvisService();
    private List<Avis>  listeComplete = new ArrayList<>();
    private List<Avis>  listeFiltree  = new ArrayList<>();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboFilter.setItems(FXCollections.observableArrayList("Tous les statuts", "En attente", "Validé", "Rejeté"));
        comboFilter.setValue("Tous les statuts");
        comboNoteFilter.setItems(FXCollections.observableArrayList("Toutes notes", "5 ⭐", "4 ⭐", "3 ⭐", "2 ⭐", "1 ⭐"));
        comboNoteFilter.setValue("Toutes notes");
        comboTri.setItems(FXCollections.observableArrayList("Trier par...", "Note ↓", "Note ↑", "Date ↓", "Date ↑"));
        comboTri.setValue("Trier par...");

        txtSearch.textProperty().addListener((o,a,b) -> appliquerFiltres());
        comboFilter.valueProperty().addListener((o,a,b) -> appliquerFiltres());
        comboNoteFilter.valueProperty().addListener((o,a,b) -> appliquerFiltres());
        comboTri.valueProperty().addListener((o,a,b) -> appliquerFiltres());

        setupTable();
        loadData();
    }

    private void setupTable() {
        tableAvis.setStyle("-fx-background-color:#1f2937;");

        // #ID
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setCellFactory(col -> new TableCell<Avis, Integer>() {
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                setStyle("-fx-background-color:#1f2937; -fx-alignment:CENTER;");
                if (empty || v == null) { setText(null); return; }
                Label lbl = new Label("#" + v);
                lbl.setStyle("-fx-background-color:#7c3aed; -fx-text-fill:white; -fx-padding:3 10; -fx-background-radius:8; -fx-font-weight:900; -fx-font-size:12px;");
                setGraphic(lbl); setText(null);
            }
        });

        // UTILISATEUR
        colUser.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUser.setCellFactory(col -> new TableCell<Avis, Integer>() {
            @Override protected void updateItem(Integer userId, boolean empty) {
                super.updateItem(userId, empty);
                setStyle("-fx-background-color:#1f2937;");
                if (empty || userId == null) { setText(null); return; }
                String nom = getNomUser(userId);
                HBox box = new HBox(6);
                box.setAlignment(Pos.CENTER_LEFT);
                Label avatar = new Label("👤");
                Label name = new Label(nom);
                name.setStyle("-fx-text-fill:#d1d5db; -fx-font-size:12px;");
                box.getChildren().addAll(avatar, name);
                setGraphic(box); setText(null);
            }
        });

        // CATÉGORIE
        if (colCategorie != null) {
            colCategorie.setCellFactory(col -> new TableCell<Avis, String>() {
                @Override protected void updateItem(String v, boolean empty) {
                    super.updateItem(v, empty);
                    setStyle("-fx-background-color:#1f2937; -fx-alignment:CENTER;");
                    if (empty) { setGraphic(null); return; }
                    Avis a = getTableView().getItems().get(getIndex());
                    String cat = getCategorieNom(a.getTypeId());
                    Label badge = new Label(cat);
                    badge.setStyle("-fx-background-color:rgba(99,102,241,0.15); -fx-text-fill:#818cf8; -fx-border-color:#818cf8; -fx-border-width:1; -fx-padding:3 10; -fx-background-radius:999; -fx-border-radius:999; -fx-font-size:11px; -fx-font-weight:700;");
                    setGraphic(badge); setText(null);
                }
            });
        }

        // NOTE avec étoiles
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colNote.setCellFactory(col -> new TableCell<Avis, Integer>() {
            @Override protected void updateItem(Integer note, boolean empty) {
                super.updateItem(note, empty);
                setStyle("-fx-background-color:#1f2937;");
                if (empty || note == null) { setGraphic(null); return; }
                HBox box = new HBox(2);
                box.setAlignment(Pos.CENTER_LEFT);
                for (int i = 1; i <= 5; i++) {
                    Label s = new Label(i <= note ? "★" : "☆");
                    s.setStyle("-fx-text-fill:" + (i <= note ? "#f59e0b" : "#374151") + "; -fx-font-size:14px;");
                    box.getChildren().add(s);
                }
                setGraphic(box); setText(null);
            }
        });

        // CONTENU
        colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colCommentaire.setCellFactory(col -> new TableCell<Avis, String>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setStyle("-fx-background-color:#1f2937; -fx-text-fill:#9ca3af; -fx-font-size:12px;");
                setText(empty || v == null ? null : v.length() > 40 ? v.substring(0,40)+"..." : v);
            }
        });

        // DATE
        if (colDate != null) {
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
            colDate.setCellFactory(col -> new TableCell<Avis, Date>() {
                @Override protected void updateItem(Date d, boolean empty) {
                    super.updateItem(d, empty);
                    setStyle("-fx-background-color:#1f2937; -fx-text-fill:#6b7280; -fx-alignment:CENTER;");
                    setText(empty || d == null ? "—" : sdf.format(d));
                }
            });
        }

        // STATUT badge
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(col -> new TableCell<Avis, String>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setStyle("-fx-background-color:#1f2937; -fx-alignment:CENTER;");
                if (empty || v == null) { setGraphic(null); return; }
                Label badge = new Label(v);
                if ("Validé".equals(v))
                    badge.setStyle("-fx-background-color:rgba(34,197,94,0.15);-fx-text-fill:#4ade80;-fx-border-color:#4ade80;-fx-border-width:1;-fx-padding:3 10;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                else if ("Rejeté".equals(v))
                    badge.setStyle("-fx-background-color:rgba(239,68,68,0.15);-fx-text-fill:#f87171;-fx-border-color:#f87171;-fx-border-width:1;-fx-padding:3 10;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                else
                    badge.setStyle("-fx-background-color:rgba(245,158,11,0.15);-fx-text-fill:#fbbf24;-fx-border-color:#fbbf24;-fx-border-width:1;-fx-padding:3 10;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                setGraphic(badge); setText(null);
            }
        });

        // SENTIMENT IA
        if (colSentiment != null) {
            colSentiment.setCellFactory(col -> new TableCell<Avis, String>() {
                @Override protected void updateItem(String v, boolean empty) {
                    super.updateItem(v, empty);
                    setStyle("-fx-background-color:#1f2937; -fx-alignment:CENTER;");
                    if (empty) { setGraphic(null); return; }
                    Avis a = getTableView().getItems().get(getIndex());
                    int note = a.getNote();
                    String sentiment = note >= 4 ? "😊 Positif" : note == 3 ? "😐 Neutre" : "😞 Négatif";
                    String color = note >= 4 ? "#4ade80" : note == 3 ? "#fbbf24" : "#f87171";
                    Label lbl = new Label(sentiment);
                    lbl.setStyle("-fx-text-fill:" + color + "; -fx-font-size:12px; -fx-font-weight:700;");
                    lbl.setStyle("-fx-background-color:rgba(99,102,241,0.1); -fx-text-fill:" + color + "; -fx-padding:3 10; -fx-background-radius:999; -fx-font-size:11px; -fx-font-weight:700;");
                    setGraphic(lbl); setText(null);
                }
            });
        }

        // RÉPONSE
        colReponse.setCellValueFactory(new PropertyValueFactory<>("reponseAdmin"));
        colReponse.setCellFactory(col -> new TableCell<Avis, String>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setStyle("-fx-background-color:#1f2937;");
                if (empty || v == null || v.isEmpty()) {
                    Label dash = new Label("—"); dash.setStyle("-fx-text-fill:#4b5563;");
                    setGraphic(dash); setText(null); return;
                }
                Label lbl = new Label("💬 " + (v.length() > 22 ? v.substring(0,22)+"..." : v));
                lbl.setStyle("-fx-text-fill:#34d399; -fx-font-size:12px; -fx-font-style:italic;");
                setGraphic(lbl); setText(null);
            }
        });

        // ACTIONS
        colActions.setCellFactory(col -> new TableCell<Avis, Void>() {
            final Button btnVal  = new Button("✓");
            final Button btnRej  = new Button("✗");
            final Button btnReply= new Button("💬");
            final Button btnDel  = new Button("🗑");
            final HBox box = new HBox(3, btnVal, btnRej, btnReply, btnDel);
            {
                btnVal.setStyle("-fx-background-color:#16a34a;-fx-text-fill:white;-fx-background-radius:6;-fx-padding:5 8;-fx-cursor:hand;-fx-font-weight:900;");
                btnRej.setStyle("-fx-background-color:#dc2626;-fx-text-fill:white;-fx-background-radius:6;-fx-padding:5 8;-fx-cursor:hand;-fx-font-weight:900;");
                btnReply.setStyle("-fx-background-color:#7c3aed;-fx-text-fill:white;-fx-background-radius:6;-fx-padding:5 8;-fx-cursor:hand;-fx-font-size:12px;");
                btnDel.setStyle("-fx-background-color:#374151;-fx-text-fill:#9ca3af;-fx-background-radius:6;-fx-padding:5 8;-fx-cursor:hand;-fx-font-size:12px;");
                box.setAlignment(Pos.CENTER);
                btnVal.setOnAction(e -> changerStatut("Validé"));
                btnRej.setOnAction(e -> changerStatut("Rejeté"));
                btnReply.setOnAction(e -> {
                    Avis a = getTableView().getItems().get(getIndex());
                    tableAvis.getSelectionModel().select(a);
                    if (txtReponse != null) {
                        txtReponse.requestFocus();
                        if (a.getReponseAdmin() != null) txtReponse.setText(a.getReponseAdmin());
                    }
                });
                btnDel.setOnAction(e -> {
                    Avis a = getTableView().getItems().get(getIndex());
                    if (AlertHelper.showConfirmation("Supprimer", "Supprimer l'avis #" + a.getId() + " ?")) {
                        try { avisService.deleteEntity(a.getId()); loadData(); }
                        catch (Exception ex) { AlertHelper.showError("Erreur", ex.getMessage()); }
                    }
                });
            }
            private void changerStatut(String statut) {
                Avis a = getTableView().getItems().get(getIndex());
                try {
                    String q = "UPDATE avis SET statut = ? WHERE id = ?";
                    try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(q)) {
                        ps.setString(1, statut); ps.setInt(2, a.getId()); ps.executeUpdate();
                    }
                    loadData();
                    AlertHelper.showSuccess("Succès", "Statut mis à jour !");
                } catch (Exception ex) { AlertHelper.showError("Erreur", ex.getMessage()); }
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
            listeComplete = avisService.getAllEntities();
            appliquerFiltres();
        } catch (SQLException e) { AlertHelper.showError("Erreur", e.getMessage()); }
    }

    private void appliquerFiltres() {
        String search = txtSearch != null ? txtSearch.getText().toLowerCase().trim() : "";
        String statut = comboFilter != null ? comboFilter.getValue() : "Tous les statuts";
        String note   = comboNoteFilter != null ? comboNoteFilter.getValue() : "Toutes notes";
        String tri    = comboTri != null ? comboTri.getValue() : "";

        listeFiltree = new ArrayList<>();
        for (Avis a : listeComplete) {
            boolean ok = search.isEmpty()
                || (a.getCommentaire() != null && a.getCommentaire().toLowerCase().contains(search))
                || String.valueOf(a.getId()).contains(search);
            if (ok && !"Tous les statuts".equals(statut) && statut != null)
                ok = statut.equals(a.getStatut());
            if (ok && !"Toutes notes".equals(note) && note != null) {
                int n = Integer.parseInt(note.substring(0,1));
                ok = a.getNote() == n;
            }
            if (ok) listeFiltree.add(a);
        }

        if ("Note ↓".equals(tri)) listeFiltree.sort((a,b) -> b.getNote() - a.getNote());
        else if ("Note ↑".equals(tri)) listeFiltree.sort((a,b) -> a.getNote() - b.getNote());
        else if ("Date ↓".equals(tri)) listeFiltree.sort((a,b) -> b.getDateCreation() != null && a.getDateCreation() != null ? b.getDateCreation().compareTo(a.getDateCreation()) : 0);
        else if ("Date ↑".equals(tri)) listeFiltree.sort((a,b) -> a.getDateCreation() != null && b.getDateCreation() != null ? a.getDateCreation().compareTo(b.getDateCreation()) : 0);

        currentPage = 1;
        afficherPage();
        if (lblCount != null) lblCount.setText(listeFiltree.size() + " avis");
    }

    private void afficherPage() {
        int total = listeFiltree.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);
        tableAvis.setItems(FXCollections.observableArrayList(listeFiltree.subList(from, to)));
        if (lblPageInfo != null) lblPageInfo.setText("Page " + currentPage + " sur " + totalPages + " — " + total + " avis");
        if (btnPrev != null) btnPrev.setDisable(currentPage <= 1);
        if (btnNext != null) btnNext.setDisable(currentPage >= totalPages);
    }

    @FXML void pagePrecedente() { if (currentPage > 1) { currentPage--; afficherPage(); } }
    @FXML void pageSuivante()   { int tp = Math.max(1,(int)Math.ceil((double)listeFiltree.size()/PAGE_SIZE)); if (currentPage < tp) { currentPage++; afficherPage(); } }

    @FXML void validerReponse() {
        Avis a = tableAvis.getSelectionModel().getSelectedItem();
        String rep = txtReponse != null ? txtReponse.getText().trim() : "";
        if (a == null) { AlertHelper.showWarning("Attention", "Sélectionnez un avis."); return; }
        if (rep.isEmpty()) { AlertHelper.showWarning("Attention", "Saisissez une réponse."); return; }
        if (AlertHelper.showConfirmation("Confirmation", "Envoyer cette réponse ?")) {
            try {
                avisService.repondreAvis(a.getId(), rep);
                if (txtReponse != null) txtReponse.clear();
                loadData();
                AlertHelper.showSuccess("Succès", "Réponse envoyée !");
            } catch (SQLException e) { AlertHelper.showError("Erreur", e.getMessage()); }
        }
    }

    @FXML void validerAvis() {
        Avis a = tableAvis.getSelectionModel().getSelectedItem();
        if (a == null) { AlertHelper.showWarning("Attention", "Sélectionnez un avis."); return; }
        try {
            String q = "UPDATE avis SET statut = 'Validé' WHERE id = ?";
            try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(q)) {
                ps.setInt(1, a.getId()); ps.executeUpdate();
            }
            loadData(); AlertHelper.showSuccess("Succès", "Avis validé !");
        } catch (Exception e) { AlertHelper.showError("Erreur", e.getMessage()); }
    }

    @FXML void rejeterAvis() {
        Avis a = tableAvis.getSelectionModel().getSelectedItem();
        if (a == null) { AlertHelper.showWarning("Attention", "Sélectionnez un avis."); return; }
        try {
            String q = "UPDATE avis SET statut = 'Rejeté' WHERE id = ?";
            try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(q)) {
                ps.setInt(1, a.getId()); ps.executeUpdate();
            }
            loadData(); AlertHelper.showSuccess("Succès", "Avis rejeté !");
        } catch (Exception e) { AlertHelper.showError("Erreur", e.getMessage()); }
    }

    @FXML void clearSearch() {
        if (txtSearch != null) txtSearch.clear();
        if (comboFilter != null) comboFilter.setValue("Tous les statuts");
        if (comboNoteFilter != null) comboNoteFilter.setValue("Toutes notes");
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

    private String getCategorieNom(int typeId) {
        if (typeId <= 0) return "—";
        try {
            String q = "SELECT nom FROM typeavis WHERE id = ?";
            try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(q)) {
                ps.setInt(1, typeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("nom");
                }
            }
        } catch (SQLException e) {}
        return "—";
    }

    @FXML void searchData() { appliquerFiltres(); }
    @FXML void applyFilter() { appliquerFiltres(); }
    @FXML void applySorting() { appliquerFiltres(); }

    @FXML void retourMenu() {
        try { Parent r = FXMLLoader.load(getClass().getResource("/admin_reclamations.fxml")); txtSearch.getScene().setRoot(r); }
        catch (Exception e) { e.printStackTrace(); }
    }
    @FXML void afficherStatistiques() {
        try { Parent r = FXMLLoader.load(getClass().getResource("/statistiques/dashboard_stats.fxml")); txtSearch.getScene().setRoot(r); }
        catch (Exception e) { e.printStackTrace(); }
    }
    @FXML void deconnexion() { SessionManager.logout(); System.exit(0); }
}
