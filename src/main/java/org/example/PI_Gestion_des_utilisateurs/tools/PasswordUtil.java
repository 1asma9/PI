package org.example.PI_Gestion_des_utilisateurs.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtil {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] passwordBytes = password.getBytes();
            byte[] hashBytes = digest.digest(passwordBytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithme SHA-256 non disponible", e);
        }
    }

    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasLower   = password.matches(".*[a-z].*");
        boolean hasUpper   = password.matches(".*[A-Z].*");
        boolean hasDigit   = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[@$!%*?&].*");
        return hasLower && hasUpper && hasDigit && hasSpecial;
    }

    public static String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*?&";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return password.toString();
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
        if (isBcryptHash(hashedPassword)) {
            return at.favre.lib.crypto.bcrypt.BCrypt.verifyer()
                    .verify(password.toCharArray(), hashedPassword).verified;
        }
        return hashPassword(password).equals(hashedPassword);
    }

    public static boolean isHashedPassword(String password) {
        if (password == null) return false;
        return isBcryptHash(password) || password.matches("^[a-fA-F0-9]{64}$");
    }

    public static boolean isBcryptHash(String password) {
        return password != null && (password.startsWith("$2y$") || password.startsWith("$2a$"));
    }

    public static void main(String[] args) {
        String testPassword = "Test123@";
        String hash = hashPassword(testPassword);
        System.out.println("Mot de passe test: " + testPassword);
        System.out.println("Hash: " + hash);
        System.out.println("Longueur: " + hash.length());
        System.out.println("Est hashé: " + isHashedPassword(hash));
        System.out.println("Vérification: " + verifyPassword(testPassword, hash));
    }

    public static String getPasswordRecommendations(String password) {
        StringBuilder recommendations = new StringBuilder();
        if (password == null || password.length() < 8)
            recommendations.append("• Au moins 8 caractères\n");
        if (!password.matches(".*[a-z].*"))
            recommendations.append("• Au moins une lettre minuscule\n");
        if (!password.matches(".*[A-Z].*"))
            recommendations.append("• Au moins une lettre majuscule\n");
        if (!password.matches(".*\\d.*"))
            recommendations.append("• Au moins un chiffre\n");
        if (!password.matches(".*[@$!%*?&].*"))
            recommendations.append("• Au moins un caractère spécial (@$!%*?&)\n");
        return recommendations.toString().trim();
    }
}