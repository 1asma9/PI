package controllers;

import entities.Reclamation;
import entities.Avis;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import services.ChatbotService;
import services.ReclamationService;
import services.AvisService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ChatbotController implements Initializable {

    @FXML
    private Label lblTitreChatbot;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox chatContainer;
    @FXML
    private TextField txtMessage;

    private ChatbotService chatbotService;
    private String chatbotType; // "reclamation" ou "avis"
    private int currentUserId = 1; // À récupérer depuis la session

    private ReclamationService reclamationService = new ReclamationService();
    private AvisService avisService = new AvisService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Scroller automatiquement vers le bas quand le contenu change
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setVvalue(1.0);
        });
    }

    public void setChatbotType(String type) {
        this.chatbotType = type;
        this.chatbotService = new ChatbotService(type);

        // Mettre à jour le titre
        if (type.equals("reclamation")) {
            lblTitreChatbot.setText("Assistant Réclamations");
        } else {
            lblTitreChatbot.setText("Assistant Avis");
        }

        // Message de bienvenue
        afficherMessageBot(obtenirMessageBienvenue());
    }

    private String obtenirMessageBienvenue() {
        if (chatbotType.equals("reclamation")) {
            return "Bonjour ! 👋 Je suis votre assistant pour créer des réclamations.\n\n" +
                    "Je vais vous poser quelques questions pour m'aider à comprendre votre situation.\n\n" +
                    "Pour commencer, pouvez-vous me donner un titre court pour votre réclamation ?";
        } else {
            return "Bonjour ! 👋 Je suis votre assistant pour laisser des avis.\n\n" +
                    "Je vais vous guider pour créer votre avis.\n\n" +
                    "Pour commencer, quelle note donneriez-vous ? (de 1 à 5 étoiles)";
        }
    }

    @FXML
    void envoyerMessage() {
        String message = txtMessage.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        // Afficher le message de l'utilisateur
        afficherMessageUser(message);
        txtMessage.clear();

        // Afficher un indicateur de chargement
        Label loading = new Label("💭 En train d'écrire...");
        loading.setStyle("-fx-text-fill: #6a7a73; -fx-font-style: italic; -fx-font-size: 12px;");
        chatContainer.getChildren().add(loading);

        // Appeler l'API dans un thread séparé
        new Thread(() -> {
            try {
                String reponse = chatbotService.envoyerMessage(message);

                Platform.runLater(() -> {
                    // Retirer l'indicateur de chargement
                    chatContainer.getChildren().remove(loading);

                    // Vérifier si c'est une confirmation
                    if (chatbotService.estConfirmation(reponse)) {
                        afficherConfirmation(reponse);
                    } else {
                        afficherMessageBot(reponse);
                    }

                    // Scroller vers le bas
                    scrollToBottom();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    chatContainer.getChildren().remove(loading);
                    afficherMessageBot("❌ Désolé, une erreur s'est produite. Veuillez réessayer.");
                    e.printStackTrace();
                });
            }
        }).start();
    }

    private void afficherMessageUser(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5));

        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(280);
        label.setStyle(
                "-fx-background-color: rgba(201,162,74,0.18); " +
                        "-fx-background-radius: 18 18 4 18; " +
                        "-fx-padding: 12 15; " +
                        "-fx-text-fill: #0f2a2a; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: 600;");

        messageBox.getChildren().add(label);
        chatContainer.getChildren().add(messageBox);
    }

    private void afficherMessageBot(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5));

        // Icône du bot
        Label icon = new Label("🤖");
        icon.setStyle("-fx-font-size: 20px; -fx-padding: 0 8 0 0;");

        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(280);
        label.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 18 18 18 4; " +
                        "-fx-padding: 12 15; " +
                        "-fx-text-fill: #0f2a2a; " +
                        "-fx-font-size: 13px; " +
                        "-fx-border-color: rgba(15,42,42,0.12); " +
                        "-fx-border-radius: 18 18 18 4;");

        messageBox.getChildren().addAll(icon, label);
        chatContainer.getChildren().add(messageBox);
    }

    private void afficherConfirmation(String message) {
        // Extraire les informations
        String[] infos = chatbotService.extraireInfos(message);

        VBox confirmBox = new VBox(12);
        confirmBox.setAlignment(Pos.CENTER);
        confirmBox.setStyle(
                "-fx-background-color: #e8fff0; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 15; " +
                        "-fx-border-color: #b9efcf; " +
                        "-fx-border-radius: 15; " +
                        "-fx-border-width: 2;");

        Label titre = new Label("✅ Récapitulatif");
        titre.setStyle("-fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: #1c7a44;");

        VBox details = new VBox(8);
        if (chatbotType.equals("reclamation")) {
            Label lblTitre = new Label("📋 Titre : " + infos[0]);
            lblTitre.setWrapText(true);
            lblTitre.setStyle("-fx-font-size: 13px; -fx-font-weight: 700;");

            Label lblDesc = new Label("📝 Description : " + infos[1]);
            lblDesc.setWrapText(true);
            lblDesc.setMaxWidth(300);
            lblDesc.setStyle("-fx-font-size: 12px;");

            details.getChildren().addAll(lblTitre, lblDesc);
        } else {
            Label lblNote = new Label("⭐ Note : " + infos[0] + "/5");
            lblNote.setStyle("-fx-font-size: 13px; -fx-font-weight: 700;");

            Label lblComm = new Label("💬 Commentaire : " + infos[1]);
            lblComm.setWrapText(true);
            lblComm.setMaxWidth(300);
            lblComm.setStyle("-fx-font-size: 12px;");

            details.getChildren().addAll(lblNote, lblComm);
        }

        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER);

        Button btnConfirmer = new Button("✓ Confirmer");
        btnConfirmer.setStyle(
                "-fx-background-color: #1c7a44; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: 900; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 8 20; " +
                        "-fx-cursor: hand;");
        btnConfirmer.setOnAction(e -> creerEntite(infos));

        Button btnAnnuler = new Button("✗ Modifier");
        btnAnnuler.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #d9534f; " +
                        "-fx-font-weight: 900; " +
                        "-fx-border-color: #d9534f; " +
                        "-fx-border-radius: 20; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 8 20; " +
                        "-fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> {
            afficherMessageBot("D'accord, recommençons. Que souhaitez-vous modifier ?");
            chatbotService.reinitialiser();
        });

        boutons.getChildren().addAll(btnConfirmer, btnAnnuler);

        confirmBox.getChildren().addAll(titre, details, boutons);
        chatContainer.getChildren().add(confirmBox);
    }

    private void creerEntite(String[] infos) {
        try {
            if (chatbotType.equals("reclamation")) {
                Reclamation r = new Reclamation(currentUserId, infos[0], infos[1]);
                reclamationService.addEntity(r);

                afficherMessageBot("✅ Parfait ! Votre réclamation a été créée avec succès.\n\n" +
                        "Elle sera traitée dans les plus brefs délais. Vous pouvez consulter " +
                        "son statut dans la liste des réclamations.");

            } else {
                int note = Integer.parseInt(infos[0]);
                Avis a = new Avis(currentUserId, note, infos[1]);
                avisService.addEntity(a);

                afficherMessageBot("✅ Merci beaucoup pour votre avis !\n\n" +
                        "Votre retour est précieux et nous aide à améliorer nos services.");
            }

            // Désactiver le chat après création
            txtMessage.setDisable(true);
            txtMessage.setPromptText("Création terminée - Rechargez pour recommencer");

        } catch (SQLException e) {
            afficherMessageBot("❌ Une erreur est survenue lors de la création. Veuillez réessayer.");
            e.printStackTrace();
        }
    }

    @FXML
    void reinitialiser() {
        chatContainer.getChildren().clear();
        chatbotService.reinitialiser();
        txtMessage.setDisable(false);
        txtMessage.clear();
        afficherMessageBot(obtenirMessageBienvenue());
    }

    private void scrollToBottom() {
        // Scroller automatiquement vers le bas
        scrollPane.setVvalue(1.0);
    }
}
