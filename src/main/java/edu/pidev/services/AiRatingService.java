package edu.pidev.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.pidev.entities.Activite;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AiRatingService {

    private static final String URL = "http://localhost/pidev_api/rate_activity.php";

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * @return rating between 0..5 if ok, or -1 if AI failed
     */
    public static double rate(Activite a) {
        try {
            String json = """
                {
                  "nom": "%s",
                  "description": "%s",
                  "type": "%s",
                  "prix": %s,
                  "duree": %d,
                  "lieu": "%s"
                }
                """.formatted(
                    esc(a.getNom()),
                    esc(a.getDescription()),
                    esc(a.getType()),
                    // avoid 90.000000 formatting issues
                    String.valueOf(a.getPrix()).replace(",", "."),
                    a.getDuree(),
                    esc(a.getLieu())
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("❌ AI HTTP " + response.statusCode() + " body=" + response.body());
                return -1;
            }

            JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();

            // ✅ expects: {"ok":true,"rating":4.6}
            if (obj.has("ok") && obj.get("ok").getAsBoolean()) {
                if (obj.has("rating") && !obj.get("rating").isJsonNull()) {
                    double r = obj.get("rating").getAsDouble();
                    // clamp
                    if (r < 0) r = 0;
                    if (r > 5) r = 5;
                    return r;
                }
            } else {
                // show server error details if present
                if (obj.has("error")) {
                    System.out.println("❌ AI error: " + obj.get("error"));
                }
                if (obj.has("details")) {
                    System.out.println("❌ AI details: " + obj.get("details"));
                }
            }

            return -1;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        // prevent JSON breaking with quotes/newlines
        return s.replace("\\", "\\\\")
                .replace("\"", "'")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}