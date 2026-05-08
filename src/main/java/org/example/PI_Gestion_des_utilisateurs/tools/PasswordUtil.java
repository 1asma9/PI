package org.example.PI_Gestion_des_utilisateurs.tools;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.security.SecureRandom;
import java.util.regex.Pattern;

public class PasswordUtil {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }
        String normalizedHash = hashedPassword.trim();
        if (!BCRYPT_PATTERN.matcher(normalizedHash).matches()) {
            return false;
        }
        return BCrypt.verifyer()
                .verify(password.toCharArray(), normalizedHash)
                .verified;
    }

    public static String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*?&";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return password.toString();
    }
}
