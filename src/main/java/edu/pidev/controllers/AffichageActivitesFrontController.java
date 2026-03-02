package edu.pidev.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pidev.entities.Activite;
import edu.pidev.services.ActiviteService;
import edu.pidev.tools.CurrencyService;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class AffichageActivitesFrontController {

    // ===== UI (cards + scroll + hero) =====
    @FXML private FlowPane cardsContainer;
    @FXML private ScrollPane scroll;

    @FXML private ImageView bannerImage;
    @FXML private StackPane hero;

    private final ActiviteService service = new ActiviteService();

    // ===== Search fields =====
    @FXML private TextField tfLieu;
    @FXML private TextField tfMinPrix;
    @FXML private TextField tfMaxPrix;

    // ✅ Currency selector
    @FXML private ComboBox<String> currencyBox;

    private String selectedCurrency = "TND";
    private double currentRate = 1.0; // TND -> selectedCurrency
    private final CurrencyService currencyService = new CurrencyService();

    // ===== Chat UI =====
    @FXML private VBox chatBubble;
    @FXML private Label chatText;
    @FXML private VBox chatMessages;
    @FXML private TextField chatInput;
    @FXML private Button btnMascot;

    private boolean chatOpen = false;

    // ===== API (PHP endpoint) =====
    private static final String CHAT_API_URL = "http://localhost/pidev_api/chat.php";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {

        // banner
        if (bannerImage != null) {
            var url = getClass().getResource("/images/banner.jpg");
            if (url == null) {
                System.out.println("❌ Banner not found: /images/banner.jpg");
            } else {
                bannerImage.setImage(new Image(url.toExternalForm(), false));
            }
            bannerImage.setPreserveRatio(false);
            bannerImage.setSmooth(true);
        }

        // mascot image inside button
        if (btnMascot != null) {
            var mu = getClass().getResource("/images/mascot.png");
            if (mu != null) {
                ImageView iv = new ImageView(new Image(mu.toExternalForm(), false));
                iv.setFitWidth(52);
                iv.setFitHeight(52);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);

                btnMascot.setGraphic(iv);
                btnMascot.setText("");
            } else {
                System.out.println("⚠️ Mascot not found: /images/mascot.png");
            }
        }

        // hide chat by default
        if (chatBubble != null) {
            chatBubble.setVisible(false);
            chatBubble.setManaged(false);
        }

        if (chatText != null) {
            chatText.setText("Salut 👋 Moi c’est NAVI. Décris ce que tu veux (lieu, budget, style) et je recommande 😄");
        }

        // scroll behavior
        if (scroll != null) {
            scroll.setFitToWidth(true);
            scroll.setPannable(true);
            scroll.setVvalue(0);
        }

        final double SIDE_PADDING = 80;

        if (bannerImage != null && scroll != null) {
            bannerImage.fitWidthProperty().bind(scroll.widthProperty().subtract(SIDE_PADDING));
            if (hero != null) bannerImage.fitHeightProperty().bind(hero.heightProperty());
        }

        if (cardsContainer != null && scroll != null) {
            cardsContainer.prefWidthProperty().bind(scroll.widthProperty().subtract(SIDE_PADDING));
        }

        // Currency default + listener
        if (currencyBox != null) {
            if (currencyBox.getValue() == null) currencyBox.setValue("TND");
            selectedCurrency = currencyBox.getValue();
            currentRate = 1.0;

            currencyBox.setOnAction(e -> {
                selectedCurrency = currencyBox.getValue();
                loadRateAndRefresh();
            });
        }

        refresh();
    }

    // ===================== CURRENCY =====================
    private void loadRateAndRefresh() {
        if (currencyBox == null) return;

        if ("TND".equalsIgnoreCase(selectedCurrency)) {
            currentRate = 1.0;
            refresh();
            return;
        }

        Task<Double> task = new Task<>() {
            @Override
            protected Double call() throws Exception {
                return currencyService.getRate("TND", selectedCurrency);
            }
        };

        task.setOnSucceeded(ev -> {
            currentRate = task.getValue();
            refresh();
        });

        task.setOnFailed(ev -> {
            System.out.println("❌ Currency rate error: " + task.getException());
            currentRate = 1.0;
            selectedCurrency = "TND";
            currencyBox.setValue("TND");
            refresh();
        });

        new Thread(task).start();
    }

    private String currencySymbol(String cur) {
        if (cur == null) return "DT";
        return switch (cur.toUpperCase()) {
            case "EUR" -> "€";
            case "USD" -> "$";
            case "GBP" -> "£";
            default -> "DT";
        };
    }

    private double displayPrice(double prixTND) {
        return prixTND * currentRate;
    }

    private double toTND(double amountInSelectedCurrency) {
        if (currentRate == 0) return amountInSelectedCurrency;
        return amountInSelectedCurrency / currentRate;
    }

    // ===================== SEARCH =====================
    @FXML
    private void onClearLieu() { if (tfLieu != null) tfLieu.clear(); }

    @FXML
    private void onResetRecherche() {
        if (tfLieu != null) tfLieu.clear();
        if (tfMinPrix != null) tfMinPrix.clear();
        if (tfMaxPrix != null) tfMaxPrix.clear();
        refresh();
    }

    @FXML
    private void onRechercher() {
        String lieu = (tfLieu == null || tfLieu.getText() == null) ? "" : tfLieu.getText().trim();
        String minText = (tfMinPrix == null || tfMinPrix.getText() == null) ? "" : tfMinPrix.getText().trim();
        String maxText = (tfMaxPrix == null || tfMaxPrix.getText() == null) ? "" : tfMaxPrix.getText().trim();

        Double min = parseDoubleOrNull(minText);
        Double max = parseDoubleOrNull(maxText);

        if (!minText.isEmpty() && min == null) { showWarn("Prix min doit être un nombre"); return; }
        if (!maxText.isEmpty() && max == null) { showWarn("Prix max doit être un nombre"); return; }

        if (min != null && min < 0) { showWarn("Prix min invalide"); return; }
        if (max != null && max < 0) { showWarn("Prix max invalide"); return; }
        if (min != null && max != null && min > max) { showWarn("Min > Max"); return; }

        Double minTND = (min == null) ? null : toTND(min);
        Double maxTND = (max == null) ? null : toTND(max);

        List<Activite> results = service.search(lieu, minTND, maxTND);
        afficherListe(results);
        if (scroll != null) scroll.setVvalue(0);
    }

    // ===================== CHAT =====================
    @FXML
    private void onMascotClick() {
        chatOpen = !chatOpen;

        if (chatBubble != null) {
            chatBubble.setVisible(chatOpen);
            chatBubble.setManaged(chatOpen);
        }

        if (chatOpen && chatMessages != null && chatMessages.getChildren().isEmpty()) {
            botSay("Salut 👋 Moi c’est NAVI 🤖");
            botSay("Dis-moi : lieu + budget + style (calme/adrénaline, mer/nature/ville).");
        }
    }

    @FXML
    private void onMascotClose() {
        chatOpen = false;
        if (chatBubble != null) {
            chatBubble.setVisible(false);
            chatBubble.setManaged(false);
        }
    }

    @FXML
    private void onChatSend() {
        if (chatInput == null) return;
        String msg = chatInput.getText();
        if (msg == null || msg.isBlank()) return;

        userSay(msg.trim());
        chatInput.clear();

        botSay("⏳ NAVI réfléchit...");

        List<Activite> all = service.getAllActivites();

        List<Map<String, Object>> acts = all.stream()
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getIdActivite());
                    m.put("nom", safe(a.getNom()));
                    m.put("lieu", safe(a.getLieu()));
                    m.put("prix", a.getPrix());
                    m.put("type", safe(a.getType()));
                    m.put("duree", a.getDuree());
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", msg.trim());
        payload.put("activities", acts);

        new Thread(() -> {
            try {
                String jsonBody = mapper.writeValueAsString(payload);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(CHAT_API_URL))
                        .timeout(Duration.ofSeconds(30))
                        .header("Content-Type", "application/json; charset=utf-8")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (res.statusCode() != 200) {
                    Platform.runLater(() -> botSay("❌ Erreur serveur (" + res.statusCode() + "). Vérifie Apache + chat.php"));
                    return;
                }

                Map<String, Object> data = mapper.readValue(res.body(), Map.class);
                String reply = String.valueOf(data.getOrDefault("reply", "…"));
                Object idsObj = data.get("recommended_ids");

                List<Integer> ids = new ArrayList<>();
                if (idsObj instanceof List<?>) {
                    for (Object o : (List<?>) idsObj) {
                        if (o instanceof Number) ids.add(((Number) o).intValue());
                        else {
                            try { ids.add(Integer.parseInt(String.valueOf(o))); } catch (Exception ignored) {}
                        }
                    }
                }

                Platform.runLater(() -> {
                    botSay(reply);

                    if (!ids.isEmpty()) {
                        List<Activite> filtered = all.stream()
                                .filter(a -> ids.contains(a.getIdActivite()))
                                .collect(Collectors.toList());
                        afficherListe(filtered);
                        if (scroll != null) scroll.setVvalue(0);
                    } else {
                        botSay("Je n’ai pas trouvé d’IDs précis 😅 Essaie d’ajouter lieu + budget.");
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> botSay("❌ Impossible de contacter l’IA. Vérifie Apache + URL: " + CHAT_API_URL));
            }
        }).start();
    }

    private void botSay(String text) {
        if (chatMessages == null) return;
        Label l = new Label(text);
        l.setWrapText(true);
        l.getStyleClass().add("botMsg");
        chatMessages.getChildren().add(l);
    }

    private void userSay(String text) {
        if (chatMessages == null) return;
        Label l = new Label(text);
        l.setWrapText(true);
        l.getStyleClass().add("userMsg");
        chatMessages.getChildren().add(l);
    }

    // ===================== LIST + CARDS =====================
    private void afficherListe(List<Activite> activites) {
        if (cardsContainer == null) return;
        cardsContainer.getChildren().clear();
        for (Activite a : activites) {
            cardsContainer.getChildren().add(createCard(a));
        }
    }

    private void refresh() {
        List<Activite> activites = service.getAllActivites();
        afficherListe(activites);
        if (scroll != null) scroll.setVvalue(0);
    }

    // ✅ FIXED: synchronous image loading + guaranteed fallback (NO MORE WHITE)
    private Image loadCardImage(Activite a) {

        // fallback (always works)
        var fallbackRes = getClass().getResource("/images/activity_banner.jpg");
        Image fallback = new Image(
                Objects.requireNonNull(fallbackRes).toExternalForm(),
                true
        );

        if (a == null) return fallback;

        String url = a.getImageUrl();
        if (url == null || url.isBlank()) return fallback;

        url = url.trim();
        System.out.println("🖼️ FRONT image_url id=" + a.getIdActivite() + " => " + url);

        try {
            // ✅ ONLY allow real image files
            if (!url.matches(".*\\.(jpg|jpeg|png|webp)$")) {
                System.out.println("❌ Not a direct image file: " + url);
                return fallback;
            }

            Image img = new Image(url, true);

            if (img.isError()) {
                System.out.println("❌ Image load failed: " + url);
                return fallback;
            }

            return img;

        } catch (Exception e) {
            System.out.println("❌ Image exception: " + url + " | " + e.getMessage());
            return fallback;
        }
    }

    private VBox createCard(Activite a) {

        VBox card = new VBox();
        card.getStyleClass().add("tripCard");
        card.setPrefWidth(360);
        card.setMaxWidth(360);

        ImageView img = new ImageView();
        img.getStyleClass().add("tripCardImage");
        img.setFitWidth(360);
        img.setFitHeight(210);
        img.setPreserveRatio(false);
        img.setSmooth(true);

        img.setImage(loadCardImage(a)); // ✅ DB image_url + fallback

        StackPane imageWrap = new StackPane(img);
        imageWrap.getStyleClass().add("tripCardImageWrap");

        VBox body = new VBox(10);
        body.setPadding(new Insets(14));

        Label country = new Label(safeUpper(a.getLieu()));
        country.getStyleClass().add("tripCardCountry");

        Label star = new Label("★");
        star.getStyleClass().add("tripCardStar");

// ✅ dynamic rating from DB (ai_rating)
        double r = (a.getAiRating() != null) ? a.getAiRating() : 4.0; // fallback if null
        Label rating = new Label(String.format("%.1f", r));
        rating.getStyleClass().add("tripCardRating");

// ✅ optional: style by score (nice UX)
        if (r >= 4.5) {
            rating.setStyle("-fx-text-fill: #E0A800;"); // gold
        } else if (r >= 4.0) {
            rating.setStyle("-fx-text-fill: #1f7a1f;"); // green
        } else {
            rating.setStyle("-fx-text-fill: #6C757D;"); // gray
        }

        HBox ratingBox = new HBox(6, star, rating);
        ratingBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        HBox topRow = new HBox(10, country, spacer1, ratingBox);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(safe(a.getNom()));
        title.getStyleClass().add("tripCardTitle");
        title.setWrapText(true);

        Label people = new Label("👥 6-10");
        people.getStyleClass().add("tripMeta");

        Label duration = new Label("📅 " + a.getDuree() + " min");
        duration.getStyleClass().add("tripMeta");

        Label badge = new Label((a.getType() == null || a.getType().isBlank()) ? "Easy" : a.getType());
        badge.getStyleClass().add("tripBadge");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox metaRow = new HBox(18, people, duration, spacer2, badge);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Separator sep = new Separator();
        sep.getStyleClass().add("tripDivider");

        double prixConverted = displayPrice(a.getPrix());
        String sym = currencySymbol(selectedCurrency);

        Label price = new Label(formatPrix(prixConverted) + " " + sym);
        price.getStyleClass().add("tripPrice");

        Label per = new Label("Par activité");
        per.getStyleClass().add("tripPer");

        VBox priceBox = new VBox(2, price, per);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        Button book = new Button("Réserver  >");
        book.getStyleClass().add("tripBookBtn");
        book.setOnAction(e -> openReservationForm(a.getIdActivite(), a.getNom()));

        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);

        HBox bottomRow = new HBox(10, priceBox, spacer3, book);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        body.getChildren().addAll(topRow, title, metaRow, sep, bottomRow);
        card.getChildren().addAll(imageWrap, body);

        return card;
    }

    // ===================== NAVIGATION =====================
    private void openReservationForm(int idActivite, String nomActivite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservation_form.fxml"));
            Parent root = loader.load();

            ReservationFormController controller = loader.getController();
            controller.setData(idActivite, nomActivite);

            Stage stage = (Stage) scroll.getScene().getWindow();

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            scene.getStylesheets().clear();
            var css = getClass().getResource("/reservation.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.show();
            Platform.runLater(() -> stage.setMaximized(true));

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void goBackOffice() {
        switchScene("/affichage_activites_back.fxml", "/back_admin.css");    }

    private void switchScene(String fxml, String cssPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) cardsContainer.getScene().getWindow();

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            scene.getStylesheets().clear();

            if (cssPath != null) {
                var css = getClass().getResource(cssPath);
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
                else System.out.println("❌ CSS introuvable: " + cssPath);
            }

            stage.show();

            Platform.runLater(() -> {
                if (stage.isMaximized()) {
                    stage.setMaximized(false);
                    stage.setMaximized(true);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
        }
    }
    // ===================== HELPERS =====================
    private void showWarn(String msg) {
        Alert err = new Alert(Alert.AlertType.WARNING);
        err.setTitle("Attention");
        err.setHeaderText("Saisie invalide");
        err.setContentText(msg);
        err.showAndWait();
    }

    private String safe(String s) { return (s == null || s.isBlank()) ? "-" : s.trim(); }
    private String safeUpper(String s) { return safe(s).toUpperCase(); }

    private String formatPrix(double prix) {
        if (Math.floor(prix) == prix) return String.valueOf((int) prix);
        return String.format("%.2f", prix);
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try { return Double.parseDouble(s.replace(",", ".")); }
        catch (NumberFormatException e) { return null; }
    }
}