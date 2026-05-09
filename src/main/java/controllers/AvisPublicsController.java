package controllers;

import entities.Avis;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.AvisService;

import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AvisPublicsController implements Initializable {

    @FXML private VBox postsContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboNote;
    @FXML private Label lblCount;
    @FXML private Label lblPage;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;

    private AvisService avisService = new AvisService();
    private List<Avis> listeComplete = new ArrayList<>();
    private List<Avis> listeFiltree  = new ArrayList<>();
    private Map<Integer, Integer> likesMap    = new HashMap<>();
    private Map<Integer, Integer> dislikesMap = new HashMap<>();
    private Map<Integer, Boolean> userLiked   = new HashMap<>();
    private static final int PAGE_SIZE = 5;
    private int currentPage = 1;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboNote.setItems(FXCollections.observableArrayList("Toutes", "5 ⭐", "4 ⭐", "3 ⭐", "2 ⭐", "1 ⭐"));
        comboNote.setValue("Toutes");
        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            listeComplete = avisService.getAllEntities();
            // Charger likes/dislikes depuis la DB
            chargerLikesDislikes();
            filtrer();
        } catch (Exception e) {
            System.err.println("Erreur chargement avis: " + e.getMessage());
        }
    }

    private void chargerLikesDislikes() {
        // Table likes_avis : id, avis_id, user_id, type ('like'|'dislike')
        try {
            String q = "SELECT avis_id, type, COUNT(*) as cnt FROM likes_avis GROUP BY avis_id, type";
            // ✅ Utilisation du nom complet pour éviter tout problème de résolution
            try (Statement st = tools.MyConnection.getInstance().getCnx().createStatement();
                 ResultSet rs = st.executeQuery(q)) {
                while (rs.next()) {
                    int avisId = rs.getInt("avis_id");
                    String type = rs.getString("type");
                    int cnt = rs.getInt("cnt");
                    if ("like".equals(type)) likesMap.put(avisId, cnt);
                    else dislikesMap.put(avisId, cnt);
                }
            }
        } catch (SQLException e) {
            System.out.println("ℹ Table likes_avis non trouvée ou vide.");
        }
    }

    private void creerTableLikesSiAbsente() {
        try {
            String create = "CREATE TABLE IF NOT EXISTS likes_avis (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "avis_id INT NOT NULL," +
                "user_id INT NOT NULL," +
                "type VARCHAR(10) NOT NULL DEFAULT 'like'," +
                "UNIQUE KEY unique_vote (avis_id, user_id)" +
                ")";
            try (Statement st = tools.MyConnection.getInstance().getCnx().createStatement()) {
                st.executeUpdate(create);
            }
        } catch (SQLException e) {
            System.err.println("Erreur création table likes: " + e.getMessage());
        }
    }

    @FXML
    void filtrer() {
        String recherche = txtRecherche != null ? txtRecherche.getText().toLowerCase().trim() : "";
        String noteVal   = comboNote != null ? comboNote.getValue() : "Toutes";

        listeFiltree = listeComplete.stream().filter(a -> {
            boolean ok = true;
            if (!recherche.isEmpty())
                ok = (a.getCommentaire() != null && a.getCommentaire().toLowerCase().contains(recherche));
            if (ok && noteVal != null && !"Toutes".equals(noteVal)) {
                try {
                    int noteInt = Integer.parseInt(noteVal.substring(0, 1));
                    ok = a.getNote() == noteInt;
                } catch (Exception e) { ok = true; }
            }
            return ok;
        }).collect(Collectors.toList());

        currentPage = 1;
        afficherPage();
        if (lblCount != null) lblCount.setText(listeFiltree.size() + " avis");
    }

    @FXML void reinitialiser() {
        if (txtRecherche != null) txtRecherche.clear();
        if (comboNote    != null) comboNote.setValue("Toutes");
        filtrer();
    }

    private void afficherPage() {
        postsContainer.getChildren().clear();
        int total      = listeFiltree.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        for (int i = from; i < to; i++) {
            postsContainer.getChildren().add(creerPostCard(listeFiltree.get(i)));
        }

        if (lblPage  != null) lblPage.setText("Page " + currentPage + " / " + totalPages);
        if (btnPrev  != null) btnPrev.setDisable(currentPage <= 1);
        if (btnNext  != null) btnNext.setDisable(currentPage >= totalPages);
    }

    private VBox creerPostCard(Avis avis) {
        VBox card = new VBox(14);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-border-radius: 20;" +
            "-fx-border-color: #e6ddd0;" +
            "-fx-border-width: 1.5;" +
            "-fx-padding: 22 26;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );

        // Header
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.setStyle("-fx-background-color: linear-gradient(135deg, #0f2a2a, #1a3a3a); -fx-background-radius: 50%; -fx-min-width: 46; -fx-min-height: 46; -fx-max-width: 46; -fx-max-height: 46;");
        String initiales = "?";
        String nomUser = getNomUser(avis.getUserId());
        if (nomUser != null && nomUser.length() >= 2) {
            String[] parts = nomUser.split(" ");
            initiales = parts.length >= 2
                ? ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase()
                : nomUser.substring(0, 2).toUpperCase();
        }
        Label lblInit = new Label(initiales);
        lblInit.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 16px;");
        avatar.getChildren().add(lblInit);

        VBox infoAuteur = new VBox(2);
        Label lblNom  = new Label(nomUser != null ? nomUser : "Voyageur anonyme");
        lblNom.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #0f2a2a;");
        Label lblDate = new Label(avis.getDateCreation() != null ? sdf.format(avis.getDateCreation()) : "");
        lblDate.setStyle("-fx-font-size: 11px; -fx-text-fill: #9a9a8a;");
        infoAuteur.getChildren().addAll(lblNom, lblDate);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String stars = "★".repeat(Math.max(0, Math.min(5, avis.getNote()))) + "☆".repeat(Math.max(0, 5 - avis.getNote()));
        Label lblStars = new Label(stars);
        lblStars.setStyle("-fx-text-fill: #c9a24a; -fx-font-size: 20px;");

        header.getChildren().addAll(avatar, infoAuteur, spacer, lblStars);

        Label lblContenu = new Label(avis.getCommentaire());
        lblContenu.setWrapText(true);
        lblContenu.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a2a; -fx-line-spacing: 4;");

        if (avis.getReponseAdmin() != null && !avis.getReponseAdmin().isEmpty()) {
            VBox reponseBox = new VBox(6);
            reponseBox.setStyle("-fx-background-color:#f0fdf4; -fx-background-radius:12; -fx-border-left-color:#22c55e; -fx-border-width:0 0 0 4; -fx-padding:12 16;");
            Label lblRep = new Label("💬 Réponse de l'équipe ViaNoVa");
            lblRep.setStyle("-fx-font-size:11px; -fx-font-weight:800; -fx-text-fill:#166534;");
            Label txtRep = new Label(avis.getReponseAdmin());
            txtRep.setWrapText(true);
            txtRep.setStyle("-fx-font-size:13px; -fx-text-fill:#166534;");
            reponseBox.getChildren().addAll(lblRep, txtRep);
            card.getChildren().add(reponseBox);
        }

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-padding: 8 0 0 0; -fx-border-top-color: #f0ebe3; -fx-border-width: 1 0 0 0;");

        int likes = likesMap.getOrDefault(avis.getId(), 0);
        int dislikes = dislikesMap.getOrDefault(avis.getId(), 0);

        Button btnLike = new Button("👍  " + likes);
        btnLike.setStyle("-fx-background-color:#f5f5f0; -fx-text-fill:#166534; -fx-background-radius:20; -fx-padding:7 16; -fx-font-weight:700; -fx-cursor:hand; -fx-border-color:#bbf7d0; -fx-border-width:1.5; -fx-border-radius:20;");
        Button btnDislike = new Button("👎  " + dislikes);
        btnDislike.setStyle("-fx-background-color:#f5f5f0; -fx-text-fill:#dc2626; -fx-background-radius:20; -fx-padding:7 16; -fx-font-weight:700; -fx-cursor:hand; -fx-border-color:#fecaca; -fx-border-width:1.5; -fx-border-radius:20;");

        btnLike.setOnAction(e -> voterAvis(avis.getId(), "like", btnLike, btnDislike));
        btnDislike.setOnAction(e -> voterAvis(avis.getId(), "dislike", btnLike, btnDislike));

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // ✅ getStatut() est maintenant disponible dans entities.Avis
        Label lblStatut = new Label(avis.getStatut() != null ? avis.getStatut() : "En attente");
        lblStatut.setStyle("-fx-background-color:#fff4df; -fx-text-fill:#9a6a11; -fx-padding:4 12; -fx-background-radius:999; -fx-font-size:11px; -fx-font-weight:900;");

        footer.getChildren().addAll(btnLike, btnDislike, spacer2, lblStatut);
        card.getChildren().addAll(header, lblContenu, footer);
        return card;
    }

    private void voterAvis(int avisId, String type, Button btnLike, Button btnDislike) {
        creerTableLikesSiAbsente();
        int userId = tools.SessionManager.getCurrentUserId();
        try {
            String checkSql = "SELECT type FROM likes_avis WHERE avis_id = ? AND user_id = ?";
            try (PreparedStatement ps = tools.MyConnection.getInstance().getCnx().prepareStatement(checkSql)) {
                ps.setInt(1, avisId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String del = "DELETE FROM likes_avis WHERE avis_id = ? AND user_id = ?";
                        try (PreparedStatement psDel = tools.MyConnection.getInstance().getCnx().prepareStatement(del)) {
                            psDel.setInt(1, avisId);
                            psDel.setInt(2, userId);
                            psDel.executeUpdate();
                        }
                    } else {
                        String ins = "INSERT INTO likes_avis (avis_id, user_id, type) VALUES (?, ?, ?)";
                        try (PreparedStatement psIns = tools.MyConnection.getInstance().getCnx().prepareStatement(ins)) {
                            psIns.setInt(1, avisId);
                            psIns.setInt(2, userId);
                            psIns.setString(3, type);
                            psIns.executeUpdate();
                        }
                    }
                }
            }
            chargerLikesDislikes();
            btnLike.setText("👍  " + likesMap.getOrDefault(avisId, 0));
            btnDislike.setText("👎  " + dislikesMap.getOrDefault(avisId, 0));
        } catch (SQLException ex) {
            System.err.println("Erreur vote: " + ex.getMessage());
        }
    }

    private String getNomUser(int userId) {
        try {
            String q = "SELECT CONCAT(prenom, ' ', nom) as fullname FROM users WHERE id = ?";
            try (PreparedStatement ps = tools.MyConnection.getInstance().getCnx().prepareStatement(q)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("fullname");
                }
            }
        } catch (SQLException e) {}
        return "Voyageur";
    }

    @FXML void pagePrecedente() { if (currentPage > 1) { currentPage--; afficherPage(); } }
    @FXML void pageSuivante() {
        int tp = Math.max(1,(int)Math.ceil((double)listeFiltree.size()/PAGE_SIZE));
        if (currentPage < tp) { currentPage++; afficherPage(); }
    }
}
