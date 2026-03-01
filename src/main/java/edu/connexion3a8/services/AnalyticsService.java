package edu.connexion3a8.services;

import edu.connexion3a8.tools.MyConnection;

import java.sql.*;
import java.util.*;

public class AnalyticsService {

    private Connection cnx;

    public AnalyticsService() {
        cnx = new MyConnection().getCnx();
    }

    /**
     * Enregistrer une vue de blog
     */
    public void trackView(int blogId, String userIdentifier) throws SQLException {
        String sql = "INSERT INTO blog_views (blog_id, user_identifier) VALUES (?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, blogId);
        ps.setString(2, userIdentifier != null ? userIdentifier : "anonymous");
        ps.executeUpdate();
    }

    /**
     * Obtenir le nombre total de blogs
     */
    public int getTotalBlogs() throws SQLException {
        String sql = "SELECT COUNT(*) FROM blog";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        return rs.next() ? rs.getInt(1) : 0;
    }

    /**
     * Obtenir le nombre total de vues
     */
    public int getTotalViews() throws SQLException {
        String sql = "SELECT COUNT(*) FROM blog_views";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        return rs.next() ? rs.getInt(1) : 0;
    }

    /**
     * Obtenir le nombre total de commentaires
     */
    public int getTotalComments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM commentaire";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        return rs.next() ? rs.getInt(1) : 0;
    }

    /**
     * Obtenir les blogs les plus vus
     */
    public Map<String, Integer> getTopBlogsByViews(int limit) throws SQLException {
        Map<String, Integer> topBlogs = new LinkedHashMap<>();

        String sql = "SELECT b.titre, COUNT(v.id) as view_count " +
                "FROM blog b " +
                "LEFT JOIN blog_views v ON b.id = v.blog_id " +
                "GROUP BY b.id, b.titre " +
                "ORDER BY view_count DESC " +
                "LIMIT ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            topBlogs.put(rs.getString("titre"), rs.getInt("view_count"));
        }

        return topBlogs;
    }

    /**
     * Obtenir les blogs les plus commentés
     */
    public Map<String, Integer> getTopBlogsByComments(int limit) throws SQLException {
        Map<String, Integer> topBlogs = new LinkedHashMap<>();

        String sql = "SELECT b.titre, COUNT(c.id) as comment_count " +
                "FROM blog b " +
                "LEFT JOIN commentaire c ON b.id = c.blog_id " +
                "GROUP BY b.id, b.titre " +
                "ORDER BY comment_count DESC " +
                "LIMIT ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            topBlogs.put(rs.getString("titre"), rs.getInt("comment_count"));
        }

        return topBlogs;
    }

    /**
     * Obtenir les blogs les mieux notés
     */
    public Map<String, Double> getTopBlogsByRating(int limit) throws SQLException {
        Map<String, Double> topBlogs = new LinkedHashMap<>();

        String sql = "SELECT titre, rating_average " +
                "FROM blog " +
                "WHERE rating_count > 0 " +
                "ORDER BY rating_average DESC, rating_count DESC " +
                "LIMIT ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            topBlogs.put(rs.getString("titre"), rs.getDouble("rating_average"));
        }

        return topBlogs;
    }

    /**
     * Obtenir les vues des X derniers jours pour un blog
     */
    public Map<String, Integer> getBlogViewsTrend(int blogId, int days) throws SQLException {
        Map<String, Integer> trend = new LinkedHashMap<>();

        String sql = "SELECT DATE(view_date) as day, COUNT(*) as views " +
                "FROM blog_views " +
                "WHERE blog_id = ? AND view_date >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                "GROUP BY DATE(view_date) " +
                "ORDER BY day ASC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, blogId);
        ps.setInt(2, days);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            trend.put(rs.getString("day"), rs.getInt("views"));
        }

        return trend;
    }

    /**
     * Obtenir les vues globales des X derniers jours
     */
    public Map<String, Integer> getGlobalViewsTrend(int days) throws SQLException {
        Map<String, Integer> trend = new LinkedHashMap<>();

        String sql = "SELECT DATE(view_date) as day, COUNT(*) as views " +
                "FROM blog_views " +
                "WHERE view_date >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                "GROUP BY DATE(view_date) " +
                "ORDER BY day ASC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, days);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            trend.put(rs.getString("day"), rs.getInt("views"));
        }

        return trend;
    }

    /**
     * Obtenir le taux de croissance des vues (%)
     */
    public double getViewsGrowthRate() throws SQLException {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM blog_views WHERE view_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)) as current_week, " +
                "(SELECT COUNT(*) FROM blog_views WHERE view_date BETWEEN DATE_SUB(NOW(), INTERVAL 14 DAY) AND DATE_SUB(NOW(), INTERVAL 7 DAY)) as previous_week";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        if (rs.next()) {
            int currentWeek = rs.getInt("current_week");
            int previousWeek = rs.getInt("previous_week");

            if (previousWeek == 0) return 100.0;

            return ((double)(currentWeek - previousWeek) / previousWeek) * 100;
        }

        return 0.0;
    }
}