package com.example.reclamation_avis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String layoutPath = tools.SessionManager.isAdmin() ? "/admin_layout.fxml" : "/user_layout.fxml";
        Parent root = FXMLLoader.load(getClass().getResource(layoutPath));
        Scene scene = new Scene(root);
        if (getClass().getResource("/style.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        }
        primaryStage.setTitle("Complaints & Reviews Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
