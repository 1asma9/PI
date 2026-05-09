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

<<<<<<< Updated upstream
    private MyConnection() {
        try {
            String host = System.getenv().getOrDefault("DB_HOST", "localhost");
            String port = System.getenv().getOrDefault("DB_PORT", "3306");
            String dbName = System.getenv().getOrDefault("DB_NAME", "voyage-1");
            String user = System.getenv().getOrDefault("DB_USER", "root");
            String password = System.getenv().getOrDefault("DB_PASSWORD", "");

            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            cnx = DriverManager.getConnection(jdbcUrl, user, password);
            System.out.println("Connexion etablie: " + jdbcUrl);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
=======
    public static MyConnection getInstance() {
        if (instance == null) instance = new MyConnection();
>>>>>>> Stashed changes
        return instance;
    }

    private MyConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(URL, LOGIN, PWD);
            System.out.println("Connexion etablie! (destination)");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver MySQL introuvable.");
        } catch (SQLException e) {
            System.out.println("Erreur connexion: " + e.getMessage());
        }
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
<<<<<<< Updated upstream
}
=======

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
>>>>>>> Stashed changes
