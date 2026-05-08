package edu.connexion3a8.services;

import edu.connexion3a8.entities.BlogRating;
import edu.connexion3a8.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatingService {

    private Connection cnx;

    public RatingService() {
        cnx = MyConnection.getInstance().getCnx();    }

    /**
     * Ajouter une note/avis à un blog
     */
    public void addRating(BlogRating rating) throws SQLException {
        String sql = "INSERT INTO blog_rating (blog_id, user_name, rating, review_text) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setInt(1, rating.getBlogId());
        ps.setString(2, rating.getUserName());
        ps.setInt(3, rating.getRating());
        ps.setString(4, rating.getReviewText());

        ps.executeUpdate();

        // Mettre à jour la moyenne du blog
        updateBlogRatingAverage(rating.getBlogId());
    }

    /**
     * Recalculer et mettre à jour la moyenne de notation d'un blog
     */
    private void updateBlogRatingAverage(int blogId) throws SQLException {
        String sql = "UPDATE blog SET " +
                "rating_average = (SELECT AVG(rating) FROM blog_rating WHERE blog_id = ?), " +
                "rating_count = (SELECT COUNT(*) FROM blog_rating WHERE blog_id = ?) " +
                "WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, blogId);
        ps.setInt(2, blogId);
        ps.setInt(3, blogId);
        ps.executeUpdate();
    }

    /**
     * Obtenir toutes les notes d'un blog
     */
    public List<BlogRating> getRatingsByBlog(int blogId) throws SQLException {
        List<BlogRating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM blog_rating WHERE blog_id = ? ORDER BY created_at DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, blogId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            BlogRating rating = new BlogRating();
            rating.setId(rs.getInt("id"));
            rating.setBlogId(rs.getInt("blog_id"));
            rating.setUserName(rs.getString("user_name"));
            rating.setRating(rs.getInt("rating"));
            rating.setReviewText(rs.getString("review_text"));
            rating.setCreatedAt(rs.getString("created_at"));
            ratings.add(rating);
        }

        return ratings;
    }

    /**
     * Obtenir la note moyenne d'un blog
     */
    public double getAverageRating(int blogId) throws SQLException {
        String sql = "SELECT rating_average FROM blog WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, blogId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getDouble("rating_average");
        }
        return 0.0;
    }

    /**
     * Vérifier si un utilisateur a déjà noté un blog
     */
    public boolean hasUserRated(int blogId, String userName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM blog_rating WHERE blog_id = ? AND user_name = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, blogId);
        ps.setString(2, userName);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    /**
     * Supprimer une note
     */
    public void deleteRating(int ratingId) throws SQLException {
        // D'abord récupérer le blog_id pour mettre à jour la moyenne après
        String getBlogIdSql = "SELECT blog_id FROM blog_rating WHERE id = ?";
        PreparedStatement ps1 = cnx.prepareStatement(getBlogIdSql);
        ps1.setInt(1, ratingId);
        ResultSet rs = ps1.executeQuery();

        int blogId = 0;
        if (rs.next()) {
            blogId = rs.getInt("blog_id");
        }

        // Supprimer la note
        String deleteSql = "DELETE FROM blog_rating WHERE id = ?";
        PreparedStatement ps2 = cnx.prepareStatement(deleteSql);
        ps2.setInt(1, ratingId);
        ps2.executeUpdate();

        // Mettre à jour la moyenne
        if (blogId > 0) {
            updateBlogRatingAverage(blogId);
        }
    }
}