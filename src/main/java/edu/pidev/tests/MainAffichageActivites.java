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
        // Rediriger vers l'interface de connexion au démarrage
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        var css = getClass().getResource("/app/app.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setTitle("Connexion - Voyage");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}
