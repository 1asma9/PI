package edu.destination.tests;

import edu.destination.tools.SceneUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainFx extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxmlUrl = getClass().getResource("/AdminDashboard.fxml");
        if (fxmlUrl == null) throw new RuntimeException("FXML AdminDashboard.fxml non trouvé !");

        Parent root = FXMLLoader.load(fxmlUrl);

        Scene scene = new Scene(root, 1200, 800);
        SceneUtil.applyCss(scene); // ✅ charge app.css

        stage.setTitle("Destination App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.forceGPU", "false");
        System.setProperty("javafx.webview.userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        launch(args);
    }
}