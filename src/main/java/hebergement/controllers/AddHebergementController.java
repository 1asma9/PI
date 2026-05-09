package hebergement.controllers;

import hebergement.entities.Hebergement;
import hebergement.entities.TypeHebergement;
import hebergement.services.HebergementService;
import hebergement.services.TypeHebergementService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import netscape.javascript.JSObject;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class AddHebergementController {

    private final HebergementService     hs = new HebergementService();
    private final TypeHebergementService ts = new TypeHebergementService();

    @FXML private TextField tfDesc;
    @FXML private TextField tfAdresse;
    @FXML private TextField tfPrix;
    @FXML private ComboBox<TypeHebergement> cbType;
    @FXML private Label lblHebMsg;

    @FXML private TextField tfLat;
    @FXML private TextField tfLng;
    @FXML private WebView   mapView;

    @FXML private Label lblImage;
    private String selectedImagePath = null;

    // ✅ Mode édition
    private Hebergement hebergementToEdit = null;

    // ====================== INIT ======================
    @FXML
    public void initialize() {
        loadTypes();
        lblHebMsg.setText("");
        if (lblImage != null) lblImage.setText("Aucune image choisie");
        showEmptyMap("Carte prête ✅ Clique sur la carte pour choisir la position.");
        loadMap(36.8065, 10.1815, "Tunis");
    }

    // ====================== MODE EDITION ======================
    public void setHebergementToEdit(Hebergement h) {
        this.hebergementToEdit = h;
        if (h == null) return;

        tfDesc.setText(h.getDescription() != null ? h.getDescription() : "");
        tfAdresse.setText(h.getAdresse() != null ? h.getAdresse() : "");
        tfPrix.setText(String.valueOf(h.getPrix()));

        if (h.getLatitude() != null)  tfLat.setText(String.valueOf(h.getLatitude()));
        if (h.getLongitude() != null) tfLng.setText(String.valueOf(h.getLongitude()));

        if (cbType.getItems() != null) {
            cbType.getItems().stream()
                    .filter(t -> t.getId() == h.getTypeId())
                    .findFirst()
                    .ifPresent(cbType::setValue);
        }

        if (h.getImagePath() != null && !h.getImagePath().isBlank()) {
            selectedImagePath = h.getImagePath();
            if (lblImage != null) lblImage.setText("✅ Image actuelle chargée");
        }

        if (h.getLatitude() != null && h.getLongitude() != null)
            loadMap(h.getLatitude(), h.getLongitude(), h.getDescription());

        lblHebMsg.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 13px;");
        lblHebMsg.setText("✏️ Mode édition — modifiez les champs puis cliquez Enregistrer.");
    }

    // ====================== LOAD TYPES ======================
    private void loadTypes() {
        try {
            List<TypeHebergement> types = ts.getData();
            cbType.setItems(FXCollections.observableArrayList(types));

            cbType.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(TypeHebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getLibelle());
                }
            });
            cbType.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(TypeHebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getLibelle());
                }
            });
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les types : " + e.getMessage());
        }
    }

    // ====================== IMAGE ======================
    @FXML
    void chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = chooser.showOpenDialog(null);
        if (file == null) return;
        try {
            String newName = UUID.randomUUID() + "_" + file.getName();
            File folder = new File("uploads");
            if (!folder.exists()) folder.mkdirs();
            File dest = new File(folder, newName);
            Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            selectedImagePath = dest.getAbsolutePath();
            lblImage.setText("✅ " + file.getName());
        } catch (Exception e) {
            showError("Erreur image", "Impossible de copier l'image : " + e.getMessage());
        }
    }

    // ====================== LOCALISATION ======================
    @FXML
    void localiserAdresse() {
        String adr = tfAdresse.getText() == null ? "" : tfAdresse.getText().trim();
        if (adr.isEmpty()) { showInfo("Validation", "Adresse obligatoire pour localiser."); return; }
        try {
            String q   = URLEncoder.encode(adr, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" + q;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req   = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "GestionHebergementApp/1.0")
                    .GET().build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            String body = res.body();
            if (body == null || body.equals("[]")) { showInfo("Localisation", "Adresse introuvable."); return; }
            String lat = body.split("\"lat\":\"")[1].split("\"")[0];
            String lon = body.split("\"lon\":\"")[1].split("\"")[0];
            tfLat.setText(lat);
            tfLng.setText(lon);
            String label = tfDesc.getText() == null || tfDesc.getText().isBlank() ? "Hébergement" : tfDesc.getText().trim();
            loadMap(Double.parseDouble(lat), Double.parseDouble(lon), label);
            lblHebMsg.setText("📍 Position trouvée : " + lat + ", " + lon);
        } catch (Exception e) {
            showError("Erreur localisation", e.getMessage());
        }
    }

    @FXML
    void showOnMap() {
        String latS = tfLat.getText() == null ? "" : tfLat.getText().trim();
        String lngS = tfLng.getText() == null ? "" : tfLng.getText().trim();
        if (latS.isEmpty() || lngS.isEmpty()) { showInfo("Carte", "Remplis Latitude et Longitude."); return; }
        try {
            double lat   = Double.parseDouble(latS);
            double lng   = Double.parseDouble(lngS);
            String label = tfDesc.getText() == null || tfDesc.getText().isBlank() ? "Hébergement" : tfDesc.getText().trim();
            loadMap(lat, lng, label);
        } catch (Exception e) {
            showInfo("Carte", "Latitude/Longitude invalides.");
        }
    }

    // ====================== MAP ======================
    public class JsBridge {
        public void pick(double lat, double lng) {
            Platform.runLater(() -> {
                tfLat.setText(String.format(java.util.Locale.US, "%.6f", lat));
                tfLng.setText(String.format(java.util.Locale.US, "%.6f", lng));
                lblHebMsg.setText("📌 Position choisie : " + lat + ", " + lng);
            });
        }
    }

    private void loadMap(double lat, double lng, String label) {
        if (mapView == null) return;
        String safeLabel = (label == null ? "" : label).replace("'", "\\'");
        String html =
                "<!DOCTYPE html><html><head><meta charset='utf-8'/>" +
                        "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                        "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                        "<style>html,body,#map{height:100%;margin:0;}</style></head><body>" +
                        "<div id='map'></div><script>" +
                        "var map=L.map('map').setView([" + lat + "," + lng + "],14);" +
                        "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19}).addTo(map);" +
                        "var marker=L.marker([" + lat + "," + lng + "]).addTo(map).bindPopup('" + safeLabel + "').openPopup();" +
                        "map.on('click',function(e){" +
                        "  marker.setLatLng([e.latlng.lat,e.latlng.lng]);" +
                        "  if(window.java&&window.java.pick)window.java.pick(e.latlng.lat,e.latlng.lng);" +
                        "});" +
                        "setTimeout(function(){map.invalidateSize();},600);" +
                        "</script></body></html>";
        mapView.getEngine().loadContent(html);
        mapView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, st) -> {
            if (st == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject win = (JSObject) mapView.getEngine().executeScript("window");
                win.setMember("java", new JsBridge());
            }
        });
    }

    private void showEmptyMap(String msg) {
        if (mapView == null) return;
        String safe = (msg == null ? "" : msg).replace("'", "\\'");
        mapView.getEngine().loadContent(
                "<html><body style='display:flex;align-items:center;justify-content:center;" +
                        "height:100%;margin:0;color:#6a7a73;font-family:Arial;'>" + safe + "</body></html>"
        );
    }

    // ====================== ENREGISTRER ======================
    @FXML
    void addHebergement() {
        lblHebMsg.setText("");

        String desc  = tfDesc.getText() == null ? "" : tfDesc.getText().trim();
        String adr   = tfAdresse.getText() == null ? "" : tfAdresse.getText().trim();
        String prixS = tfPrix.getText() == null ? "" : tfPrix.getText().trim();
        TypeHebergement type = cbType.getValue();

        if (desc.isEmpty()) { showInfo("Validation", "Description obligatoire."); return; }
        if (adr.isEmpty())  { showInfo("Validation", "Adresse obligatoire."); return; }
        if (type == null)   { showInfo("Validation", "Choisir un type."); return; }

        double prix;
        try { prix = Double.parseDouble(prixS); }
        catch (Exception e) { showInfo("Validation", "Prix invalide (ex: 250.0)."); return; }
        if (prix <= 0) { showInfo("Validation", "Prix doit être > 0."); return; }

        Double lat = null, lng = null;
        String latS = tfLat.getText() == null ? "" : tfLat.getText().trim();
        String lngS = tfLng.getText() == null ? "" : tfLng.getText().trim();
        if (!latS.isEmpty() && !lngS.isEmpty()) {
            try { lat = Double.parseDouble(latS); lng = Double.parseDouble(lngS); }
            catch (Exception e) { showInfo("Validation", "Latitude/Longitude invalides."); return; }
        }

        try {
            if (hebergementToEdit != null) {
                // ✅ MODE MODIFICATION
                hebergementToEdit.setDescription(desc);
                hebergementToEdit.setAdresse(adr);
                hebergementToEdit.setPrix(prix);
                hebergementToEdit.setTypeId(type.getId());
                hebergementToEdit.setLatitude(lat);
                hebergementToEdit.setLongitude(lng);
                if (selectedImagePath != null) hebergementToEdit.setImagePath(selectedImagePath);

                hs.update(hebergementToEdit.getId(), hebergementToEdit);

                lblHebMsg.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 13px;");
                lblHebMsg.setText("✅ Hébergement modifié avec succès !");

            } else {
                // ✅ MODE AJOUT
                Hebergement h = new Hebergement(desc, adr, prix, type.getId());
                h.setImagePath(selectedImagePath);
                h.setLatitude(lat);
                h.setLongitude(lng);

                hs.addEntity(h);

                lblHebMsg.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 13px;");
                lblHebMsg.setText("✅ Hébergement ajouté avec succès !");

                tfDesc.clear(); tfAdresse.clear(); tfPrix.clear();
                cbType.getSelectionModel().clearSelection();
                if (tfLat != null) tfLat.clear();
                if (tfLng != null) tfLng.clear();
                selectedImagePath = null;
                if (lblImage != null) lblImage.setText("Aucune image choisie");
            }

        } catch (SQLException e) {
            showError("Erreur SQL", e.getMessage());
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    // ====================== UTIL ======================
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}