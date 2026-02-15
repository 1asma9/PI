package edu.connexion3a8.services;

import edu.connexion3a8.entities.Commentaire;
import edu.connexion3a8.interfaces.ICommentaire;
import edu.connexion3a8.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService implements ICommentaire<Commentaire> {

    Connection cnx;

    public CommentaireService() {
        cnx = new MyConnection().getCnx();
    }

    // ✅ Ajouter
    @Override
    public void ajouter(Commentaire c) throws SQLException {
        String sql = "INSERT INTO commentaire (contenu, nomuser, img, likes_count, liked) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, c.getContenu());
        ps.setString(2, c.getNomuser());
        ps.setString(3, c.getImg());
        ps.setInt(4, c.getLikesCount());
        ps.setBoolean(5, c.isLiked());

        ps.executeUpdate();
    }

    // ✅ Modifier
    @Override
    public void modifier(Commentaire c) throws SQLException {
        String sql = "UPDATE commentaire SET contenu=?, nomuser=?, img=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, c.getContenu());
        ps.setString(2, c.getNomuser());
        ps.setString(3, c.getImg());
        ps.setInt(4, c.getId());

        ps.executeUpdate();
    }

    // ✅ Supprimer
    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM commentaire WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // ✅ Afficher tous
    @Override
    public List<Commentaire> afficher() throws SQLException {
        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Commentaire c = new Commentaire();

            c.setId(rs.getInt("id"));
            c.setContenu(rs.getString("contenu"));
            c.setDate(rs.getString("date_creation"));
            c.setNomuser(rs.getString("nomuser"));
            c.setImg(rs.getString("img"));
            c.setLikesCount(rs.getInt("likes_count"));
            c.setLiked(rs.getBoolean("liked"));

            list.add(c);
        }

        return list;
    }

    // ✅ Afficher un seul
    @Override
    public Commentaire afficher1(int id) throws SQLException {
        String sql = "SELECT * FROM commentaire WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Commentaire c = new Commentaire();

            c.setId(rs.getInt("id"));
            c.setContenu(rs.getString("contenu"));
            c.setDate(rs.getString("date_creation"));
            c.setNomuser(rs.getString("nomuser"));
            c.setImg(rs.getString("img"));
            c.setLikesCount(rs.getInt("likes_count"));
            c.setLiked(rs.getBoolean("liked"));

            return c;
        }

        return null;
    }

    // ✅ Ajouter Like
    @Override
    public void ajouterLike(int id) throws SQLException {
        String sql = "UPDATE commentaire SET likes_count = likes_count + 1, liked = true WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // ✅ Retirer Like
    @Override
    public void retirerLike(int id) throws SQLException {
        String sql = "UPDATE commentaire SET likes_count = likes_count - 1, liked = false WHERE id=? AND likes_count > 0";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}