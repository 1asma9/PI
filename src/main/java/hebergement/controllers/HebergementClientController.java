package hebergement.controllers;

import hebergement.entities.Hebergement;
import hebergement.services.ChambreService;
import hebergement.services.HebergementService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.stream.Collectors;

public class HebergementClientController {

    @FXML private TextField tfSearch, tfPrixMin, tfPrixMax;
    @FXML private ComboBox<String> cbType, cbTri;
    @FXML private DatePicker dpDebut, dpFin;
    @FXML private FlowPane flowPane;

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
                "Par défaut", "Prix croissant", "Prix décroissant"
        ));
        cbTri.setValue("Par défaut");

        // listeners temps réel
        tfSearch.textProperty().addListener((o, ov, nv) -> filterData());
        cbType.valueProperty().addListener((o, ov, nv) -> filterData());
        tfPrixMin.textProperty().addListener((o, ov, nv) -> filterData());
        tfPrixMax.textProperty().addListener((o, ov, nv) -> filterData());
        cbTri.valueProperty().addListener((o, ov, nv) -> filterData());

        loadData();
    }

    private void loadData() {
        try {
            allData = hs.getData();
            renderCards(allData);
        } catch (Exception e) {
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
                (q.isEmpty() || h.getDescription().toLowerCase().contains(q)) &&
                        (type == null || type.equals("Tous les types") ||
                                (h.getTypeLibelle() != null && h.getTypeLibelle().equalsIgnoreCase(type))) &&
                        h.getPrix() >= min && h.getPrix() <= max
        ).collect(Collectors.toList());

        if ("Prix croissant".equals(tri))
            filtered.sort((a, b) -> Double.compare(a.getPrix(), b.getPrix()));
        else if ("Prix décroissant".equals(tri))
            filtered.sort((a, b) -> Double.compare(b.getPrix(), a.getPrix()));

        renderCards(filtered);
    }

    private void renderCards(List<Hebergement> list) {
        flowPane.getChildren().clear();
        for (Hebergement h : list) {
            flowPane.getChildren().add(buildCard(h));
        }
    }

    private VBox buildCard(Hebergement h) {
        VBox card = new VBox(0);
        card.setPrefWidth(300);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 8, 0, 0, 2);"
        );

        // Image
        StackPane imgPane = new StackPane();
        imgPane.setPrefHeight(180);
        imgPane.setStyle("-fx-background-color: #f0f0f0;" +
                "-fx-background-radius: 12 12 0 0;");

        if (h.getImagePath() != null && !h.getImagePath().isBlank()) {
            try {
                String url = h.getImagePath().startsWith("http") ?
                        h.getImagePath() : "file:///" + h.getImagePath().replace("\\", "/");
                ImageView iv = new ImageView(new Image(url, 300, 180, true, true, true));
                iv.setFitWidth(300); iv.setFitHeight(180);
                iv.setPreserveRatio(false);
                Rectangle clip = new Rectangle(300, 180);
                clip.setArcWidth(0); clip.setArcHeight(0);
                iv.setClip(clip);
                imgPane.getChildren().add(iv);
            } catch (Exception ignored) {}
        } else {
            Label ico = new Label("🏨");
            ico.setStyle("-fx-font-size: 48px;");
            imgPane.getChildren().add(ico);
        }

        // Corps de la card
        VBox body = new VBox(6);
        body.setStyle("-fx-padding: 0;");

        // Badge type (fond rose clair comme le web)
        String typeLib = h.getTypeLibelle() != null ? h.getTypeLibelle() : "—";
        Label badge = new Label(typeLib);
        badge.setMaxWidth(Double.MAX_VALUE);
        badge.setStyle(
                "-fx-background-color: #fde8e8;" +
                        "-fx-text-fill: #c0392b;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-padding: 6 14;"
        );

        // Prix
        Label prix = new Label(String.format("%.2f TND / nuit", h.getPrix()));
        prix.setStyle(
                "-fx-text-fill: #e03030;" +
                        "-fx-font-size: 17px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-padding: 10 14 2 14;"
        );

        // Nom
        Label nom = new Label(h.getDescription());
        nom.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: #222;" +
                        "-fx-padding: 0 14;"
        );
        nom.setWrapText(true);

        // Chambres
        int nbChambres = 0;
        try { nbChambres = cs.getByHebergement(h.getId()).size(); }
        catch (Exception ignored) {}

        Label chambres = new Label("Chambres: " + nbChambres);
        chambres.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #888;" +
                        "-fx-padding: 4 14 14 14;"
        );

        body.getChildren().addAll(badge, prix, nom, chambres);
        card.getChildren().addAll(imgPane, body);

        // Hover effect
        card.setOnMouseEntered(e ->
                card.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 12;" +
                                "-fx-border-radius: 12;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 16, 0, 0, 4);" +
                                "-fx-translate-y: -2;"
                )
        );
        card.setOnMouseExited(e ->
                card.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 12;" +
                                "-fx-border-radius: 12;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 8, 0, 0, 2);"
                )
        );

        return card;
    }

    private double parsePrix(String s, double def) {
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }
}