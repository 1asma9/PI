package edu.connexion3a8.services;

import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.tools.MyConnection;

import java.sql.*;
import java.util.*;

public class SmartRecommendationService {

    private Connection cnx;

    public SmartRecommendationService() {
        cnx = new MyConnection().getCnx();
    }

    /**
     * RECOMMANDATION INTELLIGENTE basée sur plusieurs critères
     * 1. Historique des vues de l'utilisateur
     * 2. Similarité de contenu (mots-clés dans titre/extrait)
     * 3. Note moyenne élevée
     * 4. Popularité récente
     */
    public List<Blog> getPersonalizedRecommendations(int currentBlogId, String userIdentifier, int limit) throws SQLException {
        List<Blog> recommendations = new ArrayList<>();

        // Étape 1: Analyser l'historique de l'utilisateur
        List<Integer> viewedBlogIds = getUserViewHistory(userIdentifier);

        // Étape 2: Extraire les caractéristiques des blogs vus
        Map<String, Integer> preferredKeywords = extractPreferredKeywords(viewedBlogIds);

        // Étape 3: Trouver des blogs similaires
        String sql = buildSmartRecommendationQuery(currentBlogId, preferredKeywords, viewedBlogIds);

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, currentBlogId);
        ps.setInt(2, limit);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Blog blog = mapResultSetToBlog(rs);
            recommendations.add(blog);
        }

        // Si pas assez de résultats personnalisés, compléter avec des blogs populaires
        if (recommendations.size() < limit) {
            List<Blog> fallbackBlogs = getFallbackRecommendations(currentBlogId, limit - recommendations.size(), viewedBlogIds);
            recommendations.addAll(fallbackBlogs);
        }

        return recommendations;
    }

    /**
     * Obtenir l'historique des vues d'un utilisateur
     */
    private List<Integer> getUserViewHistory(String userIdentifier) throws SQLException {
        List<Integer> viewedBlogIds = new ArrayList<>();

        String sql = "SELECT DISTINCT blog_id FROM blog_views " +
                "WHERE user_identifier = ? " +
                "ORDER BY view_date DESC LIMIT 10";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, userIdentifier);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            viewedBlogIds.add(rs.getInt("blog_id"));
        }

        return viewedBlogIds;
    }

    /**
     * Extraire les mots-clés préférés de l'utilisateur
     */
    private Map<String, Integer> extractPreferredKeywords(List<Integer> blogIds) throws SQLException {
        Map<String, Integer> keywords = new HashMap<>();

        if (blogIds.isEmpty()) {
            return keywords;
        }

        // Construire la requête avec IN clause
        StringBuilder sql = new StringBuilder("SELECT titre, extrait FROM blog WHERE id IN (");
        for (int i = 0; i < blogIds.size(); i++) {
            sql.append("?");
            if (i < blogIds.size() - 1) sql.append(",");
        }
        sql.append(")");

        PreparedStatement ps = cnx.prepareStatement(sql.toString());
        for (int i = 0; i < blogIds.size(); i++) {
            ps.setInt(i + 1, blogIds.get(i));
        }

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String titre = rs.getString("titre");
            String extrait = rs.getString("extrait");

            // Extraire les mots-clés (simplification: mots de plus de 4 lettres)
            String combined = (titre + " " + extrait).toLowerCase();
            String[] words = combined.split("\\s+");

            for (String word : words) {
                // Nettoyer le mot
                word = word.replaceAll("[^a-zàâäéèêëïîôöùûüÿç]", "");

                if (word.length() > 4) {
                    keywords.put(word, keywords.getOrDefault(word, 0) + 1);
                }
            }
        }

        return keywords;
    }

    /**
     * Construire la requête de recommandation intelligente
     */
    private String buildSmartRecommendationQuery(int currentBlogId, Map<String, Integer> keywords, List<Integer> viewedIds) {
        StringBuilder sql = new StringBuilder(
                "SELECT *, " +
                        "(" +
                        "  COALESCE(rating_average, 0) * 2 + " +  // Poids sur la note
                        "  (SELECT COUNT(*) FROM blog_views WHERE blog_id = blog.id) * 0.1 + " +  // Poids sur les vues
                        "  CASE WHEN DATEDIFF(NOW(), date_creation) < 30 THEN 5 ELSE 0 END + " +  // Bonus si récent
                        "  ("
        );

        // Ajouter le score de similarité basé sur les mots-clés
        if (!keywords.isEmpty()) {
            int i = 0;
            for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
                if (i > 0) sql.append(" + ");
                sql.append(String.format(
                        "CASE WHEN LOWER(CONCAT(titre, ' ', extrait)) LIKE '%%%s%%' THEN %d ELSE 0 END",
                        entry.getKey(), entry.getValue()
                ));
                i++;
                if (i >= 5) break;  // Limiter à 5 mots-clés pour la performance
            }
        } else {
            sql.append("0");
        }

        sql.append(")");
        sql.append(") as recommendation_score ");

        sql.append("FROM blog ");
        sql.append("WHERE id != ? AND status = 1 ");

        // Exclure les blogs déjà vus
        if (!viewedIds.isEmpty()) {
            sql.append("AND id NOT IN (");
            for (int i = 0; i < viewedIds.size(); i++) {
                sql.append(viewedIds.get(i));
                if (i < viewedIds.size() - 1) sql.append(",");
            }
            sql.append(") ");
        }

        sql.append("ORDER BY recommendation_score DESC, rating_average DESC ");
        sql.append("LIMIT ?");

        return sql.toString();
    }

    /**
     * Recommandations de secours (blogs populaires)
     */
    private List<Blog> getFallbackRecommendations(int currentBlogId, int limit, List<Integer> excludeIds) throws SQLException {
        List<Blog> fallback = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM blog " +
                        "WHERE id != ? AND status = 1 "
        );

        if (!excludeIds.isEmpty()) {
            sql.append("AND id NOT IN (");
            for (int i = 0; i < excludeIds.size(); i++) {
                sql.append(excludeIds.get(i));
                if (i < excludeIds.size() - 1) sql.append(",");
            }
            sql.append(") ");
        }

        sql.append("ORDER BY rating_average DESC, date_creation DESC LIMIT ?");

        PreparedStatement ps = cnx.prepareStatement(sql.toString());
        ps.setInt(1, currentBlogId);
        ps.setInt(2, limit);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            fallback.add(mapResultSetToBlog(rs));
        }

        return fallback;
    }

    /**
     * Mapper ResultSet vers Blog
     */
    private Blog mapResultSetToBlog(ResultSet rs) throws SQLException {
        Blog blog = new Blog();
        blog.setId(rs.getInt("id"));
        blog.setTitre(rs.getString("titre"));
        blog.setContenu(rs.getString("contenu"));
        blog.setExtrait(rs.getString("extrait"));
        blog.setImage_couverture(rs.getString("image_couverture"));
        blog.setStatus(rs.getBoolean("status"));
        blog.setSlug(rs.getString("slug"));

        // Gérer la date
        Timestamp timestamp = rs.getTimestamp("date_creation");
        if (timestamp != null) {
            blog.setDate_creation(timestamp.toLocalDateTime());
        }

        // Rating
        blog.setRatingAverage(rs.getDouble("rating_average"));
        blog.setRatingCount(rs.getInt("rating_count"));

        return blog;
    }

    /**
     * Version simple: Recommandations basées uniquement sur la note et la popularité
     * (Pour backward compatibility)
     */
    public List<Blog> getRecommendations(int currentBlogId, int limit) throws SQLException {
        return getPersonalizedRecommendations(currentBlogId, "anonymous", limit);
    }
}