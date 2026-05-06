package org.example.PI_Gestion_des_utilisateurs.services;

import tools.MyConnection;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import org.example.PI_Gestion_des_utilisateurs.tools.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class utilisateur_service {

    public static String lastError = "";

    private final EmailService emailService = new EmailService();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Connection getCnx() {
        return MyConnection.getInstance().getCnx();
    }

    // =============== SQL ===============

    private static final String SQL_INSERT_USER =
            "INSERT INTO users (nom, prenom, email, password_hash, telephone) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_USERS_WITH_ROLES =
            "SELECT u.id, u.nom, u.prenom, u.email, u.password_hash, u.telephone, u.created_at, " +
                    "r.name AS role_name, r.description AS role_description " +
                    "FROM users u " +
                    "LEFT JOIN users_role ur ON ur.users_id = u.id " +
                    "LEFT JOIN role r ON r.id = ur.role_id " +
                    "ORDER BY u.id";

    private static final String SQL_SELECT_USER_BY_EMAIL =
            "SELECT u.id, u.nom, u.prenom, u.email, u.password_hash, u.telephone, u.created_at, " +
                    "r.name AS role_name, r.description AS role_description " +
                    "FROM users u " +
                    "LEFT JOIN users_role ur ON ur.users_id = u.id " +
                    "LEFT JOIN role r ON r.id = ur.role_id " +
                    "WHERE u.email=? LIMIT 1";

    private static final String SQL_SELECT_USER_BY_ID =
            "SELECT u.id, u.nom, u.prenom, u.email, u.password_hash, u.telephone, u.created_at, " +
                    "r.name AS role_name, r.description AS role_description " +
                    "FROM users u " +
                    "LEFT JOIN users_role ur ON ur.users_id = u.id " +
                    "LEFT JOIN role r ON r.id = ur.role_id " +
                    "WHERE u.id=? LIMIT 1";

    private static final String SQL_COUNT_USER_BY_EMAIL =
            "SELECT COUNT(*) FROM users WHERE email=?";

    private static final String SQL_UPDATE_USER =
            "UPDATE users SET nom=?, prenom=?, email=?, password_hash=?, telephone=? WHERE id=?";

    private static final String SQL_DELETE_USER_ROLES =
            "DELETE FROM users_role WHERE users_id=?";

    private static final String SQL_DELETE_USER =
            "DELETE FROM users WHERE id=?";

    private static final String SQL_ASSOCIER_ROLE =
            "INSERT INTO users_role (users_id, role_id) VALUES (?, ?)";

    private static final String SQL_UPDATE_PASSWORD =
            "UPDATE users SET password_hash=? WHERE id=?";

    // =============== MAPPING ===============

    private utilisateur mapUtilisateur(ResultSet rs, boolean avecRoles) throws SQLException {
        utilisateur u = new utilisateur();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password_hash"));
        u.setTelephone(rs.getString("telephone"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setDateCreation(ts.toLocalDateTime());

        if (avecRoles) {
            u.setRoleName(rs.getString("role_name"));
            u.setRoleDescription(rs.getString("role_description"));
        }
        return u;
    }

    // =============== VALIDATION ===============

    public String validerDonneesutilisateurAvecMessage(utilisateur u) {
        if (u == null) return "L'utilisateur ne peut pas être null";
        if (u.getNom() == null || u.getNom().trim().isEmpty()) return "Le nom ne peut pas être vide";
        if (u.getPrenom() == null || u.getPrenom().trim().isEmpty()) return "Le prénom ne peut pas être vide";

        String email = u.getEmail();
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) return "L'email n'est pas valide";

        String pwd = u.getPassword();
        if (pwd == null || pwd.length() < 6) return "Le mot de passe doit contenir au moins 6 caractères";

        String tel = u.getTelephone();
        if (tel != null && !tel.trim().isEmpty() && !tel.matches("\\d{8,}"))
            return "Le numéro de téléphone doit contenir uniquement des chiffres (min 8)";

        return null;
    }

    public boolean validerDonneesutilisateur(utilisateur u) {
        return validerDonneesutilisateurAvecMessage(u) == null;
    }

    public boolean verifierEmailUnique(String email) {
        if (email == null) return true;
        try (PreparedStatement ps = getCnx().prepareStatement(SQL_COUNT_USER_BY_EMAIL)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    // =============== CRUD ===============

    public boolean ajouterutilisateur(utilisateur u) {
        String err = validerDonneesutilisateurAvecMessage(u);
        if (err != null) {
            lastError = err;
            return false;
        }
        if (!verifierEmailUnique(u.getEmail())) {
            lastError = "Email déjà utilisé";
            return false;
        }

        try (PreparedStatement ps = getCnx().prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom().trim());
            ps.setString(2, u.getPrenom().trim());
            ps.setString(3, u.getEmail().trim());
            ps.setString(4, PasswordUtil.hashPassword(u.getPassword()));
            ps.setString(5, u.getTelephone());

            int rows = ps.executeUpdate();
            if (rows <= 0) {
                lastError = "Aucune ligne insérée";
                return false;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getInt(1));
            }

            envoyerEmailBienvenue(u);
            return true;

        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public List<utilisateur> afficherutilisateurs() {
        List<utilisateur> list = new ArrayList<>();
        try (PreparedStatement ps = getCnx().prepareStatement(SQL_SELECT_USERS_WITH_ROLES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapUtilisateur(rs, true));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean modifierutilisateur(utilisateur u) {
        String err = validerDonneesutilisateurAvecMessage(u);
        if (err != null) return false;

        try (PreparedStatement ps = getCnx().prepareStatement(SQL_UPDATE_USER)) {
            ps.setString(1, u.getNom().trim());
            ps.setString(2, u.getPrenom().trim());
            ps.setString(3, u.getEmail().trim());
            ps.setString(4, PasswordUtil.hashPassword(u.getPassword()));
            ps.setString(5, u.getTelephone());
            ps.setInt(6, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimerutilisateur(int id) {
        try {
            getCnx().setAutoCommit(false);

            try (PreparedStatement ps1 = getCnx().prepareStatement(SQL_DELETE_USER_ROLES)) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
            }

            boolean ok;
            try (PreparedStatement ps2 = getCnx().prepareStatement(SQL_DELETE_USER)) {
                ps2.setInt(1, id);
                ok = ps2.executeUpdate() > 0;
            }

            getCnx().commit();
            getCnx().setAutoCommit(true);
            return ok;

        } catch (SQLException e) {
            try { getCnx().rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        }
    }

    // =============== SEARCH ===============

    public Optional<utilisateur> rechercherutilisateurParEmail(String email) {
        if (email == null || email.trim().isEmpty()) return Optional.empty();
        try (PreparedStatement ps = getCnx().prepareStatement(SQL_SELECT_USER_BY_EMAIL)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapUtilisateur(rs, true));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<utilisateur> rechercherutilisateurParId(int id) {
        try (PreparedStatement ps = getCnx().prepareStatement(SQL_SELECT_USER_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapUtilisateur(rs, true));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // =============== ROLE ASSIGN ===============

    public boolean associerRoleAutilisateur(int userId, int roleId) {
        if (userId <= 0 || roleId <= 0) return false;

        try (PreparedStatement del = getCnx().prepareStatement("DELETE FROM users_role WHERE users_id=?")) {
            del.setInt(1, userId);
            del.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try (PreparedStatement ps = getCnx().prepareStatement(SQL_ASSOCIER_ROLE)) {
            ps.setInt(1, userId);
            ps.setInt(2, roleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =============== PASSWORD RESET ===============

    public boolean motDePasseOublie(String email) {
        Optional<utilisateur> opt = rechercherutilisateurParEmail(email);
        if (opt.isEmpty()) return false;

        utilisateur u = opt.get();
        String temp = PasswordUtil.generateTemporaryPassword();

        try (PreparedStatement ps = getCnx().prepareStatement(SQL_UPDATE_PASSWORD)) {
            ps.setString(1, PasswordUtil.hashPassword(temp));
            ps.setInt(2, u.getId());
            if (ps.executeUpdate() <= 0) return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try {
            String subject = "Réinitialisation de votre mot de passe";
            String body = "Bonjour " + u.getPrenom() + " " + u.getNom() + ",\n\n"
                    + "Voici votre mot de passe temporaire : " + temp + "\n\n"
                    + "Connectez-vous puis modifiez-le.\n\n"
                    + "Message automatique.";
            emailService.sendEmail(email, subject, body);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void envoyerEmailBienvenue(utilisateur u) {
        try {
            if (!emailService.isConfigured()) return;
            String sujet = "Bienvenue";
            String corps = EmailVianovaTemplate.genererEmailBienvenueSimple(
                    u.getNom(), u.getPrenom(), u.getEmail(), u.getPassword()
            );
            emailService.sendEmail(u.getEmail(), sujet, corps);
        } catch (Exception e) {
            System.err.println("Email bienvenue échoué: " + e.getMessage());
        }
    }
}