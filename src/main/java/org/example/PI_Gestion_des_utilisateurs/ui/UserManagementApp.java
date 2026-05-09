package org.example.PI_Gestion_des_utilisateurs.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.PI_Gestion_des_utilisateurs.ui.navigation.SceneNavigator;

public class UserManagementApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SceneNavigator.init(stage);
        SceneNavigator.goTo("/app/login.fxml", "Connexion");
        stage.setMinWidth(400);
        stage.setMinHeight(500);
    }

    public static void main(String[] args) {
        launch(args);
    }
}