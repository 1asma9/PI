package edu.pidev.services;

import edu.pidev.entities.Activite;
import edu.pidev.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActiviteService {

    // CREATE
    public void addActivite(Activite a) {
        String sql = "INSERT INTO activite (nom, description, type, prix, duree, lieu) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql)) {

            pst.setString(1, a.getNom());
            pst.setString(2, a.getDescription());
            pst.setString(3, a.getType());
            pst.setDouble(4, a.getPrix());
            pst.setInt(5, a.getDuree());
            pst.setString(6, a.getLieu());

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
                list.add(a);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur getAllActivites: " + e.getMessage());
        }

        return list;
    }

    // UPDATE
    public void updateActivite(Activite a) {
        String sql = "UPDATE activite SET nom=?, description=?, type=?, prix=?, duree=?, lieu=? WHERE id_activite=?";

        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql)) {

            pst.setString(1, a.getNom());
            pst.setString(2, a.getDescription());
            pst.setString(3, a.getType());
            pst.setDouble(4, a.getPrix());
            pst.setInt(5, a.getDuree());
            pst.setString(6, a.getLieu());
            pst.setInt(7, a.getIdActivite()); // <= لازم يكون موجود

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
}

