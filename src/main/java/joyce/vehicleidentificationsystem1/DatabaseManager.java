package joyce.vehicleidentificationsystem1;

public class DatabaseManager {
    private static DBConnection dbConnection;
    private static final String USER = "postgres";
    private static final String PASSWORD = "joyce1"; // Your PostgreSQL password

    private DatabaseManager() {
        // Private constructor to prevent instantiation
    }

    public static DBConnection getInstance() {
        if (dbConnection == null || !dbConnection.isConnected()) {
            dbConnection = new DBConnection(USER, PASSWORD);
        }
        return dbConnection;
    }

    public static void closeConnection() {
        if (dbConnection != null) {
            dbConnection.closeConnection();
            dbConnection = null;
        }
    }

    public static void reconnect() {
        closeConnection();
        dbConnection = new DBConnection(USER, PASSWORD);
    }

    public static boolean isConnected() {
        return dbConnection != null && dbConnection.isConnected();
    }
}