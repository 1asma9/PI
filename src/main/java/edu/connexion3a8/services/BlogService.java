package edu.connexion3a8.services;

import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.interfaces.IBlog;
import edu.connexion3a8.tools.MyConnection;

import java.beans.Statement;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class BlogService implements IBlog<Blog>{
    Connection cnx; // interface java tmathel cnnxmaa base de donne

    public BlogService() {
        cnx= MyConnection.getInstance().getCnx();
    }

    @Override
    public void ajouter(Blog blog) throws SQLException {
        String query = "INSERT INTO blog (titre, contenu, image_couverture, author_id, status, date_creation, date_publication, extrait, slug) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {

            ps.setString(1, blog.getTitre());
            ps.setString(2, blog.getContenu());
            ps.setString(3, blog.getImage_couverture());
            ps.setString(4, blog.getAuthor_id());
            ps.setBoolean(5, blog.isStatus());
            ps.setTimestamp(6, Timestamp.valueOf(blog.getDate_creation()));

            if (blog.getDate_publication() != null)
                ps.setTimestamp(7, Timestamp.valueOf(blog.getDate_publication()));
            else
                ps.setTimestamp(7, null);

            ps.setString(8, blog.getExtrait());
            ps.setString(9, blog.getSlug());

            ps.executeUpdate();
            System.out.println("Blog ajouté avec succès");
        }
    }

    @Override
    public void modifier(Blog blog) throws SQLException {
        String query = "UPDATE blog SET titre=?, contenu=?, image_couverture=?, author_id=?, status=?, date_publication=?, extrait=?, slug=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {

            ps.setString(1, blog.getTitre());
            ps.setString(2, blog.getContenu());
            ps.setString(3, blog.getImage_couverture());
            ps.setString(4, blog.getAuthor_id());
            ps.setBoolean(5, blog.isStatus());

            if (blog.getDate_publication() != null)
                ps.setTimestamp(6, Timestamp.valueOf(blog.getDate_publication()));
            else
                ps.setTimestamp(6, null);

            ps.setString(7, blog.getExtrait());
            ps.setString(8, blog.getSlug());
            ps.setInt(9, blog.getId());

            ps.executeUpdate();
            System.out.println("Blog modifié avec succès");
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM blog WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Blog supprimé avec succès");
        }
    }

    @Override
    public List<Blog> afficher() throws SQLException {
        List<Blog> blogs = new ArrayList<>();
        String query = "SELECT * FROM blog";

        try (PreparedStatement ps = cnx.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Blog blog = new Blog();

                blog.setId(rs.getInt("id"));
                blog.setTitre(rs.getString("titre"));
                blog.setContenu(rs.getString("contenu"));
                blog.setImage_couverture(rs.getString("image_couverture"));
                blog.setAuthor_id(rs.getString("author_id"));
                blog.setStatus(rs.getBoolean("status"));

                Timestamp creation = rs.getTimestamp("date_creation");
                if (creation != null)
                    blog.setDate_creation(creation.toLocalDateTime());

                Timestamp publication = rs.getTimestamp("date_publication");
                if (publication != null)
                    blog.setDate_publication(publication.toLocalDateTime());

                blog.setExtrait(rs.getString("extrait"));
                blog.setSlug(rs.getString("slug"));

                blogs.add(blog);
            }
        }

        return blogs;
    }

    @Override
    public Blog afficher1(int id) throws SQLException {
        String query = "SELECT * FROM blog WHERE id=?";
        Blog blog = null;

        try (PreparedStatement ps = cnx.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                blog = new Blog();
                blog.setId(rs.getInt("id"));
                blog.setTitre(rs.getString("titre"));
                blog.setContenu(rs.getString("contenu"));
                blog.setImage_couverture(rs.getString("image_couverture"));
                blog.setAuthor_id(rs.getString("author_id"));
                blog.setStatus(rs.getBoolean("status"));
                blog.setExtrait(rs.getString("extrait"));
                blog.setSlug(rs.getString("slug"));
            }
        }

        return blog;
    }
}
