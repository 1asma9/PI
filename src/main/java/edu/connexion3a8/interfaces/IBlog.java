package edu.connexion3a8.interfaces;

import java.sql.SQLException;
import java.util.List;

public interface IBlog <T>{
    void ajouter(T t) throws SQLException;

    void modifier(T t) throws SQLException;

    void supprimer(int id) throws SQLException;

    List<T> afficher() throws SQLException;

    T afficher1(int id) throws SQLException;
}
