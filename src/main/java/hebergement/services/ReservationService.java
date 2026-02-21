package hebergement.services;

import hebergement.entities.Reservation;
import hebergement.interfaces.Iservice;
import hebergement.tools.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements Iservice<Reservation> {

    private final Connection cnx = MyConnection.getInstance().getCnx();


    public boolean isHebergementAvailable(int hebId, LocalDate debut, LocalDate fin) throws SQLException {
        String sql = """
            SELECT COUNT(*) AS c
            FROM reservation
            WHERE hebergement_id=?
              AND statut <> 'ANNULE'
              AND ? < date_fin
              AND ? > date_debut
        """;

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, hebId);
            pst.setDate(2, Date.valueOf(debut));
            pst.setDate(3, Date.valueOf(fin));

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("c") == 0;
            }
        }
        return false;
    }


    public boolean isInsideDisponibilite(int hebId, LocalDate debut, LocalDate fin) throws SQLException {
        String sql = """
            SELECT COUNT(*) AS c
            FROM disponibilite
            WHERE hebergement_id=?
              AND disponible=1
              AND date_debut <= ?
              AND date_fin >= ?
        """;

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, hebId);
            pst.setDate(2, Date.valueOf(debut));
            pst.setDate(3, Date.valueOf(fin));

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("c") > 0;
            }
        }
        return false;
    }

    // =========================================================
    // 3) ADD avec PreparedStatement
    // =========================================================
    @Override
    public void addEntity(Reservation r) throws SQLException {

        String sql = """
            INSERT INTO reservation(hebergement_id, client_nom, client_tel, client_email,
                                    date_debut, date_fin, nb_nuits, total, statut)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, r.getHebergementId());
            pst.setString(2, r.getClientNom());
            pst.setString(3, r.getClientTel());
            pst.setString(4, r.getClientEmail());
            pst.setDate(5, Date.valueOf(r.getDateDebut()));
            pst.setDate(6, Date.valueOf(r.getDateFin()));
            pst.setInt(7, r.getNbNuits());
            pst.setDouble(8, r.getTotal());
            pst.setString(9, r.getStatut() == null ? "EN_ATTENTE" : r.getStatut());

            pst.executeUpdate();
        }
    }

    // =========================================================
    // 4) ADD avec Statement
    // =========================================================
    public void addEntityStatement(Reservation r) throws SQLException {

        String statut = (r.getStatut() == null) ? "EN_ATTENTE" : r.getStatut();
        String tel = (r.getClientTel() == null) ? "" : r.getClientTel();
        String email = (r.getClientEmail() == null) ? "" : r.getClientEmail();

        String sql = "INSERT INTO reservation(hebergement_id, client_nom, client_tel, client_email, " +
                "date_debut, date_fin, nb_nuits, total, statut) VALUES (" +
                r.getHebergementId() + ", '" +
                r.getClientNom() + "', '" +
                tel + "', '" +
                email + "', '" +
                Date.valueOf(r.getDateDebut()) + "', '" +
                Date.valueOf(r.getDateFin()) + "', " +
                r.getNbNuits() + ", " +
                r.getTotal() + ", '" +
                statut + "')";

        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    // =========================================================
    // 5) UPDATE
    // =========================================================
    @Override
    public void update(int id, Reservation r) throws SQLException {

        String sql = """
            UPDATE reservation
            SET hebergement_id=?, client_nom=?, client_tel=?, client_email=?,
                date_debut=?, date_fin=?, nb_nuits=?, total=?, statut=?
            WHERE id=?
        """;

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, r.getHebergementId());
            pst.setString(2, r.getClientNom());
            pst.setString(3, r.getClientTel());
            pst.setString(4, r.getClientEmail());
            pst.setDate(5, Date.valueOf(r.getDateDebut()));
            pst.setDate(6, Date.valueOf(r.getDateFin()));
            pst.setInt(7, r.getNbNuits());
            pst.setDouble(8, r.getTotal());
            pst.setString(9, r.getStatut());
            pst.setInt(10, id);

            pst.executeUpdate();
        }
    }

    // =========================================================
    // 6) DELETE
    // =========================================================
    @Override
    public void deleteEntity(Reservation r) throws SQLException {

        String sql = "DELETE FROM reservation WHERE id=?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, r.getId());
            pst.executeUpdate();
        }
    }

    // =========================================================
    // 7) READ ALL
    // =========================================================
    @Override
    public List<Reservation> getData() throws SQLException {

        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation ORDER BY id DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    // =========================================================
    // 8) READ by Hebergement
    // =========================================================
    public List<Reservation> getByHebergement(int hebergementId) throws SQLException {

        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE hebergement_id=? ORDER BY id DESC";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, hebergementId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    // =========================================================
    // 9) Update statut seulement
    // =========================================================
    public void updateStatus(int id, String statut) throws SQLException {
        String sql = "UPDATE reservation SET statut=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, statut);
            pst.setInt(2, id);
            pst.executeUpdate();
        }
    }

    // =========================================================
    // Mapper ResultSet -> Reservation
    // =========================================================
    private Reservation map(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setHebergementId(rs.getInt("hebergement_id"));
        r.setClientNom(rs.getString("client_nom"));
        r.setClientTel(rs.getString("client_tel"));
        r.setClientEmail(rs.getString("client_email"));
        r.setDateDebut(rs.getDate("date_debut").toLocalDate());
        r.setDateFin(rs.getDate("date_fin").toLocalDate());
        r.setNbNuits(rs.getInt("nb_nuits"));
        r.setTotal(rs.getDouble("total"));
        r.setStatut(rs.getString("statut"));
        return r;
    }
}
