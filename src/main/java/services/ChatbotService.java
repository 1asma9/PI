package services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ChatbotService {

    // API Ollama locale
    private static final String API_URL = "http://localhost:11434/api/generate";

    // Modèle à utiliser (changez selon celui que vous avez téléchargé)
    private static final String MODEL = "llama3.2"; // ou "mistral" ou "llama3"

    private final OkHttpClient client;
    private final Gson gson;
    private String chatbotType;
    private StringBuilder historique;

    public ChatbotService(String type) {
        this.chatbotType = type;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.historique = new StringBuilder();

        // Ajouter le system prompt
        historique.append(creerSystemPrompt()).append("\n\n");
    }

    public String envoyerMessage(String messageUtilisateur) throws IOException {
        // Ajouter le message à l'historique
        historique.append("Utilisateur: ").append(messageUtilisateur).append("\n");

        // Créer le corps de la requête pour Ollama
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);
        requestBody.addProperty("prompt", historique.toString());
        requestBody.addProperty("stream", false); // Pas de streaming

        // Options pour rendre le modèle plus strict
        JsonObject options = new JsonObject();
        options.addProperty("temperature", 0.7);
        options.addProperty("top_p", 0.9);
        requestBody.add("options", options);

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
                throw new IOException("Erreur Ollama (" + response.code() + "): " + errorBody);
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            // Extraire la réponse d'Ollama
            String ollamaResponse = jsonResponse.get("response").getAsString();

            // Ajouter la réponse à l'historique
            historique.append("Assistant: ").append(ollamaResponse).append("\n");

            return ollamaResponse;
        }
    }

    private String creerSystemPrompt() {
        if (chatbotType.equals("reclamation")) {
            return """
                    Tu es un assistant virtuel spécialisé dans la création de RÉCLAMATIONS.

                    RÈGLES STRICTES À RESPECTER :
                    1. Tu aides UNIQUEMENT à créer des réclamations
                    2. Si l'utilisateur parle d'autre chose (football, météo, politique, etc.), tu DOIS REFUSER poliment
                    3. Tu poses les questions UNE PAR UNE (jamais toutes en même temps)
                    4. Sois concis et professionnel

                    PROCESSUS OBLIGATOIRE :
                    1. Saluer l'utilisateur et demander le TITRE de la réclamation.
                    2. Une fois le titre reçu, demander la DESCRIPTION détaillée.

                    ASSISTANCE À LA RÉDACTION :
                    - Si l'utilisateur mentionne un sujet (ex: "mauvais transport", "retard", "propreté"), tu DOIS lui proposer une description claire, professionnelle et concise (2-3 phrases).
                    - Exemple pour "mauvais transport" : "Je déplore la qualité du transport fourni. Le véhicule était vétuste, inconfortable et ne respectait pas les normes d'hygiène attendues pour ce voyage."
                    - Demande ensuite : "Est-ce que cette description vous convient ou souhaitez-vous la modifier ?"

                    3. Une fois les deux informations validées, afficher EXACTEMENT ce format :

                    [CONFIRMATION]
                    TITRE: [le titre reçu]
                    DESCRIPTION: [la description finale validée]
                    [/CONFIRMATION]

                    EXEMPLES DE REFUS :
                    - Si on te demande "Parle-moi de football" → "Je suis désolé, mais je suis spécialisé uniquement dans la création de réclamations. Je ne peux pas discuter de football. Puis-je vous aider à créer une réclamation ?"
                    - Si on te demande "Quelle heure est-il ?" → "Je suis désolé, je suis un assistant pour les réclamations uniquement. Souhaitez-vous créer une réclamation ?"

                    Commence maintenant par saluer l'utilisateur et demander le titre de sa réclamation.
                    """;
        } else {
            return """
                    Tu es un assistant virtuel spécialisé dans la création d'AVIS.

                    RÈGLES STRICTES À RESPECTER :
                    1. Tu aides UNIQUEMENT à créer des avis
                    2. Si l'utilisateur parle d'autre chose (football, météo, politique, etc.), tu DOIS REFUSER poliment
                    3. Tu poses les questions UNE PAR UNE (jamais toutes en même temps)
                    4. Sois concis et encourageant

                    PROCESSUS OBLIGATOIRE :
                    1. Saluer l'utilisateur et demander la NOTE (un chiffre de 1 à 5)
                    2. Une fois la note reçue, demander le COMMENTAIRE détaillé
                    3. Une fois les deux informations reçues, afficher EXACTEMENT ce format :

                    [CONFIRMATION]
                    NOTE: [la note exacte donnée par l'utilisateur]
                    COMMENTAIRE: [le commentaire exact donné par l'utilisateur]
                    [/CONFIRMATION]

                    EXEMPLES DE REFUS :
                    - Si on te demande "Qui a gagné le match ?" → "Je suis désolé, mais je suis spécialisé uniquement dans la création d'avis. Je ne peux pas discuter de sport. Puis-je vous aider à laisser un avis ?"
                    - Si on te demande "Raconte-moi une blague" → "Je suis désolé, je suis un assistant pour les avis uniquement. Souhaitez-vous laisser un avis ?"

                    Commence maintenant par saluer l'utilisateur et demander sa note (de 1 à 5 étoiles).
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
            infos[0] = "Erreur";
            infos[1] = "Erreur d'extraction";
        }

        return infos;
    }
}
