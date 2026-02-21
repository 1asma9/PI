package hebergement.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private static MyConnection instance;

    private static final String URL =
            "jdbc:mysql://localhost:3306/hebergement?useSSL=false&serverTimezone=UTC";


    private static final String LOGIN = "root";
    private static final String PWD = "";

    private Connection cnx;

    public static MyConnection getInstance() {
        if (instance == null) instance = new MyConnection();
        return instance;
    }

    private MyConnection() {
        try {
            // ✅ Force load driver (important if driver not auto-registered)
            Class.forName("com.mysql.cj.jdbc.Driver");

            cnx = DriverManager.getConnection(URL, LOGIN, PWD);
            System.out.println("Connexion etablie!");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver MySQL introuvable. Vérifie mysql-connector-j dans pom.xml");
        } catch (SQLException e) {
            System.out.println("Erreur connexion: " + e.getMessage());
        }
    }

    public Connection getCnx() {
        return cnx;
    }
    public void close() {
        try {
            if (cnx != null && !cnx.isClosed()) {
                cnx.close();
                System.out.println("Connexion fermée.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
