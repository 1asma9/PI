package edu.connexion3a8.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ModerationService {

    private static final String API_URL = "https://www.purgomalum.com/service/json";

    /**
     * Vérifie et censure les bad words dans un texte
     * @param text Texte à vérifier
     * @return Texte censuré (bad words remplacés par ***)
     */
    public String filterBadWords(String text) {
        try {
            // Encoder le texte pour l'URL
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

            // Construire l'URL de l'API
            String urlString = API_URL + "?text=" + encodedText;

            // Faire l'appel HTTP
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Lire la réponse
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parser la réponse JSON
            String jsonResponse = response.toString();
            String filteredText = jsonResponse
                    .replaceAll("\\{\"result\":\"", "")
                    .replaceAll("\"\\}", "");

            return filteredText;

        } catch (Exception e) {
            e.printStackTrace();
            return text; // Retourner le texte original en cas d'erreur
        }
    }

    /**
     * Vérifie si le texte contient des bad words
     * @param text Texte à vérifier
     * @return true si contient des bad words
     */
    public boolean containsBadWords(String text) {
        String filteredText = filterBadWords(text);
        return !text.equals(filteredText);
    }

    /**
     * Compte le nombre de bad words dans un texte
     * @param text Texte à analyser
     * @return Nombre de bad words détectés
     */
    public int countBadWords(String text) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String urlString = API_URL + "?text=" + encodedText + "&fill_text=***&fill_char=*";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String filteredText = response.toString()
                    .replaceAll("\\{\"result\":\"", "")
                    .replaceAll("\"\\}", "");

            // Compter les occurrences de ***
            int count = 0;
            int index = 0;
            while ((index = filteredText.indexOf("***", index)) != -1) {
                count++;
                index += 3;
            }

            return count;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
