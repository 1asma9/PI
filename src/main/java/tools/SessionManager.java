package tools;

public class SessionManager {
    private static int currentUserId = 1; // Default to 1 for testing
    private static String username = "Ahmed Elarbi";
    private static boolean isAdmin = true; // Default to true for development

    public static void login(int userId, String user, boolean admin) {
        currentUserId = userId;
        username = user;
        isAdmin = admin;
    }

    public static void logout() {
        currentUserId = -1;
        username = "";
        isAdmin = false;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getUsername() {
        return username;
    }

    public static boolean isAdmin() {
        return isAdmin;
    }

    public static boolean isLoggedIn() {
        return currentUserId != -1;
    }
}
