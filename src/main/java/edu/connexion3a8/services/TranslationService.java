package edu.connexion3a8.services;

import edu.connexion3a8.entities.Blog;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TranslationService {

    // MyMemory Translation API - FREE, NO API KEY REQUIRED
    // Supports: EN, FR, AR, ES, DE, IT, and 50+ more languages
    // Limit: 1000 words/day (enough for testing), unlimited with free email registration
    private static final String API_URL = "https://api.mymemory.translated.net/get";

    // Language code mapping
    private String getLanguageCode(String code) {
        switch (code.toUpperCase()) {
            case "EN": return "en";
            case "FR": return "fr";
            case "AR": return "ar";
            case "ES": return "es";
            case "DE": return "de";
            case "IT": return "it";
            default: return code.toLowerCase();
        }
    }

    /**
     * Translates text using MyMemory FREE API (no key needed)
     */
    public String translate(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Limit text length (max 500 chars per request for free tier)
        String textToTranslate = text;
        if (text.length() > 500) {
            // Split and translate in chunks
            return translateLongText(text, targetLanguage);
        }

        try {
            String langCode = getLanguageCode(targetLanguage);

            // Build URL with query parameters
            String encodedText = URLEncoder.encode(textToTranslate, StandardCharsets.UTF_8);
            String urlString = API_URL + "?q=" + encodedText + "&langpair=autodetect|" + langCode;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Parse MyMemory JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONObject responseData = jsonResponse.getJSONObject("responseData");
                    String translatedText = responseData.getString("translatedText");

                    // Check for errors in response
                    if (translatedText.contains("MYMEMORY WARNING") ||
                            translatedText.contains("PLEASE SELECT TWO DISTINCT LANGUAGES")) {
                        return text; // Return original if error
                    }

                    return translatedText;
                }
            } else {
                System.err.println("Translation API error: " + responseCode);
                return text; // Return original on error
            }

        } catch (Exception e) {
            System.err.println("Translation error: " + e.getMessage());
            return text; // Return original on error
        }
    }

    /**
     * Translates long text by splitting into chunks
     */
    private String translateLongText(String text, String targetLanguage) {
        StringBuilder result = new StringBuilder();
        int chunkSize = 450; // Safe chunk size

        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, text.length());
            String chunk = text.substring(i, end);

            // Try to break at sentence or word boundary
            if (end < text.length() && !Character.isWhitespace(text.charAt(end))) {
                int lastSpace = chunk.lastIndexOf(' ');
                if (lastSpace > chunkSize / 2) {
                    chunk = text.substring(i, i + lastSpace);
                    i = i + lastSpace - chunkSize; // Adjust for next iteration
                }
            }

            String translated = translate(chunk.trim(), targetLanguage);
            result.append(translated).append(" ");

            // Small delay to avoid rate limiting
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }

        return result.toString().trim();
    }

    /**
     * Traduit un blog complet avec gestion d'erreur
     */
    public BlogTranslation translateBlog(Blog blog, String targetLanguage) {
        BlogTranslation translation = new BlogTranslation();

        System.out.println("Traduction du blog en cours vers: " + targetLanguage);

        // Traduire le titre
        String titre = translate(blog.getTitre(), targetLanguage);
        translation.setTitre(titre);

        // Traduire l'extrait
        String extrait = translate(blog.getExtrait(), targetLanguage);
        translation.setExtrait(extrait);

        // Traduire le contenu (limité pour éviter les timeouts)
        String contenu = translate(blog.getContenu(), targetLanguage);
        translation.setContenu(contenu);

        translation.setLanguage(targetLanguage);

        System.out.println("Traduction terminée");

        return translation;
    }
}