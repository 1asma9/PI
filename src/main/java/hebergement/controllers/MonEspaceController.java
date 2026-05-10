package hebergement.controllers;

import edu.destination.entities.Voyage;
import edu.destination.services.VoyageService;
import hebergement.entities.Reservation;
import hebergement.services.ReservationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import edu.destination.services.StripeService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MonEspaceController {

    // ── Header ────────────────────────────────────────────
    @FXML private Label lblUser;
    @FXML private Label lblProfil;
    @FXML private Label lblAvatar;
    @FXML private Label lblBadgeStatut;
    @FXML private Label lblStatVoyages;

    // ── Tab bar custom ────────────────────────────────────
    @FXML private HBox tabBarBox;

    // ── Panneaux ──────────────────────────────────────────
    @FXML private VBox paneInfo;
    @FXML private VBox paneModif;
    @FXML private VBox paneMdp;
    @FXML private VBox paneHeb;
    @FXML private VBox paneBlogs;
    @FXML private VBox paneRec;
    @FXML private VBox paneRes;
    @FXML private VBox paneAct;
    @FXML private VBox paneVoy;

    // ── Champs Info ───────────────────────────────────────
    @FXML private Label lblInfoNom, lblInfoPrenom, lblInfoEmail, lblInfoTel, lblInfoMembreDepuis;

    // ── Champs Modif ──────────────────────────────────────
    @FXML private TextField txtModifNom, txtModifPrenom, txtModifEmail, txtModifTel;
    @FXML private Label lblModifMsg;

    // ── Champs Mdp ────────────────────────────────────────
    @FXML private PasswordField txtMdpActuel, txtMdpNouveau, txtMdpConfirm;
    @FXML private Label lblMdpMsg;

    // ── Tables (autres onglets) ───────────────────────────
    @FXML private TableView<Object>          tvHebergements;
    @FXML private TableColumn<Object,String> colHebId, colHebDesc, colHebAdresse, colHebPrix, colHebType;
    @FXML private Label lblHebMsg;

    @FXML private TableView<Object>          tvBlogs;
    @FXML private TableColumn<Object,String> colBlogId, colBlogTitre, colBlogDate;
    @FXML private Label lblBlogMsg;

    // ── Feedbacks ──────────────────────────────────────────
    @FXML private VBox panelAvisInner;
    @FXML private VBox panelReclInner;
    @FXML private Button btnTabAvis;
    @FXML private Button btnTabRecl;
    @FXML private Button btnNouvelAvis;
    @FXML private Button btnNouvelleRec;

    @FXML private TableView<entities.Avis>               tvAvis;
    @FXML private TableColumn<entities.Avis, Integer>    colAvisNote;
    @FXML private TableColumn<entities.Avis, String>     colAvisCategorie;
    @FXML private TableColumn<entities.Avis, String>     colAvisCommentaire;
    @FXML private TableColumn<entities.Avis, String>     colAvisStatut;
    @FXML private TableColumn<entities.Avis, java.util.Date> colAvisDate;
    @FXML private TableColumn<entities.Avis, Void>       colAvisActions;
    @FXML private Label  lblPageAvis;
    @FXML private Button btnPrevAvis;
    @FXML private Button btnNextAvis;

    @FXML private TableView<entities.Reclamation>           tvReclamations;
    @FXML private TableColumn<entities.Reclamation, Integer> colRecId;
    @FXML private TableColumn<entities.Reclamation, String>  colRecSujet;
    @FXML private TableColumn<entities.Reclamation, String>  colRecCategorie;
    @FXML private TableColumn<entities.Reclamation, String>  colRecPriorite;
    @FXML private TableColumn<entities.Reclamation, String>  colRecStatut;
    @FXML private TableColumn<entities.Reclamation, Object>  colRecDate;
    @FXML private TableColumn<entities.Reclamation, Void>    colRecActions;
    @FXML private Label  lblRecMsg;
    @FXML private Label  lblPageRecl;
    @FXML private Button btnPrevRecl;
    @FXML private Button btnNextRecl;

    @FXML private TableView<Object>          tvReservations;
    @FXML private TableColumn<Object,String> colResId, colResDebut, colResFin, colResTotal;
    @FXML private Label lblResMsg;

    @FXML private TableView<Object>          tvActivites;
    @FXML private TableColumn<Object,String> colActId, colActNom, colActType, colActPrix;
    @FXML private Label lblActMsg;

    // ── Voyages cards ─────────────────────────────────────
    @FXML private VBox  voyagesCardsContainer;
    @FXML private Label lblVoyBadge;

    // ── State ─────────────────────────────────────────────
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final VoyageService      voyageService      = new VoyageService();
    private final ReservationService reservationService = new ReservationService();

    private static boolean openVoyagesTab = false;
    public static void requestOpenVoyagesTab() { openVoyagesTab = true; }

    private static boolean openFeedbacksTab = false;
    public static void requestOpenFeedbacksTab() { openFeedbacksTab = true; }

    private VBox[]   panes;
    private Button[] tabBtns;
    private int      activeTab = 0;

    // ── Pagination Feedbacks ───────────────────────────────
    private java.util.List<entities.Avis> listeAvis = new java.util.ArrayList<>();
    private java.util.List<entities.Reclamation> listeReclamations = new java.util.ArrayList<>();
    private static final int PAGE_SIZE = 5;
    private int pageAvis = 1;
    private int pageRecl = 1;
    private java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

    // ══════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        utilisateur user = MainLayoutController.getCurrentUser();
        setupHeader(user);

        setEmptyTable(tvHebergements, lblHebMsg, "Aucun hébergement.");
        setEmptyTable(tvBlogs,        lblBlogMsg, "Aucun article.");
        setEmptyTable(tvReclamations, lblRecMsg,  "Aucune réclamation.");
        setEmptyTable(tvReservations, lblResMsg,  "");
        setEmptyTable(tvActivites,    lblActMsg,  "Aucune activité.");

        loadVoyagesCards(user);
        buildTabBar();

        if (openVoyagesTab) {
            selectTab(8);
            openVoyagesTab = false;
        } else if (openFeedbacksTab) {
            selectTab(5);
            openFeedbacksTab = false;
        } else {
            selectTab(0);
        }
    }

    // ══════════════════════════════════════════════════════
    // HEADER
    // ══════════════════════════════════════════════════════
    private void setupHeader(utilisateur user) {
        if (user == null) return;
        String nom    = nvl(user.getNom(),    "");
        String prenom = nvl(user.getPrenom(), "");
        String init   = (!nom.isEmpty()    ? nom.substring(0,1).toUpperCase()    : "")
                + (!prenom.isEmpty() ? prenom.substring(0,1).toUpperCase() : "U");
        if (lblAvatar     != null) lblAvatar.setText(init);
        if (lblUser       != null) lblUser.setText(prenom + " " + nom);
        if (lblProfil     != null) lblProfil.setText(nvl(user.getEmail(), "—"));
        if (lblBadgeStatut!= null) lblBadgeStatut.setText("● Actif");
    }

    // ══════════════════════════════════════════════════════
    // TAB BAR CUSTOM
    // ══════════════════════════════════════════════════════
    private void buildTabBar() {
        if (tabBarBox == null) return;
        tabBarBox.getChildren().clear();
        panes = new VBox[]{ paneInfo, paneModif, paneMdp, paneHeb, paneBlogs, paneRec, paneRes, paneAct, paneVoy };
        String[][] tabs = {
                {"📄","Informations"}, {"✏️","Modifier"}, {"🔒","Mot de passe"},
                {"🏠","Hébergements"}, {"📝","Mes Blogs"}, {"💬","Mes Feedbacks"},
                {"📅","Réservations"}, {"🎯","Activités"}, {"✈️","Mes Voyages"}
        };
        tabBtns = new Button[tabs.length];
        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            Button btn = new Button(tabs[i][0] + "  " + tabs[i][1]);
            btn.setStyle(tabStyle(false));
            btn.setOnAction(e -> selectTab(idx));
            btn.setOnMouseEntered(e -> { if (idx != activeTab) btn.setStyle(tabHoverStyle()); });
            btn.setOnMouseExited(e  -> { if (idx != activeTab) btn.setStyle(tabStyle(false)); });
            tabBtns[i] = btn;
            tabBarBox.getChildren().add(btn);
        }
        tabBtns[0].setStyle(tabStyle(true));
    }

    private void selectTab(int idx) {
        activeTab = idx;
        for (int i = 0; i < tabBtns.length; i++) tabBtns[i].setStyle(tabStyle(i == idx));
        for (int i = 0; i < panes.length; i++) {
            if (panes[i] != null) {
                panes[i].setVisible(i == idx);
                panes[i].setManaged(i == idx);
            }
        }
        
        utilisateur user = MainLayoutController.getCurrentUser();
        if (user != null) {
            if (idx == 0) populateInfoTab(user);
            if (idx == 1) populateModifTab(user);
        }

        if (idx == 6) loadReservationsCards(user);
        if (idx == 5) {
            setupTableAvis();
            setupTableReclamations();
            chargerAvis();
            chargerReclamations();
            showTabAvis();
        }
    }

    private void populateInfoTab(utilisateur user) {
        if (lblInfoNom != null) lblInfoNom.setText(nvl(user.getNom(), "—"));
        if (lblInfoPrenom != null) lblInfoPrenom.setText(nvl(user.getPrenom(), "—"));
        if (lblInfoEmail != null) lblInfoEmail.setText(nvl(user.getEmail(), "—"));
        if (lblInfoTel != null) lblInfoTel.setText(nvl(user.getTelephone(), "—"));
        if (lblInfoMembreDepuis != null) {
            if (user.getDateCreation() != null) {
                lblInfoMembreDepuis.setText(user.getDateCreation().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                lblInfoMembreDepuis.setText(java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }
    }

    private void populateModifTab(utilisateur user) {
        if (txtModifNom != null) {
            txtModifNom.clear();
            txtModifNom.setPromptText(nvl(user.getNom(), "Nom"));
        }
        if (txtModifPrenom != null) {
            txtModifPrenom.clear();
            txtModifPrenom.setPromptText(nvl(user.getPrenom(), "Prénom"));
        }
        if (txtModifEmail != null) {
            txtModifEmail.clear();
            txtModifEmail.setPromptText(nvl(user.getEmail(), "Email"));
        }
        if (txtModifTel != null) {
            txtModifTel.clear();
            txtModifTel.setPromptText(nvl(user.getTelephone(), "Téléphone"));
        }
        if (lblModifMsg != null) lblModifMsg.setVisible(false);
    }

    @FXML private void onLogout() {
        if (tools.AlertHelper.showConfirmation("Déconnexion", "Êtes-vous sûr de vouloir vous déconnecter ?")) {
            MainLayoutController.setCurrentUser(null);
            tools.SessionManager.logout();
            try {
                javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/app/login.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(root, 1200, 700);
                javafx.stage.Stage stage = (javafx.stage.Stage) lblUser.getScene().getWindow();
                stage.setScene(scene);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML private void onSaveFace() {
        tools.AlertHelper.showInfo("Face ID", "La fonctionnalité d'enregistrement du visage sera bientôt disponible !");
    }

    @FXML private void onSaveModifications() {
        utilisateur user = MainLayoutController.getCurrentUser();
        if (user == null) return;
        
        String nom = txtModifNom.getText().trim();
        String prenom = txtModifPrenom.getText().trim();
        String email = txtModifEmail.getText().trim();
        String tel = txtModifTel.getText().trim();
        
        // Fallback to original values if empty
        if (nom.isEmpty()) nom = user.getNom();
        if (prenom.isEmpty()) prenom = user.getPrenom();
        if (email.isEmpty()) email = user.getEmail();
        if (tel.isEmpty()) tel = user.getTelephone();
        
        org.example.PI_Gestion_des_utilisateurs.services.utilisateur_service us = new org.example.PI_Gestion_des_utilisateurs.services.utilisateur_service();
        
        // Use a temporary user to validate
        utilisateur uModif = new utilisateur(nom, prenom, email, user.getPassword(), tel);
        uModif.setId(user.getId());
        
        if (!email.equals(user.getEmail()) && !us.verifierEmailUnique(email)) {
            lblModifMsg.setText("Cet email est déjà utilisé.");
            lblModifMsg.setStyle("-fx-text-fill:red;");
            lblModifMsg.setVisible(true);
            return;
        }

        if (us.modifierutilisateurSansPwd(uModif)) {
            // Update current session user
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setTelephone(tel);
            setupHeader(user);
            populateInfoTab(user);
            populateModifTab(user); // refresh prompts
            lblModifMsg.setText("Modifications enregistrées avec succès !");
            lblModifMsg.setStyle("-fx-text-fill:green;");
            lblModifMsg.setVisible(true);
        } else {
            lblModifMsg.setText("Erreur lors de la mise à jour.");
            lblModifMsg.setStyle("-fx-text-fill:red;");
            lblModifMsg.setVisible(true);
        }
    }

    @FXML private void onChangePassword() {
        utilisateur user = MainLayoutController.getCurrentUser();
        if (user == null) return;

        String actuel = txtMdpActuel.getText();
        String nouveau = txtMdpNouveau.getText();
        String confirm = txtMdpConfirm.getText();

        if (actuel.isEmpty() || nouveau.isEmpty() || confirm.isEmpty()) {
            lblMdpMsg.setText("Veuillez remplir tous les champs.");
            lblMdpMsg.setStyle("-fx-text-fill:red;");
            lblMdpMsg.setVisible(true);
            return;
        }

        if (!org.example.PI_Gestion_des_utilisateurs.tools.PasswordUtil.verifyPassword(actuel, user.getPassword())) {
            lblMdpMsg.setText("Mot de passe actuel incorrect.");
            lblMdpMsg.setStyle("-fx-text-fill:red;");
            lblMdpMsg.setVisible(true);
            return;
        }

        if (!nouveau.equals(confirm)) {
            lblMdpMsg.setText("Les mots de passe ne correspondent pas.");
            lblMdpMsg.setStyle("-fx-text-fill:red;");
            lblMdpMsg.setVisible(true);
            return;
        }

        // Basic password validation
        if (nouveau.length() < 6 || !nouveau.matches(".*[A-Z].*") || !nouveau.matches(".*[a-z].*") || !nouveau.matches(".*\\d.*") || !nouveau.matches(".*[!@#$%^&*()-+].*")) {
            lblMdpMsg.setText("Le mot de passe doit respecter les critères.");
            lblMdpMsg.setStyle("-fx-text-fill:red;");
            lblMdpMsg.setVisible(true);
            return;
        }

        org.example.PI_Gestion_des_utilisateurs.services.utilisateur_service us = new org.example.PI_Gestion_des_utilisateurs.services.utilisateur_service();
        
        user.setPassword(nouveau); // Le service va le hasher
        if (us.modifierutilisateur(user)) {
            // Mettre à jour le mot de passe actuel en mémoire (haché) pour ne pas casser la session courante
            user.setPassword(org.example.PI_Gestion_des_utilisateurs.tools.PasswordUtil.hashPassword(nouveau));
            
            txtMdpActuel.clear();
            txtMdpNouveau.clear();
            txtMdpConfirm.clear();
            lblMdpMsg.setText("Mot de passe changé avec succès !");
            lblMdpMsg.setStyle("-fx-text-fill:green;");
            lblMdpMsg.setVisible(true);
        } else {
            lblMdpMsg.setText("Erreur lors du changement de mot de passe.");
            lblMdpMsg.setStyle("-fx-text-fill:red;");
            lblMdpMsg.setVisible(true);
        }
    }

    // ══════════════════════════════════════════════════════
    // ONGLETS FEEDBACKS
    // ══════════════════════════════════════════════════════
    @FXML public void showTabAvis() {
        if (panelAvisInner != null) { panelAvisInner.setVisible(true);  panelAvisInner.setManaged(true);  }
        if (panelReclInner != null) { panelReclInner.setVisible(false); panelReclInner.setManaged(false); }
        String activeStyle = "-fx-background-color:#c9a24a;-fx-text-fill:white;-fx-font-weight:800;-fx-font-size:13px;-fx-padding:9 22;-fx-background-radius:10 10 0 0;-fx-cursor:hand;";
        String inactStyle  = "-fx-background-color:#e2e8f0;-fx-text-fill:#64748b;-fx-font-weight:700;-fx-font-size:13px;-fx-padding:9 22;-fx-background-radius:10 10 0 0;-fx-cursor:hand;";
        if (btnTabAvis != null) btnTabAvis.setStyle(activeStyle);
        if (btnTabRecl != null) btnTabRecl.setStyle(inactStyle);
    }

    @FXML public void showTabRecl() {
        if (panelAvisInner != null) { panelAvisInner.setVisible(false); panelAvisInner.setManaged(false); }
        if (panelReclInner != null) { panelReclInner.setVisible(true);  panelReclInner.setManaged(true);  }
        String activeStyle = "-fx-background-color:#0f2a2a;-fx-text-fill:white;-fx-font-weight:800;-fx-font-size:13px;-fx-padding:9 22;-fx-background-radius:10 10 0 0;-fx-cursor:hand;";
        String inactStyle  = "-fx-background-color:#e2e8f0;-fx-text-fill:#64748b;-fx-font-weight:700;-fx-font-size:13px;-fx-padding:9 22;-fx-background-radius:10 10 0 0;-fx-cursor:hand;";
        if (btnTabRecl != null) btnTabRecl.setStyle(activeStyle);
        if (btnTabAvis != null) btnTabAvis.setStyle(inactStyle);
    }

    private String tabStyle(boolean active) {
        if (active) return
                "-fx-background-color:transparent;" +
                        "-fx-border-color:transparent transparent #c8714a transparent;" +
                        "-fx-border-width:0 0 3 0;-fx-text-fill:#c8714a;" +
                        "-fx-font-size:13px;-fx-font-weight:700;" +
                        "-fx-padding:14 18 14 18;-fx-cursor:hand;-fx-background-radius:0;";
        return
                "-fx-background-color:transparent;-fx-border-color:transparent;" +
                        "-fx-text-fill:#64748b;-fx-font-size:13px;-fx-font-weight:600;" +
                        "-fx-padding:14 18 14 18;-fx-cursor:hand;-fx-background-radius:0;";
    }

    private String tabHoverStyle() {
        return "-fx-background-color:#f8fafc;-fx-border-color:transparent;" +
                "-fx-text-fill:#374151;-fx-font-size:13px;-fx-font-weight:600;" +
                "-fx-padding:14 18 14 18;-fx-cursor:hand;-fx-background-radius:0;";
    }

    // ══════════════════════════════════════════════════════
    // SETUP TABLES
    // ══════════════════════════════════════════════════════
    private void setupTableAvis() {
        if (tvAvis == null) return;

        // Style table
        tvAvis.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-border-radius: 16;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-width: 1.5;" +
            "-fx-font-size: 13px;"
        );
        tvAvis.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ── Note avec étoiles dorées ──
        colAvisNote.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("note"));
        colAvisNote.setCellFactory(col -> new javafx.scene.control.TableCell<entities.Avis, Integer>() {
            @Override protected void updateItem(Integer note, boolean empty) {
                super.updateItem(note, empty);
                setStyle("-fx-background-color: white; -fx-alignment: CENTER-LEFT;");
                if (empty || note == null) { setGraphic(null); setText(null); return; }
                javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(2);
                box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                for (int i = 1; i <= 5; i++) {
                    Label s = new Label(i <= note ? "★" : "☆");
                    s.setStyle("-fx-text-fill:" + (i <= note ? "#c9a24a" : "#d1d5db") + "; -fx-font-size:16px;");
                    box.getChildren().add(s);
                }
                Label num = new Label("  " + note + "/5");
                num.setStyle("-fx-text-fill:#6b7280; -fx-font-size:12px; -fx-font-weight:600;");
                box.getChildren().add(num);
                setGraphic(box); setText(null);
            }
        });

        // ── Catégorie ──
        if (colAvisCategorie != null) {
            colAvisCategorie.setCellValueFactory(c -> {
                // Récupérer le nom du type depuis typeavis
                try {
                    int typeId = c.getValue().getTypeId();
                    if (typeId > 0) {
                        String q = "SELECT nom FROM typeavis WHERE id = ?";
                        try (java.sql.PreparedStatement ps = tools.MyConnection.getInstance().getCnx().prepareStatement(q)) {
                            ps.setInt(1, typeId);
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) return new javafx.beans.property.SimpleStringProperty(rs.getString("nom"));
                            }
                        }
                    }
                } catch (Exception e) {}
                return new javafx.beans.property.SimpleStringProperty("—");
            });
            colAvisCategorie.setCellFactory(col -> new javafx.scene.control.TableCell<entities.Avis, String>() {
                @Override protected void updateItem(String val, boolean empty) {
                    super.updateItem(val, empty);
                    setStyle("-fx-background-color: white;");
                    if (empty || val == null) { setText("—"); setStyle("-fx-background-color:white; -fx-text-fill:#9ca3af;"); return; }
                    Label badge = new Label(val);
                    badge.setStyle("-fx-background-color:#f0f9ff; -fx-text-fill:#0369a1; -fx-border-color:#bae6fd; -fx-border-width:1; -fx-padding:3 10; -fx-background-radius:999; -fx-border-radius:999; -fx-font-size:11px; -fx-font-weight:700;");
                    setGraphic(badge); setText(null);
                }
            });
        }

        // ── Commentaire ──
        colAvisCommentaire.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("commentaire"));
        colAvisCommentaire.setCellFactory(col -> new javafx.scene.control.TableCell<entities.Avis, String>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                setStyle("-fx-background-color: white;");
                if (empty || val == null) { setText(null); return; }
                String truncated = val.length() > 50 ? val.substring(0, 50) + "..." : val;
                setText(truncated);
                setStyle("-fx-background-color:white; -fx-text-fill:#374151; -fx-font-size:13px;");
            }
        });

        // ── Statut badge ──
        colAvisStatut.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("statut"));
        colAvisStatut.setCellFactory(col -> new javafx.scene.control.TableCell<entities.Avis, String>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                setStyle("-fx-background-color: white; -fx-alignment: CENTER;");
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val);
                if ("Validé".equals(val))
                    badge.setStyle("-fx-background-color:#dcfce7;-fx-text-fill:#166534;-fx-border-color:#bbf7d0;-fx-border-width:1;-fx-padding:4 12;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                else if ("Rejeté".equals(val))
                    badge.setStyle("-fx-background-color:#fee2e2;-fx-text-fill:#991b1b;-fx-border-color:#fecaca;-fx-border-width:1;-fx-padding:4 12;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                else
                    badge.setStyle("-fx-background-color:#fef9c3;-fx-text-fill:#854d0e;-fx-border-color:#fde047;-fx-border-width:1;-fx-padding:4 12;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                setGraphic(badge); setText(null);
            }
        });

        // ── Date ──
        colAvisDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dateCreation"));
        colAvisDate.setCellFactory(col -> new javafx.scene.control.TableCell<entities.Avis, java.util.Date>() {
            @Override protected void updateItem(java.util.Date d, boolean empty) {
                super.updateItem(d, empty);
                setStyle("-fx-background-color: white; -fx-text-fill: #6b7280; -fx-alignment: CENTER;");
                setText(empty || d == null ? "—" : sdf.format(d));
            }
        });

        // ── Actions ──
        colAvisActions.setCellFactory(col -> new javafx.scene.control.TableCell<entities.Avis, Void>() {
            final Button btnView = new Button("👁");
            final Button btnEdit = new Button("✏️");
            final Button btnDel  = new Button("🗑");
            final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(6, btnView, btnEdit, btnDel);
            {
                btnView.setStyle("-fx-background-color:#eff6ff;-fx-text-fill:#1d4ed8;-fx-background-radius:8;-fx-border-color:#bfdbfe;-fx-border-width:1;-fx-padding:5 12;-fx-cursor:hand;-fx-font-size:13px;");
                btnEdit.setStyle("-fx-background-color:#fef9c3;-fx-text-fill:#854d0e;-fx-background-radius:8;-fx-border-color:#fde047;-fx-border-width:1;-fx-padding:5 12;-fx-cursor:hand;-fx-font-size:13px;");
                btnDel.setStyle("-fx-background-color:#fef2f2;-fx-text-fill:#dc2626;-fx-background-radius:8;-fx-border-color:#fecaca;-fx-border-width:1;-fx-padding:5 12;-fx-cursor:hand;-fx-font-size:13px;");
                box.setAlignment(javafx.geometry.Pos.CENTER);
                btnView.setOnAction(e -> {
                    entities.Avis a = getTableView().getItems().get(getIndex());
                    tools.AlertHelper.showInfo("Détails de l'avis",
                        "⭐ Note: " + a.getNote() + "/5\n" +
                        "📋 Statut: " + a.getStatut() + "\n\n" +
                        "💬 Commentaire:\n" + a.getCommentaire() +
                        (a.getReponseAdmin() != null && !a.getReponseAdmin().isEmpty()
                            ? "\n\n✅ Réponse admin:\n" + a.getReponseAdmin() : ""));
                });
                btnEdit.setOnAction(e -> {
                    entities.Avis a = getTableView().getItems().get(getIndex());
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/user_modifier_avis.fxml"));
                        javafx.scene.Parent root = loader.load();
                        controllers.ModifierAvisController ctrl = loader.getController();
                        ctrl.setAvis(a);
                        if (hebergement.controllers.ClientLayoutController.getInstance() != null) {
                            hebergement.controllers.ClientLayoutController.getInstance().loadPageWithRoot(root);
                        } else {
                            getTableView().getScene().setRoot(root);
                        }
                    } catch (Exception ex) {
                        tools.AlertHelper.showError("Erreur", "Impossible d'ouvrir l'édition : " + ex.getMessage());
                    }
                });
                btnDel.setOnAction(e -> {
                    entities.Avis a = getTableView().getItems().get(getIndex());
                    if (tools.AlertHelper.showConfirmation("Supprimer", "Supprimer cet avis ?")) {
                        try { new services.AvisService().deleteEntity(a.getId()); chargerAvis(); }
                        catch (Exception ex) { tools.AlertHelper.showError("Erreur", ex.getMessage()); }
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setStyle("-fx-background-color: white; -fx-alignment: CENTER;");
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupTableReclamations() {
        if (tvReclamations == null) return;

        tvReclamations.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-border-radius: 16;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-width: 1.5;" +
            "-fx-font-size: 13px;"
        );
        tvReclamations.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ── ID ──
        colRecId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colRecId.setCellFactory(col -> new javafx.scene.control.TableCell<entities.Reclamation, Integer>() {
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                setStyle("-fx-background-color:white; -fx-text-fill:#9ca3af; -fx-font-size:12px; -fx-alignment:CENTER;");
                setText(empty || v == null ? null : "#" + v);
            }
        });

        // ── Titre ──
        colRecSujet.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("titre"));
        colRecSujet.setCellFactory(col -> new javafx.scene.control.TableCell<entities.Reclamation, String>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                setStyle("-fx-background-color:white; -fx-text-fill:#111827; -fx-font-weight:700; -fx-font-size:13px;");
                setText(empty || val == null ? null : val.length() > 40 ? val.substring(0,40)+"..." : val);
            }
        });

        // ── Catégorie ──
        if (colRecCategorie != null) {
            colRecCategorie.setCellValueFactory(c -> {
                try {
                    entities.Reclamation r = (entities.Reclamation) c.getValue();
                    int typeId = r.getTypeId();
                    if (typeId > 0) {
                        String q = "SELECT nom FROM typeavis WHERE id = ?";
                        try (java.sql.PreparedStatement ps = tools.MyConnection.getInstance().getCnx().prepareStatement(q)) {
                            ps.setInt(1, typeId);
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) return new javafx.beans.property.SimpleStringProperty(rs.getString("nom"));
                            }
                        }
                    }
                } catch (Exception e) {}
                return new javafx.beans.property.SimpleStringProperty("—");
            });
            colRecCategorie.setCellFactory(col -> new javafx.scene.control.TableCell<entities.Reclamation, String>() {
                @Override protected void updateItem(String val, boolean empty) {
                    super.updateItem(val, empty);
                    setStyle("-fx-background-color:white; -fx-alignment:CENTER;");
                    if (empty || val == null || "—".equals(val)) { setText("—"); setStyle("-fx-background-color:white;-fx-text-fill:#9ca3af;"); return; }
                    Label badge = new Label(val);
                    badge.setStyle("-fx-background-color:#f0f9ff;-fx-text-fill:#0369a1;-fx-border-color:#bae6fd;-fx-border-width:1;-fx-padding:3 10;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:700;");
                    setGraphic(badge); setText(null);
                }
            });
        }

        // ── Priorité ──
        if (colRecPriorite != null) {
            colRecPriorite.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("priorite"));
            colRecPriorite.setCellFactory(col -> new javafx.scene.control.TableCell<entities.Reclamation, String>() {
                @Override protected void updateItem(String val, boolean empty) {
                    super.updateItem(val, empty);
                    setStyle("-fx-background-color:white; -fx-alignment:CENTER;");
                    if (empty || val == null) { setGraphic(null); return; }
                    Label badge = new Label(val);
                    if ("Urgente".equals(val))
                        badge.setStyle("-fx-background-color:#fef2f2;-fx-text-fill:#be123c;-fx-border-color:#fecdd3;-fx-border-width:1;-fx-padding:4 12;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                    else if ("Haute".equals(val))
                        badge.setStyle("-fx-background-color:#fff7ed;-fx-text-fill:#c2410c;-fx-border-color:#fed7aa;-fx-border-width:1;-fx-padding:4 12;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                    else if ("Moyenne".equals(val))
                        badge.setStyle("-fx-background-color:#eff6ff;-fx-text-fill:#1d4ed8;-fx-border-color:#bfdbfe;-fx-border-width:1;-fx-padding:4 12;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                    else
                        badge.setStyle("-fx-background-color:#f0fdf4;-fx-text-fill:#166534;-fx-border-color:#bbf7d0;-fx-border-width:1;-fx-padding:4 12;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                    setGraphic(badge); setText(null);
                }
            });
        }

        // ── Statut ──
        colRecStatut.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("statut"));
        colRecStatut.setCellFactory(col -> new javafx.scene.control.TableCell<entities.Reclamation, String>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                setStyle("-fx-background-color:white; -fx-alignment:CENTER;");
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val);
                if ("Résolue".equals(val) || "Traitée".equals(val))
                    badge.setStyle("-fx-background-color:#dcfce7;-fx-text-fill:#166534;-fx-border-color:#bbf7d0;-fx-border-width:1;-fx-padding:4 12;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                else if ("Rejetée".equals(val))
                    badge.setStyle("-fx-background-color:#fee2e2;-fx-text-fill:#991b1b;-fx-border-color:#fecaca;-fx-border-width:1;-fx-padding:4 12;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                else
                    badge.setStyle("-fx-background-color:#fef9c3;-fx-text-fill:#854d0e;-fx-border-color:#fde047;-fx-border-width:1;-fx-padding:4 12;-fx-background-radius:999;-fx-border-radius:999;-fx-font-size:11px;-fx-font-weight:900;");
                setGraphic(badge); setText(null);
            }
        });

        // ── Date ──
        colRecDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dateCreation"));
        colRecDate.setCellFactory(col -> new javafx.scene.control.TableCell<entities.Reclamation, Object>() {
            @Override protected void updateItem(Object d, boolean empty) {
                super.updateItem(d, empty);
                setStyle("-fx-background-color:white; -fx-text-fill:#6b7280; -fx-alignment:CENTER;");
                setText(empty || d == null ? "—" : sdf.format(d));
            }
        });

        // ── Actions ──
        if (colRecActions != null) {
            colRecActions.setCellFactory(col -> new javafx.scene.control.TableCell<entities.Reclamation, Void>() {
                final Button btnView = new Button("👁");
                final Button btnEdit = new Button("✏️");
                final Button btnDel  = new Button("🗑");
                final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(6, btnView, btnEdit, btnDel);
                {
                    btnView.setStyle("-fx-background-color:#eff6ff;-fx-text-fill:#1d4ed8;-fx-background-radius:8;-fx-border-color:#bfdbfe;-fx-border-width:1;-fx-padding:5 12;-fx-cursor:hand;-fx-font-size:13px;");
                    btnEdit.setStyle("-fx-background-color:#fef9c3;-fx-text-fill:#854d0e;-fx-background-radius:8;-fx-border-color:#fde047;-fx-border-width:1;-fx-padding:5 12;-fx-cursor:hand;-fx-font-size:13px;");
                    btnDel.setStyle("-fx-background-color:#fef2f2;-fx-text-fill:#dc2626;-fx-background-radius:8;-fx-border-color:#fecaca;-fx-border-width:1;-fx-padding:5 12;-fx-cursor:hand;-fx-font-size:13px;");
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    btnView.setOnAction(e -> {
                        entities.Reclamation r = (entities.Reclamation) getTableView().getItems().get(getIndex());
                        tools.AlertHelper.showInfo("Détails réclamation",
                            "📌 Titre: " + r.getTitre() + "\n" +
                            "🎯 Priorité: " + r.getPriorite() + "\n" +
                            "📋 Statut: " + r.getStatut() + "\n\n" +
                            "📝 Description:\n" + r.getDescription() +
                            (r.getReponseAdmin() != null && !r.getReponseAdmin().isEmpty()
                                ? "\n\n✅ Réponse admin:\n" + r.getReponseAdmin() : ""));
                    });
                    btnEdit.setOnAction(e -> {
                        entities.Reclamation r = (entities.Reclamation) getTableView().getItems().get(getIndex());
                        try {
                            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/user_modifier_reclamation.fxml"));
                            javafx.scene.Parent root = loader.load();
                            controllers.ModifierReclamationController ctrl = loader.getController();
                            ctrl.setReclamation(r);
                            if (hebergement.controllers.ClientLayoutController.getInstance() != null) {
                                hebergement.controllers.ClientLayoutController.getInstance().loadPageWithRoot(root);
                            } else {
                                getTableView().getScene().setRoot(root);
                            }
                        } catch (Exception ex) {
                            tools.AlertHelper.showError("Erreur", "Impossible d'ouvrir l'édition : " + ex.getMessage());
                        }
                    });
                    btnDel.setOnAction(e -> {
                        entities.Reclamation r = (entities.Reclamation) getTableView().getItems().get(getIndex());
                        if (tools.AlertHelper.showConfirmation("Supprimer", "Supprimer cette réclamation ?")) {
                            try { new services.ReclamationService().deleteEntity(r.getId()); chargerReclamations(); }
                            catch (Exception ex) { tools.AlertHelper.showError("Erreur", ex.getMessage()); }
                        }
                    });
                }
                @Override protected void updateItem(Void v, boolean empty) {
                    super.updateItem(v, empty);
                    setStyle("-fx-background-color:white; -fx-alignment:CENTER;");
                    setGraphic(empty ? null : box);
                }
            });
        }
    }

    // ══════════════════════════════════════════════════════
    // FEEDBACKS : Avis + Réclamations
    // ══════════════════════════════════════════════════════
    private void chargerAvis() {
        try {
            int userId = tools.SessionManager.getCurrentUserId();
            listeAvis = new services.AvisService().getByUserId(userId);
            System.out.println("✅ Avis chargés pour userId=" + userId + " : " + listeAvis.size());
            pageAvis = 1;
            afficherPageAvis();
        } catch (java.sql.SQLException e) {
            System.err.println("Erreur avis: " + e.getMessage());
        }
    }

    private void chargerReclamations() {
        try {
            int userId = tools.SessionManager.getCurrentUserId();
            listeReclamations = new services.ReclamationService().getByUserId(userId);
            System.out.println("✅ Réclamations chargées pour userId=" + userId + " : " + listeReclamations.size());
            pageRecl = 1;
            afficherPageRecl();
        } catch (java.sql.SQLException e) {
            System.err.println("Erreur réclamations: " + e.getMessage());
        }
    }

    private void afficherPageAvis() {
        if (tvAvis == null) return;
        int total = listeAvis.size(), totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int from = (pageAvis - 1) * PAGE_SIZE, to = Math.min(from + PAGE_SIZE, total);
        tvAvis.setItems(javafx.collections.FXCollections.observableArrayList(listeAvis.subList(from, to)));
        if (lblPageAvis != null) lblPageAvis.setText("Page " + pageAvis + "/" + totalPages);
        if (btnPrevAvis != null) btnPrevAvis.setDisable(pageAvis <= 1);
        if (btnNextAvis != null) btnNextAvis.setDisable(pageAvis >= totalPages);
    }

    private void afficherPageRecl() {
        if (tvReclamations == null) return;
        int total = listeReclamations.size(), totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int from = (pageRecl - 1) * PAGE_SIZE, to = Math.min(from + PAGE_SIZE, total);
        tvReclamations.setItems(javafx.collections.FXCollections.observableArrayList(listeReclamations.subList(from, to)));
        if (lblPageRecl != null) lblPageRecl.setText("Page " + pageRecl + "/" + totalPages);
        if (btnPrevRecl != null) btnPrevRecl.setDisable(pageRecl <= 1);
        if (btnNextRecl != null) btnNextRecl.setDisable(pageRecl >= totalPages);
    }

    @FXML void pagePrevAvis() { if (pageAvis > 1) { pageAvis--; afficherPageAvis(); } }
    @FXML void pageNextAvis() { int tp = Math.max(1,(int)Math.ceil((double)listeAvis.size()/PAGE_SIZE)); if (pageAvis < tp) { pageAvis++; afficherPageAvis(); } }
    @FXML void pagePrevRecl() { if (pageRecl > 1) { pageRecl--; afficherPageRecl(); } }
    @FXML void pageNextRecl() { int tp = Math.max(1,(int)Math.ceil((double)listeReclamations.size()/PAGE_SIZE)); if (pageRecl < tp) { pageRecl++; afficherPageRecl(); } }

    @FXML void ajouterAvis() {
        if (ClientLayoutController.getInstance() != null) {
            ClientLayoutController.getInstance().loadPage("/user_ajouter_avis.fxml");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'interface, layout introuvable.");
        }
    }

    @FXML void ajouterReclamation() {
        if (ClientLayoutController.getInstance() != null) {
            ClientLayoutController.getInstance().loadPage("/user_ajouter_reclamation.fxml");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'interface, layout introuvable.");
        }
    }


    // ══════════════════════════════════════════════════════
    // ✅ RÉSERVATIONS HÉBERGEMENT EN CARTES (comme les voyages)
    // ══════════════════════════════════════════════════════
    private void loadReservationsCards(utilisateur user) {
        if (paneRes == null) return;

        // Vider le panneau (garder seulement le 1er enfant = titre)
        if (paneRes.getChildren().size() > 1) {
            paneRes.getChildren().remove(1, paneRes.getChildren().size());
        }

        if (user == null) {
            paneRes.getChildren().add(emptyStateRes("📅", "Utilisateur non connecté.", null));
            return;
        }

        List<Reservation> reservations = reservationService.getByUserEmail(user.getEmail());
        System.out.println("=== Réservations hébergement pour " + user.getEmail() + " : " + reservations.size());

        // ── Remplacer le titre par un header avec badge ───
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label titre = new Label("📅  Réservations hébergement");
        titre.setStyle("-fx-font-size:17px;-fx-font-weight:800;-fx-text-fill:#1e293b;");
        headerRow.getChildren().add(titre);
        if (!reservations.isEmpty()) {
            Label badge = new Label(reservations.size() + " réservation(s)");
            badge.setStyle(
                    "-fx-background-color:#fff7ed;-fx-border-color:#fed7aa;" +
                            "-fx-border-radius:20;-fx-background-radius:20;-fx-border-width:1.5;" +
                            "-fx-text-fill:#c2410c;-fx-font-size:11px;-fx-font-weight:800;-fx-padding:2 10 2 10;"
            );
            headerRow.getChildren().add(badge);
        }
        paneRes.getChildren().set(0, headerRow);

        // ── Cas vide ──────────────────────────────────────
        if (reservations.isEmpty()) {
            paneRes.getChildren().add(
                    emptyStateRes("🏠", "Aucune réservation d'hébergement.", "Réserver un hébergement")
            );
            return;
        }

        // ── ScrollPane avec les cartes ────────────────────
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle(
                "-fx-background-color:transparent;-fx-background:transparent;-fx-border-color:transparent;"
        );
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox cardsContainer = new VBox(16);
        cardsContainer.setStyle("-fx-background-color:#f1f5f9;-fx-padding:4 0 8 0;");

        for (Reservation r : reservations) {
            cardsContainer.getChildren().add(buildReservationCard(r, user));
        }

        scroll.setContent(cardsContainer);
        paneRes.getChildren().add(scroll);
    }

    // ── Construction d'une carte réservation hébergement ──
    private VBox buildReservationCard(Reservation r, utilisateur user) {

        boolean isAnnule    = "ANNULE".equalsIgnoreCase(r.getStatut());
        boolean isEnAttente = "EN_ATTENTE".equalsIgnoreCase(r.getStatut());
        boolean isConfirme  = !isAnnule && !isEnAttente;

        // Couleurs selon statut
        String borderColor, stripColor;
        if (isAnnule) {
            borderColor = "#fecaca";
            stripColor  = "linear-gradient(from 0% 0% to 100% 0%, #ef4444, #f87171)";
        } else if (isEnAttente) {
            borderColor = "#bfdbfe";
            stripColor  = "linear-gradient(from 0% 0% to 100% 0%, #635bff, #818cf8)";
        } else {
            borderColor = "#bbf7d0";
            stripColor  = "linear-gradient(from 0% 0% to 100% 0%, #16a34a, #4ade80)";
        }

        // ── Carte ─────────────────────────────────────────
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color:white;-fx-background-radius:20;-fx-border-radius:20;" +
                        "-fx-border-color:" + borderColor + ";-fx-border-width:1.5;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),20,0,0,5);"
        );
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color:white;-fx-background-radius:20;-fx-border-radius:20;" +
                        "-fx-border-color:" + borderColor + ";-fx-border-width:1.5;" +
                        "-fx-effect:dropshadow(gaussian,rgba(200,113,74,0.18),28,0,0,8);-fx-translate-y:-2;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color:white;-fx-background-radius:20;-fx-border-radius:20;" +
                        "-fx-border-color:" + borderColor + ";-fx-border-width:1.5;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),20,0,0,5);"
        ));

        // Bande colorée top
        Pane strip = new Pane();
        strip.setPrefHeight(5);
        strip.setStyle("-fx-background-color:" + stripColor + ";-fx-background-radius:20 20 0 0;");

        // Corps
        VBox body = new VBox(16);
        body.setPadding(new Insets(20, 26, 22, 26));

        // Badge statut
        if (isAnnule)
            body.getChildren().add(makeBadge("❌  Annulé",                  "#ffebee", "#fecaca", "#E24B4A"));
        else if (isEnAttente)
            body.getChildren().add(makeBadge("⏳  En attente de paiement",  "#eff6ff", "#bfdbfe", "#1d4ed8"));
        else
            body.getChildren().add(makeBadge("✅  Confirmé",                "#E1F5EE", "#bbf7d0", "#085041"));

        // ── Ligne Début / Icône / Fin ─────────────────────
        HBox datesBox = new HBox();
        datesBox.setAlignment(Pos.CENTER);
        datesBox.setStyle(
                "-fx-background-color:#f8fafc;-fx-background-radius:14;" +
                        "-fx-border-color:#f0f4f8;-fx-border-radius:14;-fx-border-width:1;-fx-padding:16 20 16 20;"
        );

        // Début
        VBox debutBox = new VBox(3);
        HBox.setHgrow(debutBox, Priority.ALWAYS);
        Label lbDebutTitre = new Label("DÉBUT");
        lbDebutTitre.setStyle("-fx-font-size:9px;-fx-text-fill:#94a3b8;-fx-font-weight:800;");
        Label lbDebutDate = new Label(r.getDateDebut() != null ? r.getDateDebut().format(FMT) : "—");
        lbDebutDate.setStyle("-fx-font-size:17px;-fx-font-weight:900;-fx-text-fill:#1e293b;");
        Label lbDebutNuits = new Label(r.getNbNuits() + " nuit" + (r.getNbNuits() > 1 ? "s" : ""));
        lbDebutNuits.setStyle("-fx-font-size:11px;-fx-text-fill:#f97316;-fx-font-weight:700;");
        debutBox.getChildren().addAll(lbDebutTitre, lbDebutDate, lbDebutNuits);

        // Centre icône
        VBox centerBox = new VBox(5);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMinWidth(80);
        Label houseIco = new Label("🏠");
        houseIco.setStyle("-fx-font-size:24px;");
        Label resIdLbl = new Label("Rés. #" + r.getId());
        resIdLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;-fx-font-weight:700;");
        centerBox.getChildren().addAll(houseIco, resIdLbl);

        // Fin
        VBox finBox = new VBox(3);
        finBox.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(finBox, Priority.ALWAYS);
        Label lbFinTitre = new Label("FIN");
        lbFinTitre.setStyle("-fx-font-size:9px;-fx-text-fill:#94a3b8;-fx-font-weight:800;");
        Label lbFinDate = new Label(r.getDateFin() != null ? r.getDateFin().format(FMT) : "—");
        lbFinDate.setStyle("-fx-font-size:17px;-fx-font-weight:900;-fx-text-fill:#1e293b;");
        Label lbNom = new Label("👤 " + nvl(r.getClientNom(), "—"));
        lbNom.setStyle("-fx-font-size:11px;-fx-text-fill:#635bff;-fx-font-weight:700;");
        finBox.getChildren().addAll(lbFinTitre, lbFinDate, lbNom);

        datesBox.getChildren().addAll(debutBox, centerBox, finBox);

        // ── Footer : prix + boutons ───────────────────────
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox prixBox = new VBox(2);
        Label prixTitre = new Label("PRIX TOTAL");
        prixTitre.setStyle("-fx-font-size:9px;-fx-text-fill:#94a3b8;-fx-font-weight:800;");
        Label prixVal = new Label(String.format("%.2f DT", r.getTotal()));
        prixVal.setStyle("-fx-font-size:26px;-fx-font-weight:900;-fx-text-fill:#f97316;");
        prixBox.getChildren().addAll(prixTitre, prixVal);
        HBox.setHgrow(prixBox, Priority.ALWAYS);

        VBox actionsBox = new VBox(8);
        actionsBox.setMinWidth(200);

        if (isAnnule) {
            Label b = new Label("❌  Réservation annulée");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setAlignment(Pos.CENTER);
            b.setStyle(
                    "-fx-background-color:#ffebee;-fx-border-color:#fecaca;" +
                            "-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1;" +
                            "-fx-text-fill:#E24B4A;-fx-font-size:13px;-fx-font-weight:700;-fx-padding:11 16 11 16;"
            );
            actionsBox.getChildren().add(b);

        } else if (isEnAttente) {
            Button btnPay = new Button("💳  Payer maintenant");
            btnPay.setMaxWidth(Double.MAX_VALUE);
            final String psBase =
                    "-fx-background-color:linear-gradient(from 0% 0% to 100% 0%, #635bff, #4f46e5);" +
                            "-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:700;" +
                            "-fx-background-radius:10;-fx-padding:11 16 11 16;-fx-cursor:hand;" +
                            "-fx-effect:dropshadow(gaussian,rgba(99,91,255,0.35),14,0,0,4);";
            btnPay.setStyle(psBase);
            btnPay.setOnMouseEntered(e -> btnPay.setStyle(
                    "-fx-background-color:linear-gradient(from 0% 0% to 100% 0%, #4f46e5, #4338ca);" +
                            "-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:700;" +
                            "-fx-background-radius:10;-fx-padding:11 16 11 16;-fx-cursor:hand;"));
            btnPay.setOnMouseExited(e -> btnPay.setStyle(psBase));
            btnPay.setOnAction(e -> showAlert(Alert.AlertType.INFORMATION, "Paiement",
                    "Ouvrez votre navigateur pour finaliser le paiement de la réservation #" + r.getId()));

            Button btnCancel = new Button("✕  Annuler la réservation");
            btnCancel.setMaxWidth(Double.MAX_VALUE);
            final String csBase =
                    "-fx-background-color:white;-fx-border-color:#e2e8f0;" +
                            "-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1.5;" +
                            "-fx-text-fill:#94a3b8;-fx-font-size:12px;-fx-font-weight:600;-fx-padding:8 16 8 16;-fx-cursor:hand;";
            btnCancel.setStyle(csBase);
            btnCancel.setOnMouseEntered(e -> btnCancel.setStyle(
                    "-fx-background-color:#fef2f2;-fx-border-color:#fecaca;" +
                            "-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1.5;" +
                            "-fx-text-fill:#ef4444;-fx-font-size:12px;-fx-font-weight:600;-fx-padding:8 16 8 16;-fx-cursor:hand;"));
            btnCancel.setOnMouseExited(e -> btnCancel.setStyle(csBase));
            btnCancel.setOnAction(e -> handleAnnulerReservation(r, user));

            actionsBox.getChildren().addAll(btnPay, btnCancel);

        } else {
            // Confirmé
            Label b = new Label("✅  Payé & Confirmé");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setAlignment(Pos.CENTER);
            b.setStyle(
                    "-fx-background-color:#E1F5EE;-fx-border-color:#bbf7d0;" +
                            "-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1;" +
                            "-fx-text-fill:#085041;-fx-font-size:13px;-fx-font-weight:700;-fx-padding:11 16 11 16;"
            );
            actionsBox.getChildren().add(b);
        }

        footer.getChildren().addAll(prixBox, actionsBox);
        body.getChildren().addAll(datesBox, footer);
        card.getChildren().addAll(strip, body);
        return card;
    }

    // ── Annuler une réservation hébergement ───────────────
    private void handleAnnulerReservation(Reservation r, utilisateur user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annuler la réservation");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Confirmer l'annulation de la réservation #" + r.getId() +
                        "\ndu " + r.getDateDebut().format(FMT) + " au " + r.getDateFin().format(FMT) + " ?"
        );
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    reservationService.updateStatus(r.getId(), "ANNULE");
                    loadReservationsCards(user);
                    showStyledPopup("info", "Réservation annulée",
                            "Votre réservation #" + r.getId() + " a été annulée.", "OK");
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'annuler : " + ex.getMessage());
                }
            }
        });
    }

    private VBox emptyStateRes(String icon, String msg, String btnText) {
        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60, 20, 60, 20));
        box.setStyle(
                "-fx-background-color:white;-fx-background-radius:20;" +
                        "-fx-border-color:#e2e8f0;-fx-border-radius:20;-fx-border-width:1.5;-fx-border-style:dashed;"
        );
        Label ico = new Label(icon); ico.setStyle("-fx-font-size:48px;");
        Label txt = new Label(msg);  txt.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:15px;-fx-font-weight:600;");
        box.getChildren().addAll(ico, txt);
        if (btnText != null) {
            Button b = new Button(btnText);
            b.setStyle(
                    "-fx-background-color:#c8714a;-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:700;" +
                            "-fx-background-radius:10;-fx-padding:10 24 10 24;-fx-cursor:hand;"
            );
            box.getChildren().add(b);
        }
        return box;
    }

    // ══════════════════════════════════════════════════════
    // VOYAGES CARDS (inchangé)
    // ══════════════════════════════════════════════════════
    private void loadVoyagesCards(utilisateur user) {
        if (voyagesCardsContainer == null) return;
        voyagesCardsContainer.getChildren().clear();
        if (user == null) {
            voyagesCardsContainer.getChildren().add(emptyState("✈", "Utilisateur non connecté.", null));
            return;
        }
        System.out.println("=== loadVoyagesCards userId = " + user.getId() + " email = " + user.getEmail());
        List<Voyage> voyages = voyageService.getVoyagesByUser(user.getId());
        System.out.println("=== voyages trouvés : " + voyages.size());
        if (lblStatVoyages != null) lblStatVoyages.setText(String.valueOf(voyages.size()));
        if (lblVoyBadge    != null) lblVoyBadge.setText(voyages.isEmpty() ? "" : voyages.size() + " voyage(s)");
        if (voyages.isEmpty()) {
            voyagesCardsContainer.getChildren().add(
                    emptyState("✈", "Aucun voyage réservé pour le moment.", "Explorer les destinations"));
            return;
        }
        for (Voyage v : voyages) voyagesCardsContainer.getChildren().add(buildVoyageCard(v));
    }

    private VBox buildVoyageCard(Voyage v) {
        boolean isPaid   = v.getPaid() == 1;
        boolean isExpire = v.getDateDepart() != null && v.getDateDepart().isBefore(java.time.LocalDate.now());

        String borderColor = isPaid ? "#bbf7d0" : isExpire ? "#fecaca" : "#e2e8f0";
        String stripColor  = isPaid
                ? "linear-gradient(from 0% 0% to 100% 0%, #16a34a, #4ade80)"
                : isExpire
                ? "linear-gradient(from 0% 0% to 100% 0%, #ef4444, #f87171)"
                : "linear-gradient(from 0% 0% to 100% 0%, #635bff, #818cf8)";

        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:20;-fx-border-radius:20;" +
                "-fx-border-color:" + borderColor + ";-fx-border-width:1.5;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),20,0,0,5);");
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color:white;-fx-background-radius:20;-fx-border-radius:20;" +
                "-fx-border-color:" + borderColor + ";-fx-border-width:1.5;" +
                "-fx-effect:dropshadow(gaussian,rgba(200,113,74,0.18),28,0,0,8);-fx-translate-y:-2;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color:white;-fx-background-radius:20;-fx-border-radius:20;" +
                "-fx-border-color:" + borderColor + ";-fx-border-width:1.5;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),20,0,0,5);"));

        Pane strip = new Pane();
        strip.setPrefHeight(5);
        strip.setStyle("-fx-background-color:" + stripColor + ";-fx-background-radius:20 20 0 0;");

        VBox body = new VBox(16);
        body.setPadding(new Insets(20, 26, 22, 26));

        if (isPaid)        body.getChildren().add(makeBadge("✅  Paiement confirmé",       "#E1F5EE", "#bbf7d0", "#085041"));
        else if (isExpire) body.getChildren().add(makeBadge("⚠️  Voyage expiré",           "#ffebee", "#fecaca", "#E24B4A"));
        else               body.getChildren().add(makeBadge("⏳  En attente de paiement",  "#eff6ff", "#bfdbfe", "#1d4ed8"));

        HBox routeBox = new HBox();
        routeBox.setAlignment(Pos.CENTER);
        routeBox.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:14;" +
                "-fx-border-color:#f0f4f8;-fx-border-radius:14;-fx-border-width:1;-fx-padding:16 20 16 20;");

        VBox depBox = new VBox(3);
        HBox.setHgrow(depBox, Priority.ALWAYS);
        Label depLbl  = new Label("DÉPART");
        depLbl.setStyle("-fx-font-size:9px;-fx-text-fill:#94a3b8;-fx-font-weight:800;-fx-padding:0 0 2 0;");
        Label depCity = new Label(nvl(v.getPointDepart(), "—"));
        depCity.setStyle("-fx-font-size:17px;-fx-font-weight:900;-fx-text-fill:#1e293b;");
        Label depDate = new Label(v.getDateDepart() != null ? v.getDateDepart().format(FMT) : "—");
        depDate.setStyle("-fx-font-size:11px;-fx-text-fill:#f97316;-fx-font-weight:700;");
        depBox.getChildren().addAll(depLbl, depCity, depDate);

        VBox centerBox = new VBox(5);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMinWidth(90);
        HBox dotsBox = new HBox(5);
        dotsBox.setAlignment(Pos.CENTER);
        Circle d1 = new Circle(4); d1.setStyle("-fx-fill:#f97316;");
        Label planeIco = new Label("✈");
        planeIco.setStyle("-fx-font-size:20px;-fx-text-fill:#64748b;");
        Circle d2 = new Circle(4); d2.setStyle("-fx-fill:#635bff;");
        dotsBox.getChildren().addAll(d1, planeIco, d2);
        String dureeStr = "";
        if (v.getDateDepart() != null && v.getDateArrivee() != null) {
            long j = java.time.temporal.ChronoUnit.DAYS.between(v.getDateDepart(), v.getDateArrivee());
            dureeStr = j + " jour" + (j > 1 ? "s" : "");
        }
        Label dureeLbl = new Label(dureeStr);
        dureeLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;-fx-font-weight:700;");
        centerBox.getChildren().addAll(dotsBox, dureeLbl);

        VBox arrBox = new VBox(3);
        arrBox.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(arrBox, Priority.ALWAYS);
        Label arrLbl  = new Label("ARRIVÉE");
        arrLbl.setStyle("-fx-font-size:9px;-fx-text-fill:#94a3b8;-fx-font-weight:800;-fx-padding:0 0 2 0;");
        Label arrCity = new Label(nvl(v.getPointArrivee(), "—"));
        arrCity.setStyle("-fx-font-size:17px;-fx-font-weight:900;-fx-text-fill:#1e293b;");
        Label arrDate = new Label(v.getDateArrivee() != null ? v.getDateArrivee().format(FMT) : "—");
        arrDate.setStyle("-fx-font-size:11px;-fx-text-fill:#635bff;-fx-font-weight:700;");
        arrBox.getChildren().addAll(arrLbl, arrCity, arrDate);
        routeBox.getChildren().addAll(depBox, centerBox, arrBox);

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        VBox prixBox = new VBox(2);
        Label prixLbl = new Label("PRIX TOTAL");
        prixLbl.setStyle("-fx-font-size:9px;-fx-text-fill:#94a3b8;-fx-font-weight:800;");
        Label prixVal = new Label(String.format("%.0f €", v.getPrix()));
        prixVal.setStyle("-fx-font-size:26px;-fx-font-weight:900;-fx-text-fill:#f97316;");
        prixBox.getChildren().addAll(prixLbl, prixVal);
        HBox.setHgrow(prixBox, Priority.ALWAYS);

        VBox actionsBox = new VBox(8);
        actionsBox.setMinWidth(190);
        if (isPaid) {
            Label b = new Label("✅  Payé & Confirmé"); b.setMaxWidth(Double.MAX_VALUE); b.setAlignment(Pos.CENTER);
            b.setStyle("-fx-background-color:#E1F5EE;-fx-border-color:#bbf7d0;-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1;-fx-text-fill:#085041;-fx-font-size:13px;-fx-font-weight:700;-fx-padding:11 16 11 16;");
            actionsBox.getChildren().add(b);
        } else if (isExpire) {
            Label b = new Label("❌  Expiré"); b.setMaxWidth(Double.MAX_VALUE); b.setAlignment(Pos.CENTER);
            b.setStyle("-fx-background-color:#ffebee;-fx-border-color:#fecaca;-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1;-fx-text-fill:#E24B4A;-fx-font-size:13px;-fx-font-weight:700;-fx-padding:11 16 11 16;");
            actionsBox.getChildren().add(b);
        } else {
            Button btnPay = new Button("💳  Payer maintenant"); btnPay.setMaxWidth(Double.MAX_VALUE);
            btnPay.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 100% 0%, #635bff, #4f46e5);-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:700;-fx-background-radius:10;-fx-padding:11 16 11 16;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(99,91,255,0.35),14,0,0,4);");
            btnPay.setOnMouseEntered(e -> btnPay.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 100% 0%, #4f46e5, #4338ca);-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:700;-fx-background-radius:10;-fx-padding:11 16 11 16;-fx-cursor:hand;"));
            btnPay.setOnMouseExited(e  -> btnPay.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 100% 0%, #635bff, #4f46e5);-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:700;-fx-background-radius:10;-fx-padding:11 16 11 16;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(99,91,255,0.35),14,0,0,4);"));
            btnPay.setOnAction(e -> handlePayer(v));
            Button btnCancel = new Button("✕  Annuler la réservation"); btnCancel.setMaxWidth(Double.MAX_VALUE);
            btnCancel.setStyle("-fx-background-color:white;-fx-border-color:#e2e8f0;-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1.5;-fx-text-fill:#94a3b8;-fx-font-size:12px;-fx-font-weight:600;-fx-padding:8 16 8 16;-fx-cursor:hand;");
            btnCancel.setOnMouseEntered(e -> btnCancel.setStyle("-fx-background-color:#fef2f2;-fx-border-color:#fecaca;-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1.5;-fx-text-fill:#ef4444;-fx-font-size:12px;-fx-font-weight:600;-fx-padding:8 16 8 16;-fx-cursor:hand;"));
            btnCancel.setOnMouseExited(e  -> btnCancel.setStyle("-fx-background-color:white;-fx-border-color:#e2e8f0;-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1.5;-fx-text-fill:#94a3b8;-fx-font-size:12px;-fx-font-weight:600;-fx-padding:8 16 8 16;-fx-cursor:hand;"));
            btnCancel.setOnAction(e -> handleAnnuler(v));
            actionsBox.getChildren().addAll(btnPay, btnCancel);
        }

        footer.getChildren().addAll(prixBox, actionsBox);
        body.getChildren().addAll(routeBox, footer);
        card.getChildren().addAll(strip, body);
        return card;
    }

    // ══════════════════════════════════════════════════════
    // PAIEMENT STRIPE VOYAGES
    // ══════════════════════════════════════════════════════
    private void handlePayer(Voyage v) {
        showAlert(Alert.AlertType.INFORMATION, "Redirection...", "Ouverture de la page de paiement Stripe…");
        new Thread(() -> {
            try {
                String desc = "Voyage " + nvl(v.getPointDepart(), "?") + " → " + nvl(v.getPointArrivee(), "?");
                String url  = StripeService.createCheckoutUrl(v.getId(), v.getPrix(), desc);
                javafx.application.Platform.runLater(() -> openStripeWebView(v, url));
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Erreur Stripe", ex.getMessage()));
            }
        }).start();
    }

    private void openStripeWebView(Voyage v, String stripeUrl) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Paiement sécurisé — Stripe");
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
        javafx.scene.web.WebEngine engine = webView.getEngine();
        engine.locationProperty().addListener((obs, oldUrl, newUrl) -> {
            if (newUrl != null && newUrl.contains("localhost:8080/success")) {
                utilisateur currentUser = MainLayoutController.getCurrentUser();
                boolean ok = voyageService.marquerCommePaye(v.getId(), currentUser.getId());
                javafx.application.Platform.runLater(() -> {
                    stage.close();
                    if (ok) { loadVoyagesCards(currentUser); showStyledPopup("success","Paiement confirmé !","Votre voyage a été payé avec succès.","Parfait !"); }
                    else    { showStyledPopup("warning","Attention","Paiement reçu mais mise à jour échouée.","OK"); }
                });
            } else if (newUrl != null && newUrl.contains("localhost:8080/cancel")) {
                javafx.application.Platform.runLater(() -> { stage.close(); showStyledPopup("info","Paiement annulé","Vous avez annulé le paiement.","Retour"); });
            }
        });
        engine.load(stripeUrl);
        stage.setScene(new javafx.scene.Scene(webView, 900, 700));
        stage.show();
    }

    private void handleAnnuler(Voyage v) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annuler la réservation"); confirm.setHeaderText(null);
        confirm.setContentText("Confirmer l'annulation du voyage " + nvl(v.getPointDepart(),"?") + " → " + nvl(v.getPointArrivee(),"?") + " ?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                utilisateur user = MainLayoutController.getCurrentUser();
                if (user != null) {
                    boolean ok = voyageService.annulerReservation(v.getId(), user.getId());
                    if (ok) loadVoyagesCards(user);
                    else showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'annuler.");
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════
    // POPUP STYLISÉE
    // ══════════════════════════════════════════════════════
    private void showStyledPopup(String type, String title, String message, String btnLabel) {
        javafx.application.Platform.runLater(() -> {
            javafx.stage.Stage popup = new javafx.stage.Stage();
            popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popup.initStyle(javafx.stage.StageStyle.UNDECORATED);
            String stripGrad, iconBg, btnGrad, btnHover, iconText;
            switch (type) {
                case "success" -> { stripGrad="linear-gradient(from 0% 0% to 100% 0%, #16a34a, #4ade80)"; iconBg="#dcfce7"; btnGrad="linear-gradient(from 0% 0% to 100% 0%, #16a34a, #4ade80)"; btnHover="linear-gradient(from 0% 0% to 100% 0%, #15803d, #16a34a)"; iconText="✅"; }
                case "warning" -> { stripGrad="linear-gradient(from 0% 0% to 100% 0%, #f59e0b, #fbbf24)"; iconBg="#fef3c7"; btnGrad="linear-gradient(from 0% 0% to 100% 0%, #d97706, #f59e0b)"; btnHover="linear-gradient(from 0% 0% to 100% 0%, #b45309, #d97706)"; iconText="⚠️"; }
                default        -> { stripGrad="linear-gradient(from 0% 0% to 100% 0%, #635bff, #818cf8)"; iconBg="#ede9fe"; btnGrad="linear-gradient(from 0% 0% to 100% 0%, #635bff, #4f46e5)"; btnHover="linear-gradient(from 0% 0% to 100% 0%, #4f46e5, #4338ca)"; iconText="ℹ️"; }
            }
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color:white;-fx-background-radius:20;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.22),40,0,0,10);");
            root.setPrefWidth(440);
            Pane topStrip = new Pane(); topStrip.setPrefHeight(6);
            topStrip.setStyle("-fx-background-color:" + stripGrad + ";-fx-background-radius:20 20 0 0;");
            VBox body = new VBox(18); body.setPadding(new Insets(36,40,36,40)); body.setAlignment(Pos.CENTER);
            StackPane iconCircle = new StackPane(); iconCircle.setMinSize(76,76); iconCircle.setMaxSize(76,76);
            iconCircle.setStyle("-fx-background-color:" + iconBg + ";-fx-background-radius:38;");
            Label iconLbl = new Label(iconText); iconLbl.setStyle("-fx-font-size:34px;");
            iconCircle.getChildren().add(iconLbl);
            Label titleLbl = new Label(title); titleLbl.setStyle("-fx-font-size:20px;-fx-font-weight:900;-fx-text-fill:#1e293b;"); titleLbl.setWrapText(true); titleLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            Label msgLbl   = new Label(message); msgLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#64748b;-fx-font-weight:500;"); msgLbl.setWrapText(true); msgLbl.setMaxWidth(360); msgLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            Pane sep = new Pane(); sep.setPrefHeight(1); sep.setMaxWidth(Double.MAX_VALUE); sep.setStyle("-fx-background-color:#f1f5f9;");
            Button btnOk = new Button(btnLabel); btnOk.setMaxWidth(Double.MAX_VALUE);
            final String bStyle = "-fx-background-color:" + btnGrad + ";-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:800;-fx-background-radius:12;-fx-padding:13 0 13 0;-fx-cursor:hand;";
            final String bHover = "-fx-background-color:" + btnHover + ";-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:800;-fx-background-radius:12;-fx-padding:13 0 13 0;-fx-cursor:hand;";
            btnOk.setStyle(bStyle); btnOk.setOnMouseEntered(e->btnOk.setStyle(bHover)); btnOk.setOnMouseExited(e->btnOk.setStyle(bStyle)); btnOk.setOnAction(e->popup.close());
            body.getChildren().addAll(iconCircle, titleLbl, msgLbl, sep, btnOk);
            root.getChildren().addAll(topStrip, body);
            final double[] off = {0,0};
            root.setOnMousePressed(e->{off[0]=e.getSceneX();off[1]=e.getSceneY();});
            root.setOnMouseDragged(e->{popup.setX(e.getScreenX()-off[0]);popup.setY(e.getScreenY()-off[1]);});
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            popup.setScene(scene); popup.show();
        });
    }

    // ══════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════
    @FXML
    public void gotoAjouterAvis() {
        if (ClientLayoutController.getInstance() != null) {
            ClientLayoutController.getInstance().loadPage("/user_avis.fxml");
        }
    }

    @FXML
    public void gotoAjouterReclamation() {
        if (ClientLayoutController.getInstance() != null) {
            ClientLayoutController.getInstance().loadPage("/user_ajouter_reclamation.fxml");
        }
    }

    // ══════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════
    private Label makeBadge(String text, String bg, String border, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color:" + bg + ";-fx-border-color:" + border +
                ";-fx-border-radius:20;-fx-background-radius:20;-fx-border-width:1;" +
                "-fx-text-fill:" + color + ";-fx-font-size:12px;-fx-font-weight:700;-fx-padding:5 14 5 14;");
        return l;
    }

    private VBox emptyState(String icon, String msg, String btnText) {
        VBox box = new VBox(14); box.setAlignment(Pos.CENTER); box.setPadding(new Insets(60,20,60,20));
        box.setStyle("-fx-background-color:white;-fx-background-radius:20;-fx-border-color:#e2e8f0;-fx-border-radius:20;-fx-border-width:1.5;-fx-border-style:dashed;");
        Label ico = new Label(icon); ico.setStyle("-fx-font-size:48px;");
        Label txt = new Label(msg);  txt.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:15px;-fx-font-weight:600;");
        box.getChildren().addAll(ico, txt);
        if (btnText != null) {
            Button b = new Button(btnText);
            b.setStyle("-fx-background-color:#f97316;-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:700;-fx-background-radius:10;-fx-padding:10 24 10 24;-fx-cursor:hand;");
            box.getChildren().add(b);
        }
        return box;
    }

    private void setEmptyTable(TableView<?> tv, Label lbl, String msg) {
        if (tv  != null) tv.setItems(FXCollections.observableArrayList());
        if (lbl != null) lbl.setText(msg);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        javafx.application.Platform.runLater(() -> {
            Alert a = new Alert(type);
            a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
            a.showAndWait();
        });
    }

    private String nvl(String s, String fallback) {
        return (s != null && !s.isBlank()) ? s : fallback;
    }
}