package edu.destination.controllers;

import edu.destination.entities.Destination;
import edu.destination.entities.DestinationImage;
import edu.destination.services.DestinationService;
import edu.destination.services.ImageService;
import edu.destination.tools.SceneUtil;
import javafx.application.Platform;
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

    // ============================================================
    // 🔑 CLÉ GROQ
    // ============================================================
    private static final String GROQ_API_KEY = "";

    // ==============================
    // FXML — destinations
    // ==============================
    @FXML private FlowPane flowDestinations;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboSaison;
    @FXML private Button btnReset;
    @FXML private Button navDashboard, navDestinations, navTransports, navImages, navClient;

    // ==============================
    // FXML — chatbot
    // ==============================
    @FXML private VBox      chatPanel;
    @FXML private VBox      chatMessages;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField  inputMessage;
    @FXML private Button     btnEnvoyer;
    @FXML private Button     btnToggleChat;

    private final DestinationService destinationService = new DestinationService();
    private final ImageService       imageService       = new ImageService();
    private List<Destination> allDestinations;
    private boolean chatVisible = false;

    // Cache scores popularité : idDestination → score (0-100)
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
        // Calcul des scores en arrière-plan
        computePopularityScoresAsync(allDestinations);
    }

    // ============================================================
    // 🌟 INDICE DE POPULARITÉ INTELLIGENT
    // ============================================================

    /**
     * Lance le calcul IA des scores pour toutes les destinations actives
     * en arrière-plan, puis rafraîchit les cartes.
     */
    private void computePopularityScoresAsync(List<Destination> destinations) {
        List<Destination> actives = destinations.stream().filter(d -> {
            boolean expired = d.getDateDepart() != null && d.getDateDepart().isBefore(LocalDate.now());
            return d.getStatut() && !expired;
        }).collect(Collectors.toList());

        if (actives.isEmpty()) return;

        Thread thread = new Thread(() -> {
            try {
                Map<Integer, Integer> scores = callGroqPopularity(actives);
                popularityCache.putAll(scores);
                Platform.runLater(() -> renderDestinations(allDestinations));
            } catch (Exception e) {
                System.err.println("Erreur calcul popularité : " + e.getMessage());
                // Fallback local pour toutes les destinations
                actives.forEach(d -> popularityCache.put(d.getIdDestination(), computeLocalScore(d)));
                Platform.runLater(() -> renderDestinations(allDestinations));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Appel Groq : calcule un score de popularité (0-100) pour chaque destination
     * en tenant compte de : nbVisites, saison actuelle, météo typique, attractivité.
     */
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
            destList.append("ID:").append(d.getIdDestination())
                    .append("|Nom:").append(d.getNom())
                    .append("|Pays:").append(d.getPays())
                    .append("|Saison:").append(d.getMeilleureSaison())
                    .append("|NbVisites:").append(d.getNbVisites())
                    .append("|Prix:").append(d.getPrix()).append("TND")
                    .append("\n");
        }

        String prompt = "Tu es un expert en analyse touristique. "
                + "Nous sommes en " + moisActuel + ". "
                + "Pour chaque destination ci-dessous, calcule un score de popularité de 0 à 100 "
                + "en tenant compte de : "
                + "1) Le nombre de visites (plus c'est élevé, plus le score est haut), "
                + "2) La correspondance entre la saison actuelle (" + moisActuel + ") et la meilleure saison, "
                + "3) La météo typique de la destination en " + moisActuel + ", "
                + "4) L'attractivité touristique générale du lieu. "
                + "Réponds UNIQUEMENT avec des lignes au format : ID:score "
                + "Exemple : 1:87 "
                + "Une ligne par destination, rien d'autre.\n\n"
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

        // Fallback local pour les destinations non retournées par l'IA
        for (Destination d : destinations) {
            if (!scores.containsKey(d.getIdDestination())) {
                scores.put(d.getIdDestination(), computeLocalScore(d));
            }
        }

        return scores;
    }

    /**
     * Score de fallback calculé localement sans IA.
     */
    private int computeLocalScore(Destination d) {
        int score = 0;
        int visites = d.getNbVisites();
        if (visites > 0) score += (int) Math.min(40, (Math.log(visites + 1) / Math.log(1000)) * 40);

        String saisonActuelle = getSaisonActuelle();
        String saisonDest     = d.getMeilleureSaison() != null ? d.getMeilleureSaison().toLowerCase() : "";
        score += saisonDest.contains(saisonActuelle) ? 35 : 10;

        if (d.getPrix() > 0) {
            if      (d.getPrix() < 300)  score += 15;
            else if (d.getPrix() < 600)  score += 10;
            else if (d.getPrix() < 1000) score += 5;
        }

        score += new Random(d.getIdDestination()).nextInt(10);
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
    // 🎨 BADGE POPULARITÉ
    // ============================================================
    private VBox createPopularityBadge(int score) {
        String color;
        String label;
        String emoji;
        if (score >= 80)      { color = "#e74c3c"; label = "Très populaire"; emoji = "🔥"; }
        else if (score >= 60) { color = "#e67e22"; label = "Populaire";       emoji = "⭐"; }
        else if (score >= 40) { color = "#27ae60"; label = "Tendance";        emoji = "📈"; }
        else if (score >= 20) { color = "#3498db"; label = "À découvrir";     emoji = "🌍"; }
        else                  { color = "#95a5a6"; label = "Peu visité";      emoji = "🔍"; }

        Label scoreLabel = new Label(emoji + " " + score + "/100");
        scoreLabel.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");

        Label catLabel = new Label(label);
        catLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#666;");

        double barWidth  = 120.0;
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

        VBox badge = new VBox(2, scoreLabel, bar, catLabel);
        badge.setStyle("-fx-background-color:rgba(255,255,255,0.92);"
                + "-fx-background-radius:8;"
                + "-fx-padding:5 8 5 8;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),4,0,0,1);");
        badge.setAlignment(Pos.CENTER_LEFT);
        return badge;
    }

    // ==============================
    // CARTES PRINCIPALES
    // ==============================
    private void renderDestinations(List<Destination> list) {
        flowDestinations.getChildren().clear();
        if (list.isEmpty()) {
            Label l = new Label("😕 Aucune destination trouvée");
            l.setStyle("-fx-font-size:16px;-fx-text-fill:#888;-fx-padding:40;");
            flowDestinations.getChildren().add(l);
            return;
        }
        // Trier par score décroissant si les scores sont disponibles
        List<Destination> sorted = new ArrayList<>(list);
        if (!popularityCache.isEmpty()) {
            sorted.sort((a, b) -> Integer.compare(
                    popularityCache.getOrDefault(b.getIdDestination(), 0),
                    popularityCache.getOrDefault(a.getIdDestination(), 0)
            ));
        }
        sorted.forEach(d -> flowDestinations.getChildren().add(createCard(d)));
    }

    private VBox createCard(Destination d) {
        boolean expired  = d.getDateDepart() != null && d.getDateDepart().isBefore(LocalDate.now());
        boolean isActive = d.getStatut() && !expired;

        ImageView iv = new ImageView(new Image(getFirstImageUrl(d), true));
        iv.setFitWidth(260); iv.setFitHeight(160); iv.setPreserveRatio(false);
        iv.getStyleClass().add("dest-card-image");
        if (!isActive) iv.setStyle("-fx-opacity:0.45;");

        Label badge = new Label(d.getPays());
        badge.getStyleClass().add("dest-badge");

        Label statusBadge = new Label(isActive ? "✅ Actif" : "❌ " + (!d.getStatut() ? "Désactivé" : "Expiré"));
        statusBadge.setStyle("-fx-background-color:" + (isActive ? "#27ae60" : "#e74c3c")
                + ";-fx-text-fill:white;-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-padding:3 8 3 8;-fx-background-radius:20;");

        // Image container — seulement pays + statut, PAS le badge popularité
        StackPane imgContainer = new StackPane(iv, badge, statusBadge);
        StackPane.setAlignment(badge,       Pos.TOP_LEFT);
        StackPane.setAlignment(statusBadge, Pos.TOP_RIGHT);
        imgContainer.getStyleClass().add("dest-image-wrap");

        Label name = new Label(d.getNom());
        name.getStyleClass().add("dest-card-title");
        if (!isActive) name.setStyle("-fx-opacity:0.6;");

        Label saison = new Label("🌤️ " + (d.getMeilleureSaison() != null ? d.getMeilleureSaison() : "-"));
        saison.setStyle("-fx-font-size:11px;-fx-text-fill:" + (isActive ? "#888" : "#bbb") + ";");

        HBox datesBox = new HBox(10);
        if (d.getDateDepart() != null && d.getDateArrivee() != null) {
            Label dates = new Label("📅 " + d.getDateDepart() + "  →  " + d.getDateArrivee());
            dates.setStyle("-fx-font-size:11px;-fx-text-fill:" + (isActive ? "#888" : "#bbb") + ";");
            datesBox.getChildren().add(dates);
        }

        // Badge popularité SOUS l'image, au-dessus du nom
        VBox card;
        int score = popularityCache.getOrDefault(d.getIdDestination(), -1);
        if (isActive && score >= 0) {
            HBox popularityRow = createPopularityRow(score);
            card = new VBox(imgContainer, popularityRow, name, saison, datesBox);
        } else if (isActive) {
            Label loading = new Label("⏳ Calcul du score...");
            loading.setStyle("-fx-font-size:11px;-fx-text-fill:#aaa;-fx-padding:2 0 0 2;");
            card = new VBox(imgContainer, loading, name, saison, datesBox);
        } else {
            card = new VBox(imgContainer, name, saison, datesBox);
        }

        card.setSpacing(8);
        card.getStyleClass().addAll("glass-card", "dest-card");
        if (!isActive) card.setStyle("-fx-opacity:0.75;-fx-cursor:default;");
        else           card.setOnMouseClicked(e -> openDetails(d));
        return card;
    }

    /**
     * Ligne horizontale compacte de popularité (score + barre + label) — hors image.
     */
    private HBox createPopularityRow(int score) {
        String color;
        String label;
        String emoji;
        if (score >= 80)      { color = "#e74c3c"; label = "Très populaire"; emoji = "🔥"; }
        else if (score >= 60) { color = "#e67e22"; label = "Populaire";       emoji = "⭐"; }
        else if (score >= 40) { color = "#27ae60"; label = "Tendance";        emoji = "📈"; }
        else if (score >= 20) { color = "#3498db"; label = "À découvrir";     emoji = "🌍"; }
        else                  { color = "#95a5a6"; label = "Peu visité";      emoji = "🔍"; }

        Label scoreLabel = new Label(emoji + " " + score + "/100");
        scoreLabel.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");

        // Barre de progression
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
    // CHATBOT — SETUP
    // ==============================
    private void setupChatbot() {
        chatPanel.setVisible(false);
        chatPanel.setManaged(false);
        btnToggleChat.setOnAction(e -> toggleChat());
        btnEnvoyer.setOnAction(e -> envoyerMessage());
        inputMessage.setOnAction(e -> envoyerMessage());
        addBotMessage("👋 Bonjour ! Je suis votre assistant voyage.\n\nExemples :\n• Quelle destination pour l'été ?\n• Destination moins de 500 TND ?\n• Destination en Europe ?");
    }

    private void toggleChat() {
        chatVisible = !chatVisible;
        chatPanel.setVisible(chatVisible);
        chatPanel.setManaged(chatVisible);
        btnToggleChat.setText(chatVisible ? "✕ Fermer chat" : "💬 Assistant Voyage");
    }

    @FXML private void suggestionEte()    { inputMessage.setText("Quelle destination pour l'été ?");   envoyerMessage(); }
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
                    addBotMessage("❌ Erreur : " + ex.getMessage());
                    btnEnvoyer.setDisable(false);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ==============================
    // CONTEXTE DB
    // ==============================
    private String buildContext() {
        StringBuilder sb = new StringBuilder("Destinations disponibles :\n");
        for (Destination d : allDestinations) {
            boolean expired = d.getDateDepart() != null && d.getDateDepart().isBefore(LocalDate.now());
            if (!d.getStatut() || expired) continue;
            int score = popularityCache.getOrDefault(d.getIdDestination(), -1);
            sb.append("• ").append(d.getNom())
                    .append(" | Pays: ").append(d.getPays())
                    .append(" | Saison: ").append(d.getMeilleureSaison())
                    .append(" | Prix: ").append(d.getPrix()).append(" TND");
            if (score >= 0) sb.append(" | Popularité: ").append(score).append("/100");
            if (d.getDateDepart() != null) sb.append(" | Départ: ").append(d.getDateDepart());
            sb.append("\n");
        }
        return sb.toString();
    }

    // ==============================
    // APPELS GROQ
    // ==============================
    private String callGroqChat(String userMessage, String context) throws Exception {
        URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        String system = "Tu es un assistant voyage. Réponds en français. "
                + "Aide les clients à choisir une destination. "
                + "Donne des réponses complètes. Ne tronque pas.\n" + context;

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
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                result.append(c);
            }
        }
        return result.toString()
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("\\*(.*?)\\*", "$1")
                .trim();
    }

    // ==============================
    // MATCHING DESTINATIONS (CHAT)
    // ==============================
    private List<Destination> findMatchingDestinations(String userMessage, String botResponse) {
        String lowerMsg  = userMessage.toLowerCase();
        String lowerResp = botResponse.toLowerCase();

        return allDestinations.stream().filter(d -> {
            boolean expired = d.getDateDepart() != null && d.getDateDepart().isBefore(LocalDate.now());
            if (!d.getStatut() || expired) return false;

            String nom    = d.getNom()             != null ? d.getNom().toLowerCase()             : "";
            String pays   = d.getPays()            != null ? d.getPays().toLowerCase()            : "";
            String saison = d.getMeilleureSaison() != null ? d.getMeilleureSaison().toLowerCase() : "";

            boolean inBotResponse = (!nom.isEmpty()  && lowerResp.contains(nom))
                    || (!pays.isEmpty() && lowerResp.contains(pays));
            boolean saisonMatch   = false;
            if (lowerMsg.contains("été") || lowerMsg.contains("ete")) saisonMatch = saison.contains("été") || saison.contains("ete");
            if (lowerMsg.contains("hiver"))                            saisonMatch = saison.contains("hiver");
            if (lowerMsg.contains("printemps"))                        saisonMatch = saison.contains("printemps");
            if (lowerMsg.contains("automne"))                          saisonMatch = saison.contains("automne");

            boolean regionMatch = false;
            if (lowerMsg.contains("europe"))  regionMatch = isEuropean(pays);
            if (lowerMsg.contains("asie"))    regionMatch = isAsian(pays);
            if (lowerMsg.contains("afrique")) regionMatch = isAfrican(pays);

            boolean budgetMatch = false;
            Matcher m = Pattern.compile("(\\d+)\\s*(tnd|dt|dinar)?").matcher(lowerMsg);
            if (m.find()) {
                try {
                    double budget = Double.parseDouble(m.group(1));
                    budgetMatch = d.getPrix() > 0 && d.getPrix() <= budget;
                } catch (NumberFormatException ignored) {}
            }

            boolean directMatch = (!nom.isEmpty()  && lowerMsg.contains(nom))
                    || (!pays.isEmpty() && lowerMsg.contains(pays));

            return inBotResponse || saisonMatch || regionMatch || budgetMatch || directMatch;
        }).limit(4).collect(Collectors.toList());
    }

    private boolean isEuropean(String pays) {
        String p = pays.toLowerCase();
        return p.contains("france") || p.contains("italie") || p.contains("espagne")
                || p.contains("portugal") || p.contains("grèce") || p.contains("allemagne")
                || p.contains("suisse") || p.contains("belgique") || p.contains("autriche")
                || p.contains("pays-bas") || p.contains("pologne") || p.contains("turquie")
                || p.contains("croatie") || p.contains("hongrie") || p.contains("suède");
    }

    private boolean isAsian(String pays) {
        String p = pays.toLowerCase();
        return p.contains("japon") || p.contains("chine") || p.contains("thaïlande")
                || p.contains("bali") || p.contains("inde") || p.contains("vietnam")
                || p.contains("indonésie") || p.contains("corée") || p.contains("malaisie")
                || p.contains("singapour") || p.contains("philippines") || p.contains("cambodge");
    }

    private boolean isAfrican(String pays) {
        String p = pays.toLowerCase();
        return p.contains("tunisie") || p.contains("maroc") || p.contains("egypte")
                || p.contains("afrique") || p.contains("kenya") || p.contains("sénégal")
                || p.contains("algérie") || p.contains("tanzanie") || p.contains("madagascar");
    }

    // ==============================
    // SUGGESTIONS DANS CHAT
    // ==============================
    private void addDestinationSuggestions(List<Destination> destinations) {
        Label titre = new Label("✈️ Destinations suggérées :");
        titre.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#555;-fx-padding:6 8 2 8;");
        chatMessages.getChildren().add(titre);
        for (Destination d : destinations) chatMessages.getChildren().add(createChatCard(d));
        scrollToBottom();
    }

    private VBox createChatCard(Destination d) {
        ImageView iv = new ImageView(new Image(getFirstImageUrl(d), 220, 110, false, true, true));
        iv.setFitWidth(220); iv.setFitHeight(110); iv.setPreserveRatio(false);

        Label name = new Label("📍 " + d.getNom());
        name.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:#333;");
        name.setWrapText(true); name.setMaxWidth(210);

        Label info = new Label("🌍 " + d.getPays() + "   🌤️ " + (d.getMeilleureSaison() != null ? d.getMeilleureSaison() : "-"));
        info.setStyle("-fx-font-size:11px;-fx-text-fill:#666;");

        HBox datesBox = new HBox();
        if (d.getDateDepart() != null && d.getDateArrivee() != null) {
            Label dates = new Label("📅 " + d.getDateDepart() + " → " + d.getDateArrivee());
            dates.setStyle("-fx-font-size:10px;-fx-text-fill:#888;");
            datesBox.getChildren().add(dates);
        }

        Label prix = new Label("💰 " + (d.getPrix() > 0 ? d.getPrix() + " TND" : "N/A"));
        prix.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#c8a96e;");

        // Score popularité dans la carte chat
        int score = popularityCache.getOrDefault(d.getIdDestination(), -1);
        HBox scoreRow = new HBox();
        if (score >= 0) {
            Label scoreLbl = new Label(getScoreEmoji(score) + " Popularité : " + score + "/100");
            scoreLbl.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + getScoreColor(score) + ";");
            scoreRow.getChildren().add(scoreLbl);
        }

        Button btnVoir = new Button("Voir les détails →");
        btnVoir.setStyle("-fx-background-color:#c8a96e;-fx-text-fill:white;-fx-font-size:11px;"
                + "-fx-background-radius:20;-fx-cursor:hand;-fx-padding:5 12 5 12;");
        btnVoir.setOnAction(e -> openDetails(d));

        VBox card = new VBox(6, iv, name, info, datesBox, prix, scoreRow, btnVoir);
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;"
                + "-fx-padding:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.12),8,0,0,2);");
        card.setPrefWidth(240); card.setMaxWidth(240);
        card.setAlignment(Pos.CENTER_LEFT);

        HBox wrapper = new HBox(card);
        wrapper.setPadding(new Insets(4, 40, 4, 8));
        wrapper.setAlignment(Pos.CENTER_LEFT);
        return new VBox(wrapper);
    }

    private String getScoreEmoji(int score) {
        if (score >= 80) return "🔥";
        if (score >= 60) return "⭐";
        if (score >= 40) return "📈";
        if (score >= 20) return "🌍";
        return "🔍";
    }

    private String getScoreColor(int score) {
        if (score >= 80) return "#e74c3c";
        if (score >= 60) return "#e67e22";
        if (score >= 40) return "#27ae60";
        if (score >= 20) return "#3498db";
        return "#95a5a6";
    }

    // ==============================
    // BULLES DE CHAT
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
        HBox hbox = new HBox(6, new Label("✈️"), label);
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
        btnReset.setOnAction(e -> { searchField.clear(); comboSaison.getSelectionModel().selectFirst(); });
    }

    private void applyFilter() {
        String kw = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String s  = comboSaison.getValue();
        renderDestinations(allDestinations.stream().filter(d ->
                (kw.isEmpty() || d.getNom().toLowerCase().contains(kw) || d.getPays().toLowerCase().contains(kw)) &&
                        (s == null || s.equals("Toutes les saisons") || s.equalsIgnoreCase(d.getMeilleureSaison()))
        ).collect(Collectors.toList()));
    }

    // ==============================
    // UTILITAIRES
    // ==============================
    private String getFirstImageUrl(Destination d) {
        try {
            List<DestinationImage> imgs = imageService.getImagesByDestination(d.getIdDestination());
            if (imgs != null && !imgs.isEmpty()) {
                File f = new File(imgs.get(0).getUrlImage());
                if (f.exists()) return f.toURI().toString();
            }
        } catch (Exception ignored) {}
        return "https://via.placeholder.com/300x200";
    }

    private void openDetails(Destination destination) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientDetailsView.fxml"));
            Parent root = loader.load();
            ClientDetailsController ctrl = loader.getController();
            ctrl.setDestination(destination);
            Stage stage = (Stage) flowDestinations.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 900);
            SceneUtil.applyCss(scene);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupSidebar() {
        navDashboard.setOnAction(e -> openView("/AdminDashboard.fxml"));
        navDestinations.setOnAction(e -> openView("/ClientDestinationListView.fxml"));
        navTransports.setOnAction(e -> openView("/AdminTransportView.fxml"));
        navImages.setOnAction(e -> openView("/AdminImageView.fxml"));
        navClient.setOnAction(e -> openView("/ClientDestinationListView.fxml"));
    }

    private void openView(String fxml) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            Stage stage = (Stage) flowDestinations.getScene().getWindow();
            SceneUtil.setScene(stage, root, 1200, 800);
        } catch (Exception e) { e.printStackTrace(); }
    }
}