package edu.destination.tools;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private static MyConnection instance;
    private Connection cnx;

    private final String url = "jdbc:mysql://localhost:3306/voyage-1?useSSL=false&serverTimezone=UTC";
    private final String login = "root";
    private final String pwd = "";

    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("Connexion établie (destination)");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyConnection getInstance() {
        if (instance == null)
            instance = new MyConnection();
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }

}
