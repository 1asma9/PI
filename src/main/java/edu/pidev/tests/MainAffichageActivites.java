package edu.pidev.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainAffichageActivites extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/affichage_activites.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 800);

        // ✅ CSS (must be EXACT name: affichage.css)
        URL cssUrl = getClass().getResource("/affichage.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("⚠ CSS introuvable: /affichage.css");
        }

        stage.setTitle("Affichage Activités");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
