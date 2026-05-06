package edu.destination.services;

import edu.destination.entities.Destination;
import edu.destination.interfaces.IService;
import edu.destination.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinationService implements IService<Destination> {

    // ================= AJOUT =================
    @Override
    public void addEntity(Destination destination) throws SQLException {
        String sql = "INSERT INTO destination " +
                "(nom, pays, description, statut, meilleure_saison, latitude, longitude, nb_visites, video_path, nb_likes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setString(1, destination.getNom());
        pst.setString(2, destination.getPays());
        pst.setString(3, destination.getDescription());
        pst.setBoolean(4, destination.getStatut());
        pst.setString(5, destination.getMeilleureSaison());
        pst.setDouble(6, destination.getLatitude());
        pst.setDouble(7, destination.getLongitude());
        pst.setInt(8, destination.getNbVisites());
        pst.setString(9, destination.getVideoPath());
        pst.setInt(10, destination.getNbLikes());
        pst.executeUpdate();
        System.out.println("Destination ajoutée");
    }

    // ================= AJOUT2 (même chose) =================
    @Override
    public void addEntity2(Destination destination) throws SQLException {
        addEntity(destination);
    }

    // ================= SUPPRESSION =================
    @Override
    public void deleteEntity(Destination destination) {
        String sql = "DELETE FROM destination WHERE id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, destination.getId());
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
                "latitude=?, longitude=?, nb_visites=?, video_path=?, nb_likes=? " +
                "WHERE id=?";
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
            pst.setString(9, destination.getVideoPath());
            pst.setInt(10, destination.getNbLikes());
            pst.setInt(11, id);
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
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Destination d = new Destination();
                d.setId(rs.getInt("id"));
                d.setNom(rs.getString("nom"));
                d.setPays(rs.getString("pays"));
                d.setDescription(rs.getString("description"));
                d.setStatut(rs.getBoolean("statut"));
                d.setMeilleureSaison(rs.getString("meilleure_saison"));
                d.setLatitude(rs.getDouble("latitude"));
                d.setLongitude(rs.getDouble("longitude"));
                d.setNbVisites(rs.getInt("nb_visites"));
                d.setVideoPath(rs.getString("video_path"));
                d.setNbLikes(rs.getInt("nb_likes"));
                list.add(d);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }
    // ================= LIKE =================
    public boolean toggleLike(int destinationId) {
        try {
            Destination d = getById(destinationId);
            if (d == null) return false;
            d.setNbLikes(d.getNbLikes() + 1);
            update(d.getId(), d);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= GET BY ID =================
    public Destination getById(int id) {
        String sql = "SELECT * FROM destination WHERE id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Destination d = new Destination();
                d.setId(rs.getInt("id"));
                d.setNom(rs.getString("nom"));
                d.setPays(rs.getString("pays"));
                d.setDescription(rs.getString("description"));
                d.setStatut(rs.getBoolean("statut"));
                d.setMeilleureSaison(rs.getString("meilleure_saison"));
                d.setLatitude(rs.getDouble("latitude"));
                d.setLongitude(rs.getDouble("longitude"));
                d.setNbVisites(rs.getInt("nb_visites"));
                d.setVideoPath(rs.getString("video_path"));
                d.setNbLikes(rs.getInt("nb_likes"));
                return d;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}