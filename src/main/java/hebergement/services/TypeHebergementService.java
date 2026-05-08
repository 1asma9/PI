package hebergement.services;

import hebergement.entities.TypeHebergement;
import hebergement.tools.MyConnection;
import hebergement.interfaces.Iservice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TypeHebergementService implements Iservice<TypeHebergement> {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    // ===== CREATE (PreparedStatement ) =====
    @Override
    public void addEntity(TypeHebergement t) throws SQLException {
        String sql = "INSERT INTO type_hebergement(libelle) VALUES (?)";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setString(1, t.getLibelle());
        pst.executeUpdate();
    }

    // ===== CREATE (Statement ) =====
    public void addEntityStatement(TypeHebergement t) throws SQLException {
        Statement st = cnx.createStatement();
        String sql = "INSERT INTO type_hebergement(libelle) VALUES ('" + t.getLibelle() + "')";
        st.executeUpdate(sql);
    }

    @Override
    public void update(int id, TypeHebergement t) throws SQLException {
        PreparedStatement pst =
                cnx.prepareStatement("UPDATE type_hebergement SET libelle=? WHERE id=?");
        pst.setString(1, t.getLibelle());
        pst.setInt(2, id);
        pst.executeUpdate();
    }

    @Override
    public void deleteEntity(TypeHebergement t) throws SQLException {
        PreparedStatement pst =
                cnx.prepareStatement("DELETE FROM type_hebergement WHERE id=?");
        pst.setInt(1, t.getId());
        pst.executeUpdate();
    }

    @Override
    public List<TypeHebergement> getData() throws SQLException {
        List<TypeHebergement> list = new ArrayList<>();
        String sql = "SELECT * FROM type_hebergement ORDER BY id DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new TypeHebergement(rs.getInt("id"), rs.getString("libelle")));
            }
        }
        return list;
    }

    public Integer getIdByLibelle(String libelle) throws SQLException {
        String sql = "SELECT id FROM type_hebergement WHERE libelle=? ORDER BY id DESC LIMIT 1";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, libelle);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return null;
    }


}
