package edu.connexion3a8.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    // ✅ Même base que Symfony : voyage sur port 3306
    private static final String URL   = "jdbc:mysql://127.0.0.1:3306/voyage?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    private static final String LOGIN = "root";
    private static final String PWD   = "";

    private Connection cnx;
    private static MyConnection instance;

    private MyConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(URL, LOGIN, PWD);
            System.out.println("✅ Connecté à voyage (port 3306) !");
        } catch (Exception e) {
            System.out.println("❌ Connexion échouée : " + e.getMessage());
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
                instance = null;
                instance = new MyConnection();
                return instance.cnx;
            }
        } catch (SQLException e) {
            instance = null;
            instance = new MyConnection();
        }
        return cnx;
    }
}