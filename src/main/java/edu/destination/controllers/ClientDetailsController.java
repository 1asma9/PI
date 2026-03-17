package edu.destination.controllers;

import edu.destination.entities.Destination;
import edu.destination.entities.DestinationImage;
import edu.destination.entities.Transport;
import edu.destination.services.ImageService;
import edu.destination.services.TransportService;
import edu.destination.tools.SceneUtil;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

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

    private static final String WEATHER_API_KEY = "60b4f367276104cdace06362a000085f";

    // ==============================
    // FXML — champs existants
    // ==============================
    @FXML private ImageView imageView;
    @FXML private Label lblName, lblCountry, lblDescription, lblSeason, lblVisits, lblTotal;
    @FXML private Label lblDepart, lblArrival;
    @FXML private VBox transportContainer;
    @FXML private Button btnPrev, btnNext, btnBackToList;
    @FXML private HBox imagesContainer;
    @FXML private BorderPane mapPane;
    @FXML private Label lblDistance, lblDuration, lblTransport;
    @FXML private VBox weatherBox;
    @FXML private Label lblWeatherCity, lblWeatherTemp, lblWeatherDesc;
    @FXML private Label lblWeatherFeels, lblWeatherHumidity, lblWeatherWind;
    @FXML private Label lblWeatherIndex, lblWeatherPeriod;
    @FXML private HBox forecastContainer;

    // ==============================
    // FXML — section vidéo intégrée
    // ==============================
    @FXML private Button    btnWatchVideo;
    @FXML private VBox      videoSection;
    @FXML private Label     lblVideoTitle;
    @FXML private Button    btnCloseVideo;
    @FXML private StackPane videoContainer;
    @FXML private VBox      videoLoadingPane;
    @FXML private Label     lblVideoLoading;
    @FXML private VBox      videoErrorPane;
    @FXML private Label     lblVideoError;
    @FXML private HBox      videoControls;
    @FXML private Button    btnPlayPause;
    @FXML private Button    btnStop;
    @FXML private Slider    timeSlider;
    @FXML private Label     lblVideoTime;
    @FXML private Slider    volumeSlider;

    // ==============================
    // Services & état
    // ==============================
    private final ImageService     imageService     = new ImageService();
    private final TransportService transportService = new TransportService();

    private List<DestinationImage> images;
    private int currentIndex = 0;

    private WebView   mapView;
    private WebEngine webEngine;
    private JSBridge  bridge;

    private Destination currentDestination;
    private Transport   currentTransport;

    // ── Lecteur VLCJ ──
    private EmbeddedMediaPlayerComponent vlcPlayer;
    private SwingNode                    vlcSwingNode;
    private boolean                      vlcAvailable   = false;
    private boolean                      sliderDragging = false;
    private Thread                       sliderThread;

    // ── Lecteur JavaFX (fallback) ──
    private MediaPlayer mediaPlayerFx;
    private MediaView   mediaViewFx;
    private Media       currentMedia;

    // ── État Play/Pause (comme dans le tutoriel) ──
    private boolean isPlayed = false;

    // ── WebView YouTube ──
    private WebView youtubeWebView;

    // ==============================
    // INITIALIZE
    // ==============================
    @FXML
    public void initialize() {
        btnNext.setOnAction(e -> nextImage());
        btnPrev.setOnAction(e -> prevImage());
        btnBackToList.setOnAction(e -> goBackToList());

        // Slider : min=0, max=100 (pourcentage)
        if (timeSlider != null) {
            timeSlider.setMin(0);
            timeSlider.setMax(100);
            timeSlider.setValue(0);
        }

        if (volumeSlider != null) volumeSlider.setValue(1.0);

        try {
            System.setProperty("jna.library.path", "C:\\Program Files\\VideoLAN\\VLC");
            vlcAvailable = new NativeDiscovery().discover();
            System.out.println("✅ VLC détecté : " + vlcAvailable);
        } catch (Exception e) {
            vlcAvailable = false;
            System.out.println("❌ VLC non disponible : " + e.getMessage());
        }

        initMap();
    }

    // ==============================
    // SET DESTINATION
    // ==============================
    public void setDestination(Destination d) {
        if (d == null) return;
        currentDestination = d;

        lblDepart.setText(d.getDateDepart()   != null ? d.getDateDepart().toString()  : "-");
        lblArrival.setText(d.getDateArrivee() != null ? d.getDateArrivee().toString() : "-");
        lblName.setText(d.getNom());
        lblCountry.setText(d.getPays());
        lblDescription.setText(d.getDescription());
        lblSeason.setText(d.getMeilleureSaison());
        lblVisits.setText(String.valueOf(d.getNbVisites()));
        lblTotal.setText(String.valueOf(d.getPrix()));

        disposeVideo();
        hideVideoSection();

        lblVideoTitle.setText("🎬 " + d.getNom() + " — Vidéo promotionnelle");
        btnWatchVideo.setText("▶  Regarder la vidéo promotionnelle");
        btnWatchVideo.setDisable(false);

        boolean hasVideo = d.getVideoPath() != null && !d.getVideoPath().isBlank();
        btnWatchVideo.setVisible(hasVideo);
        btnWatchVideo.setManaged(hasVideo);

        loadImages(d);
        loadTransports(d);

        if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED && currentTransport != null)
            addRouteToMap(currentDestination, currentTransport);

        loadWeather(d);
    }

    // ==============================
    // VIDÉO — handleWatchVideo
    // ==============================
    @FXML
    private void handleWatchVideo() {
        String videoPath = currentDestination.getVideoPath();

        if (videoPath == null || videoPath.isBlank()) {
            showVideoSection();
            showErrorState("Aucune vidéo disponible pour cette destination.");
            return;
        }

        // Résoudre le chemin depuis les resources
        String resolvedPath = videoPath;
        if (!videoPath.startsWith("http") && !new File(videoPath).isAbsolute()) {
            URL resource = getClass().getResource("/" + videoPath);
            if (resource != null) {
                resolvedPath = resource.toExternalForm();
            }
        }

        final String finalPath = resolvedPath;
        showVideoSection();
        showLoadingState(true);
        btnWatchVideo.setDisable(true);
        btnWatchVideo.setText("⏳ Chargement...");

        Platform.runLater(() -> {
            btnWatchVideo.setDisable(false);
            btnWatchVideo.setText("▶  Regarder la vidéo promotionnelle");

            String embedUrl = convertToEmbedUrl(finalPath);

            if (embedUrl != null) {
                playWithWebView(embedUrl);
            } else if (vlcAvailable) {
                playWithVlcj(finalPath);
            } else {
                try {
                    currentMedia = new Media(finalPath);
                    playWithJavaFX(currentMedia);
                } catch (Exception ex) {
                    showErrorState("Impossible de charger la vidéo :\n" + ex.getMessage());
                }
            }
        });
    }

    // ==============================
    // CONVERTISSEUR URL YOUTUBE
    // ==============================
    private String convertToEmbedUrl(String url) {
        if (url == null) return null;

        if (url.contains("youtube.com/embed/")) {
            return url.contains("?") ? url + "&autoplay=1" : url + "?autoplay=1";
        }
        if (url.contains("youtube.com/watch?v=")) {
            String videoId = url.split("v=")[1].split("&")[0];
            return "https://www.youtube.com/embed/" + videoId + "?autoplay=1&rel=0";
        }
        if (url.contains("youtu.be/")) {
            String videoId = url.split("youtu.be/")[1].split("\\?")[0];
            return "https://www.youtube.com/embed/" + videoId + "?autoplay=1&rel=0";
        }
        return null;
    }

    // ==============================
    // LECTEUR YOUTUBE — ouvre dans le navigateur
    // ==============================
    private void playWithWebView(String embedUrl) {
        disposeVideo();
        try {
            String watchUrl = embedUrl
                    .replace("youtube.com/embed/", "youtube.com/watch?v=")
                    .replaceAll("\\?autoplay=1.*", "")
                    .replaceAll("&autoplay=1.*", "");

            java.awt.Desktop.getDesktop().browse(new java.net.URI(watchUrl));

            showVideoSection();
            showLoadingState(false);
            showControls(false);
            showErrorState("▶  La vidéo s'est ouverte dans votre navigateur.");

        } catch (Exception e) {
            showErrorState("Impossible d'ouvrir la vidéo :\n" + e.getMessage());
        }
    }

    // ==============================
    // LECTEUR VLCJ
    // ==============================
    private void playWithVlcj(String videoPath) {
        disposeVideo();
        try {
            System.out.println("🎬 Tentative lecture VLCJ : " + videoPath);

            vlcSwingNode = new SwingNode();
            StackPane wrapper = new StackPane(vlcSwingNode);
            wrapper.prefWidthProperty().bind(videoContainer.widthProperty());
            wrapper.prefHeightProperty().bind(videoContainer.heightProperty());
            wrapper.maxWidthProperty().bind(videoContainer.widthProperty());
            wrapper.maxHeightProperty().bind(videoContainer.heightProperty());
            videoContainer.getChildren().add(0, wrapper);

            showLoadingState(false);
            showControls(true);

            javax.swing.SwingUtilities.invokeLater(() -> {
                vlcPlayer = new EmbeddedMediaPlayerComponent();
                vlcSwingNode.setContent(vlcPlayer);

                final EmbeddedMediaPlayerComponent player = vlcPlayer;

                player.mediaPlayer().events().addMediaPlayerEventListener(
                        new uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter() {
                            @Override
                            public void error(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {
                                System.out.println("❌ ERREUR VLCJ lecture");
                                Platform.runLater(() -> showErrorState("Erreur VLCJ lors de la lecture."));
                            }
                            @Override
                            public void playing(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {
                                System.out.println("✅ VLCJ en cours de lecture !");
                                Platform.runLater(() -> {
                                    btnPlayPause.setText("⏸  Pause");
                                    isPlayed = true;
                                });
                            }
                            @Override
                            public void paused(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {
                                Platform.runLater(() -> {
                                    btnPlayPause.setText("▶  Play");
                                    isPlayed = false;
                                });
                            }
                            @Override
                            public void stopped(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {
                                Platform.runLater(() -> {
                                    btnPlayPause.setText("▶  Play");
                                    isPlayed = false;
                                    timeSlider.setValue(0);
                                    lblVideoTime.setText("0:00 / 0:00");
                                });
                            }
                            @Override
                            public void finished(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {
                                Platform.runLater(() -> {
                                    btnPlayPause.setText("▶  Revoir");
                                    isPlayed = false;
                                    timeSlider.setValue(100);
                                });
                            }
                        }
                );

                player.addHierarchyListener(e -> {
                    if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                            && player.isShowing()) {
                        player.removeHierarchyListener(player.getHierarchyListeners()[0]);
                        boolean result = player.mediaPlayer().media().play(videoPath);
                        System.out.println("▶ play() result : " + result);
                    }
                });
            });

            // Volume
            volumeSlider.valueProperty().addListener((obs, o, n) -> {
                if (vlcPlayer != null)
                    vlcPlayer.mediaPlayer().audio().setVolume((int)(n.doubleValue() * 100));
            });

            // Seek au relâchement du slider
            timeSlider.setOnMousePressed(e -> sliderDragging = true);
            timeSlider.setOnMouseReleased(e -> {
                sliderDragging = false;
                if (vlcPlayer != null) {
                    long total = vlcPlayer.mediaPlayer().status().length();
                    vlcPlayer.mediaPlayer().controls()
                            .setTime((long)(timeSlider.getValue() / 100.0 * total));
                }
            });

            startSliderThread();

        } catch (Exception e) {
            System.out.println("❌ Exception VLCJ : " + e.getMessage());
            showErrorState("Erreur VLCJ : " + e.getMessage());
        }
    }

    // ── Thread slider VLCJ ──
    private void startSliderThread() {
        stopSliderThread();
        sliderThread = new Thread(() -> {
            while (vlcPlayer != null && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(500);
                    if (vlcPlayer == null) break;
                    long total   = vlcPlayer.mediaPlayer().status().length();
                    long current = vlcPlayer.mediaPlayer().status().time();
                    if (total > 0 && !sliderDragging) {
                        double pct = (double) current / total * 100.0;
                        Platform.runLater(() -> {
                            timeSlider.setValue(pct);
                            lblVideoTime.setText(formatMs(current) + " / " + formatMs(total));
                        });
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        sliderThread.setDaemon(true);
        sliderThread.start();
    }

    private void stopSliderThread() {
        if (sliderThread != null) {
            sliderThread.interrupt();
            sliderThread = null;
        }
    }

    // ==============================
    // LECTEUR JAVAFX (fallback) — comme dans le tutoriel
    // ==============================
    private void playWithJavaFX(Media media) {
        disposeVideo();
        isPlayed = false;

        try {
            mediaPlayerFx = new MediaPlayer(media);
            mediaViewFx   = new MediaView(mediaPlayerFx);

            // ✅ Taille fixe et clip pour rester dans le container
            mediaViewFx.setPreserveRatio(false);
            mediaViewFx.setFitWidth(900);
            mediaViewFx.setFitHeight(430);
            mediaViewFx.setTranslateY(0);
            mediaViewFx.setTranslateX(0);

            // ✅ Clip pour empêcher le débordement
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(900, 430);
            mediaViewFx.setClip(clip);

            StackPane.setAlignment(mediaViewFx, javafx.geometry.Pos.CENTER);

            videoContainer.setClip(new javafx.scene.shape.Rectangle(
                    videoContainer.getPrefWidth(),
                    videoContainer.getPrefHeight()
            ));

            videoContainer.getChildren().add(0, mediaViewFx);

            // ── Slider seek ──
            timeSlider.setOnMousePressed(e -> sliderDragging = true);
            timeSlider.setOnMouseReleased(e -> {
                sliderDragging = false;
                if (mediaPlayerFx != null && mediaPlayerFx.getTotalDuration() != null
                        && !mediaPlayerFx.getTotalDuration().isUnknown()) {
                    double totalSec = mediaPlayerFx.getTotalDuration().toSeconds();
                    mediaPlayerFx.seek(Duration.seconds(timeSlider.getValue() / 100.0 * totalSec));
                }
            });

            // ── Listener temps courant → slider + label ──
            mediaPlayerFx.currentTimeProperty().addListener((obs, oldValue, newValue) -> {
                if (!sliderDragging && mediaPlayerFx.getTotalDuration() != null
                        && !mediaPlayerFx.getTotalDuration().isUnknown()) {
                    double totalSec   = mediaPlayerFx.getTotalDuration().toSeconds();
                    double currentSec = newValue.toSeconds();
                    double pct = totalSec > 0 ? (currentSec / totalSec) * 100.0 : 0;
                    timeSlider.setValue(pct);
                    lblVideoTime.setText(formatDuration(newValue) + " / " + formatDuration(mediaPlayerFx.getTotalDuration()));
                }
            });

            // ── onReady → initialiser et lancer ──
            mediaPlayerFx.setOnReady(() -> Platform.runLater(() -> {
                timeSlider.setMax(100);
                timeSlider.setValue(0);
                lblVideoTime.setText("0:00 / " + formatDuration(mediaPlayerFx.getTotalDuration()));
                showLoadingState(false);
                showControls(true);
                mediaPlayerFx.play();
                btnPlayPause.setText("⏸  Pause");
                isPlayed = true;
            }));

            // ── Fin de vidéo ──
            mediaPlayerFx.setOnEndOfMedia(() -> Platform.runLater(() -> {
                btnPlayPause.setText("▶  Revoir");
                isPlayed = false;
                timeSlider.setValue(100);
            }));

            // ── Erreur ──
            mediaPlayerFx.setOnError(() -> Platform.runLater(() ->
                    showErrorState(mediaPlayerFx.getError() != null
                            ? mediaPlayerFx.getError().getMessage()
                            : "Erreur de lecture.")));

            // ── Volume ──
            volumeSlider.setValue(1.0);
            mediaPlayerFx.volumeProperty().bind(volumeSlider.valueProperty());

        } catch (Exception e) {
            showErrorState("Erreur lecteur JavaFX :\n" + e.getMessage());
        }
    }
    // ==============================
    // CONTRÔLES — Play/Pause (comme dans le tutoriel avec isPlayed)
    // ==============================
    @FXML
    private void handlePlayPause() {
        // YouTube WebView : pas de contrôles natifs
        if (youtubeWebView != null) return;

        // ── VLCJ ──
        if (vlcPlayer != null) {
            if (!isPlayed) {
                // Était en pause → on joue
                vlcPlayer.mediaPlayer().controls().play();
                btnPlayPause.setText("⏸  Pause");
                isPlayed = true;
            } else {
                // Était en lecture → on met en pause
                vlcPlayer.mediaPlayer().controls().pause();
                btnPlayPause.setText("▶  Play");
                isPlayed = false;
            }
            return;
        }

        // ── JavaFX MediaPlayer (logique identique au tutoriel) ──
        if (mediaPlayerFx != null) {
            if (!isPlayed) {
                // Bouton affiche "Pause" → on joue
                btnPlayPause.setText("⏸  Pause");
                mediaPlayerFx.play();
                isPlayed = true;
            } else {
                // Bouton affiche "Play" → on met en pause
                btnPlayPause.setText("▶  Play");
                mediaPlayerFx.pause();
                isPlayed = false;
            }
        }
    }

    // ==============================
    // CONTRÔLES — Stop (comme dans le tutoriel)
    // ==============================
    @FXML
    private void handleStop() {
        // YouTube WebView : pas de contrôles natifs
        if (youtubeWebView != null) return;

        // ── VLCJ ──
        if (vlcPlayer != null) {
            vlcPlayer.mediaPlayer().controls().stop();
        }

        // ── JavaFX MediaPlayer ──
        if (mediaPlayerFx != null) {
            mediaPlayerFx.stop();
        }

        // Réinitialiser l'état (comme dans le tutoriel)
        btnPlayPause.setText("▶  Play");
        isPlayed = false;
        timeSlider.setValue(0);
        lblVideoTime.setText("0:00 / 0:00");
    }

    // ==============================
    // CONTRÔLES — Fermer la vidéo
    // ==============================
    @FXML
    private void handleCloseVideo() {
        disposeVideo();
        hideVideoSection();
        btnWatchVideo.setText("▶  Regarder la vidéo promotionnelle");
        btnWatchVideo.setDisable(false);
    }

    // ==============================
    // DISPOSE — Libérer les ressources
    // ==============================
    private void disposeVideo() {
        stopSliderThread();
        isPlayed = false;

        // Nettoyer le WebView YouTube
        if (youtubeWebView != null) {
            youtubeWebView.getEngine().load("about:blank");
            videoContainer.getChildren().remove(youtubeWebView);
            youtubeWebView = null;
        }

        // Nettoyer VLCJ
        if (vlcPlayer != null) {
            try {
                vlcPlayer.mediaPlayer().controls().stop();
                vlcPlayer.release();
            } catch (Exception ignored) {}
            vlcPlayer = null;
        }
        videoContainer.getChildren().removeIf(n ->
                n instanceof StackPane &&
                        ((StackPane) n).getChildren().stream()
                                .anyMatch(c -> c instanceof SwingNode));
        vlcSwingNode = null;

        // Nettoyer JavaFX MediaPlayer
        if (mediaPlayerFx != null) {
            try {
                mediaPlayerFx.stop();
                mediaPlayerFx.dispose();
            } catch (Exception ignored) {}
            mediaPlayerFx = null;
        }
        if (mediaViewFx != null) {
            videoContainer.getChildren().remove(mediaViewFx);
            mediaViewFx = null;
        }

        currentMedia = null;

        // Réinitialiser slider et label
        if (timeSlider   != null) timeSlider.setValue(0);
        if (lblVideoTime != null) lblVideoTime.setText("0:00 / 0:00");
        if (btnPlayPause != null) btnPlayPause.setText("▶  Play");
    }

    // ==============================
    // HELPERS VISIBILITÉ
    // ==============================
    private void showVideoSection() {
        videoSection.setVisible(true);
        videoSection.setManaged(true);
    }

    private void hideVideoSection() {
        videoSection.setVisible(false);
        videoSection.setManaged(false);
        showControls(false);
        showLoadingState(false);
    }

    private void showLoadingState(boolean loading) {
        videoLoadingPane.setVisible(loading);
        videoLoadingPane.setManaged(loading);
        videoErrorPane.setVisible(false);
        videoErrorPane.setManaged(false);
    }

    private void showErrorState(String msg) {
        showLoadingState(false);
        showControls(false);
        lblVideoError.setText(msg != null ? msg : "Erreur inconnue.");
        videoErrorPane.setVisible(true);
        videoErrorPane.setManaged(true);
    }

    private void showControls(boolean show) {
        videoControls.setVisible(show);
        videoControls.setManaged(show);
    }

    // ==============================
    // FORMATEURS DE DURÉE
    // ==============================
    private String formatDuration(Duration d) {
        if (d == null || d.isUnknown() || d.isIndefinite()) return "0:00";
        int totalSec = (int) d.toSeconds();
        return String.format("%d:%02d", totalSec / 60, totalSec % 60);
    }

    private String formatMs(long ms) {
        long totalSec = ms / 1000;
        return String.format("%d:%02d", totalSec / 60, totalSec % 60);
    }

    // ==============================
    // MÉTÉO
    // ==============================
    private void loadWeather(Destination d) {
        double lat = d.getLatitude();
        double lon = d.getLongitude();
        LocalDate dep = d.getDateDepart();
        LocalDate arr = d.getDateArrivee();
        if (lblWeatherPeriod != null && dep != null && arr != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            lblWeatherPeriod.setText(dep.format(fmt) + "  →  " + arr.format(fmt));
        }
        new Thread(() -> {
            try {
                String currentUrl = "https://api.openweathermap.org/data/2.5/weather"
                        + "?lat=" + lat + "&lon=" + lon
                        + "&appid=" + WEATHER_API_KEY + "&units=metric&lang=fr";
                JSONObject current = new JSONObject(fetchUrl(currentUrl));
                double temp      = current.getJSONObject("main").getDouble("temp");
                double feelsLike = current.getJSONObject("main").getDouble("feels_like");
                int    humidity  = current.getJSONObject("main").getInt("humidity");
                double windMs    = current.getJSONObject("wind").getDouble("speed");
                String desc      = current.getJSONArray("weather").getJSONObject(0).getString("description");
                String cityName  = current.getString("name");
                String forecastUrl = "https://api.openweathermap.org/data/2.5/forecast"
                        + "?lat=" + lat + "&lon=" + lon
                        + "&appid=" + WEATHER_API_KEY + "&units=metric&lang=fr&cnt=40";
                JSONObject forecastJson = new JSONObject(fetchUrl(forecastUrl));
                List<String[]> forecastDays = parseForecast(forecastJson);
                int index = computeTravelIndex(temp, humidity, windMs, desc);
                String indexLabel = getTravelIndexLabel(index);
                Platform.runLater(() -> {
                    lblWeatherCity.setText(cityName);
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
                    if (lblWeatherCity != null) lblWeatherCity.setText("⚠️ Météo indisponible");
                });
            }
        }).start();
    }

    private int computeTravelIndex(double temp, int humidity, double windMs, String desc) {
        int score = 10;
        if (temp < 0 || temp > 40)       score -= 4;
        else if (temp < 10 || temp > 35) score -= 2;
        else if (temp < 15 || temp > 30) score -= 1;
        if (humidity > 85)      score -= 2;
        else if (humidity > 70) score -= 1;
        double windKmh = windMs * 3.6;
        if (windKmh > 54)      score -= 2;
        else if (windKmh > 36) score -= 1;
        String dl = desc.toLowerCase();
        if (dl.contains("orage") || dl.contains("neige") || dl.contains("tempête")) score -= 3;
        else if (dl.contains("pluie") || dl.contains("brouillard"))                  score -= 2;
        else if (dl.contains("nuageux"))                                              score -= 1;
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
        String color = index >= 8 ? "#2ecc71" : index >= 6 ? "#74b9ff" : index >= 4 ? "#fdcb6e" : "#ff7675";
        lblWeatherIndex.setStyle("-fx-text-fill:" + color + ";-fx-font-weight:900;-fx-font-size:13px;");
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
        String[] gradients = {
                "linear-gradient(to bottom, #2c4a6e, #1a2e45)",
                "linear-gradient(to bottom, #3a6b3a, #1e3d1e)",
                "linear-gradient(to bottom, #6b4a1e, #3d2a0e)",
                "linear-gradient(to bottom, #4a3a6b, #2a1e3d)",
                "linear-gradient(to bottom, #6b3a3a, #3d1e1e)"
        };
        for (int i = 0; i < days.size(); i++) {
            String[] day = days.get(i);
            String gradient = gradients[i % gradients.length];
            VBox card = new VBox(8);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(18, 20, 18, 20));
            card.setMinWidth(130); card.setMaxWidth(140);
            card.setStyle("-fx-background-color:" + gradient + ";"
                    + "-fx-background-radius:12;"
                    + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),10,0,0,4);");
            Label dateL = new Label(day[0]);
            dateL.setStyle("-fx-font-weight:700;-fx-font-size:11px;-fx-text-fill:rgba(255,255,255,0.75);");
            ImageView icon = new ImageView();
            try { icon.setImage(new Image("https://openweathermap.org/img/wn/" + day[4] + "@2x.png", 50, 50, true, true)); } catch (Exception ignored) {}
            icon.setFitWidth(50); icon.setFitHeight(50);
            Label descL = new Label(day[1]);
            descL.setStyle("-fx-font-size:11px;-fx-text-fill:rgba(255,255,255,0.6);");
            descL.setWrapText(true); descL.setMaxWidth(120);
            Label tempL = new Label(day[2] + " / " + day[3]);
            tempL.setStyle("-fx-font-size:14px;-fx-font-weight:900;-fx-text-fill:white;");
            card.getChildren().addAll(dateL, icon, descL, tempL);
            forecastContainer.getChildren().add(card);
        }
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
                iv.setFitWidth(140); iv.setFitHeight(90); iv.setPreserveRatio(true);
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
        if (images == null || images.isEmpty() || index < 0 || index >= images.size()) return;
        imageView.setImage(new Image(new File(images.get(index).getUrlImage()).toURI().toString()));
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
            for (Transport t : transports) transportContainer.getChildren().add(createTransportLine(t));
        }
        lblTransport.setText(currentTransport != null ? currentTransport.getTypeTransport() : "—");
        if (currentTransport != null) addRouteToMap(currentDestination, currentTransport);
    }

    private HBox createTransportLine(Transport t) {
        Label icon = new Label(getTransportIcon(t.getTypeTransport()));
        icon.setStyle("-fx-font-size:16px;");
        Label type = new Label(t.getTypeTransport());
        type.setStyle("-fx-text-fill:#3a4a6b;-fx-font-size:13px;-fx-font-weight:600;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox box = new HBox(12, icon, type, spacer);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color:#f8faff;-fx-background-radius:10;"
                + "-fx-border-color:#e8edf5;-fx-border-radius:10;-fx-border-width:1;-fx-padding:12 16 12 16;");
        return box;
    }

    private String getTransportIcon(String type) {
        if (type == null) return "🚗";
        String l = type.toLowerCase();
        if (l.contains("avion") || l.contains("vol"))    return "✈️";
        if (l.contains("train"))                          return "🚆";
        if (l.contains("bus") || l.contains("car"))      return "🚌";
        if (l.contains("bateau") || l.contains("ferry")) return "🚢";
        return "🚗";
    }

    // ==============================
    // CARTE
    // ==============================
    private void initMap() {
        mapView = new WebView();
        webEngine = mapView.getEngine();

        webEngine.setJavaScriptEnabled(true);
        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        // logs erreurs chargement
        webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldEx, ex) -> {
            if (ex != null) {
                System.out.println("❌ WebView load exception:");
                ex.printStackTrace();
            }
        });

        URL url = getClass().getResource("/map.html");
        if (url == null) {
            System.out.println("❌ map.html introuvable dans resources !");
            return;
        }

        System.out.println("✅ map.html = " + url);
        webEngine.load(url.toExternalForm());

        bridge = new JSBridge(lblDistance, lblDuration, lblTransport);

        webEngine.getLoadWorker().stateProperty().addListener((obs, o, n) -> {
            if (n == Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaConnector", bridge);

                    Object exists = webEngine.executeScript("typeof setDestination === 'function'");
                    System.out.println("✅ setDestination exists? " + exists);

                    if (currentDestination != null && currentTransport != null) {
                        addRouteToMap(currentDestination, currentTransport);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mapPane.setCenter(mapView);

        // assure une taille
        mapView.prefWidthProperty().bind(mapPane.widthProperty());
        mapView.prefHeightProperty().bind(mapPane.heightProperty());
    }

    private void addRouteToMap(Destination d, Transport t) {
        Platform.runLater(() -> {
            try {
                Object exists = webEngine.executeScript("typeof setDestination === 'function'");
                if ("true".equals(String.valueOf(exists))) {
                    webEngine.executeScript("setDestination(" + d.getLatitude() + "," + d.getLongitude()
                            + ",'" + t.getTypeTransport() + "')");
                } else {
                    Thread th = new Thread(() -> {
                        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                        Platform.runLater(() -> addRouteToMap(d, t));
                    });
                    th.setDaemon(true);
                    th.start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    // ==============================
    // NAVIGATION
    // ==============================
    @FXML
    private void goBackToList() {
        disposeVideo();
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

        public JSBridge(Label d, Label du, Label tr) {
            lblDistance  = d;
            lblDuration  = du;
            lblTransport = tr;
        }

        public void sendRouteInfo(String distance, String duration, String transportType) {
            Platform.runLater(() -> {
                lblDistance.setText(distance + " km");
                lblDuration.setText(duration + " min");
                lblTransport.setText(transportType);
            });
        }
    }

    public Destination getCurrentDestination() {
        return currentDestination;
    }

    // ==============================
    // UTILITAIRES
    // ==============================
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
}