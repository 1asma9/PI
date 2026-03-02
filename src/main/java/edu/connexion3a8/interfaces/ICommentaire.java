package edu.connexion3a8.interfaces;

import edu.connexion3a8.entities.Commentaire;

import java.sql.SQLException;
import java.util.List;

public interface ICommentaire<T> {

    void ajouter(T t, int blogId) throws SQLException;  // ← 2 paramètres !

    void modifier(T t) throws SQLException;

    void supprimer(int id) throws SQLException;

    List<T> afficher() throws SQLException;

    T afficher1(int id) throws SQLException;

    List<T> afficherParBlog(int blogId) throws SQLException;  // ← Nouvelle méthode

    void ajouterLike(int id) throws SQLException;

    void retirerLike(int id) throws SQLException;

    Commentaire getById(Integer commentaireId) throws SQLException;
}