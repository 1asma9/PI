package edu.pidev.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.net.URL;

public class MainAffichageActivites extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/affichage_activites_front.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        var css = getClass().getResource("/affichage.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setScene(scene);
        stage.show();

        // ✅ ALWAYS maximize AFTER show (same rule as before)
        javafx.application.Platform.runLater(() -> stage.setMaximized(true));
    }
    public static void main(String[] args) {
        launch();
    }
}
