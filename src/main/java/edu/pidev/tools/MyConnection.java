package edu.pidev.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private final String url = "jdbc:mysql://localhost:3306/pidev";
    private final String login = "root";
    private final String pwd = ""; // <-- mets ton mot de passe si tu en as un

    private Connection cnx;
    private static MyConnection instance;

    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("✅ Connection établie");
        } catch (SQLException e) {
            System.out.println("❌ Erreur connexion: " + e.getMessage());
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
