package edu.destination.controllers;

import edu.destination.entities.Destination;
import edu.destination.entities.Image;
import edu.destination.entities.Transport;
import edu.destination.entities.Voyage;
import edu.destination.services.ImageService;
import edu.destination.services.TransportService;
import edu.destination.services.VoyageService;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClientDetailsController {

    private static final String WEATHER_API_KEY  = "60b4f367276104cdace06362a000085f";
    private static final String GROQ_API_KEY     = "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";
    private static final String EXCHANGERATE_KEY = "461a090f75d56bf3080aab48";

    // ── HERO
    @FXML private ImageView imageView;
    @FXML private Label     lblName, lblCountryTag, lblBreadcrumb, lblDescShort;
    @FXML private Label     lblVisits, lblLikesHero, lblSaison, lblScoreNum;
    @FXML private HBox      starsContainer, dotsContainer;
    @FXML private Button    btnPrev, btnNext, btnBackToList;

    // ── TRADUCTION
    @FXML private Button btnLangFr, btnLangEn, btnLangAr;
    @FXML private Label  lblTranslating;

    // ── CONTENU
    @FXML private Label lblDestTitle, lblDescription;
    @FXML private Label lblCountry, lblStatSaison, lblStatVisits, lblStatLikes;

    // ── VOYAGES
    @FXML private VBox  voyagesContainer;
    @FXML private Label lblVoyageCount;

    // ── ITINÉRAIRE IA
    @FXML private Label lblItinLoading;
    @FXML private HBox  itinBtnsContainer;
    @FXML private VBox  itinResultContainer;

    // ── CARTE
    @FXML private BorderPane mapPane;
    @FXML private HBox       mapVoyageBtns;
    @FXML private Label      lblRouteInfo;

    // ── MÉTÉO
    @FXML private VBox  weatherBox;
    @FXML private Label lblWeatherCity, lblWeatherTemp, lblWeatherDesc;
    @FXML private Label lblWeatherFeels, lblWeatherHumidity, lblWeatherWind, lblWeatherIndex;
    @FXML private HBox  forecastContainer;

    // ── VIDÉO
    @FXML private Button    btnWatchVideo;
    @FXML private VBox      videoSection, videoLoadingPane, videoErrorPane;
    @FXML private Label     lblVideoTitle, lblVideoError, lblVideoTime;
    @FXML private Button    btnCloseVideo, btnPlayPause, btnStop;
    @FXML private StackPane videoContainer;
    @FXML private HBox      videoControls;
    @FXML private Slider    timeSlider, volumeSlider;

    // ── GALERIE
    @FXML private HBox  galerieContainer;
    @FXML private Label lblGalerieTitre, lblGalerieCount;

    // ── SERVICES
    private final ImageService     imageService     = new ImageService();
    private final TransportService transportService = new TransportService();
    private final VoyageService    voyageService    = new VoyageService();

    // ── STATE
    private Destination  currentDestination;
    private List<Image>  images;
    private int          currentIndex = 0;
    private List<Voyage> currentVoyages;
    private String       originalDescription, originalDestTitle;

    // ── CARTE
    private WebView   mapView;
    private WebEngine webEngine;
    private JSBridge  bridge;

    // ── VIDÉO
    private EmbeddedMediaPlayerComponent vlcPlayer;
    private SwingNode  vlcSwingNode;
    private boolean    vlcAvailable = false, sliderDragging = false, isPlayed = false;
    private Thread     sliderThread;
    private MediaPlayer mediaPlayerFx;
    private MediaView   mediaViewFx;
    private Media       currentMedia;
    private WebView     youtubeWebView;

    // ══════════════════════════════════════════════════════════════
    // INITIALIZE
    // ══════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        if (btnNext      != null) btnNext.setOnAction(e -> nextImage());
        if (btnPrev      != null) btnPrev.setOnAction(e -> prevImage());
        if (btnBackToList!= null) btnBackToList.setOnAction(e -> goBackToList());
        if (timeSlider   != null) { timeSlider.setMin(0); timeSlider.setMax(100); timeSlider.setValue(0); }
        if (volumeSlider != null) volumeSlider.setValue(1.0);
        if (btnLangFr    != null) btnLangFr.setOnAction(e -> activerLangue("fr"));
        if (btnLangEn    != null) btnLangEn.setOnAction(e -> activerLangue("en"));
        if (btnLangAr    != null) btnLangAr.setOnAction(e -> activerLangue("ar"));
        try {
            System.setProperty("jna.library.path", "C:\\Program Files\\VideoLAN\\VLC");
            vlcAvailable = new NativeDiscovery().discover();
        } catch (Exception ignored) {}
        initMap();
    }

    // ══════════════════════════════════════════════════════════════
    // SET DESTINATION
    // ══════════════════════════════════════════════════════════════
    public void setDestination(Destination d) {
        if (d == null) return;
        currentDestination = d;

        if (lblName       != null) lblName.setText(d.getNom());
        if (lblCountryTag != null) lblCountryTag.setText(d.getPays());
        if (lblBreadcrumb != null) lblBreadcrumb.setText(d.getNom());
        if (lblDescShort  != null) {
            String desc = d.getDescription() != null ? d.getDescription() : "";
            lblDescShort.setText(desc.length() > 180 ? desc.substring(0, 180) + "…" : desc);
        }
        if (lblVisits    != null) lblVisits.setText(String.valueOf(d.getNbVisites()));
        if (lblLikesHero != null) lblLikesHero.setText(String.valueOf(d.getNbLikes()));
        if (lblSaison    != null) lblSaison.setText(d.getMeilleureSaison() != null ? d.getMeilleureSaison() : "—");

        if (lblCountry    != null) lblCountry.setText(d.getPays());
        if (lblStatSaison != null) lblStatSaison.setText(d.getMeilleureSaison() != null ? d.getMeilleureSaison() : "—");
        if (lblStatVisits != null) lblStatVisits.setText(String.valueOf(d.getNbVisites()));
        if (lblStatLikes  != null) lblStatLikes.setText(String.valueOf(d.getNbLikes()));

        originalDescription = d.getDescription();
        originalDestTitle   = d.getNom() + ", " + d.getPays();
        if (lblDestTitle   != null) lblDestTitle.setText(originalDestTitle);
        if (lblDescription != null) lblDescription.setText(originalDescription);

        buildStarsUI(d);

        disposeVideo();
        hideVideoSection();
        if (lblVideoTitle != null) lblVideoTitle.setText("  " + d.getNom() + " — Vidéo");
        boolean hasVideo = d.getVideoPath() != null && !d.getVideoPath().isBlank();
        if (btnWatchVideo != null) { btnWatchVideo.setVisible(hasVideo); btnWatchVideo.setManaged(hasVideo); }

        loadImages(d);
        loadVoyages(d);
        buildItineraireBtns(d);
        loadWeather(d);
    }

    // ══════════════════════════════════════════════════════════════
    // NOTE ÉTOILES
    // ══════════════════════════════════════════════════════════════
    private void buildStarsUI(Destination d) {
        if (starsContainer == null) return;
        starsContainer.getChildren().clear();

        double noteVisites = 0;
        int v = d.getNbVisites();
        if (v >= 10000) noteVisites = 2.5; else if (v >= 5000) noteVisites = 2.0;
        else if (v >= 2000) noteVisites = 1.5; else if (v >= 1000) noteVisites = 1.0;
        else if (v >= 500) noteVisites = 0.5;

        double noteSaison = 0;
        String s = d.getMeilleureSaison() != null ? d.getMeilleureSaison() : "";
        if (s.equals("Ete")) noteSaison = 2.5; else if (s.equals("Printemps")) noteSaison = 2.0;
        else if (s.equals("Automne")) noteSaison = 1.5; else if (s.equals("Hiver")) noteSaison = 1.0;

        double noteLikes = 0;
        int l = d.getNbLikes();
        if (l >= 50) noteLikes = 1.0; else if (l >= 20) noteLikes = 0.75;
        else if (l >= 10) noteLikes = 0.5; else if (l >= 5) noteLikes = 0.25;
        else if (l >= 1) noteLikes = 0.1;

        double  note    = Math.min(5.0, noteVisites + noteSaison + noteLikes);
        int     pleines = (int) Math.floor(note);
        boolean demi    = (note - pleines) >= 0.5;
        int     vides   = 5 - pleines - (demi ? 1 : 0);

        if (lblScoreNum != null) lblScoreNum.setText(String.format("%.1f", note));

        HBox stars = new HBox(4);
        stars.setAlignment(Pos.CENTER_RIGHT);
        for (int i = 0; i < pleines; i++) stars.getChildren().add(makeStar("#d4a855"));
        if (demi)                          stars.getChildren().add(makeStar("#8a7030"));
        for (int i = 0; i < vides; i++)   stars.getChildren().add(makeStar("rgba(255,255,255,0.2)"));
        starsContainer.getChildren().add(stars);
    }

    private Label makeStar(String color) {
        Label l = new Label("★");
        l.setStyle("-fx-text-fill:" + color + "; -fx-font-size:18px;");
        return l;
    }

    // ══════════════════════════════════════════════════════════════
    // IMAGES
    // ══════════════════════════════════════════════════════════════
    private void loadImages(Destination d) {
        images = imageService.getImagesByDestination(d.getId());
        currentIndex = 0;
        buildDots();
        buildGalerie(d);
        if (images != null && !images.isEmpty()) loadImage(0);
        else if (imageView != null) imageView.setImage(null);
    }

    private void buildDots() {
        if (dotsContainer == null || images == null) return;
        dotsContainer.getChildren().clear();
        for (int i = 0; i < images.size(); i++) {
            final int idx = i;
            Label dot = new Label();
            dot.setMinWidth(i == 0 ? 24 : 6); dot.setMinHeight(6);
            dot.setStyle(i == 0
                    ? "-fx-background-color:#c8956c;-fx-background-radius:3;"
                    : "-fx-background-color:rgba(255,255,255,0.25);-fx-background-radius:3;");
            dot.setOnMouseClicked(e -> goToImage(idx));
            dotsContainer.getChildren().add(dot);
        }
    }

    private void goToImage(int idx) {
        currentIndex = idx;
        loadImage(idx);
        if (dotsContainer == null) return;
        for (int i = 0; i < dotsContainer.getChildren().size(); i++) {
            Label dot = (Label) dotsContainer.getChildren().get(i);
            if (i == idx) { dot.setMinWidth(24); dot.setStyle("-fx-background-color:#c8956c;-fx-background-radius:3;"); }
            else          { dot.setMinWidth(6);  dot.setStyle("-fx-background-color:rgba(255,255,255,0.25);-fx-background-radius:3;"); }
        }
    }

    private void loadImage(int index) {
        if (images == null || images.isEmpty() || index < 0 || index >= images.size() || imageView == null) return;
        try {
            String url = images.get(index).getUrlImage();
            File f;
            if (url.startsWith("/uploads/")) {
                f = new File("C:/xampp/htdocs/VOYAGE/public" + url);
            } else {
                f = new File(url);
            }
            if (f.exists()) imageView.setImage(new javafx.scene.image.Image(f.toURI().toString()));
        } catch (Exception ignored) {}
    }

    private void nextImage() { if (images != null && !images.isEmpty()) goToImage((currentIndex + 1) % images.size()); }
    private void prevImage() { if (images != null && !images.isEmpty()) goToImage((currentIndex - 1 + images.size()) % images.size()); }

    // ══════════════════════════════════════════════════════════════
    // GALERIE
    // ══════════════════════════════════════════════════════════════
    private void buildGalerie(Destination d) {
        if (galerieContainer == null) return;
        galerieContainer.getChildren().clear();
        if (lblGalerieTitre != null) lblGalerieTitre.setText("Photos de " + d.getNom());
        if (images == null || images.isEmpty()) { if (lblGalerieCount != null) lblGalerieCount.setText("0 photo"); return; }
        if (lblGalerieCount != null) lblGalerieCount.setText(images.size() + " photo" + (images.size() > 1 ? "s" : ""));

        for (int i = 0; i < images.size(); i++) {
            Image img = images.get(i);
            double w = (i == 0) ? 420 : 200, h = (i == 0) ? 300 : 180;
            try {
                String imgUrl = img.getUrlImage();
                File f = imgUrl.startsWith("/uploads/") ? new File("C:/xampp/htdocs/VOYAGE/public" + imgUrl) : new File(imgUrl);                if (!f.exists()) continue;
                ImageView iv = new ImageView(new javafx.scene.image.Image(f.toURI().toString()));
                iv.setFitWidth(w); iv.setFitHeight(h); iv.setPreserveRatio(false);
                Rectangle clip = new Rectangle(w, h);
                clip.setArcWidth(12); clip.setArcHeight(12);
                iv.setClip(clip);
                StackPane pane = new StackPane(iv);
                Pane overlay = new Pane();
                overlay.setPrefSize(w, h);
                overlay.setStyle("-fx-background-color:rgba(200,149,108,0.25);-fx-background-radius:12;");
                overlay.setVisible(false);
                pane.getChildren().add(overlay);
                pane.setOnMouseEntered(e -> overlay.setVisible(true));
                pane.setOnMouseExited(e -> overlay.setVisible(false));
                final int idx = i;
                pane.setOnMouseClicked(e -> goToImage(idx));
                galerieContainer.getChildren().add(pane);
            } catch (Exception ignored) {}
        }
    }

    // ══════════════════════════════════════════════════════════════
    // VOYAGES TICKETS
    // ══════════════════════════════════════════════════════════════
    private void loadVoyages(Destination d) {
        if (voyagesContainer == null) return;
        voyagesContainer.getChildren().clear();
        if (mapVoyageBtns != null) mapVoyageBtns.getChildren().clear();

        currentVoyages = voyageService.getVoyagesByDestination(d.getId());
        if (lblVoyageCount != null)
            lblVoyageCount.setText(currentVoyages.size() + " voyage" + (currentVoyages.size() > 1 ? "s" : ""));

        if (currentVoyages.isEmpty()) {
            Label lbl = new Label("Aucun voyage disponible.");
            lbl.setStyle("-fx-text-fill:#a8a4a0;-fx-font-size:13px;-fx-padding:16 24 16 24;");
            voyagesContainer.getChildren().add(lbl);
            return;
        }

        for (int i = 0; i < currentVoyages.size(); i++) {
            Voyage v = currentVoyages.get(i);
            voyagesContainer.getChildren().add(createTicket(v, d));

            if (mapVoyageBtns != null) {
                List<Transport> transports = transportService.getTransportsByVoyage(v.getId());
                String icons = transports.stream().map(t -> getTransportIcon(t.getTypeTransport())).reduce("", String::concat);
                Button btn = new Button(icons + " Voyage " + v.getId());
                final Voyage voyage = v;
                boolean first = (i == 0);
                btn.setStyle(first
                        ? "-fx-background-color:#c8956c;-fx-text-fill:white;-fx-font-size:11px;-fx-font-weight:700;-fx-background-radius:20;-fx-padding:5 14 5 14;-fx-cursor:hand;"
                        : "-fx-background-color:white;-fx-text-fill:#7a7670;-fx-font-size:11px;-fx-font-weight:600;-fx-border-color:#e8e4dc;-fx-border-radius:20;-fx-background-radius:20;-fx-border-width:1;-fx-padding:5 14 5 14;-fx-cursor:hand;");
                btn.setOnAction(e -> {
                    mapVoyageBtns.getChildren().forEach(c -> { if (c instanceof Button) c.setStyle(
                            "-fx-background-color:white;-fx-text-fill:#7a7670;-fx-font-size:11px;-fx-font-weight:600;-fx-border-color:#e8e4dc;-fx-border-radius:20;-fx-background-radius:20;-fx-border-width:1;-fx-padding:5 14 5 14;-fx-cursor:hand;"); });
                    btn.setStyle("-fx-background-color:#c8956c;-fx-text-fill:white;-fx-font-size:11px;-fx-font-weight:700;-fx-background-radius:20;-fx-padding:5 14 5 14;-fx-cursor:hand;");
                    showVoyageRoute(voyage, d);
                });
                mapVoyageBtns.getChildren().add(btn);
            }
        }
        if (!currentVoyages.isEmpty()) showVoyageRoute(currentVoyages.get(0), d);
    }

    private VBox createTicket(Voyage v, Destination d) {
        double  prix   = v.getPrix();
        boolean remise = d.getMeilleureSaison() != null && d.getMeilleureSaison().equals("Hiver");
        if (remise) prix *= 0.80;

        long dureeJours = 0;
        if (v.getDateDepart() != null && v.getDateArrivee() != null)
            dureeJours = java.time.temporal.ChronoUnit.DAYS.between(v.getDateDepart(), v.getDateArrivee());

        List<Transport> transports       = transportService.getTransportsByVoyage(v.getId());
        Transport       primaryTransport = transports.isEmpty() ? null : transports.get(0);
        boolean         expire          = v.getDateDepart() != null && v.getDateDepart().isBefore(LocalDate.now());

        StackPane ticket = new StackPane();
        ticket.setMinHeight(200);

        if (images != null && !images.isEmpty()) {
            try {
                File f = new File(images.get(0).getUrlImage());
                if (f.exists()) {
                    ImageView bg = new ImageView(new javafx.scene.image.Image(f.toURI().toString()));
                    bg.setFitWidth(368); bg.setFitHeight(225); bg.setPreserveRatio(false);
                    bg.setStyle("-fx-opacity:0.38;");
                    Rectangle bgClip = new Rectangle(368, 225); bgClip.setArcWidth(16); bgClip.setArcHeight(16);
                    bg.setClip(bgClip);
                    ticket.getChildren().add(bg);
                }
            } catch (Exception ignored) {}
        }

        Pane bgGrad = new Pane(); bgGrad.setPrefSize(368, 225);
        bgGrad.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, rgba(30,26,21,0.92), rgba(13,14,18,0.92)); -fx-background-radius:16;");

        Pane veil = new Pane(); veil.setPrefSize(368, 225);
        veil.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, rgba(0,0,0,0.02), rgba(0,0,0,0.7)); -fx-background-radius:16;");

        Label notchL = new Label(); notchL.setStyle("-fx-background-color:#f7f5f1;-fx-background-radius:50%;-fx-min-width:20;-fx-max-width:20;-fx-min-height:20;-fx-max-height:20;");
        StackPane.setAlignment(notchL, Pos.CENTER_LEFT); StackPane.setMargin(notchL, new Insets(0,0,0,-10));
        Label notchR = new Label(); notchR.setStyle("-fx-background-color:#f7f5f1;-fx-background-radius:50%;-fx-min-width:20;-fx-max-width:20;-fx-min-height:20;-fx-max-height:20;");
        StackPane.setAlignment(notchR, Pos.CENTER_RIGHT); StackPane.setMargin(notchR, new Insets(0,-10,0,0));

        VBox inner = new VBox(10); inner.setPadding(new Insets(18,20,16,20)); inner.setAlignment(Pos.TOP_LEFT);

        // Ligne 1
        HBox row1 = new HBox(); row1.setAlignment(Pos.CENTER_LEFT);
        Label voyageId = new Label("Voyage " + v.getId());
        voyageId.setStyle("-fx-text-fill:rgba(255,255,255,0.35);-fx-font-size:10px;-fx-font-weight:600;");
        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);
        HBox rightTop = new HBox(8); rightTop.setAlignment(Pos.CENTER_RIGHT);
        if (remise) {
            Label br = new Label("❄ -20% Offre Hiver");
            br.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #1e3a5f, #2563eb); -fx-text-fill:white;-fx-font-size:10px;-fx-font-weight:700;-fx-padding:4 10 4 10;-fx-background-radius:12;");
            rightTop.getChildren().add(br);
        }
        VBox prixBox = new VBox(2); prixBox.setAlignment(Pos.CENTER_RIGHT);
        Label prixLabel = new Label(String.format("%.0f €", prix));
        prixLabel.setStyle("-fx-text-fill:white;-fx-font-size:26px;-fx-font-weight:300;-fx-font-family:Georgia;");
        Label dureeLabel = new Label(dureeJours > 0 ? dureeJours + " jour" + (dureeJours > 1 ? "s" : "") : "");
        dureeLabel.setStyle("-fx-text-fill:rgba(255,255,255,0.35);-fx-font-size:10px;");
        prixBox.getChildren().addAll(prixLabel, dureeLabel);
        rightTop.getChildren().add(prixBox);
        row1.getChildren().addAll(voyageId, sp1, rightTop);

        // Ligne 2 : Route
        HBox route = new HBox(8); route.setAlignment(Pos.CENTER_LEFT);
        route.setStyle("-fx-background-color:rgba(255,255,255,0.1);-fx-background-radius:12;-fx-border-color:rgba(255,255,255,0.15);-fx-border-radius:12;-fx-border-width:1;-fx-padding:12 14 12 14;");
        VBox depBox = new VBox(3); HBox.setHgrow(depBox, Priority.ALWAYS);
        Label depNom  = new Label(v.getPointDepart()  != null ? v.getPointDepart()  : "?");
        depNom.setStyle("-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:600;");
        Label depDate = new Label(v.getDateDepart()   != null ? v.getDateDepart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—");
        depDate.setStyle("-fx-text-fill:rgba(255,255,255,0.35);-fx-font-size:10px;");
        depBox.getChildren().addAll(depNom, depDate);
        Label iconTr = new Label(primaryTransport != null ? getTransportIcon(primaryTransport.getTypeTransport()) : "✈");
        iconTr.setStyle("-fx-font-size:16px;");
        VBox arrBox = new VBox(3); arrBox.setAlignment(Pos.TOP_RIGHT); HBox.setHgrow(arrBox, Priority.ALWAYS);
        Label arrNom  = new Label(v.getPointArrivee() != null ? v.getPointArrivee() : "?");
        arrNom.setStyle("-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:600;");
        Label arrDate = new Label(v.getDateArrivee()  != null ? v.getDateArrivee().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—");
        arrDate.setStyle("-fx-text-fill:rgba(255,255,255,0.35);-fx-font-size:10px;");
        arrBox.getChildren().addAll(arrNom, arrDate);
        route.getChildren().addAll(depBox, iconTr, arrBox);

        // Ligne 3 : Badges transport
        HBox row3 = new HBox(6); row3.setAlignment(Pos.CENTER_LEFT);
        for (Transport t : transports) {
            Label badge = new Label(getTransportIcon(t.getTypeTransport()) + " " + t.getTypeTransport());
            badge.setStyle("-fx-background-color:rgba(255,255,255,0.1);-fx-border-color:rgba(255,255,255,0.15);-fx-border-radius:30;-fx-background-radius:30;-fx-border-width:1;-fx-text-fill:rgba(255,255,255,0.65);-fx-font-size:10px;-fx-font-weight:500;-fx-padding:3 10 3 10;");
            row3.getChildren().add(badge);
        }

        // Ligne 4 : Convertisseur devises
        final double prixFinal = prix;
        Label prixConv = new Label(String.format("%.2f €", prixFinal));
        prixConv.setStyle("-fx-text-fill:rgba(255,255,255,0.7);-fx-font-size:12px;-fx-font-weight:600;");
        HBox devises = new HBox(5); devises.setAlignment(Pos.CENTER_LEFT);
        devises.getChildren().add(prixConv);
        for (String devise : new String[]{"EUR","USD","GBP","TND","MAD"}) {
            Button bd = new Button(devise);
            bd.setStyle("-fx-background-color:rgba(255,255,255,0.08);-fx-border-color:rgba(255,255,255,0.2);-fx-border-radius:20;-fx-background-radius:20;-fx-border-width:1;-fx-text-fill:rgba(255,255,255,0.7);-fx-font-size:10px;-fx-padding:3 10 3 10;-fx-cursor:hand;");
            bd.setOnAction(e -> convertirDevise(prixFinal, devise, prixConv));
            devises.getChildren().add(bd);
        }

        // ══ Ligne 5 : Action ══════════════════════════════════════
        VBox actionRow = new VBox(8);

        if (expire) {
            Label expLbl = new Label("⚠ Ce voyage est expiré — date de départ dépassée");
            expLbl.setWrapText(true);
            expLbl.setStyle("-fx-background-color:rgba(127,29,29,0.4);-fx-border-color:#dc2626;-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1;-fx-text-fill:#f87171;-fx-font-size:11px;-fx-font-weight:600;-fx-padding:10 16 10 16;");
            actionRow.getChildren().add(expLbl);

        } else if (v.getPaid() == 1) {
            Label paidLbl = new Label("✓ Déjà réservé et payé");
            paidLbl.setStyle("-fx-background-color:rgba(126,201,154,0.15);-fx-border-color:rgba(126,201,154,0.3);-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1;-fx-text-fill:rgba(126,201,154,0.9);-fx-font-size:12px;-fx-font-weight:500;-fx-padding:10 16 10 16;");
            actionRow.getChildren().add(paidLbl);

        } else {
            // ✅ Vérifie si l'utilisateur connecté a déjà réservé ce voyage
            utilisateur currentUser = hebergement.controllers.MainLayoutController.getCurrentUser();
            boolean dejaReserve = currentUser != null && voyageService.isDejaReserve(v.getId(), currentUser.getId());

            if (dejaReserve) {
                // Déjà réservé — affiche statut + lien Mon Espace
                Label resLbl = new Label("✓ Déjà réservé — en attente de paiement");
                resLbl.setWrapText(true);
                resLbl.setStyle("-fx-background-color:rgba(251,191,36,0.15);-fx-border-color:rgba(251,191,36,0.4);-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1;-fx-text-fill:#d97706;-fx-font-size:12px;-fx-font-weight:600;-fx-padding:10 16 10 16;");
                Button btnVoir = new Button("✈ Voir dans Mon Espace →");
                btnVoir.setStyle("-fx-background-color:#1a1814;-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:600;-fx-background-radius:10;-fx-padding:9 0 9 0;-fx-cursor:hand;");
                btnVoir.setMaxWidth(Double.MAX_VALUE);
                btnVoir.setOnAction(e -> naviguerVersMonEspace());
                actionRow.getChildren().addAll(resLbl, btnVoir);

            } else {
                // ✅ Bouton Réserver — appelle handleReserver()
                Button btnRes = new Button("✈ Réserver ce voyage →");
                btnRes.setStyle("-fx-background-color:#c8956c;-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:600;-fx-background-radius:10;-fx-padding:11 0 11 0;-fx-cursor:hand;");
                btnRes.setMaxWidth(Double.MAX_VALUE);
                btnRes.setOnMouseEntered(e -> btnRes.setStyle("-fx-background-color:#b8845a;-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:600;-fx-background-radius:10;-fx-padding:11 0 11 0;-fx-cursor:hand;"));
                btnRes.setOnMouseExited(e ->  btnRes.setStyle("-fx-background-color:#c8956c;-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:600;-fx-background-radius:10;-fx-padding:11 0 11 0;-fx-cursor:hand;"));
                // ✅ ICI : appel réel à handleReserver au lieu du println
                btnRes.setOnAction(e -> handleReserver(v, btnRes, actionRow));
                actionRow.getChildren().add(btnRes);
            }
        }

        inner.getChildren().addAll(row1, route, row3, devises, actionRow);
        ticket.getChildren().addAll(bgGrad, veil, notchL, notchR, inner);
        ticket.setOnMouseEntered(e -> { ticket.setTranslateY(-3); ticket.setStyle("-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(200,149,108,0.3),20,0,0,6);"); });
        ticket.setOnMouseExited(e ->  { ticket.setTranslateY(0);  ticket.setStyle("-fx-cursor:hand;"); });
        ticket.setOnMouseClicked(e -> { if (!(e.getTarget() instanceof Button)) showVoyageRoute(v, d); });

        VBox wrapper = new VBox(ticket);
        VBox.setMargin(ticket, new Insets(0, 16, 14, 16));
        return wrapper;
    }

    // ══════════════════════════════════════════════════════════════
    // ✅ RÉSERVATION
    // ══════════════════════════════════════════════════════════════
    private void handleReserver(Voyage v, Button btnRes, VBox actionRow) {
        System.out.println("=== handleReserver appelé pour voyage " + v.getId() + " ===");

        utilisateur currentUser = hebergement.controllers.MainLayoutController.getCurrentUser();
        if (currentUser == null) {
            System.out.println("ERREUR : utilisateur non connecté");
            showAlert(Alert.AlertType.WARNING, "Connexion requise",
                    "Vous devez être connecté pour réserver un voyage.");
            return;
        }

        System.out.println("userId = " + currentUser.getId());
        btnRes.setDisable(true);
        btnRes.setText("⏳ Réservation...");

        boolean success = voyageService.reserverVoyage(v.getId(), currentUser.getId());
        System.out.println("reserverVoyage retourne : " + success);

        if (success) {
            actionRow.getChildren().clear();

            Label okLbl = new Label("✅ Voyage réservé avec succès !");
            okLbl.setWrapText(true);
            okLbl.setStyle(
                    "-fx-background-color:rgba(16,185,129,0.15);" +
                            "-fx-border-color:rgba(16,185,129,0.4);" +
                            "-fx-border-radius:10;-fx-background-radius:10;-fx-border-width:1;" +
                            "-fx-text-fill:#059669;-fx-font-size:12px;-fx-font-weight:700;" +
                            "-fx-padding:10 16 10 16;"
            );

            Button btnVoir = new Button("✈ Voir dans Mon Espace →");
            btnVoir.setStyle(
                    "-fx-background-color:#c8956c;-fx-text-fill:white;" +
                            "-fx-font-size:13px;-fx-font-weight:700;" +
                            "-fx-background-radius:10;-fx-padding:11 0 11 0;-fx-cursor:hand;"
            );
            btnVoir.setMaxWidth(Double.MAX_VALUE);
            btnVoir.setOnAction(ev -> naviguerVersMonEspace());

            actionRow.getChildren().addAll(okLbl, btnVoir);

        } else {
            System.out.println("Réservation échouée — déjà réservé ou erreur SQL");
            showAlert(Alert.AlertType.INFORMATION, "Déjà réservé",
                    "Vous avez déjà réservé ce voyage.");
            btnRes.setDisable(false);
            btnRes.setText("✈ Réserver ce voyage →");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // ✅ NAVIGATION VERS MON ESPACE — onglet Voyages
    // ══════════════════════════════════════════════════════════════
    private void naviguerVersMonEspace() {
        hebergement.controllers.MonEspaceController.requestOpenVoyagesTab();
        hebergement.controllers.ClientLayoutController client =
                hebergement.controllers.ClientLayoutController.getInstance();
        if (client != null) {
            client.goMonEspace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void convertirDevise(double prixEur, String devise, Label lbl) {
        if (devise.equals("EUR")) { lbl.setText(String.format("%.2f €", prixEur)); return; }
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject(fetchUrl("https://v6.exchangerate-api.com/v6/" + EXCHANGERATE_KEY + "/pair/EUR/" + devise));
                if (!"success".equals(json.optString("result"))) { Platform.runLater(() -> lbl.setText(String.format("%.2f €", prixEur))); return; }
                double converted = prixEur * json.optDouble("conversion_rate", 1.0);
                String sym = switch (devise) { case "USD" -> "$"; case "GBP" -> "£"; case "TND" -> "TND "; case "MAD" -> "MAD "; default -> devise + " "; };
                Platform.runLater(() -> lbl.setText(String.format("%.2f %s", converted, sym)));
            } catch (Exception ex) { Platform.runLater(() -> lbl.setText(String.format("%.2f €", prixEur))); }
        }).start();
    }

    private String getTransportIcon(String type) {
        if (type == null) return "🚗";
        String l = type.toLowerCase();
        if (l.contains("avion") || l.contains("vol")) return "✈";
        if (l.contains("train")) return "🚆";
        if (l.contains("bus") || l.contains("car")) return "🚌";
        if (l.contains("bateau")) return "🚢";
        return "🚗";
    }

    // ══════════════════════════════════════════════════════════════
    // CARTE LEAFLET
    // ══════════════════════════════════════════════════════════════
    private void initMap() {
        mapView   = new WebView();
        webEngine = mapView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        URL url = getClass().getResource("/map.html");
        if (url == null) return;
        webEngine.load(url.toExternalForm());
        bridge = new JSBridge(lblRouteInfo);
        webEngine.getLoadWorker().stateProperty().addListener((obs, o, n) -> {
            if (n == Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaConnector", bridge);
                    if (currentDestination != null && currentVoyages != null && !currentVoyages.isEmpty())
                        showVoyageRoute(currentVoyages.get(0), currentDestination);
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
        if (mapPane != null) {
            mapPane.setCenter(mapView);
            mapView.prefWidthProperty().bind(mapPane.widthProperty());
            mapView.prefHeightProperty().bind(mapPane.heightProperty());
        }
    }

    private void showVoyageRoute(Voyage v, Destination d) {
        List<Transport> ts   = transportService.getTransportsByVoyage(v.getId());
        String          type = ts.isEmpty() ? "Avion" : ts.get(0).getTypeTransport();
        Platform.runLater(() -> {
            try {
                String dep = v.getPointDepart() != null ? v.getPointDepart().replace("'", "\\'") : "";
                String arr = d.getNom().replace("'", "\\'");
                Object ex1 = webEngine.executeScript("typeof setDestinationWithDepart === 'function'");
                if ("true".equals(String.valueOf(ex1)))
                    webEngine.executeScript("setDestinationWithDepart(" + d.getLatitude() + "," + d.getLongitude() + ",'" + type + "','" + dep + "','" + arr + "')");
                else {
                    Object ex2 = webEngine.executeScript("typeof setDestination === 'function'");
                    if ("true".equals(String.valueOf(ex2)))
                        webEngine.executeScript("setDestination(" + d.getLatitude() + "," + d.getLongitude() + ",'" + type + "')");
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // ITINÉRAIRE IA
    // ══════════════════════════════════════════════════════════════
    private void buildItineraireBtns(Destination d) {
        if (itinBtnsContainer == null) return;
        itinBtnsContainer.getChildren().clear();
        if (itinResultContainer != null) { itinResultContainer.setVisible(false); itinResultContainer.setManaged(false); itinResultContainer.getChildren().clear(); }

        List<Integer> durees = new ArrayList<>();
        if (currentVoyages != null) {
            for (Voyage v : currentVoyages) {
                if (v.getDateDepart() != null && v.getDateArrivee() != null) {
                    int nb = (int) java.time.temporal.ChronoUnit.DAYS.between(v.getDateDepart(), v.getDateArrivee());
                    if (nb > 0 && !durees.contains(nb)) durees.add(nb);
                }
            }
        }
        if (durees.isEmpty()) { durees.add(3); durees.add(5); durees.add(7); }

        for (int duree : durees) {
            Button btn = new Button(duree + " jour" + (duree > 1 ? "s" : ""));
            styleBtnInactif(btn);
            btn.setOnMouseEntered(e -> styleBtnActif(btn));
            btn.setOnMouseExited(e ->  styleBtnInactif(btn));
            final int d2 = duree;
            btn.setOnAction(e -> genererItineraire(d, d2));
            itinBtnsContainer.getChildren().add(btn);
        }

        Label lbl = new Label("Durée personnalisée :");
        lbl.setStyle("-fx-text-fill:#8a8880;-fx-font-size:12px;");
        Spinner<Integer> spinner = new Spinner<>(1, 30, 3);
        spinner.setEditable(true); spinner.setPrefWidth(80);
        Button btnC = new Button("Générer →");
        btnC.setStyle("-fx-background-color:#1a1814;-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:600;-fx-background-radius:8;-fx-padding:8 18 8 18;-fx-cursor:hand;");
        btnC.setOnAction(e -> genererItineraire(d, spinner.getValue()));
        HBox row = new HBox(10, lbl, spinner, btnC);
        row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(8, 0, 0, 0));
        itinBtnsContainer.getChildren().add(row);
    }

    private void styleBtnInactif(Button b) { b.setStyle("-fx-background-color:transparent;-fx-border-color:rgba(200,149,108,0.5);-fx-border-radius:30;-fx-background-radius:30;-fx-text-fill:#c8956c;-fx-font-size:13px;-fx-font-weight:500;-fx-padding:10 26 10 26;-fx-cursor:hand;"); }
    private void styleBtnActif(Button b)   { b.setStyle("-fx-background-color:#c8956c;-fx-border-color:#c8956c;-fx-border-radius:30;-fx-background-radius:30;-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:500;-fx-padding:10 26 10 26;-fx-cursor:hand;"); }

    private void genererItineraire(Destination d, int duree) {
        if (lblItinLoading != null) lblItinLoading.setText("⟳ Génération en cours...");
        if (itinResultContainer != null) { itinResultContainer.setVisible(false); itinResultContainer.setManaged(false); itinResultContainer.getChildren().clear(); }
        new Thread(() -> {
            try {
                String content = callGroqItineraire(d.getNom(), d.getPays(), duree);
                content = content.replaceAll("```json", "").replaceAll("```", "").trim();
                int start = content.indexOf('{'), end = content.lastIndexOf('}');
                if (start >= 0 && end > start) content = content.substring(start, end + 1);
                JSONObject json  = new JSONObject(content);
                JSONArray  jours = json.optJSONArray("jours");
                if (jours == null) { Platform.runLater(() -> { if (lblItinLoading != null) lblItinLoading.setText("❌ Format invalide"); }); return; }
                final JSONArray joursF = jours; final int dF = duree;
                Platform.runLater(() -> { if (lblItinLoading != null) lblItinLoading.setText(""); buildItineraireUI(joursF, dF); });
            } catch (Exception ex) { Platform.runLater(() -> { if (lblItinLoading != null) lblItinLoading.setText("❌ " + ex.getMessage()); }); }
        }).start();
    }

    private String callGroqItineraire(String nom, String pays, int duree) throws Exception {
        String prompt = "Tu es un expert en voyage. Génère un itinéraire touristique détaillé de "
                + duree + " jours pour " + nom + ", " + pays + ".\n"
                + "Réponds UNIQUEMENT avec ce JSON (sans aucun texte avant ou après) :\n"
                + "{\"jours\":[{\"jour\":1,\"titre\":\"Titre\",\"matin\":\"...\",\"dejeuner\":\"...\",\"apres_midi\":\"...\",\"diner\":\"...\",\"transport\":\"...\",\"conseil\":\"...\"}]}";
        String body = "{\"model\":\"llama-3.3-70b-versatile\",\"messages\":[{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}],\"max_tokens\":3000,\"temperature\":0.7}";
        URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
        conn.setDoOutput(true); conn.setConnectTimeout(25000); conn.setReadTimeout(45000);
        try (OutputStream os = conn.getOutputStream()) { os.write(body.getBytes(StandardCharsets.UTF_8)); }
        int status = conn.getResponseCode();
        InputStream is = status == 200 ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) { String line; while ((line = br.readLine()) != null) sb.append(line); }
        if (status != 200) throw new Exception("HTTP " + status);
        JSONObject resp = new JSONObject(sb.toString());
        return resp.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
    }

    private void buildItineraireUI(JSONArray jours, int dureeTotal) {
        if (itinResultContainer == null) return;
        itinResultContainer.getChildren().clear();
        String[] keys   = {"matin","dejeuner","apres_midi","diner","transport","conseil"};
        String[] labels = {"MATIN","DÉJEUNER","APRÈS-MIDI","DÎNER","TRANSPORT","CONSEIL"};
        String[] icons  = {"🌅","🍽","☀","🌙","🚗","💡"};

        for (int i = 0; i < jours.length(); i++) {
            JSONObject jour = jours.getJSONObject(i);
            HBox header = new HBox(14); header.setAlignment(Pos.CENTER_LEFT);
            header.setStyle("-fx-background-color:#fff8ee;-fx-border-color:#fde8c0;-fx-border-width:0 0 1 0;-fx-padding:14 22 14 22;");
            Label numLbl = new Label(String.valueOf(jour.optInt("jour", i + 1)));
            numLbl.setStyle("-fx-font-size:34px;-fx-font-weight:700;-fx-text-fill:#c8956c;-fx-font-family:Georgia;-fx-min-width:44;");
            Label titreLbl = new Label(jour.optString("titre", "Jour " + (i + 1)));
            titreLbl.setStyle("-fx-font-size:14px;-fx-font-weight:600;-fx-text-fill:#1a1814;");
            header.getChildren().addAll(numLbl, titreLbl);

            VBox slots = new VBox(); slots.setStyle("-fx-padding:6 22 14 22;-fx-background-color:white;");
            for (int s = 0; s < keys.length; s++) {
                String val = jour.optString(keys[s], "").trim();
                if (val.isEmpty()) continue;
                HBox slotBox = new HBox(12); slotBox.setAlignment(Pos.TOP_LEFT);
                slotBox.setStyle("-fx-padding:10 0 10 0;-fx-border-color:transparent transparent #f5f2ec transparent;-fx-border-width:0 0 1 0;");
                VBox iconBox = new VBox(); iconBox.setAlignment(Pos.CENTER); iconBox.setMinWidth(34); iconBox.setMaxWidth(34);
                iconBox.setStyle("-fx-background-color:#fff8ee;-fx-background-radius:9;-fx-border-color:#fde8c0;-fx-border-radius:9;-fx-border-width:1;-fx-padding:7 8 7 8;");
                Label iconLbl = new Label(icons[s]); iconLbl.setStyle("-fx-font-size:13px;");
                iconBox.getChildren().add(iconLbl);
                VBox textBox = new VBox(3); HBox.setHgrow(textBox, Priority.ALWAYS);
                Label keyLbl = new Label(labels[s]); keyLbl.setStyle("-fx-font-size:9px;-fx-font-weight:800;-fx-text-fill:#c8956c;");
                Label valLbl = new Label(val); valLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#5a5450;-fx-line-spacing:3;"); valLbl.setWrapText(true);
                textBox.getChildren().addAll(keyLbl, valLbl);
                slotBox.getChildren().addAll(iconBox, textBox);
                slots.getChildren().add(slotBox);
            }
            VBox dayCard = new VBox(); dayCard.setStyle("-fx-background-color:white;-fx-background-radius:14;-fx-border-color:#eeece8;-fx-border-radius:14;-fx-border-width:1;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),10,0,0,3);");
            VBox.setMargin(dayCard, new Insets(0,0,12,0));
            dayCard.getChildren().addAll(header, slots);
            itinResultContainer.getChildren().add(dayCard);
        }
        Button btnPdf = new Button("⬇  Télécharger l'itinéraire PDF");
        btnPdf.setStyle("-fx-background-color:#c8956c;-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:700;-fx-background-radius:30;-fx-padding:12 32 12 32;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(200,149,108,0.4),10,0,0,4);");
        VBox.setMargin(btnPdf, new Insets(8,0,0,0));
        btnPdf.setOnAction(e -> System.out.println("PDF " + dureeTotal + " jours"));
        itinResultContainer.getChildren().add(btnPdf);
        itinResultContainer.setVisible(true); itinResultContainer.setManaged(true);
    }

    // ══════════════════════════════════════════════════════════════
    // MÉTÉO
    // ══════════════════════════════════════════════════════════════
    private void loadWeather(Destination d) {
        double lat = d.getLatitude(), lon = d.getLongitude();
        if (lat == 0 && lon == 0) return;
        new Thread(() -> {
            try {
                JSONObject cur = new JSONObject(fetchUrl("https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + WEATHER_API_KEY + "&units=metric&lang=fr"));
                double temp = cur.getJSONObject("main").getDouble("temp");
                double feels = cur.getJSONObject("main").getDouble("feels_like");
                int    hum   = cur.getJSONObject("main").getInt("humidity");
                double wind  = cur.getJSONObject("wind").getDouble("speed");
                String desc  = cur.getJSONArray("weather").getJSONObject(0).getString("description");
                String city  = cur.getString("name");
                JSONObject fc = new JSONObject(fetchUrl("https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&appid=" + WEATHER_API_KEY + "&units=metric&lang=fr&cnt=40"));
                List<String[]> days = parseForecast(fc);
                int    idx   = computeTravelIndex(temp, hum, wind, desc);
                String idxLbl = getTravelIndexLabel(idx);
                Platform.runLater(() -> {
                    if (lblWeatherCity     != null) lblWeatherCity.setText(city);
                    if (lblWeatherTemp     != null) lblWeatherTemp.setText(String.format("%.0f°C", temp));
                    if (lblWeatherDesc     != null) lblWeatherDesc.setText(capitalize(desc));
                    if (lblWeatherFeels    != null) lblWeatherFeels.setText(String.format("%.0f°C", feels));
                    if (lblWeatherHumidity != null) lblWeatherHumidity.setText(hum + "%");
                    if (lblWeatherWind     != null) lblWeatherWind.setText(String.format("%.0f km/h", wind * 3.6));
                    if (lblWeatherIndex    != null) { lblWeatherIndex.setText(idxLbl + " (" + idx + "/10)"); applyIndexStyle(idx); }
                    buildForecastUI(days);
                    if (weatherBox != null) { weatherBox.setVisible(true); weatherBox.setManaged(true); }
                });
            } catch (Exception e) { Platform.runLater(() -> { if (lblWeatherCity != null) lblWeatherCity.setText("Météo indisponible"); }); }
        }).start();
    }

    private int computeTravelIndex(double temp, int hum, double windMs, String desc) {
        int score = 10;
        if (temp < 0 || temp > 40) score -= 4; else if (temp < 10 || temp > 35) score -= 2; else if (temp < 15 || temp > 30) score -= 1;
        if (hum > 85) score -= 2; else if (hum > 70) score -= 1;
        double wkm = windMs * 3.6;
        if (wkm > 54) score -= 2; else if (wkm > 36) score -= 1;
        String dl = desc.toLowerCase();
        if (dl.contains("orage") || dl.contains("neige") || dl.contains("tempête")) score -= 3;
        else if (dl.contains("pluie") || dl.contains("brouillard")) score -= 2;
        else if (dl.contains("nuageux")) score -= 1;
        return Math.max(0, score);
    }

    private String getTravelIndexLabel(int i) {
        if (i >= 8) return "Excellent pour voyager"; if (i >= 6) return "Bon pour voyager";
        if (i >= 4) return "Acceptable"; if (i >= 2) return "Conditions difficiles"; return "Déconseillé";
    }

    private void applyIndexStyle(int i) {
        if (lblWeatherIndex == null) return;
        String c = i >= 8 ? "#2ecc71" : i >= 6 ? "#74b9ff" : i >= 4 ? "#fdcb6e" : "#ff7675";
        lblWeatherIndex.setStyle("-fx-text-fill:" + c + ";-fx-font-weight:900;-fx-font-size:12px;");
    }

    private List<String[]> parseForecast(JSONObject fc) {
        List<String[]> days = new ArrayList<>();
        JSONArray list = fc.getJSONArray("list");
        String last = "";
        for (int i = 0; i < list.length() && days.size() < 5; i++) {
            JSONObject item = list.getJSONObject(i);
            LocalDate date = Instant.ofEpochSecond(item.getLong("dt")).atZone(ZoneId.systemDefault()).toLocalDate();
            String ds = date.format(DateTimeFormatter.ofPattern("EEE dd/MM"));
            if (!ds.equals(last)) {
                last = ds;
                days.add(new String[]{ds,
                        capitalize(item.getJSONArray("weather").getJSONObject(0).getString("description")),
                        String.format("%.0f°", item.getJSONObject("main").getDouble("temp_min")),
                        String.format("%.0f°", item.getJSONObject("main").getDouble("temp_max")),
                        item.getJSONArray("weather").getJSONObject(0).getString("icon")});
            }
        }
        return days;
    }

    private void buildForecastUI(List<String[]> days) {
        if (forecastContainer == null) return;
        forecastContainer.getChildren().clear();
        String[] bgColors = {"#2c4a6e", "#3a6b3a", "#6b4a1e", "#4a3a6b", "#6b3a3a"};
        for (int i = 0; i < days.size(); i++) {
            String[] day = days.get(i);
            VBox card = new VBox(6);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(14,12,14,12));
            card.setMinWidth(100); card.setMaxWidth(115);
            card.setStyle("-fx-background-color:" + bgColors[i % bgColors.length] + ";-fx-background-radius:12;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),8,0,0,3);");
            Label dl = new Label(day[0]); dl.setStyle("-fx-font-weight:700;-fx-font-size:10px;-fx-text-fill:rgba(255,255,255,0.75);");
            ImageView ic = new ImageView();
            try { ic.setImage(new javafx.scene.image.Image("https://openweathermap.org/img/wn/" + day[4] + "@2x.png",44,44,true,true)); } catch (Exception ignored) {}
            ic.setFitWidth(44); ic.setFitHeight(44);
            Label descL = new Label(day[1]); descL.setStyle("-fx-font-size:10px;-fx-text-fill:rgba(255,255,255,0.6);"); descL.setWrapText(true); descL.setMaxWidth(100);
            Label tempL = new Label(day[2] + "/" + day[3]); tempL.setStyle("-fx-font-size:13px;-fx-font-weight:900;-fx-text-fill:white;");
            card.getChildren().addAll(dl, ic, descL, tempL);
            forecastContainer.getChildren().add(card);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // TRADUCTION
    // ══════════════════════════════════════════════════════════════
    private void activerLangue(String lang) {
        styleLangBtn(btnLangFr, lang.equals("fr"));
        styleLangBtn(btnLangEn, lang.equals("en"));
        styleLangBtn(btnLangAr, lang.equals("ar"));
        if (lang.equals("fr")) {
            if (lblDescription != null && originalDescription != null) lblDescription.setText(originalDescription);
            if (lblDestTitle   != null && originalDestTitle   != null) lblDestTitle.setText(originalDestTitle);
            if (lblTranslating != null) lblTranslating.setText("");
            return;
        }
        if (lblTranslating != null) lblTranslating.setText("⟳ Traduction en cours...");
        new Thread(() -> {
            try {
                String tDesc  = translateGoogle(originalDescription != null ? originalDescription : "", lang);
                String tTitle = translateGoogle(originalDestTitle   != null ? originalDestTitle   : "", lang);
                Platform.runLater(() -> {
                    if (lblDescription != null) lblDescription.setText(tDesc);
                    if (lblDestTitle   != null) lblDestTitle.setText(tTitle);
                    if (lblTranslating != null) lblTranslating.setText("✓ Traduit");
                    new Thread(() -> { try { Thread.sleep(3000); } catch (Exception ignored) {} Platform.runLater(() -> { if (lblTranslating != null) lblTranslating.setText(""); }); }).start();
                });
            } catch (Exception ex) { Platform.runLater(() -> { if (lblTranslating != null) lblTranslating.setText("Erreur traduction"); }); }
        }).start();
    }

    private void styleLangBtn(Button btn, boolean actif) {
        if (btn == null) return;
        btn.setStyle(actif
                ? "-fx-background-color:#c8956c;-fx-text-fill:white;-fx-font-size:11px;-fx-font-weight:600;-fx-background-radius:20;-fx-padding:5 14 5 14;-fx-cursor:hand;"
                : "-fx-background-color:#f5f2ec;-fx-border-color:#e8e4dc;-fx-border-radius:20;-fx-background-radius:20;-fx-border-width:1;-fx-text-fill:#7a7670;-fx-font-size:11px;-fx-font-weight:600;-fx-padding:5 14 5 14;-fx-cursor:hand;");
    }

    private String translateGoogle(String text, String targetLang) throws Exception {
        if (text == null || text.isBlank()) return text;
        String trunc   = text.length() > 500 ? text.substring(0, 500) + "..." : text;
        String encoded = java.net.URLEncoder.encode(trunc, "UTF-8");
        String urlStr  = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=fr&tl=" + targetLang + "&dt=t&q=" + encoded;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        conn.setConnectTimeout(10000); conn.setReadTimeout(10000);
        InputStream is = conn.getResponseCode() == 200 ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) { String line; while ((line = br.readLine()) != null) sb.append(line); }
        JSONArray outer = new JSONArray(sb.toString());
        JSONArray segs  = outer.getJSONArray(0);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < segs.length(); i++) {
            Object seg = segs.get(i);
            if (seg instanceof JSONArray) { Object f = ((JSONArray) seg).opt(0); if (f != null && !JSONObject.NULL.equals(f)) result.append(f.toString()); }
        }
        return result.length() > 0 ? result.toString() : trunc;
    }

    // ══════════════════════════════════════════════════════════════
    // VIDÉO
    // ══════════════════════════════════════════════════════════════
    @FXML private void handleWatchVideo() {
        if (currentDestination == null) return;
        String vp = currentDestination.getVideoPath();
        if (vp == null || vp.isBlank()) { showVideoSection(); showErrorState("Aucune vidéo disponible."); return; }
        String resolved = vp;
        if (!vp.startsWith("http") && !new File(vp).isAbsolute()) { URL r = getClass().getResource("/" + vp); if (r != null) resolved = r.toExternalForm(); }
        final String fp = resolved;
        showVideoSection(); showLoadingState(true);
        if (btnWatchVideo != null) { btnWatchVideo.setDisable(true); btnWatchVideo.setText("Chargement..."); }
        Platform.runLater(() -> {
            if (btnWatchVideo != null) { btnWatchVideo.setDisable(false); btnWatchVideo.setText("Regarder la vidéo"); }
            String em = convertToEmbedUrl(fp);
            if (em != null) playWithWebView(em);
            else if (vlcAvailable) playWithVlcj(fp);
            else { try { currentMedia = new Media(fp); playWithJavaFX(currentMedia); } catch (Exception ex) { showErrorState(ex.getMessage()); } }
        });
    }

    private String convertToEmbedUrl(String u) {
        if (u == null) return null;
        if (u.contains("youtube.com/embed/")) return u.contains("?") ? u + "&autoplay=1" : u + "?autoplay=1";
        if (u.contains("youtube.com/watch?v=")) return "https://www.youtube.com/embed/" + u.split("v=")[1].split("&")[0] + "?autoplay=1&rel=0";
        if (u.contains("youtu.be/")) return "https://www.youtube.com/embed/" + u.split("youtu.be/")[1].split("\\?")[0] + "?autoplay=1&rel=0";
        return null;
    }

    private void playWithWebView(String embedUrl) {
        disposeVideo();
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(
                    embedUrl.replace("youtube.com/embed/","youtube.com/watch?v=")
                            .replaceAll("\\?autoplay=1.*","")
                            .replaceAll("&autoplay=1.*","")));
            showVideoSection(); showLoadingState(false); showControls(false);
            showErrorState("La vidéo s'est ouverte dans votre navigateur.");
        } catch (Exception e) { showErrorState(e.getMessage()); }
    }

    private void playWithVlcj(String videoPath) {
        disposeVideo();
        try {
            vlcSwingNode = new SwingNode();
            StackPane w = new StackPane(vlcSwingNode);
            w.prefWidthProperty().bind(videoContainer.widthProperty());
            w.prefHeightProperty().bind(videoContainer.heightProperty());
            videoContainer.getChildren().add(0, w);
            showLoadingState(false); showControls(true);
            javax.swing.SwingUtilities.invokeLater(() -> {
                vlcPlayer = new EmbeddedMediaPlayerComponent();
                vlcSwingNode.setContent(vlcPlayer);
                vlcPlayer.mediaPlayer().events().addMediaPlayerEventListener(new uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter() {
                    @Override public void playing(uk.co.caprica.vlcj.player.base.MediaPlayer mp) { Platform.runLater(() -> { if (btnPlayPause != null) btnPlayPause.setText("Pause"); isPlayed = true; }); }
                    @Override public void paused (uk.co.caprica.vlcj.player.base.MediaPlayer mp) { Platform.runLater(() -> { if (btnPlayPause != null) btnPlayPause.setText("Play"); isPlayed = false; }); }
                    @Override public void stopped(uk.co.caprica.vlcj.player.base.MediaPlayer mp) { Platform.runLater(() -> { if (btnPlayPause != null) btnPlayPause.setText("Play"); isPlayed = false; if (timeSlider != null) timeSlider.setValue(0); }); }
                });
                vlcPlayer.addHierarchyListener(e -> {
                    if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && vlcPlayer.isShowing()) {
                        vlcPlayer.removeHierarchyListener(vlcPlayer.getHierarchyListeners()[0]);
                        vlcPlayer.mediaPlayer().media().play(videoPath);
                    }
                });
            });
            startSliderThread();
        } catch (Exception e) { showErrorState("Erreur VLCJ : " + e.getMessage()); }
    }

    private void playWithJavaFX(Media media) {
        disposeVideo(); isPlayed = false;
        mediaPlayerFx = new MediaPlayer(media);
        mediaViewFx = new MediaView(mediaPlayerFx);
        mediaViewFx.setPreserveRatio(false); mediaViewFx.setFitWidth(900); mediaViewFx.setFitHeight(420);
        videoContainer.getChildren().add(0, mediaViewFx);
        if (timeSlider != null) {
            timeSlider.setOnMousePressed(e -> sliderDragging = true);
            timeSlider.setOnMouseReleased(e -> {
                sliderDragging = false;
                if (mediaPlayerFx.getTotalDuration() != null && !mediaPlayerFx.getTotalDuration().isUnknown())
                    mediaPlayerFx.seek(Duration.seconds(timeSlider.getValue() / 100.0 * mediaPlayerFx.getTotalDuration().toSeconds()));
            });
        }
        mediaPlayerFx.setOnReady(() -> Platform.runLater(() -> { showLoadingState(false); showControls(true); mediaPlayerFx.play(); if (btnPlayPause != null) btnPlayPause.setText("Pause"); isPlayed = true; }));
        mediaPlayerFx.setOnError(() -> Platform.runLater(() -> showErrorState(mediaPlayerFx.getError() != null ? mediaPlayerFx.getError().getMessage() : "Erreur.")));
        if (volumeSlider != null) mediaPlayerFx.volumeProperty().bind(volumeSlider.valueProperty());
    }

    private void startSliderThread() {
        stopSliderThread();
        sliderThread = new Thread(() -> {
            while (vlcPlayer != null && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(500);
                    if (vlcPlayer == null) break;
                    long total = vlcPlayer.mediaPlayer().status().length(), cur = vlcPlayer.mediaPlayer().status().time();
                    if (total > 0 && !sliderDragging) {
                        double pct = (double) cur / total * 100;
                        Platform.runLater(() -> {
                            if (timeSlider != null) timeSlider.setValue(pct);
                            if (lblVideoTime != null) lblVideoTime.setText(formatMs(cur) + " / " + formatMs(total));
                        });
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });
        sliderThread.setDaemon(true); sliderThread.start();
    }

    private void stopSliderThread() { if (sliderThread != null) { sliderThread.interrupt(); sliderThread = null; } }

    @FXML private void handlePlayPause() {
        if (vlcPlayer != null) {
            if (!isPlayed) { vlcPlayer.mediaPlayer().controls().play(); if (btnPlayPause != null) btnPlayPause.setText("Pause"); isPlayed = true; }
            else           { vlcPlayer.mediaPlayer().controls().pause(); if (btnPlayPause != null) btnPlayPause.setText("Play"); isPlayed = false; }
            return;
        }
        if (mediaPlayerFx != null) {
            if (!isPlayed) { mediaPlayerFx.play(); if (btnPlayPause != null) btnPlayPause.setText("Pause"); isPlayed = true; }
            else           { mediaPlayerFx.pause(); if (btnPlayPause != null) btnPlayPause.setText("Play"); isPlayed = false; }
        }
    }

    @FXML private void handleStop() {
        if (vlcPlayer != null) vlcPlayer.mediaPlayer().controls().stop();
        if (mediaPlayerFx != null) mediaPlayerFx.stop();
        if (btnPlayPause != null) btnPlayPause.setText("Play"); isPlayed = false;
        if (timeSlider != null) timeSlider.setValue(0);
        if (lblVideoTime != null) lblVideoTime.setText("0:00 / 0:00");
    }

    @FXML private void handleCloseVideo() {
        disposeVideo(); hideVideoSection();
        if (btnWatchVideo != null) { btnWatchVideo.setText("Regarder la vidéo"); btnWatchVideo.setDisable(false); }
    }

    private void disposeVideo() {
        stopSliderThread(); isPlayed = false;
        if (youtubeWebView != null) { youtubeWebView.getEngine().load("about:blank"); if (videoContainer != null) videoContainer.getChildren().remove(youtubeWebView); youtubeWebView = null; }
        if (vlcPlayer != null) { try { vlcPlayer.mediaPlayer().controls().stop(); vlcPlayer.release(); } catch (Exception ignored) {} vlcPlayer = null; }
        if (videoContainer != null) videoContainer.getChildren().removeIf(n -> n instanceof StackPane && ((StackPane) n).getChildren().stream().anyMatch(c -> c instanceof SwingNode));
        vlcSwingNode = null;
        if (mediaPlayerFx != null) { try { mediaPlayerFx.stop(); mediaPlayerFx.dispose(); } catch (Exception ignored) {} mediaPlayerFx = null; }
        if (mediaViewFx != null && videoContainer != null) { videoContainer.getChildren().remove(mediaViewFx); mediaViewFx = null; }
        currentMedia = null;
        if (timeSlider != null) timeSlider.setValue(0);
        if (lblVideoTime != null) lblVideoTime.setText("0:00 / 0:00");
        if (btnPlayPause != null) btnPlayPause.setText("Play");
    }

    private void showVideoSection()         { if (videoSection != null) { videoSection.setVisible(true); videoSection.setManaged(true); } }
    private void hideVideoSection()         { if (videoSection != null) { videoSection.setVisible(false); videoSection.setManaged(false); } showControls(false); showLoadingState(false); }
    private void showLoadingState(boolean v){ if (videoLoadingPane != null) { videoLoadingPane.setVisible(v); videoLoadingPane.setManaged(v); } if (videoErrorPane != null) { videoErrorPane.setVisible(false); videoErrorPane.setManaged(false); } }
    private void showErrorState(String msg) { showLoadingState(false); showControls(false); if (lblVideoError != null) lblVideoError.setText(msg != null ? msg : "Erreur."); if (videoErrorPane != null) { videoErrorPane.setVisible(true); videoErrorPane.setManaged(true); } }
    private void showControls(boolean v)    { if (videoControls != null) { videoControls.setVisible(v); videoControls.setManaged(v); } }
    private String formatMs(long ms)        { long s = ms / 1000; return String.format("%d:%02d", s / 60, s % 60); }

    // ══════════════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════════════
    @FXML private void goBackToList() {
        disposeVideo();
        try {
            hebergement.controllers.ClientLayoutController client = hebergement.controllers.ClientLayoutController.getInstance();
            if (client != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientDestinationListView.fxml"));
                Parent root = loader.load();
                client.loadPageWithRoot(root);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientDestinationListView.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) btnBackToList.getScene().getWindow();
                stage.setScene(new Scene(root, 1200, 800));
                stage.show();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════
    // JS BRIDGE
    // ══════════════════════════════════════════════════════════════
    public static class JSBridge {
        private final Label lbl;
        public JSBridge(Label l) { this.lbl = l; }
        public void sendRouteInfo(String distance, String duration, String transportType) {
            Platform.runLater(() -> { if (lbl != null) lbl.setText(transportType + " : " + distance + " km — " + duration + " min"); });
        }
    }

    // ══════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ══════════════════════════════════════════════════════════════
    private String fetchUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET"); conn.setConnectTimeout(10000); conn.setReadTimeout(10000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        InputStream is = conn.getResponseCode() == 200 ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) { String line; while ((line = br.readLine()) != null) sb.append(line); }
        return sb.toString();
    }

    private String escapeJson(String t) { return t.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t"); }
    private String capitalize(String s) { if (s == null || s.isEmpty()) return s; return Character.toUpperCase(s.charAt(0)) + s.substring(1); }
    public Destination getCurrentDestination() { return currentDestination; }
}