package edu.connexion3a8.services;

import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecommendationService {

    private Connection cnx;

    public RecommendationService() {
        cnx = new MyConnection().getCnx();
    }

    /**
     * Recommande des blogs similaires
     * @param currentBlogId ID du blog actuel
     * @param limit Nombre de recommandations
     * @return Liste de blogs recommandés
     */
    public List<Blog> getRecommendations(int currentBlogId, int limit) throws SQLException {
        List<Blog> recommendations = new ArrayList<>();

        // Stratégie : Blogs les plus récents avec sentiment positif
        String sql = "SELECT * FROM blog " +
                "WHERE id != ? AND status = 1 " +
                "ORDER BY sentiment_score DESC, date_creation DESC " +
                "LIMIT ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, currentBlogId);
        ps.setInt(2, limit);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Blog blog = new Blog();
            // Remplir le blog...
            recommendations.add(blog);
        }

        return recommendations;
    }
}
