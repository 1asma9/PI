package services;

import entities.Avis;
import interfaces.IService;
import tools.EmailService;
import tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AvisService implements IService<Avis> {

    private int getDefaultTypeAvisId() {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return 1;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM typeavis LIMIT 1")) {
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            System.err.println("Erreur getDefaultTypeAvisId: " + e.getMessage());
        }
        return 1;
    }

    @Override
    public void addEntity(Avis avis) throws SQLException {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) {
            System.out.println("❌ Base de données inaccessible.");
            return;
        }

        int typeId = avis.getTypeId() > 0 ? avis.getTypeId() : getDefaultTypeAvisId();
        String requete = "INSERT INTO avis (user_id, type_id, nb_etoiles, contenu, statut, date_avis) VALUES (?, ?, ?, ?, ?, NOW())";
        try (PreparedStatement ps = connection.prepareStatement(requete)) {
            ps.setInt(1, avis.getUserId());
            ps.setInt(2, typeId);
            ps.setInt(3, avis.getNote());
            ps.setString(4, avis.getCommentaire());
            ps.setString(5, "En attente");
            ps.executeUpdate();
            System.out.println("✅ Avis ajouté !");

            // Email à l'admin
            EmailService.sendReviewNotificationToAdmin(
                avis.getNote(),
                avis.getCommentaire(),
                tools.SessionManager.getEmail() != null ? tools.SessionManager.getEmail() : "admin@vianova.tn"
            );
        }
    }

    @Override
    public void deleteEntity(int id) throws SQLException {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return;
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM avis WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Avis supprimé");
        }
    }

    @Override
    public void updateEntity(Avis avis) throws SQLException {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return;
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE avis SET nb_etoiles = ?, contenu = ? WHERE id = ?")) {
            ps.setInt(1, avis.getNote());
            ps.setString(2, avis.getCommentaire());
            ps.setInt(3, avis.getId());
            ps.executeUpdate();
            System.out.println("✅ Avis modifié");
        }
    }

    @Override
    public List<Avis> getAllEntities() throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return avisList;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM avis ORDER BY date_avis DESC")) {
            while (rs.next()) avisList.add(mapResultSetToAvis(rs));
        }
        return avisList;
    }

    public List<Avis> getByUserId(int userId) throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return avisList;
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM avis WHERE user_id = ? ORDER BY date_avis DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) avisList.add(mapResultSetToAvis(rs));
            }
        }
        return avisList;
    }

    public void repondreAvis(int id, String reponse) throws SQLException {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return;
        try (PreparedStatement ps = connection.prepareStatement("UPDATE avis SET reponse = ?, date_reponse = NOW() WHERE id = ?")) {
            ps.setString(1, reponse);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("✅ Réponse à l'avis ajoutée");

            // Email à l'utilisateur
            String q = "SELECT u.email FROM avis a JOIN users u ON a.user_id = u.id WHERE a.id = ?";
            try (PreparedStatement psEmail = connection.prepareStatement(q)) {
                psEmail.setInt(1, id);
                try (ResultSet rs = psEmail.executeQuery()) {
                    if (rs.next()) {
                        EmailService.sendResponseNotificationToUser(
                            rs.getString("email"),
                            "Votre avis",
                            reponse
                        );
                    }
                }
            } catch (SQLException e) {
                System.err.println("Email non envoyé: " + e.getMessage());
            }
        }
    }

    public List<Avis> getAvisByNote(int note) throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return avisList;
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM avis WHERE nb_etoiles = ?")) {
            ps.setInt(1, note);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) avisList.add(mapResultSetToAvis(rs));
            }
        }
        return avisList;
    }

    public List<Avis> getAvisSortedByNote(boolean desc) throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        String order = desc ? "DESC" : "ASC";
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return avisList;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM avis ORDER BY nb_etoiles " + order)) {
            while (rs.next()) avisList.add(mapResultSetToAvis(rs));
        }
        return avisList;
    }

    public List<Avis> searchAvis(String keyword) throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return avisList;
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM avis WHERE contenu LIKE ?")) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) avisList.add(mapResultSetToAvis(rs));
            }
        }
        return avisList;
    }

    public double getAverageRating() throws SQLException {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return 0.0;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT AVG(nb_etoiles) as moyenne FROM avis")) {
            if (rs.next()) return rs.getDouble("moyenne");
        }
        return 0.0;
    }

    public int getCountByNote(int note) throws SQLException {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return 0;
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) as total FROM avis WHERE nb_etoiles = ?")) {
            ps.setInt(1, note);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        }
        return 0;
    }

    public int getTotalCount() throws SQLException {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return 0;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) as total FROM avis")) {
            if (rs.next()) return rs.getInt("total");
        }
        return 0;
    }

    public java.util.Map<Integer, String> getAllTypes() throws SQLException {
        java.util.Map<Integer, String> types = new java.util.HashMap<>();
        String q = "SELECT id, nom FROM typeavis";
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return types;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(q)) {
            while (rs.next()) types.put(rs.getInt("id"), rs.getString("nom"));
        }
        return types;
    }

    private Avis mapResultSetToAvis(ResultSet rs) throws SQLException {
        Avis a = new Avis();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        try { a.setTypeId(rs.getInt("type_id")); } catch (Exception e) {}
        a.setNote(rs.getInt("nb_etoiles"));         // nb_etoiles en DB → note en Java
        a.setCommentaire(rs.getString("contenu"));   // contenu en DB → commentaire en Java
        a.setDateCreation(rs.getTimestamp("date_avis")); // date_avis en DB → dateCreation en Java
        try { a.setReponseAdmin(rs.getString("reponse")); } catch (SQLException e) {}
        try { a.setDateReponse(rs.getTimestamp("date_reponse")); } catch (SQLException e) {}
        try { a.setStatut(rs.getString("statut")); } catch (SQLException e) {}
        return a;
    }
}
