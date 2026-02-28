package edu.destination.services;

import edu.destination.entities.Destination;
import edu.destination.interfaces.IService;
import edu.destination.tools.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DestinationService implements IService<Destination> {

    // ================= AJOUT (Statement simple) =================
    @Override
    public void addEntity(Destination destination) throws SQLException {

        String sql = "INSERT INTO destination " +
                "(nom, pays, description, statut, meilleure_saison, latitude, longitude, nb_visites, prix, date_depart, date_arrivee) " +
                "VALUES ('" + destination.getNom() + "','" +
                destination.getPays() + "','" +
                destination.getDescription() + "'," +
                destination.getStatut() + ",'" +
                destination.getMeilleureSaison() + "'," +
                destination.getLatitude() + "," +
                destination.getLongitude() + "," +
                destination.getNbVisites() + "," +
                destination.getPrix() + ",'" +
                destination.getDateDepart() + "','" +
                destination.getDateArrivee() + "')";

        Statement st = new MyConnection().getCnx().createStatement();
        st.executeUpdate(sql);
        System.out.println("Destination ajoutée");
    }

    // ================= AJOUT (PreparedStatement) =================
    @Override
    public void addEntity2(Destination destination) throws SQLException {

        String sql = "INSERT INTO destination " +
                "(nom, pays, description, statut, meilleure_saison, latitude, longitude, nb_visites, prix, date_depart, date_arrivee) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = new MyConnection().getCnx().prepareStatement(sql);

        pst.setString(1, destination.getNom());
        pst.setString(2, destination.getPays());
        pst.setString(3, destination.getDescription());
        pst.setBoolean(4, destination.getStatut());
        pst.setString(5, destination.getMeilleureSaison());
        pst.setDouble(6, destination.getLatitude());
        pst.setDouble(7, destination.getLongitude());
        pst.setInt(8, destination.getNbVisites());
        pst.setDouble(9, destination.getPrix());
        pst.setDate(10, Date.valueOf(destination.getDateDepart()));
        pst.setDate(11, Date.valueOf(destination.getDateArrivee()));

        pst.executeUpdate();
        System.out.println("Destination ajoutée");
    }

    // ================= SUPPRESSION =================
    @Override
    public void deleteEntity(Destination destination) {

        String sql = "DELETE FROM destination WHERE id_destination=?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, destination.getIdDestination());
            pst.executeUpdate();
            System.out.println("Destination supprimée");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ================= MODIFICATION =================
    @Override
    public void update(int id, Destination destination) {

        String sql = "UPDATE destination SET " +
                "nom=?, pays=?, description=?, statut=?, meilleure_saison=?, " +
                "latitude=?, longitude=?, nb_visites=?, prix=?, date_depart=?, date_arrivee=? " +
                "WHERE id_destination=?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);

            pst.setString(1, destination.getNom());
            pst.setString(2, destination.getPays());
            pst.setString(3, destination.getDescription());
            pst.setBoolean(4, destination.getStatut());
            pst.setString(5, destination.getMeilleureSaison());
            pst.setDouble(6, destination.getLatitude());
            pst.setDouble(7, destination.getLongitude());
            pst.setInt(8, destination.getNbVisites());
            pst.setDouble(9, destination.getPrix());
            pst.setDate(10, Date.valueOf(destination.getDateDepart()));
            pst.setDate(11, Date.valueOf(destination.getDateArrivee()));
            pst.setInt(12, id);

            pst.executeUpdate();
            System.out.println("Destination modifiée");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ================= AFFICHAGE =================
    @Override
    public List<Destination> getData() {

        List<Destination> list = new ArrayList<>();
        String sql = "SELECT * FROM destination";

        try {
            Statement st = new MyConnection().getCnx().createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                Destination d = new Destination();

                d.setIdDestination(rs.getInt("id_destination"));
                d.setNom(rs.getString("nom"));
                d.setPays(rs.getString("pays"));
                d.setDescription(rs.getString("description"));
                d.setStatut(rs.getBoolean("statut"));
                d.setMeilleureSaison(rs.getString("meilleure_saison"));
                d.setLatitude(rs.getDouble("latitude"));
                d.setLongitude(rs.getDouble("longitude"));
                d.setNbVisites(rs.getInt("nb_visites"));
                d.setPrix(rs.getDouble("prix"));

                Date depart = rs.getDate("date_depart");
                if(depart != null) d.setDateDepart(depart.toLocalDate());

                Date arrivee = rs.getDate("date_arrivee");
                if(arrivee != null) d.setDateArrivee(arrivee.toLocalDate());

                list.add(d);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }
}