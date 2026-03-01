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


public class AffichageActivitesBackController {
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

    // ✅ Currency selector (NEW)
    @FXML private ComboBox<String> currencyBox;

    private String selectedCurrency = "TND";
    private double currentRate = 1.0; // TND -> selectedCurrency

    private final CurrencyService currencyService = new CurrencyService();

    // ===== Chat UI (matches FXML) =====
    @FXML private VBox chatBubble;
    @FXML private Label chatText;      // ✅ exists in your FXML
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
                bannerImage.setImage(new Image(url.toExternalForm()));
            }
            bannerImage.setPreserveRatio(false);
            bannerImage.setSmooth(true);
        }

        // mascot image inside button
        if (btnMascot != null) {
            var mu = getClass().getResource("/images/mascot.png");
            if (mu != null) {
                ImageView iv = new ImageView(new Image(mu.toExternalForm()));
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

        // ✅ Currency default + listener (NEW)
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

        // if user selects TND, no need to call API
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

            // fallback to TND
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

    // Convert a stored DB price (TND) to selected currency for display
    private double displayPrice(double prixTND) {
        return prixTND * currentRate;
    }

    // Convert user input (in selected currency) back to TND for DB filtering
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

        // ✅ interpret min/max as selected currency (UI), convert to TND for DB filtering
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
                    m.put("prix", a.getPrix()); // still TND in DB
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
        for (Activite a : activites)
            cardsContainer.getChildren().add(createCard(a));
    }

    private void refresh() {
        List<Activite> activites = service.getAllActivites();
        afficherListe(activites);
        if (scroll != null) scroll.setVvalue(0);
    }

    private VBox createCard(Activite a) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        HBox top = new HBox(10);

        Label lieu = new Label(safeUpper(a.getLieu()));
        lieu.getStyleClass().add("cardSmallTop");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label rating = new Label("★ 4.8");
        rating.getStyleClass().add("metaText");

        top.getChildren().addAll(lieu, spacer, rating);

        Label title = new Label(safe(a.getNom()));
        title.getStyleClass().add("cardTitle");
        title.setWrapText(true);

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label duree = new Label("⏱ " + a.getDuree() + " min");
        duree.getStyleClass().add("metaText");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Label badge = new Label(safe(a.getType()));
        badge.getStyleClass().add("badge");

        meta.getChildren().addAll(duree, spacer2, badge);

        Separator hr = new Separator();
        hr.getStyleClass().add("hr");

        HBox bottom = new HBox(10);
        bottom.setAlignment(Pos.CENTER_LEFT);

        VBox priceBox = new VBox(2);

        // price with currency conversion
        double prixConverted = displayPrice(a.getPrix());
        String sym = currencySymbol(selectedCurrency);

        Label price = new Label(formatPrix(prixConverted) + " " + sym);
        price.getStyleClass().add("price");

        Label priceSub = new Label("Par activité");
        priceSub.getStyleClass().add("priceSub");

        priceBox.getChildren().addAll(price, priceSub);

        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);

        // ADMIN ACTIONS
        Button reserver = new Button("📌");
        reserver.getStyleClass().addAll("iconBtn", "btnAdd");
        reserver.setTooltip(new Tooltip("Réserver"));
        reserver.setOnAction(e -> openReservationForm(a.getIdActivite(), a.getNom()));

        Button modifier = new Button("✏");
        modifier.getStyleClass().addAll("iconBtn", "btnEdit");
        modifier.setTooltip(new Tooltip("Modifier"));
        modifier.setOnAction(e -> ouvrirModifier(a));

        Button ajouter = new Button("➕");
        ajouter.getStyleClass().addAll("iconBtn", "btnAdd");
        ajouter.setTooltip(new Tooltip("Ajouter"));
        ajouter.setOnAction(e -> ouvrirAjouter());

        Button supprimer = new Button("🗑");
        supprimer.getStyleClass().addAll("iconBtn", "btnDelete");
        supprimer.setTooltip(new Tooltip("Supprimer"));
        supprimer.setOnAction(e -> confirmerEtSupprimer(a));

        HBox actions = new HBox(8, reserver, modifier, ajouter, supprimer);
        actions.setAlignment(Pos.CENTER_RIGHT);

        bottom.getChildren().addAll(priceBox, spacer3, actions);

        card.getChildren().addAll(top, title, meta, hr, bottom);
        VBox.setMargin(hr, new Insets(2, 0, 2, 0));

        return card;
    }

    // ===================== NAVIGATION / CRUD =====================
    private void ouvrirModifier(Activite a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier_activite.fxml"));
            Parent root = loader.load();

            ModifierActiviteController controller = loader.getController();
            controller.setActivite(a);

            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            Scene scene = stage.getScene();

            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            // ✅ Apply the CSS for modifier page (use your modifier CSS if you have it)
            scene.getStylesheets().clear();
            var css = getClass().getResource("/affichage.css"); // or "/modifier.css" if you have one
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.show();

            // ✅ Keep fullscreen state stable
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);   // if you always want full screen
                // OR if you only want to keep current state:
                // if (stage.isMaximized()) stage.setMaximized(true);
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Modification impossible: " + ex.getMessage()).showAndWait();
        }
    }

    private void ouvrirAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajout_activite.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            var css = getClass().getResource("/form_activite.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            boolean wasMax = stage.isMaximized();
            stage.setMaximized(false);

            stage.setScene(scene);
            root.applyCss();
            root.layout();

            stage.setMaximized(wasMax);
            stage.centerOnScreen();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Ajout impossible", ex);
        }
    }

    private void openReservationForm(int idActivite, String nomActivite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservation_form.fxml"));
            Parent root = loader.load();

            ReservationFormController controller = loader.getController();
            controller.setData(idActivite, nomActivite);

            Stage stage = (Stage) cardsContainer.getScene().getWindow();

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

            // ✅ maximize AFTER showing (prevents freeze)
            javafx.application.Platform.runLater(() -> stage.setMaximized(true));

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }
    private void confirmerEtSupprimer(Activite a) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer cette activité ?");
        confirm.setContentText("Activité : " + safe(a.getNom()));

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            service.deleteActivite(a.getIdActivite());
            refresh();
        }
    }

    // ===================== HELPERS =====================
    private void showError(String title, Exception ex) {
        Alert err = new Alert(Alert.AlertType.ERROR);
        err.setTitle("Erreur");
        err.setHeaderText(title);
        err.setContentText(ex.getMessage());
        err.showAndWait();
        ex.printStackTrace();
    }

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
    @FXML
    private void goFrontOffice() {
        switchScene("/affichage_activites_front.fxml");
    }

    private void switchScene(String fxml) {
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

            // ✅ re-apply CSS every time after swapping root
            scene.getStylesheets().clear();
            var css = getClass().getResource("/affichage.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            // ❌ REMOVE these (they cause fullscreen freeze):
            // stage.sizeToScene();
            // stage.centerOnScreen();

            stage.show();

            // ✅ if currently maximized, refresh maximize safely (avoids stuck layouts)
            javafx.application.Platform.runLater(() -> {
                if (stage.isMaximized()) {
                    stage.setMaximized(false);
                    stage.setMaximized(true);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }


}