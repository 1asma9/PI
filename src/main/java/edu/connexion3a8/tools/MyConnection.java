package edu.connexion3a8.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private static MyConnection instance;

    private static final String URL =
            "jdbc:mysql://localhost:3306/voyage?useSSL=false&serverTimezone=UTC&zeroDateTimeBehavior=CONVERT_TO_NULL";

    private static final String LOGIN = "root";
    private static final String PWD = "";

    private Connection cnx;

    private MyConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(URL, LOGIN, PWD);
            System.out.println("Connexion etablie! (connexion3a8)");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver MySQL introuvable.");
        } catch (SQLException e) {
            System.out.println("Erreur connexion: " + e.getMessage());
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
                cnx = DriverManager.getConnection(URL, LOGIN, PWD);
                System.out.println("Reconnexion établie!");
            }
        } catch (SQLException e) {
            System.out.println("Erreur reconnexion: " + e.getMessage());
        }
        return cnx;
    }

    public void close() {
        try {
            if (cnx != null && !cnx.isClosed()) {
                cnx.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}