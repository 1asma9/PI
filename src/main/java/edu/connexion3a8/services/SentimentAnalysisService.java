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
            e.printStackTrace();
            return Sentiment.NEUTRAL;
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
}

// Enum pour les sentiments
enum Sentiment {
    POSITIVE, NEGATIVE, NEUTRAL
}

