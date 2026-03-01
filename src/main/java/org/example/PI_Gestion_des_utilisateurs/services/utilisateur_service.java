package org.example.PI_Gestion_des_utilisateurs.services;

import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import org.example.PI_Gestion_des_utilisateurs.interfaces.utilisateur_interface;
import org.example.PI_Gestion_des_utilisateurs.tools.MaConnection;
import org.example.PI_Gestion_des_utilisateurs.tools.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class utilisateur_service implements utilisateur_interface {
    
    private final EmailService emailService = new EmailService();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static final String SQL_INSERT_USER =
            "INSERT INTO users (nom, prenom, email, password_hash, telephone, created_at) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_USERS_WITH_ROLES =
            "SELECT u.id_user, u.nom, u.prenom, u.email, u.password_hash, u.telephone, u.created_at, " +
                    "r.name AS role_name, r.description AS role_description " +
                    "FROM users u " +
                    "LEFT JOIN user_role ur ON ur.user_id = u.id_user " +
                    "LEFT JOIN role r ON r.id = ur.role_id " +
                    "ORDER BY u.id_user";
    private static final String SQL_UPDATE_USER =
            "UPDATE users SET nom=?, prenom=?, email=?, password_hash=?, telephone=? WHERE id_user=?";
    private static final String SQL_DELETE_USER =
            "DELETE FROM users WHERE id_user=?";
    private static final String SQL_DELETE_USER_ROLES =
            "DELETE FROM user_role WHERE user_id=?";
    private static final String SQL_SELECT_USER_BY_EMAIL =
            "SELECT id_user, nom, prenom, email, password_hash, telephone, created_at FROM users WHERE email=? LIMIT 1";
    private static final String SQL_SELECT_USER_BY_ID =
            "SELECT id_user, nom, prenom, email, password_hash, telephone, created_at FROM users WHERE id_user=? LIMIT 1";
    private static final String SQL_UPDATE_PASSWORD =
            "UPDATE users SET password_hash=? WHERE id_user=?";
    private static final String SQL_COUNT_USER_BY_EMAIL =
            "SELECT COUNT(*) FROM users WHERE email=?";
    private static final String SQL_ASSOCIER_ROLE_A_UTILISATEUR =
            "INSERT INTO user_role (user_id, role_id) VALUES (?, ?)";


    private List<utilisateur> recupererUtilisateursAvecRoles() {
        List<utilisateur> utilisateurs = new ArrayList<>();
        Connection connection = MaConnection.getInstance().getConnection();
        
        try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_USERS_WITH_ROLES);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                utilisateurs.add(mapUtilisateurDepuisResultSet(rs, true));
            }
        } catch (SQLException e) {
            afficherErreur("l'affichage des utilisateurs avec rôles", e);
        }
        return utilisateurs;
    }


    private utilisateur mapUtilisateurDepuisResultSet(ResultSet rs, boolean avecRoles) throws SQLException {
        utilisateur u = new utilisateur();
        
        u.setId(rs.getInt("id_user"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password_hash"));
        u.setTelephone(rs.getString("telephone"));
        
        Timestamp dateCreation = rs.getTimestamp("created_at");
        if (dateCreation != null) {
            u.setDateCreation(dateCreation.toLocalDateTime());
        }
        
        if (avecRoles) {
            u.setRoleName(rs.getString("role_name"));
            u.setRoleDescription(rs.getString("role_description"));
        }
        
        return u;
    }

    private boolean utilisateurExiste(int idUtilisateur) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE id_user = ? LIMIT 1";
        try (PreparedStatement ps = MaConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    private boolean roleExiste(int idRole) throws SQLException {
        String sql = "SELECT 1 FROM role WHERE id = ? LIMIT 1";
        try (PreparedStatement ps = MaConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, idRole);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean associationUtilisateurRoleExiste(int idUtilisateur, int idRole) throws SQLException {
        String sql = "SELECT 1 FROM user_role WHERE user_id = ? AND role_id = ? LIMIT 1";
        try (PreparedStatement ps = MaConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            ps.setInt(2, idRole);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    private void afficherErreur(String operation, SQLException e) {
        System.err.println("Erreur " + operation + ": " + e.getMessage());
    }


    @Override
    public boolean ajouterutilisateur(utilisateur utilisateur) {
        // Validation simple (ou utiliser validerDonneesutilisateur si souhaité)
        if (!validerDonneesutilisateur(utilisateur)) {
            return false;
        }

        // Vérifier unicité de l'email
        if (!verifierEmailUnique(utilisateur.getEmail())) {
            return false;
        }

        boolean utilisateurCree = false;
        Connection connection = MaConnection.getInstance().getConnection();
        try (PreparedStatement ps = connection.prepareStatement(SQL_INSERT_USER)) {
            ps.setString(1, utilisateur.getNom().trim());
            ps.setString(2, utilisateur.getPrenom().trim());
            ps.setString(3, utilisateur.getEmail().trim());
            // Hasher le mot de passe avant de le stocker
            String hashedPassword = PasswordUtil.hashPassword(utilisateur.getPassword());
            ps.setString(4, hashedPassword);
            ps.setString(5, utilisateur.getTelephone());
            ps.setTimestamp(6, Timestamp.valueOf(utilisateur.getDateCreation()));

            // Vérifier si l'insertion a réussi
            utilisateurCree = ps.executeUpdate() > 0;
            
            if (utilisateurCree) {
                System.out.println("✅ Utilisateur créé avec succès: " + utilisateur.getEmail());
                
                // Envoyer l'email de bienvenue uniquement si création réussie
                try {
                    String sujetEmail = "Bienvenue chez Vianova - Votre aventure commence ici !";
                    
                    // 📧 Utiliser le template texte simple (format text/plain demandé)
                    String corpsEmail = EmailVianovaTemplate.genererEmailBienvenueSimple(
                            utilisateur.getNom(),
                            utilisateur.getPrenom(), 
                            utilisateur.getEmail(),
                            utilisateur.getPassword()
                    );
                    
                    // 🔍 DIAGNOSTIC DÉTAILLÉ
                    String emailDestinataire = utilisateur.getEmail();
                    System.out.println("🔍 DEBUG - Email de l'utilisateur: [" + emailDestinataire + "]");
                    System.out.println("🔍 DEBUG - Email null? " + (emailDestinataire == null));
                    System.out.println("🔍 DEBUG - Email vide? " + (emailDestinataire != null && emailDestinataire.trim().isEmpty()));
                    System.out.println("🔍 DEBUG - Email trim: [" + (emailDestinataire != null ? emailDestinataire.trim() : "NULL") + "]");
                    
                    System.out.println("📧 Envoi de l'email de bienvenue Vianova à: " + emailDestinataire);
                    
                    // ✅ Envoi en format texte simple (text/plain)
                    emailService.sendEmail(emailDestinataire, sujetEmail, corpsEmail);
                    System.out.println("✅ Email de bienvenue Vianova envoyé avec succès !");
                    
                } catch (Exception e) {
                    System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
                    e.printStackTrace();
                    // L'utilisateur est créé, mais l'email a échoué
                    // On ne retourne pas false pour ne pas bloquer la création
                }
            }
            
            return utilisateurCree;

        } catch (SQLException e) {
            afficherErreur("l'ajout d'utilisateur", e);
            return false;
        }
    }


    @Override
    public List<utilisateur> afficherutilisateurs() {
        return recupererUtilisateursAvecRoles();
    }

    @Override
    public boolean modifierutilisateur(utilisateur utilisateur) {
        if (!validerDonneesutilisateur(utilisateur)) {
            return false;
        }
        
        Connection connection = MaConnection.getInstance().getConnection();
        try (PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_USER)) {
            ps.setString(1, utilisateur.getNom().trim());
            ps.setString(2, utilisateur.getPrenom().trim());
            ps.setString(3, utilisateur.getEmail().trim());
            // Hasher le mot de passe avant de le stocker
            String hashedPassword = PasswordUtil.hashPassword(utilisateur.getPassword());
            ps.setString(4, hashedPassword);
            ps.setString(5, utilisateur.getTelephone());
            ps.setInt(6, utilisateur.getId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            afficherErreur("la modification d'utilisateur", e);
            return false;
        }
    }


    @Override
    public boolean supprimerutilisateur(int id) {
        Connection connection = MaConnection.getInstance().getConnection();
        boolean autoCommitOriginal = false;
        
        try {
            autoCommitOriginal = connection.getAutoCommit();
            connection.setAutoCommit(false);
            
            try (PreparedStatement psRoles = connection.prepareStatement(SQL_DELETE_USER_ROLES)) {
                psRoles.setInt(1, id);
                psRoles.executeUpdate();
            }
            
            try (PreparedStatement psUser = connection.prepareStatement(SQL_DELETE_USER)) {
                psUser.setInt(1, id);
                int lignesSupprimees = psUser.executeUpdate();
                connection.commit();
                return lignesSupprimees > 0;
            }
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                afficherErreur("le rollback lors de la suppression", rollbackEx);
            }
            afficherErreur("la suppression d'utilisateur", e);
            return false;
        } finally {
            try {
                connection.setAutoCommit(autoCommitOriginal);
            } catch (SQLException e) {
                afficherErreur("la restauration de l'auto-commit", e);
            }
        }
    }


    @Override
    public Optional<utilisateur> rechercherutilisateurParEmail(String email) {
        Connection connection = MaConnection.getInstance().getConnection();
        try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_USER_BY_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUtilisateurDepuisResultSet(rs, false));
                }
            }
        } catch (SQLException e) {
            afficherErreur("la recherche d'utilisateur par email", e);
        }
        return Optional.empty();
    }


    @Override
    public boolean associerRoleAutilisateur(int idUtilisateur, int idRole) {
        Connection connection = MaConnection.getInstance().getConnection();
        try {
            if (!utilisateurExiste(idUtilisateur)) {
                System.err.println("Utilisateur introuvable (id=" + idUtilisateur + ")");
                return false;
            }
            if (!roleExiste(idRole)) {
                System.err.println("Rôle introuvable (id=" + idRole + ")");
                return false;
            }
            if (associationUtilisateurRoleExiste(idUtilisateur, idRole)) {
                return true;
            }

            boolean autoCommitOriginal = connection.getAutoCommit();
            connection.setAutoCommit(false);
            
            try (PreparedStatement ps = connection.prepareStatement(SQL_ASSOCIER_ROLE_A_UTILISATEUR)) {
                ps.setInt(1, idUtilisateur);
                ps.setInt(2, idRole);
                int lignesModifiees = ps.executeUpdate();
                connection.commit();
                return lignesModifiees > 0;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommitOriginal);
            }
        } catch (SQLException e) {
            afficherErreur("l'association rôle-utilisateur", e);
            return false;
        }
    }


    @Override
    public boolean verifierEmailUnique(String email) {
        Connection connection = MaConnection.getInstance().getConnection();
        try (PreparedStatement ps = connection.prepareStatement(SQL_COUNT_USER_BY_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            afficherErreur("la vérification d'unicité d'email", e);
        }
        return true; // Retourner true par défaut si erreur SQL
    }


    /**
     * Valide les données d'un utilisateur et retourne un message d'erreur si nécessaire
     * @param utilisateur L'utilisateur à valider
     * @return Un message d'erreur si validation échoue, null si tout est valide
     */
    public String validerDonneesutilisateurAvecMessage(utilisateur utilisateur) {
        if (utilisateur == null) {
            return "L'utilisateur ne peut pas être null";
        }
        
        if (utilisateur.getNom() == null || utilisateur.getNom().trim().isEmpty()) {
            return "Le nom ne peut pas être vide";
        }
        
        if (utilisateur.getPrenom() == null || utilisateur.getPrenom().trim().isEmpty()) {
            return "Le prénom ne peut pas être vide";
        }
        
        String email = utilisateur.getEmail();
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "L'email n'est pas valide";
        }
        
        String password = utilisateur.getPassword();
        if (password == null || password.length() < 8) {
            return "Le mot de passe doit contenir au moins 8 caractères";
        }
        
        // Vérifier la force du mot de passe
        if (!PasswordUtil.isPasswordStrong(password)) {
            String recommendations = PasswordUtil.getPasswordRecommendations(password);
            return "Le mot de passe n'est pas assez fort.\n\nRecommandations:\n" + recommendations;
        }
        
        String telephone = utilisateur.getTelephone();
        if (telephone != null && !telephone.trim().isEmpty() && !telephone.matches("\\d{8,}")) {
            return "Le numéro téléphone doit contenir uniquement des chiffres (min 8)";
        }
        
        return null; // Pas d'erreur
    }

    @Override
    public boolean validerDonneesutilisateur(utilisateur utilisateur) {
        String erreur = validerDonneesutilisateurAvecMessage(utilisateur);
        if (erreur != null) {
            System.err.println(erreur);
            return false;
        }
        return true;
    }

    /**
     * Change le mot de passe d'un utilisateur et retourne un message d'erreur si nécessaire
     * @param userId L'ID de l'utilisateur
     * @param ancienMotDePasse L'ancien mot de passe à vérifier
     * @param nouveauMotDePasse Le nouveau mot de passe à définir
     * @return Un message d'erreur si échec, null si succès
     */
    public String changerMotDePasseAvecMessage(int userId, String ancienMotDePasse, String nouveauMotDePasse) {
        if (ancienMotDePasse == null || ancienMotDePasse.trim().isEmpty()) {
            return "L'ancien mot de passe ne peut pas être vide";
        }
        
        if (nouveauMotDePasse == null || nouveauMotDePasse.trim().isEmpty()) {
            return "Le nouveau mot de passe ne peut pas être vide";
        }
        
        // Vérifier la force du nouveau mot de passe
        if (!PasswordUtil.isPasswordStrong(nouveauMotDePasse)) {
            String recommendations = PasswordUtil.getPasswordRecommendations(nouveauMotDePasse);
            return "Le nouveau mot de passe n'est pas assez fort.\n\nRecommandations:\n" + recommendations;
        }
        
        try {
            // Récupérer l'utilisateur pour vérifier l'ancien mot de passe
            Optional<utilisateur> userOpt = rechercherutilisateurParId(userId);
            if (!userOpt.isPresent()) {
                return "Utilisateur non trouvé (ID: " + userId + ")";
            }
            
            utilisateur user = userOpt.get();
            
            // Vérification intelligente de l'ancien mot de passe
            boolean ancienMotDePasseValide = false;
            if (PasswordUtil.isHashedPassword(user.getPassword())) {
                // Le mot de passe stocké est hashé : utiliser la vérification par hash
                ancienMotDePasseValide = PasswordUtil.verifyPassword(ancienMotDePasse, user.getPassword());
            } else {
                // Le mot de passe stocké est en clair : utiliser la comparaison directe
                ancienMotDePasseValide = ancienMotDePasse.equals(user.getPassword());
            }
            
            if (!ancienMotDePasseValide) {
                return "L'ancien mot de passe est incorrect";
            }
            
            // Mettre à jour avec le nouveau mot de passe hashé
            Connection connection = MaConnection.getInstance().getConnection();
            try (PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_PASSWORD)) {
                String hashedNewPassword = PasswordUtil.hashPassword(nouveauMotDePasse);
                ps.setString(1, hashedNewPassword);
                ps.setInt(2, userId);
                
                int result = ps.executeUpdate();
                if (result > 0) {
                    return null; // Succès
                } else {
                    return "Échec lors de la mise à jour du mot de passe";
                }
            }
            
        } catch (SQLException e) {
            return "Erreur lors du changement de mot de passe: " + e.getMessage();
        }
    }

    /**
     * Change le mot de passe d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @param ancienMotDePasse L'ancien mot de passe à vérifier
     * @param nouveauMotDePasse Le nouveau mot de passe à définir
     * @return true si le changement a réussi, false sinon
     */
    public boolean changerMotDePasse(int userId, String ancienMotDePasse, String nouveauMotDePasse) {
        if (ancienMotDePasse == null || ancienMotDePasse.trim().isEmpty()) {
            System.err.println("L'ancien mot de passe ne peut pas être vide");
            return false;
        }
        
        if (nouveauMotDePasse == null || nouveauMotDePasse.trim().isEmpty()) {
            System.err.println("Le nouveau mot de passe ne peut pas être vide");
            return false;
        }
        
        // Vérifier la force du nouveau mot de passe
        if (!PasswordUtil.isPasswordStrong(nouveauMotDePasse)) {
            System.err.println("Le nouveau mot de passe n'est pas assez fort. " + 
                             "Recommandations: " + PasswordUtil.getPasswordRecommendations(nouveauMotDePasse));
            return false;
        }
        
        try {
            // Récupérer l'utilisateur pour vérifier l'ancien mot de passe
            Optional<utilisateur> userOpt = rechercherutilisateurParId(userId);
            if (!userOpt.isPresent()) {
                System.err.println("Utilisateur non trouvé (ID: " + userId + ")");
                return false;
            }
            
            utilisateur user = userOpt.get();
            
            // Vérification intelligente de l'ancien mot de passe
            boolean ancienMotDePasseValide = false;
            if (PasswordUtil.isHashedPassword(user.getPassword())) {
                // Le mot de passe stocké est hashé : utiliser la vérification par hash
                ancienMotDePasseValide = PasswordUtil.verifyPassword(ancienMotDePasse, user.getPassword());
            } else {
                // Le mot de passe stocké est en clair : utiliser la comparaison directe
                ancienMotDePasseValide = ancienMotDePasse.equals(user.getPassword());
            }
            
            if (!ancienMotDePasseValide) {
                System.err.println("L'ancien mot de passe est incorrect");
                return false;
            }
            
            // Mettre à jour avec le nouveau mot de passe hashé
            Connection connection = MaConnection.getInstance().getConnection();
            try (PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_PASSWORD)) {
                String hashedNewPassword = PasswordUtil.hashPassword(nouveauMotDePasse);
                ps.setString(1, hashedNewPassword);
                ps.setInt(2, userId);
                
                return ps.executeUpdate() > 0;
            }
            
        } catch (SQLException e) {
            afficherErreur("le changement de mot de passe", e);
            return false;
        }
    }
    
    /**
     * Réinitialise le mot de passe d'un utilisateur via son email
     * @param email L'email de l'utilisateur
     * @return true si la réinitialisation a réussi, false sinon
     */
    public boolean reinitialiserMotDePasse(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.err.println("L'email ne peut pas être vide");
            return false;
        }
        
        try {
            // Récupérer l'utilisateur par email
            Optional<utilisateur> userOpt = rechercherutilisateurParEmail(email);
            if (!userOpt.isPresent()) {
                System.err.println("Aucun utilisateur trouvé avec cet email: " + email);
                return false;
            }
            
            utilisateur user = userOpt.get();
            
            // Générer un mot de passe temporaire
            String tempPassword = PasswordUtil.generateTemporaryPassword();
            String hashedTempPassword = PasswordUtil.hashPassword(tempPassword);
            
            // Mettre à jour le mot de passe en base
            Connection connection = MaConnection.getInstance().getConnection();
            try (PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_PASSWORD)) {
                ps.setString(1, hashedTempPassword);
                ps.setInt(2, user.getId());
                
                int result = ps.executeUpdate();
                if (result > 0) {
                    System.out.println("Mot de passe réinitialisé pour " + email + 
                                     ". Nouveau mot de passe temporaire: " + tempPassword);
                    return true;
                }
                return false;
            }
            
        } catch (SQLException e) {
            afficherErreur("la réinitialisation du mot de passe", e);
            return false;
        }
    }
    
    /**
     * Recherche un utilisateur par son ID
     * @param userId L'ID de l'utilisateur
     * @return L'utilisateur trouvé ou Optional.empty()
     */
    private Optional<utilisateur> rechercherutilisateurParId(int userId) {
        Connection connection = MaConnection.getInstance().getConnection();
        try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_USER_BY_ID)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUtilisateurDepuisResultSet(rs, false));
                }
            }
        } catch (SQLException e) {
            afficherErreur("la recherche d'utilisateur par ID", e);
        }
        return Optional.empty();
    }

    /**
     * Gère la fonctionnalité "mot de passe oublié"
     * @param email L'email de l'utilisateur
     * @return true si succès, false sinon
     */
    public boolean motDePasseOublie(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.err.println("Email ne peut pas être vide");
            return false;
        }
        
        // Vérifier si la configuration email est valide
        if (!EmailService.isConfigurationValide()) {
            System.err.println("La configuration SMTP n'est pas valide. Veuillez configurer EmailService.");
            EmailService.afficherInstructionsConfiguration();
            return false;
        }
        
        try {
            // Vérifier que l'utilisateur existe
            Optional<utilisateur> userOpt = rechercherutilisateurParEmail(email);
            if (!userOpt.isPresent()) {
                System.err.println("Aucun utilisateur trouvé avec cet email: " + email);
                return false;
            }
            
            utilisateur user = userOpt.get();
            
            // Générer un mot de passe temporaire
            String tempPassword = PasswordUtil.generateTemporaryPassword();
            
            // Hasher ce mot de passe
            String hashedTempPassword = PasswordUtil.hashPassword(tempPassword);
            
            // Mettre à jour en base
            Connection connection = MaConnection.getInstance().getConnection();
            try (PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_PASSWORD)) {
                ps.setString(1, hashedTempPassword);
                ps.setInt(2, user.getId());
                
                int updated = ps.executeUpdate();
                if (updated <= 0) {
                    System.err.println("Échec de mise à jour du mot de passe temporaire en base");
                    return false;
                }
            }
            
            // Envoyer l'email
            String subject = "Réinitialisation de votre mot de passe";
            String body = "Bonjour " + user.getPrenom() + " " + user.getNom() + ",\n\n"
                    + "Vous avez demandé une réinitialisation de votre mot de passe.\n"
                    + "Voici votre mot de passe temporaire : " + tempPassword + "\n\n"
                    + "Merci de vous connecter avec ce mot de passe puis de le modifier depuis votre profil.\n\n"
                    + "Ceci est un message automatique, merci de ne pas répondre.";
            
            emailService.sendEmail(email, subject, body);
            
            System.out.println("Mot de passe temporaire envoyé à: " + email);
            return true;
            
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement motDePasseOublie: " + e.getMessage());
            return false;
        }
    }
}
