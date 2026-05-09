package edu.pidev.services;

import edu.pidev.entities.Activite;
import edu.pidev.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActiviteService {

    // ✅ helper: store rating in DB
    private void updateAiRating(int idActivite, double rating) throws SQLException {
        String sql = "UPDATE activite SET ai_rating=? WHERE id_activite=?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql)) {
            pst.setDouble(1, rating);
            pst.setInt(2, idActivite);
            pst.executeUpdate();
        }
    }

    // ✅ helper: read ai_rating safely (NULL stays NULL, not 0.0)
    private Double readAiRating(ResultSet rs) {
        try {
            double r = rs.getDouble("ai_rating");
            if (rs.wasNull()) return null;
            return r;
        } catch (SQLException e) {
            return null;
        }
    }

    // CREATE (✅ generates AI rating once, stores it ONLY if ok)
    public void addActivite(Activite a) {
        String sql = "INSERT INTO activite (nom, description, type, prix, duree, lieu, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, a.getNom());
            pst.setString(2, a.getDescription());
            pst.setString(3, a.getType());
            pst.setDouble(4, a.getPrix());
            pst.setInt(5, a.getDuree());
            pst.setString(6, a.getLieu());
            pst.setString(7, a.getImageUrl());

            pst.executeUpdate();

            // ✅ get generated id
            int newId = -1;
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) newId = keys.getInt(1);
            }

            System.out.println("✅ Activité ajoutée ! id=" + newId);

            // ✅ AI rating -> store in DB ONLY if valid (>= 0)
            if (newId != -1) {
                a.setIdActivite(newId);

                double rating = AiRatingService.rate(a); // returns 0..5 or -1
                if (rating >= 0) {
                    updateAiRating(newId, rating);
                    a.setAiRating(rating);
                    System.out.println("⭐ AI rating saved: " + rating);
                } else {
                    System.out.println("⚠️ AI rating failed, not saved (ai_rating stays NULL).");
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur addActivite: " + e.getMessage());
        }
    }

    // READ (ALL) (✅ reads ai_rating)
    public List<Activite> getAllActivites() {
        List<Activite> list = new ArrayList<>();
        String sql = "SELECT * FROM activite";

        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Activite a = new Activite(
                        rs.getInt("id_activite"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getDouble("prix"),
                        rs.getInt("duree"),
                        rs.getString("lieu")
                );

                a.setImageUrl(rs.getString("image_url"));

                // ✅ rating can be NULL
                a.setAiRating(readAiRating(rs));

                list.add(a);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur getAllActivites: " + e.getMessage());
        }

        return list;
    }

    // UPDATE (✅ re-rates with AI and saves ONLY if ok)
    public void updateActivite(Activite a) {
        String sql = "UPDATE activite SET nom=?, description=?, type=?, prix=?, duree=?, lieu=?, image_url=? WHERE id_activite=?";

        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql)) {

            pst.setString(1, a.getNom());
            pst.setString(2, a.getDescription());
            pst.setString(3, a.getType());
            pst.setDouble(4, a.getPrix());
            pst.setInt(5, a.getDuree());
            pst.setString(6, a.getLieu());
            pst.setString(7, a.getImageUrl());
            pst.setInt(8, a.getIdActivite());

            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("✏️ Activité modifiée !");

                // ✅ AI rating -> update ONLY if valid
                double rating = AiRatingService.rate(a); // returns 0..5 or -1
                if (rating >= 0) {
                    updateAiRating(a.getIdActivite(), rating);
                    a.setAiRating(rating);
                    System.out.println("⭐ AI rating updated: " + rating);
                } else {
                    System.out.println("⚠️ AI rating failed, not saved (keeps old ai_rating).");
                }

            } else {
                System.out.println("⚠️ Aucun enregistrement trouvé avec id=" + a.getIdActivite());
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur updateActivite: " + e.getMessage());
        }
    }

    // DELETE
    public void deleteActivite(int id) {
        String sql = "DELETE FROM activite WHERE id_activite=?";

        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql)) {
            pst.setInt(1, id);

            int rows = pst.executeUpdate();
            if (rows > 0) System.out.println("🗑️ Activité supprimée !");
            else System.out.println("⚠️ Aucun enregistrement trouvé avec id=" + id);

        } catch (SQLException e) {
            System.out.println("❌ Erreur deleteActivite: " + e.getMessage());
        }
    }

    // SEARCH (✅ includes image_url + ai_rating)
    public List<Activite> search(String lieu, Double minPrix, Double maxPrix) {
        List<Activite> list = new ArrayList<>();

        String sql = """
        SELECT * FROM activite
        WHERE (? IS NULL OR LOWER(lieu) LIKE LOWER(CONCAT('%', ?, '%')))
          AND (? IS NULL OR prix >= ?)
          AND (? IS NULL OR prix <= ?)
        ORDER BY prix ASC
        """;

        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql)) {

            String l = (lieu == null || lieu.trim().isEmpty()) ? null : lieu.trim();

            pst.setString(1, l);
            pst.setString(2, l);

            if (minPrix == null) {
                pst.setNull(3, Types.DOUBLE);
                pst.setNull(4, Types.DOUBLE);
            } else {
                pst.setDouble(3, minPrix);
                pst.setDouble(4, minPrix);
            }

            if (maxPrix == null) {
                pst.setNull(5, Types.DOUBLE);
                pst.setNull(6, Types.DOUBLE);
            } else {
                pst.setDouble(5, maxPrix);
                pst.setDouble(6, maxPrix);
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Activite a = new Activite();
                    a.setIdActivite(rs.getInt("id_activite"));
                    a.setNom(rs.getString("nom"));
                    a.setDescription(rs.getString("description"));
                    a.setType(rs.getString("type"));
                    a.setPrix(rs.getDouble("prix"));
                    a.setDuree(rs.getInt("duree"));
                    a.setLieu(rs.getString("lieu"));
                    a.setImageUrl(rs.getString("image_url"));

                    // ✅ rating can be NULL
                    a.setAiRating(readAiRating(rs));

                    list.add(a);
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur search Activite: " + e.getMessage());
        }

        return list;
    }
}