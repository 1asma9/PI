package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FrontendMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le frontend public
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/FrontendBlogList.fxml"));

            Scene scene = new Scene(root, 1366, 750);

            primaryStage.setTitle("My Travel Blog - Page Publique");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du frontend : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}