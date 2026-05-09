package hebergement.controllers;

import hebergement.entities.Hebergement;
import hebergement.services.ChambreService;
import hebergement.services.HebergementService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class HebergementGalleryController {

    // ✅ Ton vrai chemin vers les images web
    private static final String WEB_UPLOADS =
            "C:/Users/ferja/voyage/public/uploads/hebergements/";

    // ✅ Racine du projet web (pour les chemins relatifs)
    private static final String WEB_ROOT =
            "C:/Users/ferja/voyage/";

    @FXML private FlowPane cardsPane;
    @FXML private Label lblCount;
    @FXML private Label lblStatus;
    @FXML private TextField tfSearch, tfPrixMin, tfPrixMax;
    @FXML private ComboBox<String> cbType, cbTri;
    @FXML private DatePicker dpDebut, dpFin;

    private final HebergementService hs = new HebergementService();
    private final ChambreService cs = new ChambreService();
    private List<Hebergement> allData;

    @FXML
    public void initialize() {
        cbType.setItems(FXCollections.observableArrayList(
                "Tous les types", "hotel", "villa", "appartement"
        ));
        cbType.setValue("Tous les types");

        cbTri.setItems(FXCollections.observableArrayList(
                "Par défaut", "Prix croissant ↑", "Prix décroissant ↓"
        ));
        cbTri.setValue("Par défaut");

        tfSearch.textProperty().addListener((o, ov, nv) -> filterData());
        cbType.valueProperty().addListener((o, ov, nv) -> filterData());
        tfPrixMin.textProperty().addListener((o, ov, nv) -> filterData());
        tfPrixMax.textProperty().addListener((o, ov, nv) -> filterData());
        cbTri.valueProperty().addListener((o, ov, nv) -> filterData());

        loadCards();
    }

    private void loadCards() {
        try {
            allData = hs.getData();

            // ✅ DEBUG — affiche les chemins en console
            for (Hebergement h : allData) {
                System.out.println("ID: " + h.getId() +
                        " | image_path: [" + h.getImagePath() + "]");

                // Vérifie si le fichier existe
                if (h.getImagePath() != null) {
                    String p = h.getImagePath().trim().replace("\\", "/");
                    if (p.matches("^[A-Za-z]:/.*")) {
                        java.io.File f = new java.io.File(p);
                        System.out.println("  → Fichier existe? " + f.exists());
                        System.out.println("  → Nom fichier: " + f.getName());
                        java.io.File webF = new java.io.File(WEB_UPLOADS + f.getName());
                        System.out.println("  → Dans WEB_UPLOADS? " + webF.exists());
                        System.out.println("  → Chemin WEB: " + webF.getAbsolutePath());
                    }
                }
            }

            updateCount(allData.size());
            renderCards(allData);
        } catch (Exception e) {
            if (lblStatus != null)
                lblStatus.setText("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void filterData() {
        if (allData == null) return;

        String q    = tfSearch.getText().toLowerCase();
        String type = cbType.getValue();
        double min  = parsePrix(tfPrixMin.getText(), 0);
        double max  = parsePrix(tfPrixMax.getText(), 9999);
        String tri  = cbTri.getValue();

        List<Hebergement> filtered = allData.stream().filter(h ->
                (q.isEmpty() || (h.getDescription() != null &&
                        h.getDescription().toLowerCase().contains(q))) &&
                        (type == null || type.equals("Tous les types") ||
                                (h.getTypeLibelle() != null &&
                                        h.getTypeLibelle().equalsIgnoreCase(type))) &&
                        h.getPrix() >= min && h.getPrix() <= max
        ).collect(Collectors.toList());

        if ("Prix croissant ↑".equals(tri))
            filtered.sort((a, b) -> Double.compare(a.getPrix(), b.getPrix()));
        else if ("Prix décroissant ↓".equals(tri))
            filtered.sort((a, b) -> Double.compare(b.getPrix(), a.getPrix()));

        updateCount(filtered.size());
        renderCards(filtered);
    }


    @FXML
    private void resetFilters() {
        tfSearch.clear();
        tfPrixMin.clear();
        tfPrixMax.clear();
        cbType.setValue("Tous les types");
        cbTri.setValue("Par défaut");
        dpDebut.setValue(null);
        dpFin.setValue(null);
        renderCards(allData);
        updateCount(allData.size());
        lblStatus.setText("");
    }

    private void renderCards(List<Hebergement> list) {
        cardsPane.getChildren().clear();
        if (list.isEmpty()) {
            lblStatus.setText("Aucun hébergement trouvé. Essayez de modifier vos critères.");
        } else {
            lblStatus.setText("");
            for (Hebergement h : list) {
                cardsPane.getChildren().add(createCard(h));
            }
        }
    }

    private void updateCount(int n) {
        if (lblCount != null)
            lblCount.setText(n + " hébergement(s) trouvé(s)");
    }

    private VBox createCard(Hebergement h) {
        VBox card = new VBox(0);
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: rgba(0,0,0,0.08);" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 12, 0, 0, 3);"
        );

        // ===== IMAGE =====
        StackPane imageWrap = new StackPane();
        imageWrap.setPrefHeight(220);
        imageWrap.setMinHeight(220);

        ImageView imgView = new ImageView();
        imgView.setFitWidth(300);
        imgView.setFitHeight(220);
        imgView.setPreserveRatio(false);
        imgView.setSmooth(true);

        Rectangle clip = new Rectangle(300, 220);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imgView.setClip(clip);

        imgView.setImage(loadImage(h.getImagePath()));

        imageWrap.setStyle("-fx-background-color: #eee; -fx-background-radius: 10 10 0 0;");
        imageWrap.getChildren().add(imgView);

        // ===== BADGE TYPE =====
        String typeLib = h.getTypeLibelle() != null ? h.getTypeLibelle() : "Hébergement";
        Label badge = new Label(typeLib);
        badge.setMaxWidth(Double.MAX_VALUE);
        badge.setStyle(
                "-fx-background-color: #fde8e8;" +
                        "-fx-text-fill: #c0392b;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-padding: 8 16;"
        );

        // ===== BODY =====
        VBox body = new VBox(6);
        body.setPadding(new Insets(10, 16, 0, 16));

        Label lblPrix = new Label(String.format("%.2f TND / nuit", h.getPrix()));
        lblPrix.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-text-fill: #f75959;"
        );

        Label lblNom = new Label(h.getDescription() != null ? h.getDescription() : "-");
        lblNom.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: #222;"
        );
        lblNom.setWrapText(true);

        int nbChambres = 0;
        try { nbChambres = cs.getByHebergement(h.getId()).size(); }
        catch (Exception ignored) {}

        Label lblChambres = new Label("Chambres: " + nbChambres);
        lblChambres.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        body.getChildren().addAll(lblPrix, lblNom, lblChambres);

        // ===== BOUTON =====
        VBox btnBox = new VBox();
        btnBox.setPadding(new Insets(12, 16, 16, 16));

        Button btnVoir = new Button("Voir & Réserver");
        btnVoir.setMaxWidth(Double.MAX_VALUE);
        String styleNormal =
                "-fx-background-color: #173b3b;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 700;" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 0;" +
                        "-fx-cursor: hand;";
        String styleHover =
                "-fx-background-color: #f75959;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 700;" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 0;" +
                        "-fx-cursor: hand;";

        btnVoir.setStyle(styleNormal);
        btnVoir.setOnAction(e -> ouvrirFormulaire(h));
        btnVoir.setOnMouseEntered(e -> btnVoir.setStyle(styleHover));
        btnVoir.setOnMouseExited(e -> btnVoir.setStyle(styleNormal));

        btnBox.getChildren().add(btnVoir);
        card.getChildren().addAll(imageWrap, badge, body, btnBox);

        // ===== HOVER CARTE =====
        String cardNormal =
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: rgba(0,0,0,0.08);" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 12, 0, 0, 3);";
        String cardHover =
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: rgba(0,0,0,0.08);" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 6);" +
                        "-fx-translate-y: -4;";

        card.setOnMouseEntered(e -> card.setStyle(cardHover));
        card.setOnMouseExited(e -> card.setStyle(cardNormal));

        return card;
    }

    private Image loadImage(String path) {
        // Image par défaut
        Image fallback = null;
        try {
            var res = getClass().getResource("/images/hebergement_default.jpg");
            if (res == null) res = getClass().getResource("/images/activity_banner.jpg");
            if (res != null) fallback = new Image(res.toExternalForm(), true);
        } catch (Exception ignored) {}

        if (path == null || path.isBlank()) return fallback;

        try {
            String p = path.trim().replace("\\", "/");

            // ✅ URL complète http ou file:
            if (p.startsWith("http") || p.startsWith("file:")) {
                return new Image(p, 300, 220, false, true, true);
            }

            // ✅ Chemin absolu Windows ex: C:/Users/ferja/...
            if (p.matches("^[A-Za-z]:/.*")) {
                File f = new File(p);
                if (f.exists()) return new Image(f.toURI().toString(), 300, 220, false, true, true);
                // Si le fichier n'existe plus à l'ancien emplacement,
                // essaye de retrouver juste le nom du fichier dans WEB_UPLOADS
                String fileName = f.getName();
                File webFile = new File(WEB_UPLOADS + fileName);
                if (webFile.exists()) return new Image(webFile.toURI().toString(), 300, 220, false, true, true);
            }

            // ✅ Nom de fichier seul ex: "villa-69d3.jpg"
            if (!p.contains("/")) {
                File f = new File(WEB_UPLOADS + p);
                if (f.exists()) return new Image(f.toURI().toString(), 300, 220, false, true, true);
            }

            // ✅ Chemin relatif ex: "public/uploads/hebergements/villa.jpg"
            File f = new File(p);
            if (!f.exists()) f = new File(WEB_ROOT + p);
            if (f.exists()) return new Image(f.toURI().toString(), 300, 220, false, true, true);

            System.err.println("❌ Image non trouvée : " + path);
            return fallback;

        } catch (Exception e) {
            System.err.println("❌ Erreur image : " + path + " → " + e.getMessage());
            return fallback;
        }
    }

    private void ouvrirFormulaire(Hebergement h) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/app/hebergement_detail.fxml")
            );
            Parent root = loader.load();

            HebergementDetailController controller = loader.getController();
            controller.setHebergement(h);

            var css = getClass().getResource("/app/app.css");
            if (css != null && !root.getStylesheets().contains(css.toExternalForm())) {
                root.getStylesheets().add(css.toExternalForm());
            }

            ClientLayoutController layout = ClientLayoutController.getInstance();
            if (layout != null) {
                layout.loadPageWithRoot(root);
            } else {
                cardsPane.getScene().setRoot(root);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double parsePrix(String s, double def) {
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }
}