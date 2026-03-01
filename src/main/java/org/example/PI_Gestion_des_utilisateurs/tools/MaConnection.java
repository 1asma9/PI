package org.example.PI_Gestion_des_utilisateurs.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MaConnection {


    private final String URL = "jdbc:mysql://localhost:3306/projet%20int%C3%A9gr%C3%A9?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
    private final String USER = "root";
    private final String PASS = "";
    private Connection connection;

    private static MaConnection instance;

    private MaConnection(){
        this.connection = openConnection();
    }

    public static MaConnection getInstance() {
        if(instance == null)
            instance = new MaConnection();
        return instance;
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = openConnection();
            }
        } catch (SQLException e) {
            connection = openConnection();
        }
        return connection;
    }

    public synchronized void closeConnection() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
        } finally {
            connection = null;
        }
    }

    private Connection openConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'établir la connexion à la base de données", e);
        }
    }
}

