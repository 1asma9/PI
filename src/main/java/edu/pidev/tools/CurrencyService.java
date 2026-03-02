package edu.pidev.tools;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurrencyService {

    private static final String API_URL = "http://localhost/pidev_api/rates.php";

    private final HttpClient client = HttpClient.newHttpClient();

    public double getRate(String from, String to) throws Exception {
        if (from == null || to == null) return 1.0;
        if (from.equalsIgnoreCase(to)) return 1.0;

        String url = API_URL + "?from=" + from.toUpperCase() + "&to=" + to.toUpperCase();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Rates API error: " + response.body());
        }

        String body = response.body(); // {"from":"TND","to":"EUR","rate":0.29}

        int idx = body.indexOf("\"rate\":");
        if (idx == -1) throw new RuntimeException("Invalid JSON: " + body);

        String rateStr = body.substring(idx + 7)
                .replaceAll("[^0-9.\\-E]", "");

        return Double.parseDouble(rateStr);
    }
}