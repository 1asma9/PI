package edu.destination.tools;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private static MyConnection instance;
    private Connection cnx;

    private String url3306 = "jdbc:mysql://localhost:3306/voyage-1?useSSL=false&serverTimezone=UTC";
    private String url3307 = "jdbc:mysql://localhost:3307/voyage-1?useSSL=false&serverTimezone=UTC";
    private final String login = "root";
    private final String pwd = "";

    private MyConnection() {
        connect();
    }

    private void connect() {
        try {
            // Essayer d'abord le port 3306
            cnx = DriverManager.getConnection(url3306, login, pwd);
            System.out.println("✅ Connexion établie sur le port 3306 (destination)");
        } catch (SQLException e1) {
            System.out.println("⚠️ Échec port 3306, tentative sur port 3307...");
            try {
                // Repli sur le port 3307
                cnx = DriverManager.getConnection(url3307, login, pwd);
                System.out.println("✅ Connexion établie sur le port 3307 (destination)");
            } catch (SQLException e2) {
                System.out.println("❌ Erreur critique : Impossible de se connecter à la base de données.");
                System.out.println("Détails: " + e2.getMessage());
            }
        }
    }

    public static MyConnection getInstance() {
        if (instance == null)
            instance = new MyConnection();
        return instance;
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return cnx;
    }

}
