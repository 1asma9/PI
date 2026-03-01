package services;

import entities.Reclamation;
import interfaces.IService;
import tools.EmailService;
import tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ReclamationService implements IService<Reclamation> {

    @Override
    public void addEntity(Reclamation reclamation) throws SQLException {
        String requete = "INSERT INTO reclamation (user_id, titre, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setInt(1, reclamation.getUserId());
            ps.setString(2, reclamation.getTitre());
            ps.setString(3, reclamation.getDescription());
            ps.executeUpdate();
            System.out.println("Complaint added");

            // Send email notification to admin
            EmailService.sendComplaintNotificationToAdmin(
                    reclamation.getTitre(),
                    reclamation.getDescription(),
                    "user@test.com" // Placeholder for current session email
            );
        }
    }

    @Override
    public void deleteEntity(int id) throws SQLException {
        String requete = "DELETE FROM reclamation WHERE id = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Complaint deleted");
        }
    }

    @Override
    public void updateEntity(Reclamation reclamation) throws SQLException {
        String requete = "UPDATE reclamation SET titre = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setString(1, reclamation.getTitre());
            ps.setString(2, reclamation.getDescription());
            ps.setInt(3, reclamation.getId());
            ps.executeUpdate();
            System.out.println("Complaint updated");
        }
    }

    @Override
    public List<Reclamation> getAllEntities() throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String requete = "SELECT * FROM reclamation";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
                ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                reclamations.add(mapResultSetToReclamation(rs));
            }
        }
        return reclamations;
    }

    public List<Reclamation> getByUserId(int userId) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String requete = "SELECT * FROM reclamation WHERE user_id = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reclamations.add(mapResultSetToReclamation(rs));
                }
            }
        }
        return reclamations;
    }

    public void repondreReclamation(int id, String reponse) throws SQLException {
        String requete = "UPDATE reclamation SET reponse_admin = ?, statut = 'Traitée', date_reponse = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setString(1, reponse);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("Response added");

            // Get complaint details and send email to user
            String getUserEmail = "SELECT u.email, r.titre FROM reclamation r JOIN user u ON r.user_id = u.id WHERE r.id = ?";
            try (PreparedStatement psEmail = MyConnection.getInstance().getCnx().prepareStatement(getUserEmail)) {
                psEmail.setInt(1, id);
                try (ResultSet rs = psEmail.executeQuery()) {
                    if (rs.next()) {
                        String userEmail = rs.getString("email");
                        String titre = rs.getString("titre");
                        EmailService.sendResponseNotificationToUser(userEmail, titre, reponse);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Could not send response email: " + e.getMessage());
            }
        }
    }

    // Search by title or description
    public List<Reclamation> searchReclamations(String keyword) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String requete = "SELECT * FROM reclamation WHERE titre LIKE ? OR description LIKE ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reclamations.add(mapResultSetToReclamation(rs));
                }
            }
        }
        return reclamations;
    }

    // Sort by date (newest first)
    public List<Reclamation> getReclamationsSortedByDate() throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String requete = "SELECT * FROM reclamation ORDER BY date_creation DESC";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
                ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                reclamations.add(mapResultSetToReclamation(rs));
            }
        }
        return reclamations;
    }

    // Sort by status
    public List<Reclamation> getReclamationsSortedByStatus() throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String requete = "SELECT * FROM reclamation ORDER BY statut ASC";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
                ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                reclamations.add(mapResultSetToReclamation(rs));
            }
        }
        return reclamations;
    }

    // Filter by status
    public List<Reclamation> getReclamationsByStatus(String statut) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String requete = "SELECT * FROM reclamation WHERE statut = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reclamations.add(mapResultSetToReclamation(rs));
                }
            }
        }
        return reclamations;
    }

    // Get statistics
    public int getTotalCount() throws SQLException {
        String requete = "SELECT COUNT(*) as total FROM reclamation";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
                ResultSet rs = st.executeQuery(requete)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public int getCountByStatus(String statut) throws SQLException {
        String requete = "SELECT COUNT(*) as total FROM reclamation WHERE statut = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    private Reclamation mapResultSetToReclamation(ResultSet rs) throws SQLException {
        Reclamation r = new Reclamation();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setTitre(rs.getString("titre"));
        r.setDescription(rs.getString("description"));
        r.setDateCreation(rs.getTimestamp("date_creation"));
        r.setStatut(rs.getString("statut"));
        r.setReponseAdmin(rs.getString("reponse_admin"));
        r.setDateReponse(rs.getTimestamp("date_reponse"));
        return r;
    }
}
