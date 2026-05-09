package edu.connexion3a8.services;

import edu.connexion3a8.entities.Personne;
import edu.connexion3a8.interfaces.IService;
import edu.connexion3a8.tools.MyConnection;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class PersonneService implements IService<Personne> {
    @Override
    public void addEntity(Personne personne) throws SQLException {
        String requete = "INSERT INTO personne (nom,prenom)" +
                "VALUES ('"+personne.getNom()+"','"+personne.getPrenom()+"')";
        Statement st = new MyConnection().getCnx().createStatement();
        st.executeUpdate(requete);
        System.out.println("Personne ajouté");
    }

    @Override
    public void deleteEntity(Personne personne) {

    }

    @Override
    public void update(int id, Personne personne) {

    }

    @Override
    public List<Personne> getData() {
        return null;
    }
}
