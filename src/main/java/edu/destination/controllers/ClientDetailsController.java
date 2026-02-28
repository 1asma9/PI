package edu.destination.controllers;

import edu.destination.entities.Destination;
import edu.destination.entities.DestinationImage;
import edu.destination.entities.Transport;
import edu.destination.services.ImageService;
import edu.destination.services.TransportService;
import edu.destination.tools.SceneUtil;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClientDetailsController {

    // ============================================================
    // 🔑 CLÉ API OPENWEATHERMAP — remplace ici ta clé
    //    Inscription gratuite : https://openweathermap.org/api
    // ============================================================
    private static final String WEATHER_API_KEY = "60b4f367276104cdace06362a000085f";

    // ==============================
    // FXML — existant
    // ==============================
    @FXML private ImageView imageView;
    @FXML private Label lblName, lblCountry, lblDescription, lblSeason, lblVisits, lblTotal;
    @FXML private Label lblDepart, lblArrival;
    @FXML private VBox transportContainer;
    @FXML private Button btnPrev, btnNext, btnBackToList;
    @FXML private HBox imagesContainer;
    @FXML private BorderPane mapPane;
    @FXML private Label lblDistance, lblDuration, lblTransport;
    @FXML private Button btnReserve;

    // ==============================
    // FXML — météo (dans WeatherView.fxml)
    // ==============================
    @FXML private VBox weatherBox;
    @FXML private Label lblWeatherCity;
    @FXML private Label lblWeatherTemp;
    @FXML private Label lblWeatherDesc;
    @FXML private Label lblWeatherFeels;
    @FXML private Label lblWeatherHumidity;
    @FXML private Label lblWeatherWind;
    @FXML private Label lblWeatherIndex;
    @FXML private Label lblWeatherPeriod;
    @FXML private HBox forecastContainer;

    // ==============================
    // Services & état
    // ==============================
    private final ImageService imageService = new ImageService();
    private final TransportService transportService = new TransportService();

    private List<DestinationImage> images;
    private int currentIndex = 0;

    private WebView mapView;
    private WebEngine webEngine;
    private JSBridge bridge;

    private Destination currentDestination;
    private Transport currentTransport;

    // ==============================
    // INITIALIZE
    // ==============================
    @FXML
    public void initialize() {
        btnNext.setOnAction(e -> nextImage());
        btnPrev.setOnAction(e -> prevImage());
        btnBackToList.setOnAction(e -> goBackToList());
        btnReserve.setOnAction(e -> {
            System.out.println("Réservation confirmée !");
            System.out.println(lblDistance.getText() + " | " + lblDuration.getText() + " | " + lblTransport.getText());
        });

        initMap();
    }

    // ==============================
    // SET DESTINATION
    // ==============================
    public void setDestination(Destination d) {
        if (d == null) return;
        currentDestination = d;

        lblDepart.setText(d.getDateDepart() != null ? d.getDateDepart().toString() : "-");
        lblArrival.setText(d.getDateArrivee() != null ? d.getDateArrivee().toString() : "-");
        lblName.setText(d.getNom());
        lblCountry.setText(d.getPays());
        lblDescription.setText(d.getDescription());
        lblSeason.setText(d.getMeilleureSaison());
        lblVisits.setText(String.valueOf(d.getNbVisites()));
        lblTotal.setText(String.valueOf(d.getPrix()));

        loadImages(d);
        loadTransports(d);

        if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED && currentTransport != null) {
            addRouteToMap(currentDestination, currentTransport);
        }

        // ✅ Charger la météo
        loadWeather(d);
    }

    // ==============================
    // MÉTÉO
    // ==============================
    private void loadWeather(Destination d) {
        double lat = d.getLatitude();
        double lon = d.getLongitude();

        // ✅ getDateDepart() retourne déjà LocalDate — pas de toLocalDate()
        LocalDate dateDepart  = d.getDateDepart();
        LocalDate dateArrivee = d.getDateArrivee();

        if (lblWeatherPeriod != null && dateDepart != null && dateArrivee != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            lblWeatherPeriod.setText("🗓️ " + dateDepart.format(fmt) + "  →  " + dateArrivee.format(fmt));
        }

        Thread thread = new Thread(() -> {
            try {
                // Météo actuelle
                String currentUrl = "https://api.openweathermap.org/data/2.5/weather"
                        + "?lat=" + lat + "&lon=" + lon
                        + "&appid=" + WEATHER_API_KEY
                        + "&units=metric&lang=fr";

                JSONObject current = new JSONObject(fetchUrl(currentUrl));

                double temp      = current.getJSONObject("main").getDouble("temp");
                double feelsLike = current.getJSONObject("main").getDouble("feels_like");
                int    humidity  = current.getJSONObject("main").getInt("humidity");
                double windMs    = current.getJSONObject("wind").getDouble("speed");
                String desc      = current.getJSONArray("weather").getJSONObject(0).getString("description");
                String cityName  = current.getString("name");

                // Prévisions 5 jours
                String forecastUrl = "https://api.openweathermap.org/data/2.5/forecast"
                        + "?lat=" + lat + "&lon=" + lon
                        + "&appid=" + WEATHER_API_KEY
                        + "&units=metric&lang=fr&cnt=40";

                JSONObject forecastJson = new JSONObject(fetchUrl(forecastUrl));
                List<String[]> forecastDays = parseForecast(forecastJson);

                // Indice voyage
                int    index      = computeTravelIndex(temp, humidity, windMs, desc);
                String indexLabel = getTravelIndexLabel(index);

                // Mise à jour UI
                Platform.runLater(() -> {
                    lblWeatherCity.setText("📍 " + cityName);
                    lblWeatherTemp.setText(String.format("%.0f°C", temp));
                    lblWeatherDesc.setText(capitalize(desc));
                    lblWeatherFeels.setText(String.format("Ressenti : %.0f°C", feelsLike));
                    lblWeatherHumidity.setText("💧 Humidité : " + humidity + "%");
                    lblWeatherWind.setText(String.format("💨 Vent : %.0f km/h", windMs * 3.6));
                    lblWeatherIndex.setText(indexLabel + "  (" + index + "/10)");
                    applyIndexStyle(index);
                    buildForecastUI(forecastDays);
                    if (weatherBox != null) weatherBox.setVisible(true);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (lblWeatherCity != null)
                        lblWeatherCity.setText("⚠️ Météo indisponible — vérifier la clé API");
                });
                System.err.println("Erreur météo : " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private int computeTravelIndex(double temp, int humidity, double windMs, String desc) {
        int score = 10;
        if (temp < 0 || temp > 40)        score -= 4;
        else if (temp < 10 || temp > 35)  score -= 2;
        else if (temp < 15 || temp > 30)  score -= 1;

        if (humidity > 85)      score -= 2;
        else if (humidity > 70) score -= 1;

        double windKmh = windMs * 3.6;
        if (windKmh > 54)       score -= 2;
        else if (windKmh > 36)  score -= 1;

        String dl = desc.toLowerCase();
        if (dl.contains("orage") || dl.contains("neige") || dl.contains("tempête")) score -= 3;
        else if (dl.contains("pluie") || dl.contains("brouillard"))                 score -= 2;
        else if (dl.contains("nuageux"))                                             score -= 1;

        return Math.max(0, score);
    }

    private String getTravelIndexLabel(int index) {
        if (index >= 8) return "🌟 Excellent pour voyager";
        if (index >= 6) return "✅ Bon pour voyager";
        if (index >= 4) return "⚠️ Acceptable";
        if (index >= 2) return "🌧️ Conditions difficiles";
        return "❌ Déconseillé";
    }

    private void applyIndexStyle(int index) {
        if (lblWeatherIndex == null) return;
        String color;
        if (index >= 8)      color = "#27ae60";
        else if (index >= 6) color = "#2980b9";
        else if (index >= 4) color = "#f39c12";
        else                 color = "#e74c3c";
        lblWeatherIndex.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 13px;");
    }

    private List<String[]> parseForecast(JSONObject forecastJson) {
        List<String[]> days = new ArrayList<>();
        JSONArray list = forecastJson.getJSONArray("list");
        String lastDate = "";

        for (int i = 0; i < list.length() && days.size() < 5; i++) {
            JSONObject item = list.getJSONObject(i);
            long dt = item.getLong("dt");
            LocalDate date = Instant.ofEpochSecond(dt).atZone(ZoneId.systemDefault()).toLocalDate();
            String dateStr = date.format(DateTimeFormatter.ofPattern("EEE dd/MM"));

            if (!dateStr.equals(lastDate)) {
                lastDate = dateStr;
                String desc = item.getJSONArray("weather").getJSONObject(0).getString("description");
                String icon = item.getJSONArray("weather").getJSONObject(0).getString("icon");
                String tMin = String.format("%.0f°", item.getJSONObject("main").getDouble("temp_min"));
                String tMax = String.format("%.0f°", item.getJSONObject("main").getDouble("temp_max"));
                days.add(new String[]{dateStr, capitalize(desc), tMin, tMax, icon});
            }
        }
        return days;
    }

    private void buildForecastUI(List<String[]> days) {
        if (forecastContainer == null) return;
        forecastContainer.getChildren().clear();

        for (String[] day : days) {
            VBox card = new VBox(4);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(8));
            card.setMinWidth(90);
            card.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.15);" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: rgba(255,255,255,0.25);" +
                            "-fx-border-radius: 10; -fx-border-width: 1;"
            );

            Label dateL = new Label(day[0]);
            dateL.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: white;");

            ImageView icon = new ImageView();
            try {
                icon.setImage(new Image(
                        "https://openweathermap.org/img/wn/" + day[4] + "@2x.png",
                        48, 48, true, true
                ));
            } catch (Exception ignored) {}
            icon.setFitWidth(48);
            icon.setFitHeight(48);

            Label descL = new Label(day[1]);
            descL.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.85);");
            descL.setWrapText(true);
            descL.setMaxWidth(85);

            Label tempL = new Label(day[2] + " / " + day[3]);
            tempL.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");

            card.getChildren().addAll(dateL, icon, descL, tempL);
            forecastContainer.getChildren().add(card);
        }
    }

    private String fetchUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ==============================
    // IMAGES
    // ==============================
    private void loadImages(Destination d) {
        images = imageService.getImagesByDestination(d.getIdDestination());
        imagesContainer.getChildren().clear();
        currentIndex = 0;
        if (images != null && !images.isEmpty()) {
            for (DestinationImage img : images) {
                File file = new File(img.getUrlImage());
                Image fxImg = new Image(file.toURI().toString());
                ImageView iv = new ImageView(fxImg);
                iv.setFitWidth(140);
                iv.setFitHeight(90);
                iv.setPreserveRatio(true);
                iv.getStyleClass().add("tableImg");
                iv.setOnMouseClicked(e -> {
                    imageView.setImage(iv.getImage());
                    currentIndex = images.indexOf(img);
                });
                imagesContainer.getChildren().add(iv);
            }
            loadImage(0);
        } else {
            imageView.setImage(null);
        }
    }

    private void loadImage(int index) {
        if (images == null || images.isEmpty()) return;
        if (index < 0 || index >= images.size()) return;
        File file = new File(images.get(index).getUrlImage());
        imageView.setImage(new Image(file.toURI().toString()));
    }

    private void nextImage() {
        if (images == null || images.isEmpty()) return;
        currentIndex = (currentIndex + 1) % images.size();
        loadImage(currentIndex);
    }

    private void prevImage() {
        if (images == null || images.isEmpty()) return;
        currentIndex = (currentIndex - 1 + images.size()) % images.size();
        loadImage(currentIndex);
    }

    // ==============================
    // TRANSPORTS
    // ==============================
    private void loadTransports(Destination d) {
        transportContainer.getChildren().clear();
        currentTransport = null;

        List<Transport> transports = transportService.getTransportsByDestination(d.getIdDestination());
        if (transports != null && !transports.isEmpty()) {
            currentTransport = transports.get(0);
            for (Transport t : transports) {
                transportContainer.getChildren().add(createTransportLine(t));
            }
        }

        if (currentTransport != null) {
            lblTransport.setText("Transport : " + currentTransport.getTypeTransport());
            addRouteToMap(currentDestination, currentTransport);
        } else {
            lblTransport.setText("Transport : -");
        }
    }

    private HBox createTransportLine(Transport t) {
        Label type = new Label("Transport : " + t.getTypeTransport());
        type.getStyleClass().add("details-text");
        HBox box = new HBox(type);
        box.setSpacing(10);
        box.getStyleClass().add("glass-card");
        return box;
    }

    // ==============================
    // CARTE
    // ==============================
    private void initMap() {
        mapView = new WebView();
        webEngine = mapView.getEngine();

        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        webEngine.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        );
        webEngine.setJavaScriptEnabled(true);

        URL url = getClass().getResource("/map.html");
        if (url != null) webEngine.load(url.toExternalForm());

        bridge = new JSBridge(lblDistance, lblDuration, lblTransport);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", bridge);

                Object exists = webEngine.executeScript("typeof setDestination === 'function'");
                if (exists != null && "true".equals(exists.toString())
                        && currentDestination != null && currentTransport != null) {
                    addRouteToMap(currentDestination, currentTransport);
                }
            }
        });

        mapPane.setCenter(mapView);
    }

    private void addRouteToMap(Destination d, Transport t) {
        try {
            Object exists = webEngine.executeScript("typeof setDestination === 'function'");
            if (exists != null && "true".equals(exists.toString())) {
                webEngine.executeScript(
                        "setDestination(" + d.getLatitude() + "," + d.getLongitude()
                                + ",'" + t.getTypeTransport() + "')"
                );
            } else {
                System.err.println("JS function setDestination non trouvée !");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ==============================
    // NAVIGATION
    // ==============================
    @FXML
    private void goBackToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientDestinationListView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnBackToList.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            SceneUtil.applyCss(scene);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==============================
    // JS BRIDGE
    // ==============================
    public static class JSBridge {
        private final Label lblDistance, lblDuration, lblTransport;

        public JSBridge(Label lblDistance, Label lblDuration, Label lblTransport) {
            this.lblDistance = lblDistance;
            this.lblDuration = lblDuration;
            this.lblTransport = lblTransport;
        }

        public void sendRouteInfo(String distance, String duration, String transportType) {
            Platform.runLater(() -> {
                lblDistance.setText("Distance : " + distance + " km");
                lblDuration.setText("Durée : " + duration + " min");
                lblTransport.setText("Transport : " + transportType);
            });
        }
    }

    public Destination getCurrentDestination() {
        return currentDestination;
    }
}