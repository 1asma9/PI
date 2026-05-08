package edu.connexion3a8.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private static MyConnection instance;
    private Connection cnx;

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
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
