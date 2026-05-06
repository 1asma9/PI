package edu.destination.tools;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneUtil {

    private SceneUtil() {}

    // Appliquer le CSS global (app.css — pour les collègues)
    public static void applyCss(Scene scene) {
        applyCss(scene, "/app/app.css");
    }

    // Appliquer un CSS spécifique
    public static void applyCss(Scene scene, String cssPath) {
        scene.getStylesheets().clear();
        var css = SceneUtil.class.getResource(cssPath);
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.out.println("⚠ CSS introuvable : " + cssPath);
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
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement de : " + fxml, e);
        }
    }
}