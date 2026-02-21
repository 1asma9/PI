package hebergement.services;

import hebergement.entities.Hebergement;
import hebergement.interfaces.Iservice;
import hebergement.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HebergementService implements Iservice<Hebergement> {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    // ===== CREATE (PreparedStatement) =====
    @Override
    public void addEntity(Hebergement h) throws SQLException {

        String sql = """
            INSERT INTO hebergement(description, adresse, prix, type_id, image_path)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, h.getDescription());
            pst.setString(2, h.getAdresse());
            pst.setDouble(3, h.getPrix());

            if (h.getTypeId() == null) pst.setNull(4, Types.INTEGER);
            else pst.setInt(4, h.getTypeId());

            pst.setString(5, h.getImagePath()); // ✅ image path
            pst.executeUpdate();
        }
    }

    // ===== CREATE (Statement) pédagogique =====
    public void addEntityStatement(Hebergement h) throws SQLException {

        String typeVal = (h.getTypeId() == null) ? "NULL" : h.getTypeId().toString();
        String imgVal = (h.getImagePath() == null) ? "NULL" : ("'" + h.getImagePath() + "'");

        String sql = "INSERT INTO hebergement(description, adresse, prix, type_id, image_path) VALUES ('"
                + h.getDescription() + "', '"
                + h.getAdresse() + "', "
                + h.getPrix() + ", "
                + typeVal + ", "
                + imgVal + ")";

        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    // ===== UPDATE (PreparedStatement) =====
    @Override
    public void update(int id, Hebergement h) throws SQLException {

        String sql = """
            UPDATE hebergement
            SET description=?, adresse=?, prix=?, type_id=?, image_path=?
            WHERE id=?
        """;

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, h.getDescription());
            pst.setString(2, h.getAdresse());
            pst.setDouble(3, h.getPrix());

            if (h.getTypeId() == null) pst.setNull(4, Types.INTEGER);
            else pst.setInt(4, h.getTypeId());

            pst.setString(5, h.getImagePath()); // ✅ image path
            pst.setInt(6, id);

            pst.executeUpdate();
        }
    }

    // ===== DELETE =====
    @Override
    public void deleteEntity(Hebergement h) throws SQLException {
        try (PreparedStatement pst = cnx.prepareStatement("DELETE FROM hebergement WHERE id=?")) {
            pst.setInt(1, h.getId());
            pst.executeUpdate();
        }
    }

    // ===== CREATE + RETURN ID =====
    public int addEntityReturnId(Hebergement h) throws SQLException {

        String sql = """
            INSERT INTO hebergement(description, adresse, prix, type_id, image_path)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, h.getDescription());
            pst.setString(2, h.getAdresse());
            pst.setDouble(3, h.getPrix());

            if (h.getTypeId() == null) pst.setNull(4, Types.INTEGER);
            else pst.setInt(4, h.getTypeId());

            pst.setString(5, h.getImagePath());

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // ===== READ ALL =====
    @Override
    public List<Hebergement> getData() throws SQLException {

        List<Hebergement> list = new ArrayList<>();
        String sql = """
        SELECT h.*, t.libelle AS type_libelle
        FROM hebergement h
        LEFT JOIN type_hebergement t ON h.type_id = t.id
        ORDER BY h.id ASC
    """;

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Hebergement h = new Hebergement();
                h.setId(rs.getInt("id"));
                h.setDescription(rs.getString("description"));
                h.setAdresse(rs.getString("adresse"));
                h.setPrix(rs.getDouble("prix"));
                h.setTypeId((Integer) rs.getObject("type_id"));
                h.setTypeLibelle(rs.getString("type_libelle"));
                h.setImagePath(rs.getString("image_path"));
                list.add(h);
            }
        }

        return list;
    }

}
