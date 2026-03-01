package edu.connexion3a8.services;

import edu.connexion3a8.entities.Commentaire;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SentimentAnalysisService {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_KEY = "VOTRE_CLE_CLAUDE_API";

    /**
     * Analyse le sentiment d'un commentaire
     * @param text Texte à analyser
     * @return Sentiment (POSITIVE, NEGATIVE, NEUTRAL)
     */
    public Sentiment analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Sentiment.NEUTRAL;
        }

        // Fallback to local heuristic when API key is not configured.
        if (API_KEY == null || API_KEY.trim().isEmpty() || API_KEY.contains("VOTRE_CLE")) {
            return analyzeSentimentHeuristic(text);
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "claude-sonnet-4-20250514");
            requestBody.put("max_tokens", 100);

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content",
                    "Analyse le sentiment de ce texte et réponds UNIQUEMENT par un seul mot : " +
                            "POSITIVE, NEGATIVE, ou NEUTRAL. Texte : " + text
            );
            messages.put(message);
            requestBody.put("messages", messages);

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("x-api-key", API_KEY);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            os.close();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            String sentimentText = jsonResponse
                    .getJSONArray("content")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
                    .toUpperCase();

            return Sentiment.valueOf(sentimentText);

        } catch (Exception e) {
            System.err.println("Sentiment API error, using fallback: " + e.getMessage());
            return analyzeSentimentHeuristic(text);
        }
    }

    /**
     * Analyse tous les commentaires d'un blog
     */
    public BlogSentimentScore analyzeBlogComments(List<Commentaire> comments) {
        int positive = 0;
        int negative = 0;
        int neutral = 0;

        for (Commentaire comment : comments) {
            Sentiment sentiment = analyzeSentiment(comment.getContenu());

            switch (sentiment) {
                case POSITIVE:
                    positive++;
                    break;
                case NEGATIVE:
                    negative++;
                    break;
                case NEUTRAL:
                    neutral++;
                    break;
            }
        }

        BlogSentimentScore score = new BlogSentimentScore();
        score.setPositive(positive);
        score.setNegative(negative);
        score.setNeutral(neutral);
        score.setTotal(comments.size());

        // Calculer le score global (0-100)
        if (comments.size() > 0) {
            int globalScore = (positive * 100 + neutral * 50) / comments.size();
            score.setGlobalScore(globalScore);
        }

        return score;
    }

    private Sentiment analyzeSentimentHeuristic(String text) {
        String lower = text.toLowerCase();

        String[] positiveWords = {
                "good", "great", "excellent", "amazing", "awesome", "love", "liked", "helpful", "nice",
                "perfect", "super", "fantastic", "beautiful", "interesting", "merci", "bravo", "top"
        };
        String[] negativeWords = {
                "bad", "terrible", "awful", "hate", "boring", "poor", "worst", "useless", "slow",
                "problem", "bug", "nul", "mauvais", "déçu", "decu"
        };

        int positive = 0;
        int negative = 0;

        for (String word : positiveWords) {
            if (lower.contains(word)) {
                positive++;
            }
        }
        for (String word : negativeWords) {
            if (lower.contains(word)) {
                negative++;
            }
        }

        if (positive > negative) return Sentiment.POSITIVE;
        if (negative > positive) return Sentiment.NEGATIVE;
        return Sentiment.NEUTRAL;
    }
}

// Enum pour les sentiments
enum Sentiment {
    POSITIVE, NEGATIVE, NEUTRAL
}

