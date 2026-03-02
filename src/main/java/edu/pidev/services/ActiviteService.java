package edu.pidev.services;

import edu.pidev.entities.Activite;
import edu.pidev.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActiviteService {

    // CREATE
    public void addActivite(Activite a) {
        String sql = "INSERT INTO activite (nom, description, type, prix, duree, lieu, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql)) {

            pst.setString(1, a.getNom());
            pst.setString(2, a.getDescription());
            pst.setString(3, a.getType());
            pst.setDouble(4, a.getPrix());
            pst.setInt(5, a.getDuree());
            pst.setString(6, a.getLieu());
            pst.setString(7, a.getImageUrl()); // ✅

            pst.executeUpdate();
            System.out.println("✅ Activité ajoutée !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur addActivite: " + e.getMessage());
        }
    }

    // READ (ALL)
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

                // ✅ IMPORTANT: read image_url from DB
                a.setImageUrl(rs.getString("image_url"));

                list.add(a);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur getAllActivites: " + e.getMessage());
        }

        return list;
    }

    // UPDATE (✅ includes image_url)
    public void updateActivite(Activite a) {
        String sql = "UPDATE activite SET nom=?, description=?, type=?, prix=?, duree=?, lieu=?, image_url=? WHERE id_activite=?";

        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql)) {

            pst.setString(1, a.getNom());
            pst.setString(2, a.getDescription());
            pst.setString(3, a.getType());
            pst.setDouble(4, a.getPrix());
            pst.setInt(5, a.getDuree());
            pst.setString(6, a.getLieu());
            pst.setString(7, a.getImageUrl()); // ✅
            pst.setInt(8, a.getIdActivite());  // ✅

            int rows = pst.executeUpdate();
            if (rows > 0) System.out.println("✏️ Activité modifiée !");
            else System.out.println("⚠️ Aucun enregistrement trouvé avec id=" + a.getIdActivite());

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

    // SEARCH (✅ includes image_url)
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
                pst.setNull(3, java.sql.Types.DOUBLE);
                pst.setNull(4, java.sql.Types.DOUBLE);
            } else {
                pst.setDouble(3, minPrix);
                pst.setDouble(4, minPrix);
            }

            if (maxPrix == null) {
                pst.setNull(5, java.sql.Types.DOUBLE);
                pst.setNull(6, java.sql.Types.DOUBLE);
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

                    // ✅ IMPORTANT: read image_url
                    a.setImageUrl(rs.getString("image_url"));

                    list.add(a);
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur search Activite: " + e.getMessage());
        }

        return list;
    }
}