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
    public void addEntity(Hebergement h) throws SQLException {

        String sql =
                "INSERT INTO hebergement(id_user, description, adresse, prix, type_id, image_path, latitude, longitude) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            // id_user
            if (h.getIdUser() == null) pst.setNull(1, Types.INTEGER);
            else pst.setInt(1, h.getIdUser());

            pst.setString(2, h.getDescription());
            pst.setString(3, h.getAdresse());
            pst.setDouble(4, h.getPrix());

            if (h.getTypeId() == null) pst.setNull(5, Types.INTEGER);
            else pst.setInt(5, h.getTypeId());

            pst.setString(6, h.getImagePath());

            if (h.getLatitude() == null) pst.setNull(7, Types.DOUBLE);
            else pst.setDouble(7, h.getLatitude());

            if (h.getLongitude() == null) pst.setNull(8, Types.DOUBLE);
            else pst.setDouble(8, h.getLongitude());

            pst.executeUpdate();
        }
    }

    // ===== CREATE (Statement) pédagogique =====
    // ⚠️ conseillé: utiliser addEntity() avec PreparedStatement
    public void addEntityStatement(Hebergement h) throws SQLException {
        throw new UnsupportedOperationException("Utilise addEntity(Hebergement) avec PreparedStatement.");
    }

    // ===== UPDATE (PreparedStatement) =====
    @Override
    public void update(int id, Hebergement h) throws SQLException {

        String sql =
                "UPDATE hebergement SET description=?, adresse=?, prix=?, type_id=?, image_path=?, latitude=?, longitude=? " +
                        "WHERE id=?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, h.getDescription());
            pst.setString(2, h.getAdresse());
            pst.setDouble(3, h.getPrix());

            if (h.getTypeId() == null) pst.setNull(4, Types.INTEGER);
            else pst.setInt(4, h.getTypeId());

            pst.setString(5, h.getImagePath());

            if (h.getLatitude() == null) pst.setNull(6, Types.DOUBLE);
            else pst.setDouble(6, h.getLatitude());

            if (h.getLongitude() == null) pst.setNull(7, Types.DOUBLE);
            else pst.setDouble(7, h.getLongitude());

            pst.setInt(8, id);

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
    // ⚠️ inclu id_user aussi (sinon tu perds la relation)
    public int addEntityReturnId(Hebergement h) throws SQLException {

        String sql =
                "INSERT INTO hebergement(id_user, description, adresse, prix, type_id, image_path, latitude, longitude) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (h.getIdUser() == null) pst.setNull(1, Types.INTEGER);
            else pst.setInt(1, h.getIdUser());

            pst.setString(2, h.getDescription());
            pst.setString(3, h.getAdresse());
            pst.setDouble(4, h.getPrix());

            if (h.getTypeId() == null) pst.setNull(5, Types.INTEGER);
            else pst.setInt(5, h.getTypeId());

            pst.setString(6, h.getImagePath());

            if (h.getLatitude() == null) pst.setNull(7, Types.DOUBLE);
            else pst.setDouble(7, h.getLatitude());

            if (h.getLongitude() == null) pst.setNull(8, Types.DOUBLE);
            else pst.setDouble(8, h.getLongitude());

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

        String sql =
                "SELECT h.*, t.libelle AS type_libelle " +
                        "FROM hebergement h " +
                        "LEFT JOIN type_hebergement t ON h.type_id = t.id " +
                        "ORDER BY h.id ASC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Hebergement h = mapRow(rs);
                list.add(h);
            }
        }

        return list;
    }

    // ===== READ BY USER (UNE SEULE FOIS ✅) =====
    public List<Hebergement> getByUser(int idUser) throws SQLException {
        List<Hebergement> list = new ArrayList<>();

        String sql =
                "SELECT h.*, t.libelle AS type_libelle " +
                        "FROM hebergement h " +
                        "LEFT JOIN type_hebergement t ON h.type_id = t.id " +
                        "WHERE h.id_user = ? " +
                        "ORDER BY h.id DESC";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, idUser);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // ===== Mapper (évite duplication) =====
    private Hebergement mapRow(ResultSet rs) throws SQLException {
        Hebergement h = new Hebergement();
        h.setId(rs.getInt("id"));
        h.setDescription(rs.getString("description"));
        h.setAdresse(rs.getString("adresse"));
        h.setPrix(rs.getDouble("prix"));
        h.setTypeId((Integer) rs.getObject("type_id"));
        h.setTypeLibelle(rs.getString("type_libelle"));
        h.setImagePath(rs.getString("image_path"));
        h.setLatitude((Double) rs.getObject("latitude"));
        h.setLongitude((Double) rs.getObject("longitude"));
        h.setIdUser((Integer) rs.getObject("id_user"));
        return h;
    }
}