package edu.pidev.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private final String url = "jdbc:mysql://localhost:3307/voyage?useSSL=false&serverTimezone=UTC";
    private final String login = "root";
    private final String pwd = "";

    private Connection cnx;
    private static MyConnection instance;

    private MyConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("✅ Connection établie");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver MySQL introuvable.");
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
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = DriverManager.getConnection(url, login, pwd);
                System.out.println("Reconnexion établie!");
            }
        } catch (SQLException e) {
            System.out.println("Erreur reconnexion: " + e.getMessage());
        }
        return cnx;
    }
}