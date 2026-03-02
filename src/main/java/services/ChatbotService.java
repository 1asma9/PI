package services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ChatbotService {

    // ⚠️ METTEZ VOTRE CLÉ API ICI
    private static final String API_KEY = "AIzaSyCksjaGtKNgfX8BUAcNr9j0n7GWE0T4PTA";

    // ✅ MODÈLE CORRIGÉ
    private static final String MODEL = "gemini-pro";

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
            + MODEL + ":generateContent?key=" + API_KEY;

    private final OkHttpClient client;
    private final Gson gson;
    private String chatbotType;
    private StringBuilder historique;

    public ChatbotService(String type) {
        this.chatbotType = type;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.historique = new StringBuilder();

        // Ajouter le system prompt au début
        historique.append(creerSystemPrompt()).append("\n\n");
    }

    public String envoyerMessage(String messageUtilisateur) throws IOException {
        // Ajouter le message à l'historique
        historique.append("Utilisateur: ").append(messageUtilisateur).append("\n");

        // Créer le corps de la requête
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();

        JsonObject message = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", historique.toString());
        parts.add(textPart);
        message.add("parts", parts);

        contents.add(message);
        requestBody.add("contents", contents);

        // Faire l'appel API
        RequestBody body = RequestBody.create(
                gson.toJson(requestBody),
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("Erreur API (" + response.code() + "): " + errorBody);
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            // Extraire la réponse
            String geminiResponse = jsonResponse
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            // Ajouter la réponse à l'historique
            historique.append("Assistant: ").append(geminiResponse).append("\n");

            return geminiResponse;
        }
    }

    private String creerSystemPrompt() {
        if (chatbotType.equals("reclamation")) {
            return """
                    Tu es un assistant virtuel pour créer des RÉCLAMATIONS.

                    RÈGLES STRICTES:
                    1. Tu aides UNIQUEMENT à créer des réclamations
                    2. Si l'utilisateur parle d'autre chose (football, météo, etc.), tu REFUSES poliment
                    3. Tu poses les questions UNE PAR UNE

                    PROCESSUS:
                    1. Saluer et demander le TITRE
                    2. Demander la DESCRIPTION
                    3. Afficher le récapitulatif avec ce format EXACT:

                    [CONFIRMATION]
                    TITRE: [le titre donné par l'utilisateur]
                    DESCRIPTION: [la description donnée par l'utilisateur]
                    [/CONFIRMATION]

                    EXEMPLE DE REFUS:
                    Si l'utilisateur demande "Parle-moi de football", tu réponds:
                    "Je suis désolé, mais je suis spécialisé uniquement dans la création de réclamations.
                    Je ne peux pas discuter de football. Puis-je vous aider avec une réclamation ?"
                    """;
        } else {
            return """
                    Tu es un assistant virtuel pour créer des AVIS.

                    RÈGLES STRICTES:
                    1. Tu aides UNIQUEMENT à créer des avis
                    2. Si l'utilisateur parle d'autre chose, tu REFUSES poliment
                    3. Tu poses les questions UNE PAR UNE

                    PROCESSUS:
                    1. Saluer et demander la NOTE (1-5)
                    2. Demander le COMMENTAIRE
                    3. Afficher le récapitulatif avec ce format EXACT:

                    [CONFIRMATION]
                    NOTE: [la note donnée par l'utilisateur]
                    COMMENTAIRE: [le commentaire donné par l'utilisateur]
                    [/CONFIRMATION]

                    EXEMPLE DE REFUS:
                    Si l'utilisateur demande "Qui a gagné le match ?", tu réponds:
                    "Je suis désolé, mais je suis spécialisé uniquement dans la création d'avis.
                    Je ne peux pas discuter de football. Puis-je vous aider à laisser un avis ?"
                    """;
        }
    }

    public void reinitialiser() {
        historique = new StringBuilder();
        historique.append(creerSystemPrompt()).append("\n\n");
    }

    public boolean estConfirmation(String message) {
        return message.contains("[CONFIRMATION]") && message.contains("[/CONFIRMATION]");
    }

    public String[] extraireInfos(String message) {
        String[] infos = new String[2];

        try {
            if (chatbotType.equals("reclamation")) {
                // Extraire TITRE
                int titreStart = message.indexOf("TITRE:") + 6;
                int titreEnd = message.indexOf("\n", titreStart);
                if (titreEnd == -1) {
                    titreEnd = message.indexOf("DESCRIPTION:");
                }
                infos[0] = message.substring(titreStart, titreEnd).trim();

                // Extraire DESCRIPTION
                int descStart = message.indexOf("DESCRIPTION:") + 12;
                int descEnd = message.indexOf("[/CONFIRMATION]");
                infos[1] = message.substring(descStart, descEnd).trim();

            } else {
                // Extraire NOTE
                int noteStart = message.indexOf("NOTE:") + 5;
                int noteEnd = message.indexOf("\n", noteStart);
                if (noteEnd == -1) {
                    noteEnd = message.indexOf("COMMENTAIRE:");
                }
                infos[0] = message.substring(noteStart, noteEnd).trim();

                // Extraire COMMENTAIRE
                int commStart = message.indexOf("COMMENTAIRE:") + 12;
                int commEnd = message.indexOf("[/CONFIRMATION]");
                infos[1] = message.substring(commStart, commEnd).trim();
            }
        } catch (Exception e) {
            System.err.println("Erreur extraction: " + e.getMessage());
            infos[0] = "Titre par défaut";
            infos[1] = "Description par défaut";
        }

        return infos;
    }
}
