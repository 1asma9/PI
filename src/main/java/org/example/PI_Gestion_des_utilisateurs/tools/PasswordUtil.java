package org.example.PI_Gestion_des_utilisateurs.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utilitaire pour la gestion sécurisée des mots de passe
 * Fournit des méthodes de hashage, validation et génération
 */
public class PasswordUtil {
    
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Hash un mot de passe en utilisant l'algorithme SHA-256
     * @param password Le mot de passe en clair à hasher
     * @return Le hash du mot de passe en format hexadécimal
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Convertir le mot de passe en bytes
            byte[] passwordBytes = password.getBytes();
            
            // Calculer le hash
            byte[] hashBytes = digest.digest(passwordBytes);
            
            // Convertir le hash en format hexadécimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 est toujours disponible dans Java, mais au cas où
            throw new RuntimeException("Algorithme SHA-256 non disponible", e);
        }
    }
    
    /**
     * Vérifie si un mot de passe est suffisamment fort
     * @param password Le mot de passe à valider
     * @return true si le mot de passe est fort, false sinon
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Vérifier les critères de force
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[@$!%*?&].*");
        
        return hasLower && hasUpper && hasDigit && hasSpecial;
    }
    
    /**
     * Génère un mot de passe temporaire sécurisé
     * @return Un mot de passe temporaire de 12 caractères
     */
    public static String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*?&";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 12; i++) {
            int randomIndex = secureRandom.nextInt(chars.length());
            password.append(chars.charAt(randomIndex));
        }
        
        return password.toString();
    }
    
    /**
     * Vérifie si un mot de passe correspond à un hash
     * @param password Le mot de passe en clair à vérifier
     * @param hashedPassword Le hash stocké en base de données
     * @return true si le mot de passe correspond au hash, false sinon
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        String computedHash = hashPassword(password);
        return computedHash.equals(hashedPassword);
    }
    
    /**
     * Vérifie si une chaîne ressemble à un hash SHA-256
     * @param password La chaîne à vérifier
     * @return true si la chaîne ressemble à un hash SHA-256, false sinon
     */
    public static boolean isHashedPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        // Un hash SHA-256 fait toujours 64 caractères hexadécimaux
        // Mais on ajoute une vérification plus stricte pour éviter les faux positifs
        if (password.length() != 64) {
            return false;
        }
        
        // Vérifier que tous les caractères sont hexadécimaux
        return password.matches("^[a-fA-F0-9]{64}$");
    }
    
    /**
     * Méthode de test pour vérifier le fonctionnement du hashage
     * @param args non utilisé
     */
    public static void main(String[] args) {
        String testPassword = "Test123@";
        String hash = hashPassword(testPassword);
        
        System.out.println("Mot de passe test: " + testPassword);
        System.out.println("Hash: " + hash);
        System.out.println("Longueur: " + hash.length());
        System.out.println("Est hashé: " + isHashedPassword(hash));
        System.out.println("Vérification: " + verifyPassword(testPassword, hash));
    }
    
    /**
     * Retourne des recommandations pour améliorer un mot de passe
     * @param password Le mot de passe à analyser
     * @return Une chaîne de caractères avec les recommandations
     */
    public static String getPasswordRecommendations(String password) {
        StringBuilder recommendations = new StringBuilder();
        
        if (password == null || password.length() < 8) {
            recommendations.append("• Au moins 8 caractères\n");
        }
        
        if (!password.matches(".*[a-z].*")) {
            recommendations.append("• Au moins une lettre minuscule\n");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            recommendations.append("• Au moins une lettre majuscule\n");
        }
        
        if (!password.matches(".*\\d.*")) {
            recommendations.append("• Au moins un chiffre\n");
        }
        
        if (!password.matches(".*[@$!%*?&].*")) {
            recommendations.append("• Au moins un caractère spécial (@$!%*?&)\n");
        }
        
        return recommendations.toString().trim();
    }
}
