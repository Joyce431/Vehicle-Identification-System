package joyce.vehicleidentificationsystem1;

public class SessionManager {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
        if (user != null) {
            System.out.println("========================================");
            System.out.println("SESSION: User logged in");
            System.out.println("  User ID: " + user.getUserId());
            System.out.println("  Username: " + user.getUsername());
            System.out.println("  Name: " + user.getFullName());
            System.out.println("  Role: " + user.getRole());
            System.out.println("========================================");
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public static String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }

    public static String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : null;
    }

    public static void logout() {
        if (currentUser != null) {
            System.out.println("SESSION: User logged out: " + currentUser.getUsername());
        }
        currentUser = null;
    }

    public static boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().equals(role);
    }
}