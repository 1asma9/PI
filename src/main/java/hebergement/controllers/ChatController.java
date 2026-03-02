package hebergement.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatController {

    // ⚙️ CHANGE UNIQUEMENT CETTE URL
    private static final String N8N_WEBHOOK_URL =
            "https://app-cabin-ray-instrumentation.trycloudflare.com/api/reservations";

    @FXML private ListView<String> lvChat;
    @FXML private TextField tfMessage;

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("d/M/yyyy");

    // Etat mémorisé entre messages
    private String    lastVille   = null;
    private DateRange lastDr      = null;
    private Integer   lastPersons = null;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    public void initialize() {
        lvChat.setItems(FXCollections.observableArrayList());
        addBot("Salut 👋 Dis-moi ta demande de réservation.\n" +
                "Ex: \"réserver à Tunis du 10 au 12 pour 2 personnes\"\n" +
                "Puis tape \"confirmer\" pour valider automatiquement.");

        tfMessage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) { onSend(); e.consume(); }
        });
    }

    @FXML
    private void onSend() {
        String msg = tfMessage.getText();
        if (msg == null || msg.trim().isEmpty()) return;
        msg = msg.trim();
        addUser(msg);
        tfMessage.clear();
        addBot(handleMessage(msg));
    }

    private String handleMessage(String raw) {
        String t = normalize(raw);

        // --- CONFIRMATION ---
        if (t.contains("confirmer") || t.contains("valider") || t.equals("oui")) {
            if (lastVille == null && lastDr == null) {
                return "⚠️ Rien à confirmer. Décris d'abord ville, dates, personnes.";
            }
            sendReservationToN8n();
            return "⏳ Envoi en cours...";
        }

        // --- EXTRACTION ---
        String    ville   = extractVille(t);
        DateRange dr      = extractDates(t);
        Integer   persons = extractPersons(t);

        if (ville   != null) lastVille   = ville;
        if (dr      != null) lastDr      = dr;
        if (persons != null) lastPersons = persons;

        if (lastVille == null && lastDr == null && lastPersons == null) {
            return "Je n'ai pas compris 😅\nEssaie: \"réserver à Tunis du 10 au 12 pour 2 personnes\"";
        }

        StringBuilder sb = new StringBuilder("✅ J'ai compris:\n");
        sb.append(lastVille   != null ? "• Ville     : " + capitalize(lastVille) + "\n"
                : "• Ville     : ❓ manquante\n");
        if (lastDr != null)
            sb.append("• Dates     : ").append(lastDr.start.format(df))
                    .append(" → ").append(lastDr.end.format(df))
                    .append(" (").append(lastDr.nights).append(" nuits)\n");
        else
            sb.append("• Dates     : ❓ manquantes\n");
        sb.append(lastPersons != null ? "• Personnes : " + lastPersons + "\n"
                : "• Personnes : ❓ manquant\n");

        sb.append(lastVille != null && lastDr != null
                ? "\n👉 Tape \"confirmer\" pour envoyer !"
                : "\n👉 Complète les infos manquantes.");

        return sb.toString();
    }

    // =============================================
    // ENVOI HTTP vers n8n
    // =============================================
    private void sendReservationToN8n() {
        String json = String.format(
                "{\"ville\":\"%s\",\"dateArrivee\":\"%s\",\"dateDepart\":\"%s\",\"personnes\":%d}",
                lastVille   != null ? lastVille : "inconnue",
                lastDr      != null ? lastDr.start.format(DateTimeFormatter.ISO_LOCAL_DATE) : "",
                lastDr      != null ? lastDr.end.format(DateTimeFormatter.ISO_LOCAL_DATE)   : "",
                lastPersons != null ? lastPersons : 1
        );

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(N8N_WEBHOOK_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> javafx.application.Platform.runLater(() -> {
                    if (resp.statusCode() == 200) {
                        addBot("🏨 Réponse n8n:\n" + parseN8nMessage(resp.body()));
                        lastVille = null; lastDr = null; lastPersons = null;
                    } else {
                        addBot("❌ Erreur " + resp.statusCode() + ": " + resp.body());
                    }
                }))
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() ->
                            addBot("❌ Connexion impossible: " + ex.getMessage())
                    );
                    return null;
                });
    }

    private String parseN8nMessage(String json) {
        Matcher m = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        if (m.find()) return m.group(1);
        Matcher m2 = Pattern.compile("\"status\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        if (m2.find()) return "Statut: " + m2.group(1);
        return json;
    }

    // =============================================
    // UI
    // =============================================
    private void addUser(String msg) { lvChat.getItems().add("👤 Moi: " + msg); lvChat.scrollTo(lvChat.getItems().size()-1); }
    private void addBot(String msg)  { lvChat.getItems().add("🤖 Assistant: " + msg); lvChat.scrollTo(lvChat.getItems().size()-1); }

    // =============================================
    // NLP
    // =============================================
    private String normalize(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("['']"," ").replaceAll("\\s+"," ").trim();
    }
    private String extractVille(String t) {
        Matcher m = Pattern.compile("\\b(?:a|à)\\s+([a-zA-Z\\-]+(?:\\s+[a-zA-Z\\-]+){0,2})\\b").matcher(t);
        if (m.find()) {
            String v = m.group(1).replaceAll("\\b(du|de|pour|le|la|les|au)\\b.*$","").trim();
            return v.isEmpty() ? null : v;
        }
        return null;
    }
    private Integer extractPersons(String t) {
        Matcher m = Pattern.compile("\\b(\\d{1,2})\\s*(?:personnes?|pers)\\b").matcher(t);
        if (m.find()) return Integer.parseInt(m.group(1));
        Matcher m2 = Pattern.compile("\\bpour\\s+(\\d{1,2})\\b").matcher(t);
        if (m2.find()) return Integer.parseInt(m2.group(1));
        return null;
    }
    private DateRange extractDates(String t) {
        Matcher m = Pattern.compile("\\bdu\\s+(\\d{1,2})\\s+au\\s+(\\d{1,2})(?:\\s+(janvier|fevrier|février|mars|avril|mai|juin|juillet|aout|août|septembre|octobre|novembre|decembre|décembre))?").matcher(t);
        if (!m.find()) return null;
        int d1 = Integer.parseInt(m.group(1)), d2 = Integer.parseInt(m.group(2));
        String ms = m.group(3);
        LocalDate now = LocalDate.now();
        int month = ms == null ? now.getMonthValue() : moisToInt(ms);
        try {
            LocalDate s = LocalDate.of(now.getYear(), month, d1);
            LocalDate e = LocalDate.of(now.getYear(), month, d2);
            if (e.isBefore(s)) e = e.plusMonths(1);
            long n = java.time.temporal.ChronoUnit.DAYS.between(s, e);
            return new DateRange(s, e, (int)(n <= 0 ? 1 : n));
        } catch (Exception ex) { return null; }
    }
    private int moisToInt(String m) {
        return switch(m.toLowerCase(Locale.ROOT)) {
            case "janvier" -> 1; case "fevrier","février" -> 2; case "mars" -> 3;
            case "avril" -> 4; case "mai" -> 5; case "juin" -> 6; case "juillet" -> 7;
            case "aout","août" -> 8; case "septembre" -> 9; case "octobre" -> 10;
            case "novembre" -> 11; case "decembre","décembre" -> 12;
            default -> LocalDate.now().getMonthValue();
        };
    }
    private static class DateRange {
        LocalDate start, end; int nights;
        DateRange(LocalDate s, LocalDate e, int n){start=s;end=e;nights=n;}
    }
    private String capitalize(String s) {
        return (s==null||s.isEmpty()) ? s : s.substring(0,1).toUpperCase()+s.substring(1);
    }
}