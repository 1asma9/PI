package tools;

public class SessionManager {
    private static int currentUserId = 1;
    private static String username = "Rayen Hafian";
    private static String email = "rayenhafian72@gmail.com";
    private static boolean isAdmin = false;

    public static void login(int userId, String user, boolean admin) {
        currentUserId = userId;
        username = user;
        isAdmin = admin;
    }

    public static void login(int userId, String user, String userEmail, boolean admin) {
        currentUserId = userId;
        username = user;
        email = userEmail;
        isAdmin = admin;
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
