package tools;

public class SessionManager {
    private static int currentUserId = -1;
    private static String username = "";
    private static String email = "";
    private static boolean isAdmin = false;
    private static org.example.PI_Gestion_des_utilisateurs.entities.utilisateur tempUser;

    public static void setTempUser(org.example.PI_Gestion_des_utilisateurs.entities.utilisateur u) { tempUser = u; }
    public static org.example.PI_Gestion_des_utilisateurs.entities.utilisateur getTempUser() { return tempUser; }

    public static void login(int userId, String user, boolean admin) {
        currentUserId = userId;
        username = user;
        isAdmin = admin;
        System.out.println("✅ Session sauvegardée : userId=" + userId + ", user=" + user);
    }

    public static void login(int userId, String user, String userEmail, boolean admin) {
        currentUserId = userId;
        username = user;
        email = userEmail;
        isAdmin = admin;
        System.out.println("✅ Session sauvegardée : userId=" + userId + ", user=" + user);
    }

    public static void logout() {
        currentUserId = -1;
        username = "";
        email = "";
        isAdmin = false;
    }

    public static int getCurrentUserId() { return currentUserId; }
    public static String getUsername() { return username; }
    public static String getEmail() { return email; }
    public static boolean isAdmin() { return isAdmin; }
    public static boolean isLoggedIn() { return currentUserId != -1; }
}
