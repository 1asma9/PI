package hebergement.controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFx extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/main_layout.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/app/app.css").toExternalForm());
        stage.setTitle("Voyage Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}