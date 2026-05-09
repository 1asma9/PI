package hebergement.services;

import hebergement.entities.Chambre;
import hebergement.entities.Hebergement;
import hebergement.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChambreService {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    // ===== AJOUTER =====
    public void add(Chambre c) throws SQLException {
        validate(c);
        String sql = "INSERT INTO chambre (hebergement_id, numero, type_chambre, prix_nuit, capacite, equipements) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt   (1, c.getHebergement().getId());
        ps.setString(2, c.getNumero());
        ps.setString(3, c.getTypeChambre());
        ps.setDouble(4, c.getPrixNuit());
        ps.setInt   (5, c.getCapacite());
        ps.setString(6, c.getEquipements());
        ps.executeUpdate();
    }

    // ===== MODIFIER =====
    public void update(int id, Chambre c) throws SQLException {
        validate(c);
        String sql = "UPDATE chambre SET hebergement_id=?, numero=?, type_chambre=?, " +
                "prix_nuit=?, capacite=?, equipements=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt   (1, c.getHebergement().getId());
        ps.setString(2, c.getNumero());
        ps.setString(3, c.getTypeChambre());
        ps.setDouble(4, c.getPrixNuit());
        ps.setInt   (5, c.getCapacite());
        ps.setString(6, c.getEquipements());
        ps.setInt   (7, id);
        ps.executeUpdate();
    }

    // ===== SUPPRIMER =====
    public void delete(Chambre c) throws SQLException {
        String sql = "DELETE FROM chambre WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, c.getId());
        ps.executeUpdate();
    }

    // ===== LISTER TOUT =====
    public List<Chambre> getData() throws SQLException {
        List<Chambre> list = new ArrayList<>();
        String sql = "SELECT c.*, h.description as heb_desc, h.adresse as heb_adr " +
                "FROM chambre c " +
                "LEFT JOIN hebergement h ON c.hebergement_id = h.id " +
                "ORDER BY c.id DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    // ===== LISTER PAR HEBERGEMENT =====
    public List<Chambre> getByHebergement(int hebId) throws SQLException {
        List<Chambre> list = new ArrayList<>();
        String sql = "SELECT c.*, h.description as heb_desc, h.adresse as heb_adr " +
                "FROM chambre c " +
                "LEFT JOIN hebergement h ON c.hebergement_id = h.id " +
                "WHERE c.hebergement_id = ? ORDER BY c.id DESC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, hebId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    // ===== MAPPING ResultSet → Chambre =====
    private Chambre mapRow(ResultSet rs) throws SQLException {
        Chambre c = new Chambre();
        c.setId(rs.getInt("id"));
        c.setNumero(rs.getString("numero"));
        c.setTypeChambre(rs.getString("type_chambre"));
        c.setPrixNuit(rs.getDouble("prix_nuit"));
        c.setCapacite(rs.getInt("capacite"));
        c.setEquipements(rs.getString("equipements"));

        Hebergement h = new Hebergement();
        h.setId(rs.getInt("hebergement_id"));
        h.setDescription(rs.getString("heb_desc"));
        h.setAdresse(rs.getString("heb_adr"));
        c.setHebergement(h);

        return c;
    }

    // ===== VALIDATION =====
    private void validate(Chambre c) {
        if (c.getHebergement() == null)
            throw new IllegalArgumentException("L'hébergement est obligatoire.");
        if (c.getNumero() == null || c.getNumero().isBlank())
            throw new IllegalArgumentException("Le numéro est obligatoire.");
        if (c.getNumero().length() > 10)
            throw new IllegalArgumentException("Le numéro ne peut pas dépasser 10 caractères.");
        if (c.getTypeChambre() == null || c.getTypeChambre().isBlank())
            throw new IllegalArgumentException("Le type est obligatoire.");
        if (!List.of("simple","double","suite","familiale").contains(c.getTypeChambre().toLowerCase()))
            throw new IllegalArgumentException("Type doit être : simple, double, suite ou familiale.");
        if (c.getPrixNuit() <= 0)
            throw new IllegalArgumentException("Le prix doit être positif.");
        if (c.getCapacite() <= 0)
            throw new IllegalArgumentException("La capacité doit être positive.");
        if (c.getCapacite() > 20)
            throw new IllegalArgumentException("La capacité ne peut pas dépasser 20 personnes.");
        if (c.getEquipements() == null || c.getEquipements().isBlank())
            throw new IllegalArgumentException("Les équipements sont obligatoires.");
    }
}