package edu.destination.services;

import edu.destination.entities.Transport;
import edu.destination.interfaces.IService;
import edu.destination.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransportService implements IService<Transport> {

    // ================= AJOUT =================
    @Override
    public void addEntity(Transport transport) throws SQLException {
        String sql = "INSERT INTO transport (type_transport, voyage_id) VALUES (?, ?)";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setString(1, transport.getTypeTransport());
        pst.setInt(2, transport.getVoyageId());
        pst.executeUpdate();
        System.out.println("Transport ajouté");
    }

    // ================= AJOUT2 =================
    @Override
    public void addEntity2(Transport transport) throws SQLException {
        addEntity(transport);
    }

    // ================= SUPPRESSION =================
    @Override
    public void deleteEntity(Transport transport) {
        String sql = "DELETE FROM transport WHERE id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, transport.getId());
            pst.executeUpdate();
            System.out.println("Transport supprimé");
        } catch (SQLException e) {
            System.out.println("Erreur suppression : " + e.getMessage());
        }
    }

    // ================= MODIFICATION =================
    @Override
    public void update(int id, Transport transport) {
        String sql = "UPDATE transport SET type_transport=?, voyage_id=? WHERE id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setString(1, transport.getTypeTransport());
            pst.setInt(2, transport.getVoyageId());
            pst.setInt(3, id);
            pst.executeUpdate();
            System.out.println("Transport modifié");
        } catch (SQLException e) {
            System.out.println("Erreur modification : " + e.getMessage());
        }
    }

    // ================= AFFICHAGE =================
    @Override
    public List<Transport> getData() {
        List<Transport> list = new ArrayList<>();
        String sql = "SELECT * FROM transport";
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Transport t = new Transport();
                t.setId(rs.getInt("id"));
                t.setTypeTransport(rs.getString("type_transport"));
                t.setVoyageId(rs.getInt("voyage_id"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.out.println("Erreur affichage : " + e.getMessage());
        }
        return list;
    }

    // ================= FILTRAGE PAR VOYAGE =================
    public List<Transport> getTransportsByVoyage(int voyageId) {
        List<Transport> list = new ArrayList<>();
        String sql = "SELECT * FROM transport WHERE voyage_id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, voyageId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Transport t = new Transport();
                t.setId(rs.getInt("id"));
                t.setTypeTransport(rs.getString("type_transport"));
                t.setVoyageId(rs.getInt("voyage_id"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.out.println("Erreur filtrage : " + e.getMessage());
        }
        return list;
    }
}