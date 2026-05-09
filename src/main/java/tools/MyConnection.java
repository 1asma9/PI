package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private String url3309 = "jdbc:mysql://127.0.0.1:3309/voyage-1?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private String url3306 = "jdbc:mysql://127.0.0.1:3306/voyage-1?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private String url3307 = "jdbc:mysql://127.0.0.1:3307/voyage-1?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private String login = "root";
    private String pwd = "";
    private Connection cnx;
    private static MyConnection instance;

    private MyConnection() {
        connect();
    }

    private void connect() {
        // Essayer port 3309 en premier (XAMPP configuré sur 3309)
        String[] urls = { url3309, url3306, url3307 };
        int[] ports = { 3309, 3306, 3307 };
        for (int i = 0; i < urls.length; i++) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                cnx = DriverManager.getConnection(urls[i], login, pwd);
                System.out.println("✅ Connexion établie sur port " + ports[i] + " — voyage-1 !");
                return;
            } catch (Exception e) {
                System.out.println("⚠️ Port " + ports[i] + " échoué...");
            }
        }
        System.out.println("❌ Connexion impossible à voyage-1.");
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
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return cnx;
    }
}
