package edu.destination.controllers;

import edu.destination.entities.Destination;
import edu.destination.entities.Image;
import edu.destination.services.DestinationService;
import edu.destination.services.ImageService;
import edu.destination.tools.SceneUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClientDestinationController {

    private static final String GROQ_API_KEY = "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";

    @FXML private FlowPane flowDestinations;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboSaison;
    @FXML private Button btnReset;

    @FXML private VBox chatPanel;
    @FXML private VBox chatMessages;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField inputMessage;
    @FXML private Button btnEnvoyer;
    @FXML private Button btnToggleChat;
    @FXML private ImageView heroImageView;
    @FXML private Button btnRetourLogin;

    // ✅ Nouveaux boutons du design
    @FXML private Button btnTous;
    @FXML private Button btnFilterPrintemps;
    @FXML private Button btnFilterEte;
    @FXML private Button btnFilterAutomne;
    @FXML private Button btnFilterHiver;
    @FXML private Button btnPrintemps;
    @FXML private Button btnEte;
    @FXML private Button btnAutomne;
    @FXML private Button btnHiver;
    @FXML private Button btnSearch;
    @FXML private Label lblTotalDest;

    private final DestinationService destinationService = new DestinationService();
    private final ImageService imageService = new ImageService();
    private List<Destination> allDestinations;
    private boolean chatVisible = false;

    private final Map<Integer, Integer> popularityCache = new ConcurrentHashMap<>();

    // ==============================
    // INITIALISATION
    // ==============================
    @FXML
    public void initialize() {
        allDestinations = destinationService.getData();
        setupSearch();
        setupSidebar();
        renderDestinations(allDestinations);
        setupChatbot();
        computePopularityScoresAsync(allDestinations);

        // ✅ Compteur destinations
        if (lblTotalDest != null)
            lblTotalDest.setText(allDestinations.size() + "+");

        // ✅ Hero image depuis Unsplash
        if (heroImageView != null) {
            try {
                javafx.scene.image.Image hero = new javafx.scene.image.Image(
                        "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=1920&q=85&auto=format&fit=crop",
                        1400, 700, false, true, true
                );
                heroImageView.setImage(hero);
                heroImageView.setFitWidth(1400);
                heroImageView.setFitHeight(700);
                heroImageView.setPreserveRatio(false);
            } catch (Exception ignored) {}
        }

        // ✅ Boutons hero rapides
        if (btnPrintemps != null) btnPrintemps.setOnAction(e -> filterBySaison("Printemps"));
        if (btnEte != null)       btnEte.setOnAction(e -> filterBySaison("Ete"));
        if (btnAutomne != null)   btnAutomne.setOnAction(e -> filterBySaison("Automne"));
        if (btnHiver != null)     btnHiver.setOnAction(e -> filterBySaison("Hiver"));
        if (btnSearch != null)    btnSearch.setOnAction(e -> applyFilter());

        // ✅ Boutons filtre section
        if (btnTous != null) btnTous.setOnAction(e -> {
            searchField.clear();
            comboSaison.getSelectionModel().selectFirst();
            setActiveFilterButton(btnTous);
        });
        if (btnFilterPrintemps != null) btnFilterPrintemps.setOnAction(e -> {
            filterBySaison("Printemps");
            setActiveFilterButton(btnFilterPrintemps);
        });
        if (btnFilterEte != null) btnFilterEte.setOnAction(e -> {
            filterBySaison("Ete");
            setActiveFilterButton(btnFilterEte);
        });
        if (btnFilterAutomne != null) btnFilterAutomne.setOnAction(e -> {
            filterBySaison("Automne");
            setActiveFilterButton(btnFilterAutomne);
        });
        if (btnFilterHiver != null) btnFilterHiver.setOnAction(e -> {
            filterBySaison("Hiver");
            setActiveFilterButton(btnFilterHiver);
        });

        // ✅ Déconnexion
        btnRetourLogin.setOnAction(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
                Stage stage = (Stage) flowDestinations.getScene().getWindow();
                SceneUtil.setScene(stage, root, 800, 600);
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    // ✅ Active visuellement le bouton filtre sélectionné
    private void setActiveFilterButton(Button active) {
        List<Button> filterBtns = Arrays.asList(
                btnTous, btnFilterPrintemps, btnFilterEte, btnFilterAutomne, btnFilterHiver
        );
        String inactiveStyle =
                "-fx-background-color: white; -fx-text-fill: #7a7670;" +
                        "-fx-font-size: 11px; -fx-font-weight: 700; -fx-letter-spacing: 2px;" +
                        "-fx-border-color: #e8e4dc; -fx-border-radius: 4; -fx-background-radius: 4;" +
                        "-fx-border-width: 1; -fx-padding: 9 24 9 24; -fx-cursor: hand;";
        String activeStyle =
                "-fx-background-color: #c8956c; -fx-text-fill: white;" +
                        "-fx-font-size: 11px; -fx-font-weight: 700; -fx-letter-spacing: 2px;" +
                        "-fx-background-radius: 4; -fx-border-radius: 4;" +
                        "-fx-padding: 9 24 9 24; -fx-cursor: hand;";

        for (Button btn : filterBtns) {
            if (btn != null) btn.setStyle(inactiveStyle);
        }
        if (active != null) active.setStyle(activeStyle);
    }

    // ✅ Filtre par saison
    private void filterBySaison(String saison) {
        comboSaison.setValue(saison);
        applyFilter();
    }

    // ============================================================
    // POPULARITÉ
    // ============================================================
    private void computePopularityScoresAsync(List<Destination> destinations) {
        List<Destination> actives = destinations.stream()
                .filter(Destination::getStatut)
                .collect(Collectors.toList());

        if (actives.isEmpty()) return;

        Thread thread = new Thread(() -> {
            try {
                Map<Integer, Integer> scores = callGroqPopularity(actives);
                popularityCache.putAll(scores);
                Platform.runLater(() -> renderDestinations(allDestinations));
            } catch (Exception e) {
                System.err.println("Erreur calcul popularité : " + e.getMessage());
                actives.forEach(d -> popularityCache.put(d.getId(), computeLocalScore(d)));
                Platform.runLater(() -> renderDestinations(allDestinations));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private Map<Integer, Integer> callGroqPopularity(List<Destination> destinations) throws Exception {
        URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        String moisActuel = LocalDate.now().getMonth()
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH);

        StringBuilder destList = new StringBuilder();
        for (Destination d : destinations) {
            destList.append("ID:").append(d.getId())
                    .append("|Nom:").append(d.getNom())
                    .append("|Pays:").append(d.getPays())
                    .append("|Saison:").append(d.getMeilleureSaison())
                    .append("|NbVisites:").append(d.getNbVisites())
                    .append("|NbLikes:").append(d.getNbLikes())
                    .append("\n");
        }

        String prompt = "Tu es un expert en analyse touristique. "
                + "Nous sommes en " + moisActuel + ". "
                + "Pour chaque destination ci-dessous, calcule un score de popularité de 0 à 100. "
                + "Réponds UNIQUEMENT avec des lignes au format : ID:score\n\n"
                + destList;

        String system = "Tu es un système d'analyse. Réponds uniquement avec le format : ID:score, une ligne par destination.";

        String body = "{\"model\":\"llama-3.3-70b-versatile\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escapeJson(system) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}],"
                + "\"max_tokens\":500,\"temperature\":0.3}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        if (status != 200) throw new Exception("HTTP " + status);

        String content = parseGroqContent(sb.toString());
        Map<Integer, Integer> scores = new HashMap<>();
        Pattern p = Pattern.compile("(\\d+)\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(content);
        while (m.find()) {
            int id    = Integer.parseInt(m.group(1));
            int score = Math.min(100, Math.max(0, Integer.parseInt(m.group(2))));
            scores.put(id, score);
        }

        for (Destination d : destinations) {
            if (!scores.containsKey(d.getId()))
                scores.put(d.getId(), computeLocalScore(d));
        }

        return scores;
    }

    private int computeLocalScore(Destination d) {
        int score = 0;
        int visites = d.getNbVisites();
        if (visites > 0) score += (int) Math.min(40, (Math.log(visites + 1) / Math.log(1000)) * 40);
        int likes = d.getNbLikes();
        if (likes > 0) score += (int) Math.min(20, (Math.log(likes + 1) / Math.log(100)) * 20);
        String saisonActuelle = getSaisonActuelle();
        String saisonDest = d.getMeilleureSaison() != null ? d.getMeilleureSaison().toLowerCase() : "";
        score += saisonDest.contains(saisonActuelle) ? 30 : 5;
        score += new Random(d.getId()).nextInt(10);
        return Math.min(100, score);
    }

    private String getSaisonActuelle() {
        Month month = LocalDate.now().getMonth();
        return switch (month) {
            case DECEMBER, JANUARY, FEBRUARY -> "hiver";
            case MARCH, APRIL, MAY           -> "printemps";
            case JUNE, JULY, AUGUST          -> "été";
            default                          -> "automne";
        };
    }

    // ============================================================
    // BADGE POPULARITÉ
    // ============================================================
    private HBox createPopularityRow(int score) {
        String color, label, emoji;
        if (score >= 80)      { color = "#e74c3c"; label = "Tres populaire"; emoji = "Feu"; }
        else if (score >= 60) { color = "#e67e22"; label = "Populaire";      emoji = "Top"; }
        else if (score >= 40) { color = "#27ae60"; label = "Tendance";       emoji = "Up";  }
        else if (score >= 20) { color = "#3498db"; label = "A decouvrir";    emoji = "Go";  }
        else                  { color = "#95a5a6"; label = "Peu visite";     emoji = "New"; }

        Label scoreLabel = new Label(emoji + " " + score + "/100");
        scoreLabel.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");

        double barWidth  = 80.0;
        double fillWidth = Math.max(4, (score / 100.0) * barWidth);

        Rectangle barBg = new Rectangle(barWidth, 5);
        barBg.setArcWidth(5); barBg.setArcHeight(5);
        barBg.setFill(Color.web("#e0e0e0"));

        Rectangle barFill = new Rectangle(fillWidth, 5);
        barFill.setArcWidth(5); barFill.setArcHeight(5);
        barFill.setFill(Color.web(color));

        StackPane bar = new StackPane(barBg, barFill);
        StackPane.setAlignment(barFill, Pos.CENTER_LEFT);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label catLabel = new Label(label);
        catLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#888;");

        HBox row = new HBox(8, scoreLabel, bar, catLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 0, 0, 2));
        return row;
    }

    // ==============================
    // CARTES DESTINATIONS
    // ==============================
    private void renderDestinations(List<Destination> list) {
        flowDestinations.getChildren().clear();
        if (list.isEmpty()) {
            Label l = new Label("Aucune destination trouvee");
            l.setStyle("-fx-font-size:16px;-fx-text-fill:#888;-fx-padding:40;");
            flowDestinations.getChildren().add(l);
            return;
        }

        List<Destination> sorted = new ArrayList<>(list);
        if (!popularityCache.isEmpty()) {
            sorted.sort((a, b) -> Integer.compare(
                    popularityCache.getOrDefault(b.getId(), 0),
                    popularityCache.getOrDefault(a.getId(), 0)
            ));
        }
        sorted.forEach(d -> flowDestinations.getChildren().add(createCard(d)));
    }

    private VBox createCard(Destination d) {
        boolean isActive = d.getStatut();

        // ✅ Image avec style amélioré
        ImageView iv = new ImageView(new javafx.scene.image.Image(getFirstImageUrl(d), true));
        iv.setFitWidth(280); iv.setFitHeight(180); iv.setPreserveRatio(false);
        if (!isActive) iv.setStyle("-fx-opacity:0.45;");

        // Overlay dégradé sur l'image
        Pane overlay = new Pane();
        overlay.setPrefSize(280, 180);
        overlay.setStyle("-fx-background-color: linear-gradient(to top, rgba(10,9,8,0.6) 0%, transparent 50%);");

        // Badge pays (en haut à gauche)
        Label badge = new Label(d.getPays().toUpperCase());
        badge.setStyle(
                "-fx-background-color: rgba(10,9,8,0.6);" +
                        "-fx-text-fill: rgba(255,255,255,0.85);" +
                        "-fx-font-size: 10px; -fx-font-weight: 700;" +
                        "-fx-letter-spacing: 2px;" +
                        "-fx-padding: 5 12 5 12;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: rgba(255,255,255,0.15);" +
                        "-fx-border-radius: 20; -fx-border-width: 1;"
        );

        // Badge statut (en haut à droite)
        Label statusBadge = new Label(isActive ? "Actif" : "Inactif");
        statusBadge.setStyle(
                "-fx-background-color:" + (isActive ? "#27ae60" : "#e74c3c") + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 10px; -fx-font-weight: 700;" +
                        "-fx-padding: 4 10 4 10;" +
                        "-fx-background-radius: 20;"
        );

        StackPane imgContainer = new StackPane(iv, overlay, badge, statusBadge);
        imgContainer.setPrefSize(280, 180);
        StackPane.setAlignment(badge,       Pos.TOP_LEFT);
        StackPane.setAlignment(statusBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge,       new Insets(12, 0, 0, 12));
        StackPane.setMargin(statusBadge, new Insets(12, 12, 0, 0));

        // Corps de la carte
        VBox body = new VBox(8);
        body.setPadding(new Insets(16, 20, 16, 20));

        // Pays (petit label)
        Label paysLabel = new Label(d.getPays().toUpperCase());
        paysLabel.setStyle(
                "-fx-font-size: 10px; -fx-font-weight: 700;" +
                        "-fx-letter-spacing: 2px; -fx-text-fill: #c8956c;"
        );

        // Nom destination
        Label name = new Label(d.getNom());
        name.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: 700;" +
                        "-fx-text-fill: #1a1814; -fx-font-family: Georgia;" +
                        "-fx-cursor: hand;"
        );
        if (!isActive) name.setStyle(name.getStyle() + "-fx-opacity:0.6;");

        // Saison
        Label saison = new Label(d.getMeilleureSaison() != null ? d.getMeilleureSaison() : "-");
        saison.setStyle("-fx-font-size:12px;-fx-text-fill:#7a7670;");

        // Séparateur
        Pane sep = new Pane();
        sep.setPrefHeight(1);
        sep.setMaxWidth(Double.MAX_VALUE);
        sep.setStyle("-fx-background-color: #e8e4dc;");

        // Footer : like + visites + voir
        final int[] likeCount = {d.getNbLikes()};
        final boolean[] liked = {false};

        Button btnLike = new Button("♡  " + likeCount[0]);
        btnLike.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #e8e4dc;" +
                        "-fx-border-radius: 20; -fx-background-radius: 20;" +
                        "-fx-text-fill: #7a7670;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 5 14 5 14; -fx-cursor: hand;"
        );

        btnLike.setOnAction(e -> {
            if (!isActive) return;
            liked[0] = !liked[0];
            if (liked[0]) {
                likeCount[0]++;
                btnLike.setText("♥  " + likeCount[0]);
                btnLike.setStyle(
                        "-fx-background-color: #fff0f0;" +
                                "-fx-border-color: #fca5a5;" +
                                "-fx-border-radius: 20; -fx-background-radius: 20;" +
                                "-fx-text-fill: #e11d48;" +
                                "-fx-font-size: 12px;" +
                                "-fx-padding: 5 14 5 14; -fx-cursor: hand;"
                );
            } else {
                likeCount[0]--;
                btnLike.setText("♡  " + likeCount[0]);
                btnLike.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-color: #e8e4dc;" +
                                "-fx-border-radius: 20; -fx-background-radius: 20;" +
                                "-fx-text-fill: #7a7670;" +
                                "-fx-font-size: 12px;" +
                                "-fx-padding: 5 14 5 14; -fx-cursor: hand;"
                );
            }

            Thread t = new Thread(() -> {
                try {
                    Destination updated = destinationService.getById(d.getId());
                    if (updated != null) {
                        updated.setNbLikes(likeCount[0]);
                        destinationService.update(updated.getId(), updated);
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            });
            t.setDaemon(true);
            t.start();
        });

        Label visites = new Label("Visites: " + d.getNbVisites());
        visites.setStyle("-fx-font-size:11px;-fx-text-fill:#999;");

        Button btnVoir = new Button("Voir");
        btnVoir.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #c8956c;" +
                        "-fx-font-size: 12px; -fx-font-weight: 700;" +
                        "-fx-cursor: hand; -fx-padding: 5 0 5 0;"
        );
        if (isActive) btnVoir.setOnAction(e -> openDetails(d));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(10, btnLike, visites, spacer, btnVoir);
        footer.setAlignment(Pos.CENTER_LEFT);

        // Score popularité
        int score = popularityCache.getOrDefault(d.getId(), -1);
        if (isActive && score >= 0) {
            HBox popularityRow = createPopularityRow(score);
            body.getChildren().addAll(paysLabel, name, saison, popularityRow, sep, footer);
        } else if (isActive) {
            Label loading = new Label("Calcul du score...");
            loading.setStyle("-fx-font-size:11px;-fx-text-fill:#aaa;");
            body.getChildren().addAll(paysLabel, name, saison, loading, sep, footer);
        } else {
            body.getChildren().addAll(paysLabel, name, saison, sep, footer);
        }

        // ✅ Carte finale avec style Symfony
        VBox card = new VBox(imgContainer, body);
        card.setPrefWidth(280);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #e8e4dc;" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);" +
                        "-fx-cursor: " + (isActive ? "hand" : "default") + ";"
        );

        if (isActive) {
            card.setOnMouseEntered(e -> card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: rgba(200,149,108,0.4);" +
                            "-fx-border-radius: 20;" +
                            "-fx-border-width: 1;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.14), 24, 0, 0, 8);" +
                            "-fx-cursor: hand;" +
                            "-fx-translate-y: -4;"
            ));
            card.setOnMouseExited(e -> card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: #e8e4dc;" +
                            "-fx-border-radius: 20;" +
                            "-fx-border-width: 1;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);" +
                            "-fx-cursor: hand;"
            ));
            card.setOnMouseClicked(e -> {
                if (!(e.getTarget() instanceof Button)) openDetails(d);
            });
        }

        return card;
    }

    // ==============================
    // CHATBOT
    // ==============================
    private void setupChatbot() {
        chatPanel.setVisible(false);
        chatPanel.setManaged(false);
        btnToggleChat.setOnAction(e -> toggleChat());
        btnEnvoyer.setOnAction(e -> envoyerMessage());
        inputMessage.setOnAction(e -> envoyerMessage());
        addBotMessage("Bonjour ! Je suis votre assistant voyage.\n\nExemples :\n- Quelle destination pour l'ete ?\n- Destination en Europe ?");
    }

    private void toggleChat() {
        chatVisible = !chatVisible;
        chatPanel.setVisible(chatVisible);
        chatPanel.setManaged(chatVisible);
        btnToggleChat.setText(chatVisible ? "Fermer chat" : "Assistant Voyage");
    }

    @FXML private void suggestionEte()    { inputMessage.setText("Quelle destination pour l'ete ?");   envoyerMessage(); }
    @FXML private void suggestionHiver()  { inputMessage.setText("Quelle destination pour l'hiver ?"); envoyerMessage(); }
    @FXML private void suggestionBudget() { inputMessage.setText("Destination moins de 500 TND ?");    envoyerMessage(); }
    @FXML private void suggestionEurope() { inputMessage.setText("Destinations en Europe ?");          envoyerMessage(); }

    private void envoyerMessage() {
        String message = inputMessage.getText().trim();
        if (message.isEmpty()) return;
        addUserMessage(message);
        inputMessage.clear();
        btnEnvoyer.setDisable(true);

        Thread thread = new Thread(() -> {
            try {
                String response = callGroqChat(message, buildContext());
                List<Destination> matched = findMatchingDestinations(message, response);
                Platform.runLater(() -> {
                    addBotMessage(response);
                    if (!matched.isEmpty()) addDestinationSuggestions(matched);
                    btnEnvoyer.setDisable(false);
                    scrollToBottom();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    addBotMessage("Erreur : " + ex.getMessage());
                    btnEnvoyer.setDisable(false);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String buildContext() {
        StringBuilder sb = new StringBuilder("Destinations disponibles :\n");
        for (Destination d : allDestinations) {
            if (!d.getStatut()) continue;
            int score = popularityCache.getOrDefault(d.getId(), -1);
            sb.append("• ").append(d.getNom())
                    .append(" | Pays: ").append(d.getPays())
                    .append(" | Saison: ").append(d.getMeilleureSaison())
                    .append(" | Likes: ").append(d.getNbLikes())
                    .append(" | Visites: ").append(d.getNbVisites());
            if (score >= 0) sb.append(" | Popularite: ").append(score).append("/100");
            sb.append("\n");
        }
        return sb.toString();
    }

    private String callGroqChat(String userMessage, String context) throws Exception {
        URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        String system = "Tu es un assistant voyage. Reponds en francais. "
                + "Aide les clients a choisir une destination.\n" + context;

        String body = "{\"model\":\"llama-3.3-70b-versatile\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escapeJson(system) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(userMessage) + "\"}],"
                + "\"max_tokens\":800,\"temperature\":0.7}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        if (status != 200) throw new Exception("HTTP " + status);
        return parseGroqContent(sb.toString());
    }

    private String parseGroqContent(String json) throws Exception {
        int idx   = json.indexOf("\"role\":\"assistant\"");
        if (idx == -1) idx = 0;
        int start = json.indexOf("\"content\":\"", idx);
        if (start == -1) throw new Exception("Contenu introuvable");
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
                    case 'u':
                        if (i + 4 < json.length()) {
                            String hex = json.substring(i + 1, i + 5);
                            try { result.append((char) Integer.parseInt(hex, 16)); i += 4; }
                            catch (NumberFormatException e) { result.append("\\u").append(hex); }
                        }
                        break;
                    default: result.append(c);
                }
                escape = false;
            } else if (c == '\\') { escape = true; }
            else if (c == '"')    { break; }
            else                  { result.append(c); }
        }
        return result.toString()
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("\\*(.*?)\\*", "$1")
                .trim();
    }

    // ==============================
    // MATCHING
    // ==============================
    private List<Destination> findMatchingDestinations(String userMessage, String botResponse) {
        String lowerMsg  = userMessage.toLowerCase();
        String lowerResp = botResponse.toLowerCase();

        return allDestinations.stream().filter(d -> {
            if (!d.getStatut()) return false;
            String nom    = d.getNom()             != null ? d.getNom().toLowerCase()             : "";
            String pays   = d.getPays()            != null ? d.getPays().toLowerCase()            : "";
            String saison = d.getMeilleureSaison() != null ? d.getMeilleureSaison().toLowerCase() : "";

            boolean inBotResponse = (!nom.isEmpty()  && lowerResp.contains(nom))
                    || (!pays.isEmpty() && lowerResp.contains(pays));

            boolean saisonMatch = false;
            if (lowerMsg.contains("ete") || lowerMsg.contains("été"))
                saisonMatch = saison.contains("ete") || saison.contains("été");
            if (lowerMsg.contains("hiver"))     saisonMatch = saison.contains("hiver");
            if (lowerMsg.contains("printemps")) saisonMatch = saison.contains("printemps");
            if (lowerMsg.contains("automne"))   saisonMatch = saison.contains("automne");

            boolean regionMatch = false;
            if (lowerMsg.contains("europe"))  regionMatch = isEuropean(pays);
            if (lowerMsg.contains("asie"))    regionMatch = isAsian(pays);
            if (lowerMsg.contains("afrique")) regionMatch = isAfrican(pays);

            boolean directMatch = (!nom.isEmpty()  && lowerMsg.contains(nom))
                    || (!pays.isEmpty() && lowerMsg.contains(pays));

            return inBotResponse || saisonMatch || regionMatch || directMatch;
        }).limit(4).collect(Collectors.toList());
    }

    private boolean isEuropean(String pays) {
        String p = pays.toLowerCase();
        return p.contains("france") || p.contains("italie") || p.contains("espagne")
                || p.contains("portugal") || p.contains("grece") || p.contains("allemagne")
                || p.contains("suisse") || p.contains("belgique") || p.contains("autriche");
    }

    private boolean isAsian(String pays) {
        String p = pays.toLowerCase();
        return p.contains("japon") || p.contains("chine") || p.contains("thailande")
                || p.contains("bali") || p.contains("inde") || p.contains("vietnam");
    }

    private boolean isAfrican(String pays) {
        String p = pays.toLowerCase();
        return p.contains("tunisie") || p.contains("maroc") || p.contains("egypte")
                || p.contains("afrique") || p.contains("kenya") || p.contains("senegal");
    }

    // ==============================
    // SUGGESTIONS CHAT
    // ==============================
    private void addDestinationSuggestions(List<Destination> destinations) {
        Label titre = new Label("Destinations suggerees :");
        titre.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#555;-fx-padding:6 8 2 8;");
        chatMessages.getChildren().add(titre);
        for (Destination d : destinations) chatMessages.getChildren().add(createChatCard(d));
        scrollToBottom();
    }

    private VBox createChatCard(Destination d) {
        ImageView iv = new ImageView(new javafx.scene.image.Image(getFirstImageUrl(d), 220, 110, false, true, true));
        iv.setFitWidth(220); iv.setFitHeight(110); iv.setPreserveRatio(false);

        Label name = new Label(d.getNom());
        name.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:#333;");
        name.setWrapText(true); name.setMaxWidth(210);

        Label info = new Label(d.getPays() + " - " + (d.getMeilleureSaison() != null ? d.getMeilleureSaison() : "-"));
        info.setStyle("-fx-font-size:11px;-fx-text-fill:#666;");

        Label likesVisites = new Label("Likes: " + d.getNbLikes() + "  Visites: " + d.getNbVisites());
        likesVisites.setStyle("-fx-font-size:11px;-fx-text-fill:#999;");

        int score = popularityCache.getOrDefault(d.getId(), -1);
        HBox scoreRow = new HBox();
        if (score >= 0) {
            Label scoreLbl = new Label("Popularite : " + score + "/100");
            scoreLbl.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + getScoreColor(score) + ";");
            scoreRow.getChildren().add(scoreLbl);
        }

        Button btnVoir = new Button("Voir les details");
        btnVoir.setStyle("-fx-background-color:#c8a96e;-fx-text-fill:white;-fx-font-size:11px;"
                + "-fx-background-radius:20;-fx-cursor:hand;-fx-padding:5 12 5 12;");
        btnVoir.setOnAction(e -> openDetails(d));

        VBox card = new VBox(6, iv, name, info, likesVisites, scoreRow, btnVoir);
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;"
                + "-fx-padding:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.12),8,0,0,2);");
        card.setPrefWidth(240); card.setMaxWidth(240);
        card.setAlignment(Pos.CENTER_LEFT);

        HBox wrapper = new HBox(card);
        wrapper.setPadding(new Insets(4, 40, 4, 8));
        wrapper.setAlignment(Pos.CENTER_LEFT);
        return new VBox(wrapper);
    }

    private String getScoreColor(int score) {
        if (score >= 80) return "#e74c3c";
        if (score >= 60) return "#e67e22";
        if (score >= 40) return "#27ae60";
        if (score >= 20) return "#3498db";
        return "#95a5a6";
    }

    // ==============================
    // BULLES CHAT
    // ==============================
    private void addUserMessage(String text) {
        Label label = new Label(text);
        label.setWrapText(true); label.setMaxWidth(260);
        label.setStyle("-fx-background-color:#c8a96e;-fx-text-fill:white;"
                + "-fx-padding:10 14 10 14;-fx-background-radius:18 18 4 18;-fx-font-size:12px;");
        HBox hbox = new HBox(label);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(3, 8, 3, 40));
        chatMessages.getChildren().add(hbox);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        Label label = new Label(text);
        label.setWrapText(true); label.setMaxWidth(260);
        label.setStyle("-fx-background-color:#f0f0f0;-fx-text-fill:#333;"
                + "-fx-padding:10 14 10 14;-fx-background-radius:18 18 18 4;-fx-font-size:12px;");
        HBox hbox = new HBox(6, new Label("V"), label);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(3, 40, 3, 8));
        chatMessages.getChildren().add(hbox);
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private String escapeJson(String t) {
        return t.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    // ==============================
    // RECHERCHE / FILTRE
    // ==============================
    private void setupSearch() {
        List<String> saisons = allDestinations.stream()
                .map(Destination::getMeilleureSaison)
                .filter(s -> s != null && !s.isBlank())
                .distinct().sorted().collect(Collectors.toList());
        comboSaison.getItems().add("Toutes les saisons");
        comboSaison.getItems().addAll(saisons);
        comboSaison.getSelectionModel().selectFirst();
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        comboSaison.valueProperty().addListener((obs, o, n) -> applyFilter());
        btnReset.setOnAction(e -> {
            searchField.clear();
            comboSaison.getSelectionModel().selectFirst();
            if (btnTous != null) setActiveFilterButton(btnTous);
        });
    }

    private void applyFilter() {
        String kw = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String s  = comboSaison.getValue();
        renderDestinations(allDestinations.stream().filter(d ->
                (kw.isEmpty() || d.getNom().toLowerCase().contains(kw)
                        || d.getPays().toLowerCase().contains(kw)) &&
                        (s == null || s.equals("Toutes les saisons")
                                || s.equalsIgnoreCase(d.getMeilleureSaison()))
        ).collect(Collectors.toList()));
    }

    // ==============================
    // UTILITAIRES
    // ==============================
    private String getFirstImageUrl(Destination d) {
        try {
            List<Image> imgs = imageService.getImagesByDestination(d.getId());
            if (imgs != null && !imgs.isEmpty()) {
                String url = imgs.get(0).getUrlImage();
                System.out.println("URL image dest " + d.getId() + " : " + url);
                if (url.startsWith("/uploads/")) {
                    File f = new File("C:/xampp/htdocs/VOYAGE/public" + url);
                    System.out.println("Fichier existe : " + f.exists() + " -> " + f.getAbsolutePath());
                    if (f.exists()) return f.toURI().toString();
                }
                File f = new File(url);
                if (f.exists()) return f.toURI().toString();
            }
        } catch (Exception e) { System.out.println("Erreur : " + e.getMessage()); }
        return "https://images.unsplash.com/photo-1488085061387-422e29b40080?w=400&q=80";
    }
    private void openDetails(Destination destination) {
        try {
            hebergement.controllers.ClientLayoutController client =
                    hebergement.controllers.ClientLayoutController.getInstance();
            if (client != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientDetailsView.fxml"));
                Parent root = loader.load();
                ClientDetailsController ctrl = loader.getController();
                ctrl.setDestination(destination);
                client.loadPageWithRoot(root);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupSidebar() {}

    private void openView(String fxml) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            Stage stage = (Stage) flowDestinations.getScene().getWindow();
            SceneUtil.setScene(stage, root, 1200, 800);
        } catch (Exception e) { e.printStackTrace(); }
    }
}