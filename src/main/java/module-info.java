module edu.destination {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.jsobject;
    requires java.sql;
    requires org.json;
    requires javafx.swing;
    requires uk.co.caprica.vlcj;        // ← remplace "requires vlcj"
    requires java.desktop;
    requires javafx.media;
    requires java.management;
    exports edu.destination.tests;
    opens edu.destination.controllers to javafx.web, javafx.fxml;
    opens edu.destination.entities to javafx.base;
}