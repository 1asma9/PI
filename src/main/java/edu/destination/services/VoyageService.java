package edu.destination.services;

import edu.destination.entities.Voyage;
import edu.destination.interfaces.IService;
import edu.destination.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoyageService implements IService<Voyage> {

    // ================= AJOUT =================
    @Override
    public void addEntity(Voyage voyage) throws SQLException {
        String sql = "INSERT INTO voyage (date_depart, date_arrivee, point_depart, point_arrivee, prix, destination_id, paid) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setDate(1, voyage.getDateDepart() != null ? Date.valueOf(voyage.getDateDepart()) : null);
        pst.setDate(2, voyage.getDateArrivee() != null ? Date.valueOf(voyage.getDateArrivee()) : null);
        pst.setString(3, voyage.getPointDepart());
        pst.setString(4, voyage.getPointArrivee());
        pst.setDouble(5, voyage.getPrix());
        pst.setInt(6, voyage.getDestinationId());
        pst.setInt(7, voyage.getPaid());
        pst.executeUpdate();
        System.out.println("Voyage ajouté");
    }

    // ================= AJOUT2 =================
    @Override
    public void addEntity2(Voyage voyage) throws SQLException {
        addEntity(voyage);
    }

    // ================= SUPPRESSION =================
    @Override
    public void deleteEntity(Voyage voyage) {
        String sql = "DELETE FROM voyage WHERE id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, voyage.getId());
            pst.executeUpdate();
            System.out.println("Voyage supprimé");
        } catch (SQLException e) {
            System.out.println("Erreur suppression : " + e.getMessage());
        }
    }

    // ================= MODIFICATION =================
    @Override
    public void update(int id, Voyage voyage) {
        String sql = "UPDATE voyage SET date_depart=?, date_arrivee=?, point_depart=?, " +
                "point_arrivee=?, prix=?, destination_id=?, paid=? WHERE id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setDate(1, voyage.getDateDepart() != null ? Date.valueOf(voyage.getDateDepart()) : null);
            pst.setDate(2, voyage.getDateArrivee() != null ? Date.valueOf(voyage.getDateArrivee()) : null);
            pst.setString(3, voyage.getPointDepart());
            pst.setString(4, voyage.getPointArrivee());
            pst.setDouble(5, voyage.getPrix());
            pst.setInt(6, voyage.getDestinationId());
            pst.setInt(7, voyage.getPaid());
            pst.setInt(8, id);
            pst.executeUpdate();
            System.out.println("Voyage modifié");
        } catch (SQLException e) {
            System.out.println("Erreur modification : " + e.getMessage());
        }
    }

    // ================= AFFICHAGE TOUS =================
    @Override
    public List<Voyage> getData() {
        List<Voyage> list = new ArrayList<>();
        String sql = "SELECT * FROM voyage";
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur affichage : " + e.getMessage());
        }
        return list;
    }

    // ================= FILTRAGE PAR DESTINATION =================
    public List<Voyage> getVoyagesByDestination(int destinationId) {
        List<Voyage> list = new ArrayList<>();
        String sql = "SELECT * FROM voyage WHERE destination_id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, destinationId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur filtrage : " + e.getMessage());
        }
        return list;
    }

    // ================= VOYAGES NON PAYÉS =================
    public List<Voyage> getVoyagesNonPayes() {
        List<Voyage> list = new ArrayList<>();
        String sql = "SELECT * FROM voyage WHERE paid=0";
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        return list;
    }

    // ================= RÉSERVER =================
    public boolean reserverVoyage(int voyageId, int userId) {
        if (isDejaReserve(voyageId, userId)) return false;
        String sql = "INSERT INTO voyage_reservations (voyage_id, users_id) VALUES (?, ?)";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, voyageId);
            pst.setInt(2, userId);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur réservation : " + e.getMessage());
            return false;
        }
    }

    // ================= ANNULER =================
    public boolean annulerReservation(int voyageId, int userId) {
        String sql = "DELETE FROM voyage_reservations WHERE voyage_id=? AND users_id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, voyageId);
            pst.setInt(2, userId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur annulation : " + e.getMessage());
            return false;
        }
    }

    // ================= VÉRIFIER SI DÉJÀ RÉSERVÉ =================
    public boolean isDejaReserve(int voyageId, int userId) {
        String sql = "SELECT COUNT(*) FROM voyage_reservations WHERE voyage_id=? AND users_id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, voyageId);
            pst.setInt(2, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Erreur vérification : " + e.getMessage());
        }
        return false;
    }

    // ================= VOYAGES PAR UTILISATEUR =================
    public List<Voyage> getVoyagesByUser(int userId) {
        List<Voyage> list = new ArrayList<>();
        String sql = "SELECT v.*, vr.paid as user_paid FROM voyage v "
                + "INNER JOIN voyage_reservations vr ON v.id = vr.voyage_id "
                + "WHERE vr.users_id = ? ORDER BY v.date_depart DESC";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Voyage v = mapRow(rs);
                v.setPaid(rs.getInt("user_paid"));
                list.add(v);
            }
        } catch (SQLException e) {
            System.out.println("Erreur getVoyagesByUser : " + e.getMessage());
        }
        return list;
    }

    // ================= UTILITAIRE — ResultSet → Voyage =================
    private Voyage mapRow(ResultSet rs) throws SQLException {
        Voyage v = new Voyage();
        v.setId(rs.getInt("id"));

        Date dateDepart = rs.getDate("date_depart");
        if (dateDepart != null) v.setDateDepart(dateDepart.toLocalDate());

        Date dateArrivee = rs.getDate("date_arrivee");
        if (dateArrivee != null) v.setDateArrivee(dateArrivee.toLocalDate());

        v.setPointDepart(rs.getString("point_depart"));
        v.setPointArrivee(rs.getString("point_arrivee"));
        v.setPrix(rs.getDouble("prix"));
        v.setDestinationId(rs.getInt("destination_id"));
        try { v.setPaid(rs.getInt("paid")); } catch (Exception ignored) {}        return v;
    }
    public List<Integer> getUserIdsByVoyage(int voyageId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT users_id FROM voyage_reservations WHERE voyage_id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, voyageId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) ids.add(rs.getInt("users_id"));
        } catch (SQLException e) {
            System.out.println("Erreur getUserIdsByVoyage : " + e.getMessage());
        }
        return ids;
    }
    public boolean isPaidByUser(int voyageId, int userId) {
        String sql = "SELECT v.paid FROM voyage v " +
                "INNER JOIN voyage_reservations vr ON v.id = vr.voyage_id " +
                "WHERE vr.voyage_id=? AND vr.users_id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, voyageId);
            pst.setInt(2, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt("paid") == 1;
        } catch (SQLException e) {
            System.out.println("Erreur isPaidByUser : " + e.getMessage());
        }
        return false;
    }
    // ================= MARQUER COMME PAYÉ (par utilisateur) =================
    public boolean marquerCommePaye(int voyageId, int userId) {
        String sql = "UPDATE voyage_reservations SET paid = 1 WHERE voyage_id = ? AND users_id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, voyageId);
            pst.setInt(2, userId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur marquerCommePaye : " + e.getMessage());
            return false;
        }
    }
}