package services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import entities.Avis;
import interfaces.IService;
import tools.MyConnection;
import tools.EmailService;

public class AvisService implements IService<Avis> {

    @Override
    public void addEntity(Avis avis) throws SQLException {
        String requete = "INSERT INTO avis (user_id, note, commentaire) VALUES (?, ?, ?)";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setInt(1, avis.getUserId());
            ps.setInt(2, avis.getNote());
            ps.setString(3, avis.getCommentaire());
            ps.executeUpdate();
            System.out.println("Review added");

            // Send email notification to admin
            EmailService.sendReviewNotificationToAdmin(
                    avis.getNote(),
                    avis.getCommentaire(),
                    "user@test.com" // Placeholder for current session email
            );
        }
    }

    @Override
    public void deleteEntity(int id) throws SQLException {
        String requete = "DELETE FROM avis WHERE id = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Review deleted");
        }
    }

    @Override
    public void updateEntity(Avis avis) throws SQLException {
        String requete = "UPDATE avis SET note = ?, commentaire = ? WHERE id = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setInt(1, avis.getNote());
            ps.setString(2, avis.getCommentaire());
            ps.setInt(3, avis.getId());
            ps.executeUpdate();
            System.out.println("Review updated");
        }
    }

    @Override
    public List<Avis> getAllEntities() throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        String requete = "SELECT * FROM avis";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
                ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                avisList.add(mapResultSetToAvis(rs));
            }
        }
        return avisList;
    }

    public List<Avis> getByUserId(int userId) throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        String requete = "SELECT * FROM avis WHERE user_id = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    avisList.add(mapResultSetToAvis(rs));
                }
            }
        }
        return avisList;
    }

    public void repondreAvis(int id, String reponse) throws SQLException {
        String requete = "UPDATE avis SET reponse_admin = ?, date_reponse = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setString(1, reponse);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("Response added to review");
        }
    }

    // Search by comment
    public List<Avis> searchAvis(String keyword) throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        String requete = "SELECT * FROM avis WHERE commentaire LIKE ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    avisList.add(mapResultSetToAvis(rs));
                }
            }
        }
        return avisList;
    }

    // Sort by rating
    public List<Avis> getAvisSortedByNote(boolean descending) throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        String order = descending ? "DESC" : "ASC";
        String requete = "SELECT * FROM avis ORDER BY note " + order;
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
                ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                avisList.add(mapResultSetToAvis(rs));
            }
        }
        return avisList;
    }

    // Filter by rating
    public List<Avis> getAvisByNote(int note) throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        String requete = "SELECT * FROM avis WHERE note = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setInt(1, note);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    avisList.add(mapResultSetToAvis(rs));
                }
            }
        }
        return avisList;
    }

    // Get reviews with rating >= X
    public List<Avis> getAvisWithMinNote(int minNote) throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        String requete = "SELECT * FROM avis WHERE note >= ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setInt(1, minNote);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    avisList.add(mapResultSetToAvis(rs));
                }
            }
        }
        return avisList;
    }

    // Get average rating
    public double getAverageRating() throws SQLException {
        String requete = "SELECT AVG(note) as moyenne FROM avis";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
                ResultSet rs = st.executeQuery(requete)) {
            if (rs.next()) {
                return rs.getDouble("moyenne");
            }
        }
        return 0.0;
    }

    // Get count by rating
    public int getCountByNote(int note) throws SQLException {
        String requete = "SELECT COUNT(*) as total FROM avis WHERE note = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setInt(1, note);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    private Avis mapResultSetToAvis(ResultSet rs) throws SQLException {
        Avis a = new Avis();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setNote(rs.getInt("note"));
        a.setCommentaire(rs.getString("commentaire"));
        a.setDateCreation(rs.getTimestamp("date_creation"));
        a.setReponseAdmin(rs.getString("reponse_admin"));
        a.setDateReponse(rs.getTimestamp("date_reponse"));
        return a;
    }
}
