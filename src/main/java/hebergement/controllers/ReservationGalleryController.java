package hebergement.controllers;

import hebergement.entities.Hebergement;
import hebergement.services.HebergementService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

public class ReservationGalleryController {

    private final HebergementService hs = new HebergementService();

    @FXML private FlowPane cardsWrap;

    @FXML
    public void initialize() {
        loadCards();
    }

    private void loadCards() {
        try {
            List<Hebergement> list = hs.getData();
            cardsWrap.getChildren().clear();

            for (Hebergement h : list) {
                cardsWrap.getChildren().add(buildCard(h));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox buildCard(Hebergement h) {

        ImageView img = new ImageView(loadHebImage(h));
        img.setFitWidth(260);
        img.setFitHeight(360);
        img.setPreserveRatio(false);
        img.setSmooth(true);

        StackPane wrapper = new StackPane(img);
        wrapper.getStyleClass().add("imgCard");

        VBox overlay = new VBox(6);
        overlay.getStyleClass().add("imgOverlay");

        Label small = new Label("Réserver maintenant");
        small.getStyleClass().add("imgSmall");

        Label big = new Label(h.getDescription());
        big.getStyleClass().add("imgBig");

        overlay.getChildren().addAll(small, big);
        wrapper.getChildren().add(overlay);

        VBox card = new VBox(wrapper);
        card.getStyleClass().add("imgCardRoot");
        card.setOnMouseClicked(e -> openForm(h));

        return card;
    }

    private Image loadHebImage(Hebergement h) {
        try {
            String path = h.getImagePath();

            if (path != null && !path.isBlank()) {
                File f = new File(path);

                // ✅ si c'est un chemin relatif "uploads/..."
                if (!f.isAbsolute()) {
                    f = new File(System.getProperty("user.dir"), path);
                }

                if (f.exists()) {
                    return new Image(f.toURI().toString(), 260, 360, false, true);
                }
            }

            return new Image(getClass().getResourceAsStream("/images/hebergements/default.jpg"));
        } catch (Exception e) {
            return new Image(getClass().getResourceAsStream("/images/hebergements/default.jpg"));
        }
    }

    private void openForm(Hebergement selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/reservation.fxml"));
            Node view = loader.load();

            ReservationController controller = loader.getController();
            controller.setSelectedHebergement(selected);

            // IMPORTANT: main_layout.fxml doit avoir id="contentPane" et id="pageTitle"
            StackPane contentPane = (StackPane) cardsWrap.getScene().lookup("#contentPane");
            contentPane.getChildren().setAll(view);

            Label pageTitle = (Label) cardsWrap.getScene().lookup("#pageTitle");
            if (pageTitle != null) pageTitle.setText("Réserver");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}