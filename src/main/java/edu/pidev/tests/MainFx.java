package edu.pidev.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFx extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // ✅ TEST IMAGE PATH (doit afficher un URL, pas "null")
        System.out.println("IMAGE URL = " + MainFx.class.getResource("/images/background.png"));

        FXMLLoader loader = new FXMLLoader(
                MainFx.class.getResource("/ajout_activite.fxml")
        );

        Scene scene = new Scene(loader.load());
        stage.setTitle("Ajout Activité");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
