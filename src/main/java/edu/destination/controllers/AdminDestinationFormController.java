package edu.destination.controllers;

import edu.destination.entities.Destination;
import edu.destination.services.DestinationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class AdminDestinationFormController {

    private static final String GROQ_API_KEY = "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";

    @FXML private TextField txtNom, txtPays, txtDescription;
    @FXML private TextField txtLatitude, txtLongitude, txtNbVisites;
    @FXML private ComboBox<String> comboSaison;   // ✅ remplace txtSaison
    @FXML private ComboBox<String> comboStatut;   // ✅ remplace chkStatut
    @FXML private Button btnSave, btnCancel;
    @FXML private TextField txtVideoPath;
    @FXML private Label errVideoPath;
    @FXML private Label errNom, errPays, errDescription, errSaison;
    @FXML private Label errLatitude, errLongitude, errNbVisites;
    @FXML private Button btnGenererDescription;
    @FXML private Label lblIaStatus;
    @FXML private WebView mapView;

    private final DestinationService service = new DestinationService();
    private Destination destination;

    @FXML
    private void initialize() {
        // ✅ Remplir les saisons
        comboSaison.getItems().addAll("Printemps", "Ete", "Automne", "Hiver");

        // ✅ Remplir les statuts
        comboStatut.getItems().addAll("Actif", "Inactif");
        comboStatut.getSelectionModel().selectFirst();

        btnSave.setOnAction(e -> save());
        btnCancel.setOnAction(e -> closeWindow());
        btnGenererDescription.setOnAction(e -> genererDescriptionIA());
        initMap();

        txtLatitude.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) updateMarkerFromFields();
        });
        txtLongitude.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) updateMarkerFromFields();
        });
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
        if (destination != null) {
            txtNom.setText(destination.getNom());
            txtPays.setText(destination.getPays());
            txtDescription.setText(destination.getDescription());

            // ✅ Sélectionner la saison
            if (destination.getMeilleureSaison() != null)
                comboSaison.setValue(destination.getMeilleureSaison());

            // ✅ Sélectionner le statut
            comboStatut.setValue(destination.getStatut() ? "Actif" : "Inactif");

            txtLatitude.setText(String.valueOf(destination.getLatitude()));
            txtLongitude.setText(String.valueOf(destination.getLongitude()));
            txtNbVisites.setText(String.valueOf(destination.getNbVisites()));

            if (destination.getVideoPath() != null)
                txtVideoPath.setText(destination.getVideoPath());

            Platform.runLater(this::updateMarkerFromFields);
        }
    }

    private void initMap() {
        String html = "<!DOCTYPE html>" +
                "<html><head><meta charset=\"utf-8\"/>" +
                "<style>" +
                "* { margin:0; padding:0; box-sizing:border-box; }" +
                "html,body { width:100%; height:100%; }" +
                "#map { width:100%; height:100vh; }" +
                "#search-container {" +
                "  position:absolute; top:10px; left:50%; transform:translateX(-50%);" +
                "  z-index:1000; display:flex; gap:6px; background:white;" +
                "  padding:8px 10px; border-radius:8px;" +
                "  box-shadow:0 2px 8px rgba(0,0,0,0.3); width:80%;" +
                "}" +
                "#search-input {" +
                "  flex:1; padding:6px 10px; border:1px solid #ccc;" +
                "  border-radius:6px; font-size:13px; outline:none;" +
                "}" +
                "#search-btn {" +
                "  padding:6px 14px; background:#3b82f6; color:white;" +
                "  border:none; border-radius:6px; cursor:pointer;" +
                "  font-size:13px; font-weight:600;" +
                "}" +
                "#search-status {" +
                "  position:absolute; top:60px; left:50%; transform:translateX(-50%);" +
                "  z-index:1000; background:rgba(0,0,0,0.75); color:white;" +
                "  padding:5px 14px; border-radius:6px; font-size:12px; display:none;" +
                "}" +
                "</style>" +
                "<link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\"/>" +
                "<script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>" +
                "</head><body>" +
                "<div id=\"search-container\">" +
                "<input id=\"search-input\" type=\"text\" placeholder=\"Rechercher une ville...\"" +
                " onkeydown=\"if(event.key==='Enter') searchLocation()\"/>" +
                "<button id=\"search-btn\" onclick=\"searchLocation()\">Rechercher</button>" +
                "</div>" +
                "<div id=\"search-status\"></div>" +
                "<div id=\"map\"></div>" +
                "<script>" +
                "var map = L.map('map').setView([33.8869, 9.5375], 5);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "  attribution: 'OpenStreetMap'" +
                "}).addTo(map);" +
                "var marker = null;" +
                "map.on('click', function(e) {" +
                "  var lat = e.latlng.lat.toFixed(6);" +
                "  var lng = e.latlng.lng.toFixed(6);" +
                "  placeMarker(lat, lng);" +
                "  window.javaConnector.setCoordinates(lat, lng);" +
                "});" +
                "function searchLocation() {" +
                "  var query = document.getElementById('search-input').value.trim();" +
                "  if (!query) return;" +
                "  showStatus('Recherche en cours...');" +
                "  window.javaConnector.searchPlace(query);" +
                "}" +
                "function setSearchResult(lat, lng, name) {" +
                "  placeMarker(lat, lng);" +
                "  map.setView([lat, lng], 8);" +
                "  showStatus('Trouve: ' + name, true);" +
                "}" +
                "function setSearchError(msg) {" +
                "  showStatus('Introuvable: ' + msg, true);" +
                "}" +
                "function placeMarker(lat, lng) {" +
                "  var latlng = L.latLng(lat, lng);" +
                "  if (marker) { marker.setLatLng(latlng); }" +
                "  else { marker = L.marker(latlng).addTo(map); }" +
                "}" +
                "function moveMarker(lat, lng) {" +
                "  placeMarker(lat, lng);" +
                "  map.setView(L.latLng(lat, lng), 8);" +
                "}" +
                "function showStatus(msg, autoHide) {" +
                "  var el = document.getElementById('search-status');" +
                "  el.textContent = msg;" +
                "  el.style.display = 'block';" +
                "  if (autoHide) { setTimeout(function(){ el.style.display='none'; }, 3000); }" +
                "}" +
                "</script>" +
                "</body></html>";

        mapView.getEngine().loadContent(html);

        mapView.getEngine().getLoadWorker().stateProperty().addListener(
                (obs, old, state) -> {
                    if (state == javafx.concurrent.Worker.State.SUCCEEDED) {
                        netscape.javascript.JSObject window =
                                (netscape.javascript.JSObject) mapView.getEngine().executeScript("window");
                        window.setMember("javaConnector", new MapConnector());
                    }
                }
        );
    }

    private void updateMarkerFromFields() {
        try {
            double lat = Double.parseDouble(txtLatitude.getText().trim());
            double lng = Double.parseDouble(txtLongitude.getText().trim());
            mapView.getEngine().executeScript("moveMarker(" + lat + "," + lng + ");");
        } catch (NumberFormatException ignored) {}
    }

    public class MapConnector {

        public void setCoordinates(String lat, String lng) {
            Platform.runLater(() -> {
                txtLatitude.setText(lat);
                txtLongitude.setText(lng);
            });
        }

        public void searchPlace(String query) {
            Thread thread = new Thread(() -> {
                String[] urls = {
                        "https://nominatim.openstreetmap.org/search?q="
                                + encode(query) + "&format=json&limit=1&accept-language=fr",
                        "https://nominatim.openstreetmap.org/search?q="
                                + encode(query) + "&format=json&limit=1"
                };

                for (String urlStr : urls) {
                    try {
                        System.out.println("Tentative: " + urlStr);
                        java.net.URL url = new java.net.URL(urlStr);
                        java.net.HttpURLConnection conn =
                                (java.net.HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible)");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setConnectTimeout(8000);
                        conn.setReadTimeout(8000);

                        int status = conn.getResponseCode();
                        System.out.println("Status: " + status);
                        if (status != 200) continue;

                        StringBuilder sb = new StringBuilder();
                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(
                                        conn.getInputStream(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = br.readLine()) != null) sb.append(line);
                        }

                        String json = sb.toString();
                        if (json.equals("[]") || !json.contains("\"lat\"")) {
                            callJS("setSearchError('Lieu introuvable')");
                            return;
                        }

                        String lat  = extractJson(json, "lat");
                        String lon  = extractJson(json, "lon");
                        String name = extractJson(json, "display_name");

                        if (lat.isEmpty() || lon.isEmpty()) {
                            callJS("setSearchError('Coordonnees manquantes')");
                            return;
                        }

                        double latD = Double.parseDouble(lat);
                        double lonD = Double.parseDouble(lon);

                        if (name.length() > 40) name = name.substring(0, 40);
                        final String safeName = name.replace("'", " ").replace("\"", " ");
                        final String latF = String.format("%.6f", latD);
                        final String lonF = String.format("%.6f", lonD);

                        Platform.runLater(() -> {
                            txtLatitude.setText(latF);
                            txtLongitude.setText(lonF);
                        });

                        callJS("setSearchResult(" + latD + "," + lonD + ",'" + safeName + "')");
                        return;

                    } catch (Exception e) {
                        System.err.println("Erreur: " + e.getMessage());
                    }
                }
                callJS("setSearchError('Reseau inaccessible')");
            });
            thread.setDaemon(true);
            thread.start();
        }

        private String encode(String s) {
            try { return java.net.URLEncoder.encode(s, "UTF-8"); }
            catch (Exception e) { return s; }
        }

        private void callJS(String script) {
            Platform.runLater(() -> mapView.getEngine().executeScript(script));
        }

        private String extractJson(String json, String key) {
            String search = "\"" + key + "\":\"";
            int start = json.indexOf(search);
            if (start == -1) return "";
            start += search.length();
            int end = json.indexOf("\"", start);
            if (end == -1) return "";
            return json.substring(start, end);
        }
    }

    private void genererDescriptionIA() {
        String nom   = txtNom.getText().trim();
        String pays  = txtPays.getText().trim();
        String saison = comboSaison.getValue() != null ? comboSaison.getValue() : "";

        if (nom.isEmpty() || pays.isEmpty()) {
            lblIaStatus.setText("Remplis le nom et le pays d'abord");
            lblIaStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        btnGenererDescription.setDisable(true);
        lblIaStatus.setText("Generation en cours...");
        lblIaStatus.setStyle("-fx-text-fill: #f39c12;");

        Thread thread = new Thread(() -> {
            try {
                String description = callGroq(nom, pays, saison);
                Platform.runLater(() -> {
                    txtDescription.setText(description);
                    lblIaStatus.setText("Description generee !");
                    lblIaStatus.setStyle("-fx-text-fill: #27ae60;");
                    btnGenererDescription.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    lblIaStatus.setText("Erreur: " + ex.getMessage());
                    lblIaStatus.setStyle("-fx-text-fill: #e74c3c;");
                    btnGenererDescription.setDisable(false);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String callGroq(String nom, String pays, String saison) throws Exception {
        URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        String prompt = "Tu es un assistant de voyage strict. "
                + "Si le nom '" + nom + "' ou le pays '" + pays + "' ne correspondent pas "
                + "a une vraie destination geographique connue, "
                + "reponds UNIQUEMENT avec le mot : DESTINATION_INVALIDE "
                + "Sinon, redige une description de voyage courte et attractive (3 a 4 phrases) "
                + "pour la destination : " + nom + ", situee en " + pays + ". "
                + (saison != null && !saison.isBlank()
                ? "La meilleure saison pour visiter est : " + saison + ". " : "")
                + "Reponds uniquement avec la description, sans titre ni introduction.";

        String body = "{\"model\":\"llama-3.3-70b-versatile\","
                + "\"messages\":[{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}],"
                + "\"max_tokens\":300,\"temperature\":0.2}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        if (status != 200) throw new Exception("Groq HTTP " + status);

        String json = sb.toString();
        int start = json.indexOf("\"content\":\"");
        if (start == -1) throw new Exception("Reponse Groq invalide");
        start += 11;

        StringBuilder result = new StringBuilder();
        boolean escape = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escape) {
                switch (c) {
                    case 'n':  result.append('\n'); break;
                    case 't':  result.append('\t'); break;
                    case '"':  result.append('"');  break;
                    case '\\': result.append('\\'); break;
                    default:   result.append(c);
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                result.append(c);
            }
        }

        String description = result.toString()
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("\\*(.*?)\\*", "$1")
                .trim();

        if (description.toUpperCase().contains("DESTINATION_INVALIDE"))
            throw new Exception("Destination invalide.");

        return description;
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private void save() {
        clearErrors();
        if (!validateFields()) return;

        try {
            if (destination == null) destination = new Destination();

            destination.setNom(txtNom.getText().trim());
            destination.setPays(txtPays.getText().trim());
            destination.setDescription(txtDescription.getText().trim());

            // ✅ Saison depuis ComboBox
            destination.setMeilleureSaison(comboSaison.getValue());

            // ✅ Statut depuis ComboBox
            destination.setStatut("Actif".equals(comboStatut.getValue()));

            destination.setLatitude(Double.parseDouble(txtLatitude.getText().trim()));
            destination.setLongitude(Double.parseDouble(txtLongitude.getText().trim()));
            destination.setNbVisites(Integer.parseInt(txtNbVisites.getText().trim()));

            String videoPath = txtVideoPath.getText().trim();
            destination.setVideoPath(videoPath.isEmpty() ? null : videoPath);

            if (destination.getId() == 0) {
                service.addEntity(destination);
            } else {
                service.update(destination.getId(), destination);
            }

            closeWindow();

        } catch (SQLException ex) {
            showAlert("Erreur base de donnees", ex.getMessage());
        } catch (Exception ex) {
            showAlert("Erreur", ex.getMessage());
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        String nom = txtNom.getText().trim();
        if (nom.isEmpty()) {
            setError(txtNom, errNom, "Le nom est obligatoire"); isValid = false;
        } else if (!nom.matches("^[A-Za-zÀ-ÿ\\s-]+$")) {
            setError(txtNom, errNom, "Lettres uniquement"); isValid = false;
        }

        String pays = txtPays.getText().trim();
        if (pays.isEmpty()) {
            setError(txtPays, errPays, "Le pays est obligatoire"); isValid = false;
        } else if (!pays.matches("^[A-Za-zÀ-ÿ\\s-]+$")) {
            setError(txtPays, errPays, "Lettres uniquement"); isValid = false;
        }

        String desc = txtDescription.getText().trim();
        if (desc.isEmpty()) {
            setError(txtDescription, errDescription, "Description obligatoire"); isValid = false;
        } else if (desc.length() < 10) {
            setError(txtDescription, errDescription, "Minimum 10 caracteres"); isValid = false;
        }

        // ✅ Validation ComboBox saison
        if (comboSaison.getValue() == null) {
            errSaison.setText("Veuillez choisir une saison");
            isValid = false;
        }

        // ✅ Validation ComboBox statut
        if (comboStatut.getValue() == null) {
            comboStatut.setValue("Actif");
        }

        isValid = validateDouble(txtLatitude, errLatitude,
                "Latitude entre -90 et 90", -90, 90) && isValid;
        isValid = validateDouble(txtLongitude, errLongitude,
                "Longitude entre -180 et 180", -180, 180) && isValid;

        try {
            int nb = Integer.parseInt(txtNbVisites.getText().trim());
            if (nb < 0) {
                setError(txtNbVisites, errNbVisites, "Doit etre positif"); isValid = false;
            }
        } catch (NumberFormatException e) {
            setError(txtNbVisites, errNbVisites, "Nombre invalide"); isValid = false;
        }

        return isValid;
    }

    private boolean validateDouble(TextField field, Label errLabel,
                                   String msg, double min, double max) {
        try {
            double value = Double.parseDouble(field.getText().trim());
            if (value < min || value > max) { setError(field, errLabel, msg); return false; }
            return true;
        } catch (NumberFormatException e) {
            setError(field, errLabel, "Nombre invalide"); return false;
        }
    }

    private void setError(Control field, Label label, String message) {
        if (field != null && !field.getStyleClass().contains("input-error"))
            field.getStyleClass().add("input-error");
        if (label != null) label.setText(message);
    }

    private void clearErrors() {
        Control[] fields = { txtNom, txtPays, txtDescription,
                txtLatitude, txtLongitude, txtNbVisites, txtVideoPath };
        Label[] labels = { errNom, errPays, errDescription, errSaison,
                errLatitude, errLongitude, errNbVisites, errVideoPath };

        for (Control c : fields) if (c != null) c.getStyleClass().remove("input-error");
        for (Label  l : labels)  if (l != null) l.setText("");
    }

    private void closeWindow() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}