package services;

import entities.Reclamation;
import interfaces.IService;
import tools.EmailService;
import tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService implements IService<Reclamation> {

    // Récupère le premier type_id disponible dans typeavis
    private int getDefaultTypeId() {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return 1;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM typeavis LIMIT 1")) {
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            System.err.println("Erreur getDefaultTypeId: " + e.getMessage());
        }
        return 1;
    }

    @Override
    public void addEntity(Reclamation reclamation) throws SQLException {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) {
            System.out.println("❌ Base de données inaccessible.");
            return;
        }

        int typeId = reclamation.getTypeId() > 0 ? reclamation.getTypeId() : getDefaultTypeId();
        String priorite = reclamation.getPriorite() != null ? reclamation.getPriorite() : "Moyenne";

        String requete = "INSERT INTO reclamation (user_id, type_id, titre, contenu, statut, priorite, type_feedback, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        try (PreparedStatement ps = connection.prepareStatement(requete)) {
            ps.setInt(1, reclamation.getUserId());
            ps.setInt(2, typeId);
            ps.setString(3, reclamation.getTitre());
            ps.setString(4, reclamation.getDescription()); // description en Java = contenu en DB
            ps.setString(5, "En attente");
            ps.setString(6, priorite);
            ps.setString(7, "Général");
            ps.executeUpdate();
            System.out.println("✅ Réclamation ajoutée !");

            // Email à l'admin
            EmailService.sendComplaintNotificationToAdmin(
                reclamation.getTitre(),
                reclamation.getDescription(),
                tools.SessionManager.getEmail() != null ? tools.SessionManager.getEmail() : "admin@vianova.tn"
            );
        }
    }

    @Override
    public void deleteEntity(int id) throws SQLException {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return;
        String requete = "DELETE FROM reclamation WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(requete)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Réclamation supprimée");
        }
    }

    @Override
    public void updateEntity(Reclamation reclamation) throws SQLException {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return;
        String requete = "UPDATE reclamation SET titre = ?, contenu = ?, priorite = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(requete)) {
            ps.setString(1, reclamation.getTitre());
            ps.setString(2, reclamation.getDescription());
            ps.setString(3, reclamation.getPriorite() != null ? reclamation.getPriorite() : "Moyenne");
            ps.setInt(4, reclamation.getId());
            ps.executeUpdate();
            System.out.println("✅ Réclamation modifiée");
        }
    }

    @Override
    public List<Reclamation> getAllEntities() throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String requete = "SELECT * FROM reclamation ORDER BY date_creation DESC";
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return reclamations;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                reclamations.add(mapResultSetToReclamation(rs));
            }
        }
        return reclamations;
    }

    public List<Reclamation> getByUserId(int userId) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String requete = "SELECT * FROM reclamation WHERE user_id = ? ORDER BY date_creation DESC";
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return reclamations;
        System.out.println("🔍 Chargement réclamations pour user_id=" + userId);
        try (PreparedStatement ps = connection.prepareStatement(requete)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reclamations.add(mapResultSetToReclamation(rs));
                }
            }
        }
        System.out.println("✅ " + reclamations.size() + " réclamation(s) trouvée(s) pour user_id=" + userId);
        return reclamations;
    }

    public void repondreReclamation(int id, String reponse) throws SQLException {
        String requete = "UPDATE reclamation SET reponse = ?, statut = 'Résolue' WHERE id = ?";
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return;
        try (PreparedStatement ps = connection.prepareStatement(requete)) {
            ps.setString(1, reponse);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("✅ Réponse ajoutée");

            // Email à l'utilisateur
            String getUserEmail = "SELECT u.email, r.titre FROM reclamation r JOIN users u ON r.user_id = u.id WHERE r.id = ?";
            try (PreparedStatement psEmail = connection.prepareStatement(getUserEmail)) {
                psEmail.setInt(1, id);
                try (ResultSet rs = psEmail.executeQuery()) {
                    if (rs.next()) {
                        EmailService.sendResponseNotificationToUser(
                            rs.getString("email"),
                            rs.getString("titre"),
                            reponse
                        );
                    }
                }
            } catch (SQLException e) {
                System.err.println("Email non envoyé: " + e.getMessage());
            }
        }
    }

    public List<Reclamation> searchReclamations(String keyword) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String requete = "SELECT * FROM reclamation WHERE titre LIKE ? OR contenu LIKE ?";
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return reclamations;
        try (PreparedStatement ps = connection.prepareStatement(requete)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) reclamations.add(mapResultSetToReclamation(rs));
            }
        }
        return reclamations;
    }

    public List<Reclamation> getReclamationsSortedByDate() throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return list;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM reclamation ORDER BY date_creation DESC")) {
            while (rs.next()) list.add(mapResultSetToReclamation(rs));
        }
        return list;
    }

    public List<Reclamation> getReclamationsSortedByStatus() throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return list;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM reclamation ORDER BY statut ASC")) {
            while (rs.next()) list.add(mapResultSetToReclamation(rs));
        }
        return list;
    }

    public List<Reclamation> getReclamationsByStatus(String statut) throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return list;
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM reclamation WHERE statut = ?")) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToReclamation(rs));
            }
        }
        return list;
    }

    public int getTotalCount() throws SQLException {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return 0;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) as total FROM reclamation")) {
            if (rs.next()) return rs.getInt("total");
        }
        return 0;
    }

    public int getCountByStatus(String statut) throws SQLException {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return 0;
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) as total FROM reclamation WHERE statut = ?")) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        }
        return 0;
    }

    public int getCountByPriorite(String priorite) throws SQLException {
        Connection connection = MyConnection.getInstance().getCnx();
        if (connection == null) return 0;
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) as total FROM reclamation WHERE priorite = ?")) {
            ps.setString(1, priorite);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        }
        return 0;
    }

    private Reclamation mapResultSetToReclamation(ResultSet rs) throws SQLException {
        Reclamation r = new Reclamation();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        try { r.setTypeId(rs.getInt("type_id")); } catch (Exception e) {}
        r.setTitre(rs.getString("titre"));
        r.setDescription(rs.getString("contenu")); // contenu en DB → description en Java
        r.setDateCreation(rs.getTimestamp("date_creation"));
        r.setStatut(rs.getString("statut"));
        try { r.setReponseAdmin(rs.getString("reponse")); } catch (SQLException e) {}
        try { r.setDateReponse(rs.getTimestamp("date_reponse")); } catch (SQLException e) {}
        try { r.setPriorite(rs.getString("priorite")); } catch (SQLException e) {}
        return r;
    }
}
