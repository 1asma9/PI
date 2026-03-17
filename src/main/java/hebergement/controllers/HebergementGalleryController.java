package hebergement.controllers;

import hebergement.entities.Hebergement;
import hebergement.services.HebergementService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class HebergementGalleryController {

    @FXML private FlowPane cardsPane;
    @FXML private Label lblCount;
    @FXML private Label lblStatus;

    private final HebergementService hs = new HebergementService();

    @FXML
    public void initialize() {
        loadCards();
    }

    private void loadCards() {
        try {
            List<Hebergement> list = hs.getData();

            lblCount.setText(list.size() + " hébergements");
            cardsPane.getChildren().clear();

            for (Hebergement h : list) {
                cardsPane.getChildren().add(createCard(h));
            }

            if (list.isEmpty()) {
                lblStatus.setText("Aucun hébergement disponible.");
            }

        } catch (Exception e) {
            if (lblStatus != null)
                lblStatus.setText("Erreur de chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createCard(Hebergement h) {
        VBox card = new VBox();
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: rgba(0,0,0,0.07);" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 5);"
        );

        // ===== IMAGE =====
        StackPane imageWrap = new StackPane();
        imageWrap.setPrefHeight(180);
        imageWrap.setMinHeight(180);
        imageWrap.setStyle("-fx-background-radius: 18 18 0 0;");

        ImageView imgView = new ImageView();
        imgView.setFitWidth(280);
        imgView.setFitHeight(180);
        imgView.setPreserveRatio(false);
        imgView.setSmooth(true);

        // Clip arrondi en haut
        Rectangle clip = new Rectangle(280, 180);
        clip.setArcWidth(36);
        clip.setArcHeight(36);
        imgView.setClip(clip);

        // Charger l'image
        imgView.setImage(loadImage(h.getImagePath()));

        // Badge type
        if (h.getTypeLibelle() != null && !h.getTypeLibelle().isBlank()) {
            Label badge = new Label(h.getTypeLibelle());
            badge.setStyle(
                    "-fx-background-color: rgba(23,59,59,0.85);" +
                            "-fx-text-fill: #c9a24a;" +
                            "-fx-font-size: 10.5px;" +
                            "-fx-font-weight: 700;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 4 10;"
            );
            StackPane.setAlignment(badge, Pos.TOP_LEFT);
            StackPane.setMargin(badge, new Insets(10, 0, 0, 10));
            imageWrap.getChildren().addAll(imgView, badge);
        } else {
            imageWrap.getChildren().add(imgView);
        }

        // ===== BODY =====
        VBox body = new VBox(8);
        body.setPadding(new Insets(14, 16, 16, 16));

        // Description
        Label lblDesc = new Label(h.getDescription() != null ? h.getDescription() : "-");
        lblDesc.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: #173b3b;"
        );
        lblDesc.setWrapText(true);

        // Adresse
        Label lblAddr = new Label("📍 " + (h.getAdresse() != null ? h.getAdresse() : "-"));
        lblAddr.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #777;");
        lblAddr.setWrapText(true);

        // Separator
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: rgba(0,0,0,0.07);");

        // Prix + Bouton
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        VBox priceBox = new VBox(2);
        Label lblPrix = new Label(String.format("%.0f DT", h.getPrix()));
        lblPrix.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-text-fill: #c9a24a;"
        );
        Label lblNuit = new Label("/ nuit");
        lblNuit.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");
        priceBox.getChildren().addAll(lblPrix, lblNuit);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnReserver = new Button("Réserver →");
        btnReserver.setStyle(
                "-fx-background-color: #173b3b;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 700;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        );

        // ✅ Au clic → ouvrir le formulaire de réservation avec cet hébergement pré-sélectionné
        btnReserver.setOnAction(e -> ouvrirFormulaire(h));

        // Hover effect
        btnReserver.setOnMouseEntered(e -> btnReserver.setStyle(
                "-fx-background-color: #c9a24a;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 700;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        ));
        btnReserver.setOnMouseExited(e -> btnReserver.setStyle(
                "-fx-background-color: #173b3b;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 700;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        ));

        bottomRow.getChildren().addAll(priceBox, spacer, btnReserver);
        body.getChildren().addAll(lblDesc, lblAddr, sep, bottomRow);
        card.getChildren().addAll(imageWrap, body);

        return card;
    }

    private Image loadImage(String path) {
        // Fallback image
        Image fallback = null;
        try {
            var res = getClass().getResource("/images/hebergement_default.jpg");
            if (res == null) res = getClass().getResource("/images/activity_banner.jpg");
            if (res != null) fallback = new Image(res.toExternalForm(), true);
        } catch (Exception ignored) {}

        if (path == null || path.isBlank()) return fallback;

        try {
            String url = path.trim();
            // Si c'est un chemin local, le convertir en file://
            if (!url.startsWith("http") && !url.startsWith("file:")) {
                url = "file:///" + url.replace("\\", "/");
            }
            Image img = new Image(url, 280, 180, false, true, true);
            return img;
        } catch (Exception e) {
            System.out.println("❌ Image load error: " + path + " | " + e.getMessage());
            return fallback;
        }
    }

    // ✅ Ouvre le formulaire reservation.fxml avec l'hébergement pré-sélectionné
    private void ouvrirFormulaire(Hebergement h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/reservation.fxml"));
            Parent root = loader.load();

            // Pré-sélectionner l'hébergement dans le formulaire
            ReservationController controller = loader.getController();
            controller.setSelectedHebergement(h);

            // Charger le CSS
            var css = getClass().getResource("/app/app.css");
            if (css != null && !root.getStylesheets().contains(css.toExternalForm())) {
                root.getStylesheets().add(css.toExternalForm());
            }

            // Naviguer via ClientLayoutController si disponible
            ClientLayoutController layout = ClientLayoutController.getInstance();
            if (layout != null) {
                layout.loadPageWithRoot(root);
            } else {
                // Fallback : setRoot direct
                cardsPane.getScene().setRoot(root);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur ouverture formulaire : " + e.getMessage());
        }
    }
}