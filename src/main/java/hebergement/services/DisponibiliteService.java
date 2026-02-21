package hebergement.services;

import hebergement.entities.Disponibilite;
import hebergement.tools.MyConnection;
import hebergement.interfaces.Iservice;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DisponibiliteService implements Iservice<Disponibilite> {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    // ===== CREATE (PreparedStatement → OFFICIEL) =====
    @Override
    public void addEntity(Disponibilite d) throws SQLException {
        String sql = """
            INSERT INTO disponibilite(hebergement_id, date_debut, date_fin, disponible)
            VALUES (?, ?, ?, ?)
        """;
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, d.getHebergementId());
        pst.setDate(2, Date.valueOf(d.getDateDebut()));
        pst.setDate(3, Date.valueOf(d.getDateFin()));
        pst.setBoolean(4, d.isDisponible());
        pst.executeUpdate();
    }

    // ===== CREATE (Statement → pédagogique) =====
    public void addEntityStatement(Disponibilite d) throws SQLException {
        Statement st = cnx.createStatement();
        String sql = "INSERT INTO disponibilite(hebergement_id, date_debut, date_fin, disponible) VALUES ("
                + d.getHebergementId() + ", '"
                + Date.valueOf(d.getDateDebut()) + "', '"
                + Date.valueOf(d.getDateFin()) + "', "
                + (d.isDisponible() ? 1 : 0) + ")";
        st.executeUpdate(sql);
    }

    @Override
    public void update(int id, Disponibilite d) throws SQLException {
        PreparedStatement pst = cnx.prepareStatement("""
            UPDATE disponibilite
            SET hebergement_id=?, date_debut=?, date_fin=?, disponible=?
            WHERE id=?
        """);
        pst.setInt(1, d.getHebergementId());
        pst.setDate(2, Date.valueOf(d.getDateDebut()));
        pst.setDate(3, Date.valueOf(d.getDateFin()));
        pst.setBoolean(4, d.isDisponible());
        pst.setInt(5, id);
        pst.executeUpdate();
    }

    @Override
    public void deleteEntity(Disponibilite d) throws SQLException {
        PreparedStatement pst =
                cnx.prepareStatement("DELETE FROM disponibilite WHERE id=?");
        pst.setInt(1, d.getId());
        pst.executeUpdate();
    }

    @Override
    public List<Disponibilite> getData() throws SQLException {
        List<Disponibilite> list = new ArrayList<>();
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM disponibilite");

        while (rs.next()) {
            Disponibilite d = new Disponibilite();
            d.setId(rs.getInt("id"));
            d.setHebergementId(rs.getInt("hebergement_id"));
            d.setDateDebut(rs.getDate("date_debut").toLocalDate());
            d.setDateFin(rs.getDate("date_fin").toLocalDate());
            d.setDisponible(rs.getBoolean("disponible"));
            list.add(d);
        }
        return list;
    }
    public List<Disponibilite> getByHebergement(int hebergementId) throws SQLException {
        List<Disponibilite> list = new ArrayList<>();
        String sql = """
    SELECT * FROM disponibilite
    WHERE hebergement_id=?
    ORDER BY id DESC
""";


        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, hebergementId);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Disponibilite d = new Disponibilite();
            d.setId(rs.getInt("id"));
            d.setHebergementId(rs.getInt("hebergement_id"));
            d.setDateDebut(rs.getDate("date_debut").toLocalDate());
            d.setDateFin(rs.getDate("date_fin").toLocalDate());
            d.setDisponible(rs.getBoolean("disponible"));
            list.add(d);
        }

        return list;
    }

}
