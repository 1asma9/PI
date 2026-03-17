package edu.pidev.services;

import edu.pidev.entities.ReservationActivite;
import edu.pidev.tools.MyConnection;
import edu.pidev.tools.validation.ReservationActiviteValidator;
import edu.pidev.tools.validation.ValidationException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationActiviteService {

    private final Connection cnx;

    public ReservationActiviteService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    private boolean activiteExiste(int idActivite) throws SQLException {
        String sql = "SELECT 1 FROM activite WHERE id_activite=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idActivite);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    // ✅ NEW: get prix of activite
    private double getPrixActivite(int idActivite) throws SQLException {
        String sql = "SELECT prix FROM activite WHERE id_activite=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idActivite);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("prix");
            }
        }
        throw new SQLException("Prix introuvable pour l'activité id=" + idActivite);
    }

    // ✅ NEW: métier total calculation
    public double calculerTotal(double prixUnitaire, int nbPersonnes) {
        double total = prixUnitaire * nbPersonnes;

        if (nbPersonnes >= 3 && nbPersonnes <= 5) {
            total *= 0.95; // -5%
        } else if (nbPersonnes >= 6) {
            total *= 0.90; // -10%
        }

        return Math.round(total * 100.0) / 100.0;
    }

    // CREATE (✅ now stores total)
    public void addReservation(ReservationActivite r) {
        try {
            // ✅ Contrôle de saisie
            ReservationActiviteValidator.validate(r);

            // ✅ Vérifier que l'activité existe
            if (!activiteExiste(r.getIdActivite())) {
                throw new ValidationException("Activité introuvable (id=" + r.getIdActivite() + ")");
            }

            // ✅ MÉTIER: total
            double prix = getPrixActivite(r.getIdActivite());
            double total = calculerTotal(prix, r.getNombrePersonnes());
            r.setTotal(total);

            String sql = """
                    INSERT INTO reservation_activite (date_reservation, nombre_personnes, statut, id_activite, total)
                    VALUES (?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(r.getDateReservation())); // LocalDate -> SQL Date
                ps.setInt(2, r.getNombrePersonnes());
                ps.setString(3, r.getStatut().trim().toUpperCase());
                ps.setInt(4, r.getIdActivite());
                ps.setDouble(5, r.getTotal());

                ps.executeUpdate();
                System.out.println("✅ Réservation ajoutée ! Total=" + r.getTotal());
            }

        } catch (ValidationException e) {
            System.out.println("🚫 Erreur saisie : " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ Erreur ajout réservation : " + e.getMessage());
        }
    }

    // READ ALL (✅ reads total)
    public List<ReservationActivite> getAllReservations() {
        List<ReservationActivite> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation_activite";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int idReservation = rs.getInt("id_reservation");
                LocalDate dateReservation = rs.getDate("date_reservation").toLocalDate();
                int nb = rs.getInt("nombre_personnes");
                String statut = rs.getString("statut");
                int idActivite = rs.getInt("id_activite");
                double total = rs.getDouble("total");

                list.add(new ReservationActivite(idReservation, dateReservation, nb, statut, idActivite, total));
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur getAllReservations : " + e.getMessage());
        }

        return list;
    }

    // UPDATE (✅ recompute + update total)
    public void updateReservation(ReservationActivite r) {
        try {
            // ✅ Contrôle de saisie
            ReservationActiviteValidator.validate(r);

            // id obligatoire en UPDATE
            if (r.getIdReservation() <= 0) {
                throw new ValidationException("idReservation invalide (doit être > 0) pour UPDATE.");
            }

            // ✅ Vérifier que l'activité existe
            if (!activiteExiste(r.getIdActivite())) {
                throw new ValidationException("Activité introuvable (id=" + r.getIdActivite() + ")");
            }

            // ✅ MÉTIER: total recalculé
            double prix = getPrixActivite(r.getIdActivite());
            double total = calculerTotal(prix, r.getNombrePersonnes());
            r.setTotal(total);

            String sql = """
                    UPDATE reservation_activite
                    SET date_reservation=?, nombre_personnes=?, statut=?, id_activite=?, total=?
                    WHERE id_reservation=?
                    """;

            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(r.getDateReservation()));
                ps.setInt(2, r.getNombrePersonnes());
                ps.setString(3, r.getStatut().trim().toUpperCase());
                ps.setInt(4, r.getIdActivite());
                ps.setDouble(5, r.getTotal());
                ps.setInt(6, r.getIdReservation());

                int rows = ps.executeUpdate();
                if (rows > 0) System.out.println("✏️ Réservation modifiée ! Total=" + r.getTotal());
                else System.out.println("⚠️ Aucune réservation trouvée avec id = " + r.getIdReservation());
            }

        } catch (ValidationException e) {
            System.out.println("🚫 Erreur saisie : " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ Erreur updateReservation : " + e.getMessage());
        }
    }

    // DELETE
    public void deleteReservation(int idReservation) {
        String sql = "DELETE FROM reservation_activite WHERE id_reservation=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idReservation);

            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("🗑️ Réservation supprimée !");
            else System.out.println("⚠️ Aucune réservation trouvée avec id = " + idReservation);

        } catch (SQLException e) {
            System.out.println("❌ Erreur deleteReservation : " + e.getMessage());
        }
    }

    // get reservations for one activite (✅ reads total)
    public List<ReservationActivite> getReservationsByActivite(int idActivite) {
        List<ReservationActivite> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation_activite WHERE id_activite=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idActivite);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new ReservationActivite(
                        rs.getInt("id_reservation"),
                        rs.getDate("date_reservation").toLocalDate(),
                        rs.getInt("nombre_personnes"),
                        rs.getString("statut"),
                        rs.getInt("id_activite"),
                        rs.getDouble("total")
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur getReservationsByActivite : " + e.getMessage());
        }

        return list;
    }

    // JOIN (unchanged) - optional: show total too
    public void afficherReservationsAvecNomActivite() {
        String sql = """
                SELECT r.id_reservation, r.date_reservation, r.nombre_personnes, r.statut, r.total,
                       a.nom AS nom_activite
                FROM reservation_activite r
                JOIN activite a ON r.id_activite = a.id_activite
                """;

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(
                        "Res#" + rs.getInt("id_reservation") +
                                " | " + rs.getDate("date_reservation") +
                                " | nb=" + rs.getInt("nombre_personnes") +
                                " | " + rs.getString("statut") +
                                " | total=" + rs.getDouble("total") +
                                " | Activite=" + rs.getString("nom_activite")
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur JOIN : " + e.getMessage());
        }
    }
}