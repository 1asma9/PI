package edu.connexion3a8.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private final String URL = "jdbc:mysql://localhost:3306/pi";
    private final String LOGIN = "root";
    private final String PWD = "";

    private Connection cnx;
    private static MyConnection instance;

    public MyConnection() {
        try {
            cnx = DriverManager.getConnection(URL, LOGIN, PWD);
            System.out.println("Connexion établie!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
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