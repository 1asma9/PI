/*package edu.connexion3a8.tests;

import edu.connexion3a8.tools.MyConnection;

public class MainClass {
    public static void main(String[] args) {
        MyConnection mc = new MyConnection();
    }
}*/
package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClass extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le fichier FXML de la sidebar
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/sidenavbar.fxml"));

            Scene scene = new Scene(root, 1366, 750);

            primaryStage.setTitle("Blog Management System");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'interface : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
