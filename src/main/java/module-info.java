module edu.destination {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web; // <-- pour WebView / WebEngine
    requires jdk.jsobject;
    requires java.sql;
    requires org.json;
    exports edu.destination.tests;
    opens edu.destination.controllers to javafx.web, javafx.fxml;
    opens edu.destination.entities to javafx.base;
}