package edu.destination.tools;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneUtil {

    private SceneUtil() {}

    // Appliquer le CSS global
    public static void applyCss(Scene scene) {
        scene.getStylesheets().clear();
        var css = SceneUtil.class.getResource("/app/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.out.println("⚠ app.css introuvable dans resources !");
        }
    }

    // Changer la scène
    public static void setScene(Stage stage, Parent root, int width, int height) {
        Scene scene = new Scene(root, width, height);
        applyCss(scene);
        stage.setScene(scene);
        stage.show();
    }

    // Charger un fichier FXML
    public static Parent load(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneUtil.class.getResource(fxml));
            return loader.load(); // ✅ RETURN obligatoire
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement de : " + fxml, e);
        }
    }
}