package edu.pidev.controllers;

import edu.pidev.entities.Activite;
import edu.pidev.services.ActiviteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class AffichageActivitesController {

    @FXML private FlowPane cardsContainer;
    @FXML private ScrollPane scroll;

    @FXML private ImageView bannerImage;
    @FXML private StackPane hero;

    private final ActiviteService service = new ActiviteService();

    @FXML
    public void initialize() {

        // ✅ Banner (safe)
        if (bannerImage != null) {
            var url = getClass().getResource("/images/banner.jpg");
            if (url == null) {
                System.out.println("❌ Banner not found: /images/banner.jpg");
            } else {
                bannerImage.setImage(new Image(url.toExternalForm()));
            }

            bannerImage.setPreserveRatio(false);
            bannerImage.setSmooth(true);
        }

        // ✅ Scroll behavior
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setVvalue(0);

        final double SIDE_PADDING = 80;

        if (bannerImage != null) {
            bannerImage.fitWidthProperty().bind(scroll.widthProperty().subtract(SIDE_PADDING));
            if (hero != null) {
                bannerImage.fitHeightProperty().bind(hero.heightProperty());
            }
        }

        if (cardsContainer != null) {
            cardsContainer.prefWidthProperty().bind(scroll.widthProperty().subtract(SIDE_PADDING));
        }

        refresh();
    }

    private void refresh() {
        cardsContainer.getChildren().clear();

        List<Activite> activites = service.getAllActivites();
        for (Activite a : activites) {
            cardsContainer.getChildren().add(createCard(a));
        }

        scroll.setVvalue(0);
    }

    private VBox createCard(Activite a) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        HBox top = new HBox(10);

        Label lieu = new Label(safeUpper(a.getLieu()));
        lieu.getStyleClass().add("cardSmallTop");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label rating = new Label("★ 4.8");
        rating.getStyleClass().add("metaText");

        top.getChildren().addAll(lieu, spacer, rating);

        Label title = new Label(safe(a.getNom()));
        title.getStyleClass().add("cardTitle");

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label duree = new Label("⏱ " + a.getDuree() + " min");
        duree.getStyleClass().add("metaText");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Label badge = new Label(safe(a.getType()));
        badge.getStyleClass().add("badge");

        meta.getChildren().addAll(duree, spacer2, badge);

        Separator hr = new Separator();
        hr.getStyleClass().add("hr");

        HBox bottom = new HBox(10);
        bottom.setAlignment(Pos.CENTER_LEFT);

        VBox priceBox = new VBox(2);
        Label price = new Label(formatPrix(a.getPrix()) + " DT");
        price.getStyleClass().add("price");
        Label priceSub = new Label("Par activité");
        priceSub.getStyleClass().add("priceSub");
        priceBox.getChildren().addAll(price, priceSub);

        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);

        Button reserver = new Button("📌");
        reserver.getStyleClass().addAll("iconBtn", "btnAdd");
        reserver.setTooltip(new Tooltip("Réserver"));
        reserver.setOnAction(e -> openReservationForm(a.getIdActivite(), a.getNom()));

        Button modifier = new Button("✏");
        modifier.getStyleClass().addAll("iconBtn", "btnEdit");
        modifier.setTooltip(new Tooltip("Modifier"));
        modifier.setOnAction(e -> ouvrirModifier(a));

        Button ajouter = new Button("➕");
        ajouter.getStyleClass().addAll("iconBtn", "btnAdd");
        ajouter.setTooltip(new Tooltip("Ajouter"));
        ajouter.setOnAction(e -> ouvrirAjouter());

        Button supprimer = new Button("🗑");
        supprimer.getStyleClass().addAll("iconBtn", "btnDelete");
        supprimer.setTooltip(new Tooltip("Supprimer"));
        supprimer.setOnAction(e -> confirmerEtSupprimer(a));

        HBox actions = new HBox(8, reserver, modifier, ajouter, supprimer);
        actions.setAlignment(Pos.CENTER_RIGHT);

        bottom.getChildren().addAll(priceBox, spacer3, actions);

        card.getChildren().addAll(top, title, meta, hr, bottom);
        VBox.setMargin(hr, new Insets(2, 0, 2, 0));

        return card;
    }

    private void ouvrirModifier(Activite a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier_activite.fxml"));
            Parent root = loader.load();

            ModifierActiviteController controller = loader.getController();
            controller.setActivite(a);

            Scene scene = new Scene(root);

            var css = getClass().getResource("/affichage.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            else System.out.println("❌ CSS not found: /affichage.css");

            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            boolean wasMax = stage.isMaximized();
            stage.setMaximized(false);
            stage.setScene(scene);
            root.applyCss();
            root.layout();
            stage.setMaximized(wasMax);
            stage.centerOnScreen();


        } catch (Exception ex) {
            showError("Modification impossible", ex);
        }
    }

    private void ouvrirAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajout_activite.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // ✅ DO NOT load /ajout.css (it’s missing)
            // If your ajout_activite.fxml already has stylesheets="@form_activite.css", you can skip this.
            var css = getClass().getResource("/form_activite.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            else System.out.println("❌ CSS not found: /form_activite.css");

            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            boolean wasMax = stage.isMaximized();
            stage.setMaximized(false);
            stage.setScene(scene);
            root.applyCss();
            root.layout();
            stage.setMaximized(wasMax);
            stage.centerOnScreen();


        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Ajout impossible", ex);
        }
    }

    private void openReservationForm(int idActivite, String nomActivite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservation_form.fxml"));
            Parent root = loader.load();

            ReservationFormController controller = loader.getController();
            controller.setData(idActivite, nomActivite);

            Scene scene = new Scene(root);

            var css = getClass().getResource("/reservation.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            // ✅ Use SAME stage
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);   // optional but recommended
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR, e.getMessage());
            err.showAndWait();
        }
    }


    private void confirmerEtSupprimer(Activite a) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer cette activité ?");
        confirm.setContentText("Activité : " + safe(a.getNom()));

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            service.deleteActivite(a.getIdActivite());
            refresh();
        }
    }

    private void showError(String title, Exception ex) {
        Alert err = new Alert(Alert.AlertType.ERROR);
        err.setTitle("Erreur");
        err.setHeaderText(title);
        err.setContentText(ex.getMessage());
        err.showAndWait();
        ex.printStackTrace();
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s.trim();
    }

    private String safeUpper(String s) {
        return safe(s).toUpperCase();
    }

    private String formatPrix(double prix) {
        if (Math.floor(prix) == prix) return String.valueOf((int) prix);
        return String.format("%.2f", prix);
    }
}
