package hebergement.controllers;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;

import hebergement.entities.Chambre;
import hebergement.entities.Hebergement;
import hebergement.entities.Reservation;
import hebergement.services.ChambreService;
import hebergement.services.HebergementService;
import hebergement.services.PaymentService;
import hebergement.services.ReservationService;
import hebergement.tools.LocalPaymentCallbackServer;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;


import java.awt.Desktop;
import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class HebergementDetailController {

    @FXML private ImageView imgPrincipal;
    @FXML private Label lblBreadcrumb, lblHeroType, lblHeroPrix;
    @FXML private Label lblTypeBadge, lblDescription, lblPrixInfo, lblAdresse;
    @FXML private Label lblNbChambres, lblPrixStat;
    @FXML private FlowPane chambresPane;
    @FXML private WebView mapView;

    // Chambre sélectionnée
    @FXML private VBox panelChambreSelectionnee;
    @FXML private Label lblChambreInfo, lblChambrePrix;

    // Formulaire
    @FXML private TextField tfNom, tfTel, tfEmail;
    @FXML private DatePicker dpDebut, dpFin;
    @FXML private VBox panelPrixTotal;
    @FXML private Label lblTotal, lblMsg;

    private final HebergementService hs = new HebergementService();
    private final ChambreService cs = new ChambreService();
    private final ReservationService rs = new ReservationService();
    private final PaymentService payService = new PaymentService();
    private final LocalPaymentCallbackServer callbackServer = new LocalPaymentCallbackServer();

    private Hebergement currentHebergement;
    private Chambre selectedChambre;
    private double selectedPrixNuit = 0;

    @FXML
    public void initialize() {
        dpDebut.valueProperty().addListener((o, ov, nv) -> computeTotal());
        dpFin.valueProperty().addListener((o, ov, nv) -> computeTotal());
        showEmptyMap();
    }

    public void setHebergement(Hebergement h) {
        this.currentHebergement = h;
        if (h == null) return;

        // ✅ Pré-remplir avec l'utilisateur connecté
        utilisateur user = MainLayoutController.getCurrentUser();
        if (user != null) {
            if (user.getPrenom() != null || user.getNom() != null) {
                tfNom.setText(
                        (user.getPrenom() != null ? user.getPrenom() : "") + " " +
                                (user.getNom() != null ? user.getNom() : "")
                );
            }
            if (user.getEmail() != null)
                tfEmail.setText(user.getEmail());
            if (user.getTelephone() != null)
                tfTel.setText(String.valueOf(user.getTelephone()));
        }

        // Hero
        lblHeroType.setText(h.getTypeLibelle() != null ? h.getTypeLibelle() : "Hébergement");
        lblHeroPrix.setText("À partir de " + String.format("%.2f", h.getPrix()) + " TND / nuit");
        lblBreadcrumb.setText("Hébergements / " + (h.getTypeLibelle() != null ? h.getTypeLibelle() : "Détail"));

        // Infos
        lblTypeBadge.setText(h.getTypeLibelle() != null ? h.getTypeLibelle() : "");
        lblDescription.setText(h.getDescription() != null ? h.getDescription() : "");
        lblPrixInfo.setText("Prix : " + String.format("%.2f", h.getPrix()) + " TND / nuit");
        lblAdresse.setText(h.getAdresse() != null ? "📍 " + h.getAdresse() : "");

        // Stats
        lblPrixStat.setText(String.format("%.0f TND", h.getPrix()));
        selectedPrixNuit = h.getPrix();

        // Image principale
        loadMainImage(h.getImagePath());

        // Chambres
        loadChambres(h.getId());

        // Carte
        if (h.getLatitude() != null && h.getLongitude() != null) {
            loadMap(h.getLatitude(), h.getLongitude(), h.getDescription());
        } else {
            showEmptyMap();
        }
    }

    private void loadMainImage(String path) {
        try {
            if (path != null && !path.isBlank()) {
                String url = path.startsWith("http") ? path :
                        "file:///" + path.replace("\\", "/");
                imgPrincipal.setImage(new Image(url, 680, 340, false, true, true));
                Rectangle clip = new Rectangle(680, 340);
                clip.setArcWidth(24); clip.setArcHeight(24);
                imgPrincipal.setClip(clip);
            }
        } catch (Exception ignored) {}
    }

    private void loadChambres(int hebId) {
        chambresPane.getChildren().clear();
        try {
            List<Chambre> list = cs.getByHebergement(hebId);
            lblNbChambres.setText(String.valueOf(list.size()));

            if (list.isEmpty()) {
                Label lbl = new Label("Aucune chambre disponible");
                lbl.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
                chambresPane.getChildren().add(lbl);
                return;
            }

            for (Chambre c : list) {
                chambresPane.getChildren().add(buildChambreCard(c));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox buildChambreCard(Chambre c) {
        VBox card = new VBox(6);
        card.setPrefWidth(200);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color: #f9f9f9;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #eee;" +
                        "-fx-border-width: 2;" +
                        "-fx-cursor: hand;"
        );

        Label lblNum = new Label("Chambre " + c.getNumero());
        lblNum.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        Label lblType = new Label(c.getTypeChambre() != null ? c.getTypeChambre() : "");
        lblType.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        Label lblPrix = new Label(String.format("%.2f TND/nuit", c.getPrixNuit()));
        lblPrix.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #f75959;");

        Label lblCap = new Label("👥 " + c.getCapacite() + " personnes");
        lblCap.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        if (c.getEquipements() != null && !c.getEquipements().isBlank()) {
            Label lblEquip = new Label("🛎 " + c.getEquipements());
            lblEquip.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");
            lblEquip.setWrapText(true);
            card.getChildren().addAll(lblNum, lblType, lblPrix, lblCap, lblEquip);
        } else {
            card.getChildren().addAll(lblNum, lblType, lblPrix, lblCap);
        }

        // Bouton sélectionner
        Button btnSel = new Button("Sélectionner cette chambre");
        btnSel.setMaxWidth(Double.MAX_VALUE);
        btnSel.setStyle(
                "-fx-background-color: #f97316;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 700;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 0;" +
                        "-fx-cursor: hand;"
        );
        card.getChildren().add(btnSel);

        // Clic — sélectionner
        btnSel.setOnAction(e -> selectChambre(c, card));
        card.setOnMouseClicked(e -> selectChambre(c, card));

        // Hover
        card.setOnMouseEntered(e -> {
            if (selectedChambre == null || selectedChambre.getId() != c.getId())
                card.setStyle(
                        "-fx-background-color: #f0f9ff;" +
                                "-fx-background-radius: 12;" +
                                "-fx-border-radius: 12;" +
                                "-fx-border-color: #f97316;" +
                                "-fx-border-width: 2;" +
                                "-fx-cursor: hand;"
                );
        });
        card.setOnMouseExited(e -> {
            if (selectedChambre == null || selectedChambre.getId() != c.getId())
                card.setStyle(
                        "-fx-background-color: #f9f9f9;" +
                                "-fx-background-radius: 12;" +
                                "-fx-border-radius: 12;" +
                                "-fx-border-color: #eee;" +
                                "-fx-border-width: 2;" +
                                "-fx-cursor: hand;"
                );
        });

        return card;
    }

    private void selectChambre(Chambre c, VBox card) {
        // Reset toutes les cards
        chambresPane.getChildren().forEach(node -> node.setStyle(
                "-fx-background-color: #f9f9f9;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #eee;" +
                        "-fx-border-width: 2;" +
                        "-fx-cursor: hand;"
        ));

        // Highlight card sélectionnée
        card.setStyle(
                "-fx-background-color: #f0fdf4;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #16a34a;" +
                        "-fx-border-width: 2;" +
                        "-fx-cursor: hand;"
        );

        selectedChambre = c;
        selectedPrixNuit = c.getPrixNuit();

        // Panel chambre sélectionnée
        panelChambreSelectionnee.setVisible(true);
        panelChambreSelectionnee.setManaged(true);
        lblChambreInfo.setText("Chambre " + c.getNumero() + " — " +
                c.getTypeChambre() + " — " + c.getCapacite() + " personnes");
        lblChambrePrix.setText(String.format("%.2f TND / nuit", c.getPrixNuit()));

        computeTotal();
    }

    private void computeTotal() {
        LocalDate debut = dpDebut.getValue();
        LocalDate fin   = dpFin.getValue();

        if (debut == null || fin == null || !fin.isAfter(debut)) {
            panelPrixTotal.setVisible(false);
            panelPrixTotal.setManaged(false);
            return;
        }

        long nuits = ChronoUnit.DAYS.between(debut, fin);
        double total = nuits * selectedPrixNuit;

        panelPrixTotal.setVisible(true);
        panelPrixTotal.setManaged(true);
        lblTotal.setText(nuits + " nuit(s) × " +
                String.format("%.2f", selectedPrixNuit) + " TND = " +
                String.format("%.2f", total) + " TND");
    }

    @FXML
    private void addReservation() {
        lblMsg.setText("");

        if (currentHebergement == null) return;

        String nom   = tfNom.getText() == null ? "" : tfNom.getText().trim();
        String tel   = tfTel.getText() == null ? "" : tfTel.getText().trim();
        String email = tfEmail.getText() == null ? "" : tfEmail.getText().trim();
        LocalDate debut = dpDebut.getValue();
        LocalDate fin   = dpFin.getValue();

        if (selectedChambre == null) {
            lblMsg.setText("⚠️ Veuillez sélectionner une chambre.");
            return;
        }
        if (nom.isEmpty()) { lblMsg.setText("⚠️ Nom obligatoire."); return; }
        if (!tel.matches("\\d{8}")) { lblMsg.setText("⚠️ Téléphone : 8 chiffres."); return; }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            lblMsg.setText("⚠️ Email invalide."); return;
        }
        if (debut == null || fin == null) {
            lblMsg.setText("⚠️ Choisir les dates."); return;
        }
        if (!fin.isAfter(debut)) {
            lblMsg.setText("⚠️ Date fin doit être après date début."); return;
        }

        try {
            if (!rs.isInsideDisponibilite(currentHebergement.getId(), debut, fin)) {
                lblMsg.setText("⚠️ Ces dates ne sont pas disponibles.");
                return;
            }
            if (!rs.isHebergementAvailable(currentHebergement.getId(), debut, fin)) {
                lblMsg.setText("⚠️ Hébergement déjà réservé sur cette période.");
                return;
            }

            int nuits  = (int) ChronoUnit.DAYS.between(debut, fin);
            double total = nuits * selectedPrixNuit;

            Reservation r = new Reservation(
                    currentHebergement.getId(), nom, tel, email,
                    debut, fin, nuits, total, "EN_ATTENTE"
            );

            int reservationId = rs.addEntityReturnId(r);
            if (reservationId == -1) {
                lblMsg.setText("❌ Impossible de créer la réservation.");
                return;
            }

            callbackServer.start(rs, payService);
            int port = callbackServer.getPort();
            var session = payService.createCheckoutSession(reservationId, total, email, port);
            Desktop.getDesktop().browse(new URI(session.getUrl()));

            lblMsg.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 13px;");
            lblMsg.setText("💳 Paiement en cours... Terminez dans le navigateur.");

        } catch (Exception e) {
            lblMsg.setText("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showEmptyMap() {
        if (mapView == null) return;
        mapView.getEngine().loadContent(
                "<html><body style='display:flex;align-items:center;justify-content:center;" +
                        "height:100%;margin:0;color:#888;font-family:Arial;'>" +
                        "Aucune localisation disponible</body></html>"
        );
    }

    private void loadMap(double lat, double lng, String label) {
        if (mapView == null) return;
        String safeLabel = (label == null ? "" : label).replace("'", "\\'");
        String html = String.format(
                "<!DOCTYPE html><html><head><meta charset='utf-8'/>" +
                        "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                        "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                        "<style>html,body,#map{height:100%%;margin:0;}</style></head>" +
                        "<body><div id='map'></div><script>" +
                        "var map=L.map('map').setView([%f,%f],14);" +
                        "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);" +
                        "L.marker([%f,%f]).addTo(map).bindPopup('%s').openPopup();" +
                        "setTimeout(function(){map.invalidateSize();},600);" +
                        "</script></body></html>",
                lat, lng, lat, lng, safeLabel
        );
        mapView.getEngine().loadContent(html);
    }
}