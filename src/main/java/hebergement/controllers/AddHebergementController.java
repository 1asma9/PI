package hebergement.controllers;

import hebergement.entities.Disponibilite;
import hebergement.entities.Hebergement;
import hebergement.entities.TypeHebergement;
import hebergement.services.DisponibiliteService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AddHebergementController {

    private final HebergementService hs = new HebergementService();
    private final DisponibiliteService ds = new DisponibiliteService();
    private final TypeHebergementService ts = new TypeHebergementService();

    @FXML private TextField tfDesc;
    @FXML private TextField tfAdresse;
    @FXML private TextField tfPrix;
    @FXML private ComboBox<TypeHebergement> cbType;
    @FXML private Label lblHebMsg;

    // ✅ Position
    @FXML private TextField tfLat;
    @FXML private TextField tfLng;

    // ✅ Map
    @FXML private WebView mapView;

    // ✅ Image
    @FXML private Label lblImage;
    private String selectedImagePath = null;

    // Disponibilité
    @FXML private ComboBox<Hebergement> cbHebergement;
    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;
    @FXML private CheckBox chkDisponible;
    @FXML private Label lblDispoMsg;

    // ====================== INIT ======================
    @FXML
    public void initialize() {
        loadTypes();
        loadHebergements();

        lblHebMsg.setText("");
        lblDispoMsg.setText("");
        if (lblImage != null) lblImage.setText("Aucune image choisie");

        // ✅ Carte par défaut (Tunis)
        showEmptyMap("Carte prête ✅ Clique sur la carte pour choisir la position.");
        loadMap(36.8065, 10.1815, "Tunis");
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

    // ====================== LOAD HEBERGEMENTS ======================
    private void loadHebergements() {
        try {
            List<Hebergement> list = hs.getData();
            cbHebergement.setItems(FXCollections.observableArrayList(list));

            cbHebergement.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Hebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : ("#" + item.getId() + " - " + item.getDescription()));
                }
            });
            cbHebergement.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Hebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : ("#" + item.getId() + " - " + item.getDescription()));
                }
            });

        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les hébergements : " + e.getMessage());
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

    // ====================== LOCALISATION OSM ======================
    @FXML
    void localiserAdresse() {
        String adr = tfAdresse.getText() == null ? "" : tfAdresse.getText().trim();
        if (adr.isEmpty()) {
            showInfo("Validation", "Adresse obligatoire pour localiser.");
            return;
        }

        try {
            String q = URLEncoder.encode(adr, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" + q;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "GestionHebergementApp/1.0 (contact: etudiant@example.com)")
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            String body = res.body();

            if (body == null || body.equals("[]")) {
                showInfo("Localisation", "Adresse introuvable.");
                return;
            }

            String lat = body.split("\"lat\":\"")[1].split("\"")[0];
            String lon = body.split("\"lon\":\"")[1].split("\"")[0];

            tfLat.setText(lat);
            tfLng.setText(lon);

            String label = tfDesc.getText() == null ? "Hébergement" : tfDesc.getText().trim();
            if (label.isEmpty()) label = "Hébergement";

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

        if (latS.isEmpty() || lngS.isEmpty()) {
            showInfo("Carte", "Remplis Latitude et Longitude (ou clique sur Localiser via adresse).");
            return;
        }

        try {
            double lat = Double.parseDouble(latS);
            double lng = Double.parseDouble(lngS);

            String label = tfDesc.getText() == null ? "Hébergement" : tfDesc.getText().trim();
            if (label.isEmpty()) label = "Hébergement";

            loadMap(lat, lng, label);
        } catch (Exception e) {
            showInfo("Carte", "Latitude/Longitude invalides.");
        }
    }

    // ====================== MAP (clic => lat/lng) ======================
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
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>" +
                        "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                        "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                        "<style>html,body,#map{height:100%;margin:0;}</style>" +
                        "</head><body><div id='map'></div>" +
                        "<script>" +
                        "var map = L.map('map').setView([" + lat + "," + lng + "], 14);" +
                        "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom:19, attribution:'&copy; OpenStreetMap'}).addTo(map);" +
                        "var marker = L.marker([" + lat + "," + lng + "]).addTo(map).bindPopup('" + safeLabel + "').openPopup();" +

                        // ✅ clic = déplacer marker + envoyer à Java
                        "map.on('click', function(e){" +
                        "  var la = e.latlng.lat;" +
                        "  var lo = e.latlng.lng;" +
                        "  marker.setLatLng([la, lo]);" +
                        "  if(window.java && window.java.pick){ window.java.pick(la, lo); }" +
                        "});" +

                        "setTimeout(function(){ map.invalidateSize(); }, 600);" +
                        "</script></body></html>";

        mapView.getEngine().loadContent(html);

        // ✅ connecter JS -> Java quand la page est chargée
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
        String html =
                "<!DOCTYPE html><html><head><meta charset='utf-8'/>" +
                        "<style>html,body{height:100%;margin:0;font-family:Arial;}" +
                        ".box{height:100%;display:flex;align-items:center;justify-content:center;color:#6a7a73;}</style>" +
                        "</head><body><div class='box'>" + safe + "</div></body></html>";

        mapView.getEngine().loadContent(html);
    }

    // ====================== ADD HEBERGEMENT ======================
    @FXML
    void addHebergement() {
        lblHebMsg.setText("");

        String desc = tfDesc.getText() == null ? "" : tfDesc.getText().trim();
        String adr  = tfAdresse.getText() == null ? "" : tfAdresse.getText().trim();
        String prixS = tfPrix.getText() == null ? "" : tfPrix.getText().trim();
        TypeHebergement type = cbType.getValue();

        if (desc.isEmpty()) { showInfo("Validation", "Description obligatoire."); return; }
        if (adr.isEmpty())  { showInfo("Validation", "Adresse obligatoire."); return; }
        if (type == null)   { showInfo("Validation", "Choisir un type."); return; }

        double prix;
        try { prix = Double.parseDouble(prixS); }
        catch (Exception e) { showInfo("Validation", "Prix invalide (ex: 250.0)."); return; }
        if (prix <= 0) { showInfo("Validation", "Prix doit être > 0."); return; }

        // ✅ lat/lng optionnels
        Double lat = null, lng = null;
        String latS = tfLat.getText() == null ? "" : tfLat.getText().trim();
        String lngS = tfLng.getText() == null ? "" : tfLng.getText().trim();

        if (!latS.isEmpty() && !lngS.isEmpty()) {
            try {
                lat = Double.parseDouble(latS);
                lng = Double.parseDouble(lngS);
            } catch (Exception e) {
                showInfo("Validation", "Latitude/Longitude invalides.");
                return;
            }
        }

        try {
            Hebergement h = new Hebergement(desc, adr, prix, type.getId());
            h.setImagePath(selectedImagePath);
            h.setLatitude(lat);
            h.setLongitude(lng);

            hs.addEntity(h);

            lblHebMsg.setText("✅ Hébergement ajouté.");
            tfDesc.clear(); tfAdresse.clear(); tfPrix.clear();
            cbType.getSelectionModel().clearSelection();
            if (tfLat != null) tfLat.clear();
            if (tfLng != null) tfLng.clear();

            selectedImagePath = null;
            lblImage.setText("Aucune image choisie");

            loadHebergements();

        } catch (SQLException e) {
            showError("Erreur SQL", e.getMessage());
        } catch (Exception e) {
            showError("Erreur ajout", e.getMessage());
        }
    }

    // ====================== ADD DISPONIBILITE ======================
    @FXML
    void addDisponibilite() {
        lblDispoMsg.setText("");

        Hebergement selected = cbHebergement.getValue();
        LocalDate debut = dpDebut.getValue();
        LocalDate fin = dpFin.getValue();

        if (selected == null) { showInfo("Validation", "Choisir un hébergement."); return; }
        if (debut == null || fin == null) { showInfo("Validation", "Choisir début et fin."); return; }
        if (fin.isBefore(debut)) { showInfo("Validation", "Date fin doit être >= date début."); return; }

        try {
            Disponibilite dispo = new Disponibilite(selected.getId(), debut, fin, chkDisponible.isSelected());
            ds.addEntity(dispo);

            lblDispoMsg.setText("✅ Disponibilité ajoutée.");
            dpDebut.setValue(null);
            dpFin.setValue(null);
            chkDisponible.setSelected(true);

        } catch (Exception e) {
            showError("Erreur dispo", e.getMessage());
        }
    }

    // ====================== UTIL ======================
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
}