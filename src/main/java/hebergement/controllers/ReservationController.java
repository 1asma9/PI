package hebergement.controllers;

import hebergement.entities.Hebergement;
import hebergement.entities.Reservation;
import hebergement.services.HebergementService;
import hebergement.services.ReservationService;
import hebergement.services.PaymentService;
import hebergement.tools.LocalPaymentCallbackServer;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebView;

import java.awt.Desktop;
import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReservationController {

    private final HebergementService hs = new HebergementService();
    private final ReservationService rs = new ReservationService();

    private final PaymentService payService = new PaymentService();
    private final LocalPaymentCallbackServer callbackServer = new LocalPaymentCallbackServer();

    @FXML private ComboBox<Hebergement> cbHebergement;
    @FXML private TextField tfNom;
    @FXML private TextField tfTel;
    @FXML private TextField tfEmail;
    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;
    @FXML private Label lblTotal;
    @FXML private Label lblMsg;

    // ✅ MAP (doit matcher reservation.fxml)
    @FXML private WebView mapViewRes;
    @FXML private Label lblMapInfoRes;

    @FXML
    public void initialize() {
        lblMsg.setText("");
        loadHebergements();

        cbHebergement.valueProperty().addListener((obs, o, n) -> {
            computeTotal();
            showSelectedHebergementOnMap(n);
        });

        dpDebut.valueProperty().addListener((obs, o, n) -> computeTotal());
        dpFin.valueProperty().addListener((obs, o, n) -> computeTotal());

        showEmptyMap("Sélectionne un hébergement pour voir sa position.");
    }

    private void loadHebergements() {
        try {
            List<Hebergement> list = hs.getData();
            cbHebergement.setItems(FXCollections.observableArrayList(list));

            cbHebergement.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Hebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" :
                            "#" + item.getId() + " - " + item.getDescription() + " (" + item.getPrix() + " DT)");
                }
            });

            cbHebergement.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Hebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" :
                            "#" + item.getId() + " - " + item.getDescription());
                }
            });

        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les hébergements : " + e.getMessage());
        }
    }

    private void computeTotal() {
        Hebergement h = cbHebergement.getValue();
        LocalDate debut = dpDebut.getValue();
        LocalDate fin = dpFin.getValue();

        if (h == null || debut == null || fin == null || !fin.isAfter(debut)) {
            lblTotal.setText("Total: 0.0 DT");
            return;
        }

        long nuits = ChronoUnit.DAYS.between(debut, fin);
        double total = nuits * h.getPrix();
        lblTotal.setText("Total: " + total + " DT (" + nuits + " nuits)");
    }

    // ===================== MAP =====================

    private void showSelectedHebergementOnMap(Hebergement h) {
        if (h == null) {
            showEmptyMap("Sélectionne un hébergement pour voir sa position.");
            return;
        }

        if (h.getLatitude() == null || h.getLongitude() == null) {
            showEmptyMap("⚠️ Cet hébergement n’a pas de latitude/longitude.");
            lblMapInfoRes.setText("⚠️ Pas de position enregistrée.");
            return;
        }

        lblMapInfoRes.setText("📍 " + h.getLatitude() + ", " + h.getLongitude());
        loadMap(h.getLatitude(), h.getLongitude(), h.getDescription());
    }

    private void showEmptyMap(String msg) {
        if (mapViewRes == null) return;

        String safe = (msg == null ? "" : msg).replace("'", "\\'");

        String html =
                "<!DOCTYPE html><html><head><meta charset='utf-8'/>" +
                        "<style>html,body{height:100%;margin:0;font-family:Arial;}" +
                        ".box{height:100%;display:flex;align-items:center;justify-content:center;color:#6a7a73;}" +
                        "</style></head><body><div class='box'>" + safe + "</div></body></html>";

        mapViewRes.getEngine().loadContent(html);
        if (lblMapInfoRes != null) lblMapInfoRes.setText(msg);
    }

    private void loadMap(double lat, double lng, String label) {
        if (mapViewRes == null) return;

        String safeLabel = (label == null ? "" : label).replace("'", "\\'");

        String html = String.format(
                "<!DOCTYPE html>" +
                        "<html><head><meta charset='utf-8'/>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>" +
                        "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                        "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                        "<style>html,body,#map{height:100%%;margin:0;}</style>" +
                        "</head><body><div id='map'></div>" +
                        "<script>" +
                        "var map=L.map('map').setView([%f,%f],14);" +
                        "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'&copy; OpenStreetMap'}).addTo(map);" +
                        "L.marker([%f,%f]).addTo(map).bindPopup('%s').openPopup();" +
                        "setTimeout(function(){map.invalidateSize();},600);" +
                        "</script></body></html>",
                lat, lng, lat, lng, safeLabel
        );

        mapViewRes.getEngine().loadContent(html);
    }

    // ===================== RESERVATION (ton code Stripe intact) =====================

    @FXML
    void addReservation() {
        lblMsg.setText("");

        Hebergement h = cbHebergement.getValue();
        String nom = tfNom.getText() == null ? "" : tfNom.getText().trim();
        String tel = tfTel.getText() == null ? "" : tfTel.getText().trim();
        String email = tfEmail.getText() == null ? "" : tfEmail.getText().trim();
        LocalDate debut = dpDebut.getValue();
        LocalDate fin = dpFin.getValue();

        if (h == null) { showInfo("Validation", "Choisir un hébergement."); return; }
        if (nom.isEmpty()) { showInfo("Validation", "Nom du client obligatoire."); return; }
        if (tel == null || !tel.matches("\\d{8}")) { showInfo("Validation", "Le téléphone doit contenir exactement 8 chiffres."); return; }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) { showInfo("Validation", "Email invalide."); return; }
        if (debut == null || fin == null) { showInfo("Validation", "Choisir date début et date fin."); return; }
        if (!fin.isAfter(debut)) { showInfo("Validation", "Date fin doit être > date début."); return; }

        try {
            if (!rs.isInsideDisponibilite(h.getId(), debut, fin)) {
                showInfo("Indisponible", "Ces dates ne sont pas dans une période disponible.");
                return;
            }

            if (!rs.isHebergementAvailable(h.getId(), debut, fin)) {
                showInfo("Conflit", "Cet hébergement est déjà réservé sur cette période.");
                return;
            }

            int nuits = (int) ChronoUnit.DAYS.between(debut, fin);
            double total = nuits * h.getPrix();

            Reservation r = new Reservation(
                    h.getId(), nom, tel, email, debut, fin, nuits, total, "EN_ATTENTE"
            );

            int reservationId = rs.addEntityReturnId(r);
            if (reservationId == -1) {
                showError("Erreur", "Impossible de créer la réservation.");
                return;
            }

            callbackServer.start(rs, payService);
            int port = callbackServer.getPort();

            var session = payService.createCheckoutSession(reservationId, total, email, port);
            Desktop.getDesktop().browse(new URI(session.getUrl()));

            lblMsg.setText("💳 Paiement en cours... Terminez le paiement dans le navigateur.");
            clearForm();

        } catch (Exception e) {
            showError("Erreur réservation/paiement", e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearForm() {
        cbHebergement.getSelectionModel().clearSelection();
        tfNom.clear();
        tfTel.clear();
        tfEmail.clear();
        dpDebut.setValue(null);
        dpFin.setValue(null);
        lblTotal.setText("Total: 0.0 DT");
        showEmptyMap("Sélectionne un hébergement pour voir sa position.");
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public void setSelectedHebergement(Hebergement h) {
        if (h == null) return;
        if (cbHebergement.getItems() == null || cbHebergement.getItems().isEmpty()) loadHebergements();
        cbHebergement.getSelectionModel().select(h);
        computeTotal();
        showSelectedHebergementOnMap(h);
    }
}