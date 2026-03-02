package edu.destination.services;

import edu.destination.entities.Transport;
import edu.destination.interfaces.IService;
import edu.destination.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransportService implements IService<Transport> {

    // ================= AJOUT (Statement simple) =================
    @Override
    public void addEntity(Transport transport) throws SQLException {
        String sql = "INSERT INTO transport (type_transport, id_destination) VALUES ('"
                + transport.getTypeTransport() + "', "
                + transport.getIdDestination() + ")";

        Statement st = new MyConnection().getCnx().createStatement();
        st.executeUpdate(sql);
        System.out.println("Transport ajouté (Statement)");
    }

    // ================= AJOUT (PreparedStatement) =================
    @Override
    public void addEntity2(Transport transport) throws SQLException {
        String sql = "INSERT INTO transport (type_transport, id_destination) VALUES (?, ?)";
        PreparedStatement pst = new MyConnection().getCnx().prepareStatement(sql);
        pst.setString(1, transport.getTypeTransport());
        pst.setInt(2, transport.getIdDestination());
        pst.executeUpdate();
        System.out.println("Transport ajouté (PreparedStatement)");
    }

    // ================= SUPPRESSION =================
    @Override
    public void deleteEntity(Transport transport) {
        String sql = "DELETE FROM transport WHERE id_transport = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, transport.getIdTransport());
            pst.executeUpdate();
            System.out.println("Transport supprimé");
        } catch (SQLException e) {
            System.out.println("Erreur suppression : " + e.getMessage());
        }
    }

    // ================= MODIFICATION =================
    @Override
    public void update(int id, Transport transport) {
        String sql = "UPDATE transport SET type_transport = ?, id_destination = ? WHERE id_transport = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setString(1, transport.getTypeTransport());
            pst.setInt(2, transport.getIdDestination());
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
            Statement st = new MyConnection().getCnx().createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Transport t = new Transport();
                t.setIdTransport(rs.getInt("id_transport"));
                t.setTypeTransport(rs.getString("type_transport")); // <-- ici
                t.setIdDestination(rs.getInt("id_destination"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.out.println("Erreur affichage : " + e.getMessage());
        }
        return list;
    }

    // ================= FILTRAGE PAR DESTINATION =================
    public List<Transport> getTransportsByDestination(int idDestination) {
        List<Transport> list = new ArrayList<>();
        String sql = "SELECT * FROM transport WHERE id_destination = ?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, idDestination);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Transport t = new Transport();
                t.setIdTransport(rs.getInt("id_transport"));
                t.setTypeTransport(rs.getString("type_transport")); // <-- ici aussi
                t.setIdDestination(rs.getInt("id_destination"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.out.println("Erreur filtrage : " + e.getMessage());
        }

        return list;
    }
}