package hebergement.controllers;

import edu.destination.entities.Voyage;
import edu.destination.services.VoyageService;
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
    @FXML private VBox paneHeb;
    @FXML private VBox paneBlogs;
    @FXML private VBox paneRec;
    @FXML private VBox paneRes;
    @FXML private VBox paneAct;
    @FXML private VBox paneVoy;

    // ── Tables ────────────────────────────────────────────
    @FXML private TableView<Object>          tvHebergements;
    @FXML private TableColumn<Object,String> colHebId, colHebDesc, colHebAdresse, colHebPrix, colHebType;
    @FXML private Label lblHebMsg;

    @FXML private TableView<Object>          tvBlogs;
    @FXML private TableColumn<Object,String> colBlogId, colBlogTitre, colBlogDate;
    @FXML private Label lblBlogMsg;

    @FXML private TableView<Object>          tvReclamations;
    @FXML private TableColumn<Object,String> colRecId, colRecSujet, colRecStatut, colRecDate;
    @FXML private Label lblRecMsg;

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
    private final VoyageService voyageService   = new VoyageService();

    private static boolean openVoyagesTab = false;
    public static void requestOpenVoyagesTab() { openVoyagesTab = true; }

    private VBox[]    panes;
    private Button[]  tabBtns;
    private int       activeTab = 0;

    // ══════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        utilisateur user = MainLayoutController.getCurrentUser();
        setupHeader(user);
        setEmptyTable(tvHebergements, lblHebMsg, "Aucun hébergement.");
        setEmptyTable(tvBlogs,        lblBlogMsg, "Aucun article.");
        setEmptyTable(tvReclamations, lblRecMsg,  "Aucune réclamation.");
        setEmptyTable(tvReservations, lblResMsg,  "Aucune réservation d'hébergement.");
        setEmptyTable(tvActivites,    lblActMsg,  "Aucune activité.");
        loadVoyagesCards(user);
        buildTabBar();
        if (openVoyagesTab) {
            selectTab(5);
            openVoyagesTab = false;
        }
    }

    // ══════════════════════════════════════════════════════
    // HEADER
    // ══════════════════════════════════════════════════════
    private void setupHeader(utilisateur user) {
        if (user == null) return;
        String nom    = nvl(user.getNom(),    "");
        String prenom = nvl(user.getPrenom(), "");
        String init   = (!nom.isEmpty() ? nom.substring(0,1).toUpperCase() : "")
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
        panes = new VBox[]{ paneHeb, paneBlogs, paneRec, paneRes, paneAct, paneVoy };
        String[][] tabs = {
                {"🏠", "Hébergements"}, {"📝", "Mes Blogs"}, {"🚨", "Réclamations"},
                {"📅", "Réservations"}, {"🎯", "Activités"}, {"✈️", "Mes Voyages"}
        };
        tabBtns = new Button[tabs.length];
        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            Button btn = new Button(tabs[i][0] + "  " + tabs[i][1]);
            btn.setStyle(tabStyle(false));
            btn.setOnAction(e -> selectTab(idx));
            btn.setOnMouseEntered(e -> { if (idx != activeTab) btn.setStyle(tabHoverStyle()); });
            btn.setOnMouseExited(e ->  { if (idx != activeTab) btn.setStyle(tabStyle(false)); });
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
    // VOYAGES CARDS
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

        if (isPaid) {
            body.getChildren().add(makeBadge("✅  Paiement confirmé", "#E1F5EE", "#bbf7d0", "#085041"));
        } else if (isExpire) {
            body.getChildren().add(makeBadge("⚠️  Voyage expiré", "#ffebee", "#fecaca", "#E24B4A"));
        } else {
            body.getChildren().add(makeBadge("⏳  En attente de paiement", "#eff6ff", "#bfdbfe", "#1d4ed8"));
        }

        // Route
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

        // Footer
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
            Label b = new Label("✅  Payé & Confirmé");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setAlignment(Pos.CENTER);
            b.setStyle("-fx-background-color:#E1F5EE;-fx-border-color:#bbf7d0;" +
                    "-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1;" +
                    "-fx-text-fill:#085041;-fx-font-size:13px;-fx-font-weight:700;-fx-padding:11 16 11 16;");
            actionsBox.getChildren().add(b);
        } else if (isExpire) {
            Label b = new Label("❌  Expiré");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setAlignment(Pos.CENTER);
            b.setStyle("-fx-background-color:#ffebee;-fx-border-color:#fecaca;" +
                    "-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1;" +
                    "-fx-text-fill:#E24B4A;-fx-font-size:13px;-fx-font-weight:700;-fx-padding:11 16 11 16;");
            actionsBox.getChildren().add(b);
        } else {
            Button btnPay = new Button("💳  Payer maintenant");
            btnPay.setMaxWidth(Double.MAX_VALUE);
            btnPay.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 100% 0%, #635bff, #4f46e5);" +
                    "-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:700;" +
                    "-fx-background-radius:10;-fx-padding:11 16 11 16;-fx-cursor:hand;" +
                    "-fx-effect:dropshadow(gaussian,rgba(99,91,255,0.35),14,0,0,4);");
            btnPay.setOnMouseEntered(e -> btnPay.setStyle(
                    "-fx-background-color:linear-gradient(from 0% 0% to 100% 0%, #4f46e5, #4338ca);" +
                            "-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:700;" +
                            "-fx-background-radius:10;-fx-padding:11 16 11 16;-fx-cursor:hand;"));
            btnPay.setOnMouseExited(e -> btnPay.setStyle(
                    "-fx-background-color:linear-gradient(from 0% 0% to 100% 0%, #635bff, #4f46e5);" +
                            "-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:700;" +
                            "-fx-background-radius:10;-fx-padding:11 16 11 16;-fx-cursor:hand;" +
                            "-fx-effect:dropshadow(gaussian,rgba(99,91,255,0.35),14,0,0,4);"));
            btnPay.setOnAction(e -> handlePayer(v));

            Button btnCancel = new Button("✕  Annuler la réservation");
            btnCancel.setMaxWidth(Double.MAX_VALUE);
            btnCancel.setStyle("-fx-background-color:white;-fx-border-color:#e2e8f0;" +
                    "-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1.5;" +
                    "-fx-text-fill:#94a3b8;-fx-font-size:12px;-fx-font-weight:600;-fx-padding:8 16 8 16;-fx-cursor:hand;");
            btnCancel.setOnMouseEntered(e -> btnCancel.setStyle(
                    "-fx-background-color:#fef2f2;-fx-border-color:#fecaca;" +
                            "-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1.5;" +
                            "-fx-text-fill:#ef4444;-fx-font-size:12px;-fx-font-weight:600;-fx-padding:8 16 8 16;-fx-cursor:hand;"));
            btnCancel.setOnMouseExited(e -> btnCancel.setStyle(
                    "-fx-background-color:white;-fx-border-color:#e2e8f0;" +
                            "-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1.5;" +
                            "-fx-text-fill:#94a3b8;-fx-font-size:12px;-fx-font-weight:600;-fx-padding:8 16 8 16;-fx-cursor:hand;"));
            btnCancel.setOnAction(e -> handleAnnuler(v));
            actionsBox.getChildren().addAll(btnPay, btnCancel);
        }

        footer.getChildren().addAll(prixBox, actionsBox);
        body.getChildren().addAll(routeBox, footer);
        card.getChildren().addAll(strip, body);
        return card;
    }

    // ══════════════════════════════════════════════════════
    // PAIEMENT STRIPE
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
                    if (ok) {
                        loadVoyagesCards(currentUser);
                        showStyledPopup(
                                "success",
                                "Paiement confirmé !",
                                "Votre voyage " + nvl(v.getPointDepart(), "") + " → " + nvl(v.getPointArrivee(), "") + " a été payé avec succès.",
                                "Parfait !"
                        );
                    } else {
                        showStyledPopup(
                                "warning",
                                "Attention",
                                "Paiement reçu mais mise à jour échouée. Contactez le support.",
                                "OK"
                        );
                    }
                });
            } else if (newUrl != null && newUrl.contains("localhost:8080/cancel")) {
                javafx.application.Platform.runLater(() -> {
                    stage.close();
                    showStyledPopup("info", "Paiement annulé", "Vous avez annulé le paiement.", "Retour");
                });
            }
        });

        engine.load(stripeUrl);
        javafx.scene.Scene scene = new javafx.scene.Scene(webView, 900, 700);
        stage.setScene(scene);
        stage.show();
    }

    private void handleAnnuler(Voyage v) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annuler la réservation");
        confirm.setHeaderText(null);
        confirm.setContentText("Confirmer l'annulation du voyage "
                + nvl(v.getPointDepart(), "?") + " → " + nvl(v.getPointArrivee(), "?") + " ?");
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
    // type: "success" | "warning" | "info"
    // ══════════════════════════════════════════════════════
    private void showStyledPopup(String type, String title, String message, String btnLabel) {
        javafx.application.Platform.runLater(() -> {
            javafx.stage.Stage popup = new javafx.stage.Stage();
            popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popup.initStyle(javafx.stage.StageStyle.UNDECORATED);

            // Couleurs selon le type
            String stripGrad, iconBg, btnGrad, btnHover, iconText;
            switch (type) {
                case "success" -> {
                    stripGrad = "linear-gradient(from 0% 0% to 100% 0%, #16a34a, #4ade80)";
                    iconBg    = "#dcfce7"; btnGrad = "linear-gradient(from 0% 0% to 100% 0%, #16a34a, #4ade80)";
                    btnHover  = "linear-gradient(from 0% 0% to 100% 0%, #15803d, #16a34a)";
                    iconText  = "✅";
                }
                case "warning" -> {
                    stripGrad = "linear-gradient(from 0% 0% to 100% 0%, #f59e0b, #fbbf24)";
                    iconBg    = "#fef3c7"; btnGrad = "linear-gradient(from 0% 0% to 100% 0%, #d97706, #f59e0b)";
                    btnHover  = "linear-gradient(from 0% 0% to 100% 0%, #b45309, #d97706)";
                    iconText  = "⚠️";
                }
                default -> {
                    stripGrad = "linear-gradient(from 0% 0% to 100% 0%, #635bff, #818cf8)";
                    iconBg    = "#ede9fe"; btnGrad = "linear-gradient(from 0% 0% to 100% 0%, #635bff, #4f46e5)";
                    btnHover  = "linear-gradient(from 0% 0% to 100% 0%, #4f46e5, #4338ca)";
                    iconText  = "ℹ️";
                }
            }

            VBox root = new VBox(0);
            root.setStyle("-fx-background-color:white;-fx-background-radius:20;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.22),40,0,0,10);");
            root.setPrefWidth(440);

            // Bande colorée top
            Pane topStrip = new Pane();
            topStrip.setPrefHeight(6);
            topStrip.setStyle("-fx-background-color:" + stripGrad + ";-fx-background-radius:20 20 0 0;");

            // Corps
            VBox body = new VBox(18);
            body.setPadding(new Insets(36, 40, 36, 40));
            body.setAlignment(Pos.CENTER);

            // Cercle icône
            StackPane iconCircle = new StackPane();
            iconCircle.setMinSize(76, 76);
            iconCircle.setMaxSize(76, 76);
            iconCircle.setStyle("-fx-background-color:" + iconBg + ";-fx-background-radius:38;");
            Label iconLbl = new Label(iconText);
            iconLbl.setStyle("-fx-font-size:34px;");
            iconCircle.getChildren().add(iconLbl);

            // Titre
            Label titleLbl = new Label(title);
            titleLbl.setStyle("-fx-font-size:20px;-fx-font-weight:900;-fx-text-fill:#1e293b;");
            titleLbl.setWrapText(true);
            titleLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            // Message
            Label msgLbl = new Label(message);
            msgLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#64748b;-fx-font-weight:500;");
            msgLbl.setWrapText(true);
            msgLbl.setMaxWidth(360);
            msgLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            // Séparateur
            Pane sep = new Pane();
            sep.setPrefHeight(1);
            sep.setMaxWidth(Double.MAX_VALUE);
            sep.setStyle("-fx-background-color:#f1f5f9;");

            // Bouton
            Button btnOk = new Button(btnLabel);
            btnOk.setMaxWidth(Double.MAX_VALUE);
            final String bStyle = "-fx-background-color:" + btnGrad + ";" +
                    "-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:800;" +
                    "-fx-background-radius:12;-fx-padding:13 0 13 0;-fx-cursor:hand;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),14,0,0,4);";
            final String bHoverStyle = "-fx-background-color:" + btnHover + ";" +
                    "-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:800;" +
                    "-fx-background-radius:12;-fx-padding:13 0 13 0;-fx-cursor:hand;";
            btnOk.setStyle(bStyle);
            btnOk.setOnMouseEntered(e -> btnOk.setStyle(bHoverStyle));
            btnOk.setOnMouseExited(e  -> btnOk.setStyle(bStyle));
            btnOk.setOnAction(e -> popup.close());

            body.getChildren().addAll(iconCircle, titleLbl, msgLbl, sep, btnOk);
            root.getChildren().addAll(topStrip, body);

            // Drag
            final double[] offset = {0, 0};
            root.setOnMousePressed(e  -> { offset[0] = e.getSceneX(); offset[1] = e.getSceneY(); });
            root.setOnMouseDragged(e  -> { popup.setX(e.getScreenX() - offset[0]); popup.setY(e.getScreenY() - offset[1]); });

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            popup.setScene(scene);
            popup.show();
        });
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
        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60, 20, 60, 20));
        box.setStyle("-fx-background-color:white;-fx-background-radius:20;" +
                "-fx-border-color:#e2e8f0;-fx-border-radius:20;-fx-border-width:1.5;-fx-border-style:dashed;");
        Label ico = new Label(icon); ico.setStyle("-fx-font-size:48px;");
        Label txt = new Label(msg);  txt.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:15px;-fx-font-weight:600;");
        box.getChildren().addAll(ico, txt);
        if (btnText != null) {
            Button b = new Button(btnText);
            b.setStyle("-fx-background-color:#f97316;-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:700;" +
                    "-fx-background-radius:10;-fx-padding:10 24 10 24;-fx-cursor:hand;");
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