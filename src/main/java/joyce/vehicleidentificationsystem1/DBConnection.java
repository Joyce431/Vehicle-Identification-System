package joyce.vehicleidentificationsystem1;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {

    // Database connection parameters
    private static final String URL = "jdbc:postgresql://localhost:5432/vehicle_identification_system";
    private static final String HOST = "localhost";
    private static final String PORT = "5432";
    private static String USER = "postgres" ;
    private static String PASSWORD = "joyce1";  // Your actual PostgreSQL password
    private static Connection connection = null;

    // Private constructor to prevent instantiation
    private DBConnection(String user, String password) {
        USER = user;
        PASSWORD = password;
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load PostgreSQL JDBC Driver
                Class.forName("org.postgresql.Driver");
                // Create connection
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected successfully!");
            } catch (ClassNotFoundException e) {
                System.err.println("PostgreSQL JDBC Driver not found!");
                e.printStackTrace();
                throw new SQLException("Database driver not found!", e);
            } catch (SQLException e) {
                System.err.println("Connection failed! Error: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    /**
     * Close database connection
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public static int executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            setParameters(pstmt, params);
            return pstmt.executeUpdate();
        }
    }

    public static int executeInsert(String sql, Object... params) throws SQLException {
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(pstmt, params);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        }
    }

    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement pstmt = getConnection().prepareStatement(sql);
        setParameters(pstmt, params);
        return pstmt.executeQuery();
    }

    public static void executeProcedure(String procedureName, Object... params) throws SQLException {
        StringBuilder sql = new StringBuilder("{call " + procedureName + "(");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")}");

        try (CallableStatement cstmt = getConnection().prepareCall(sql.toString())) {
            for (int i = 0; i < params.length; i++) {
                cstmt.setObject(i + 1, params[i]);
            }
            cstmt.execute();
        }
    }

    public static Object executeFunction(String functionName, int returnType, Object... params) throws SQLException {
        StringBuilder sql = new StringBuilder("{? = call " + functionName + "(");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")}");

        try (CallableStatement cstmt = getConnection().prepareCall(sql.toString())) {
            cstmt.registerOutParameter(1, returnType);
            for (int i = 0; i < params.length; i++) {
                cstmt.setObject(i + 2, params[i]);
            }
            cstmt.execute();
            return cstmt.getObject(1);
        }
    }

    private static void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param == null) {
                pstmt.setNull(i + 1, Types.NULL);
            } else if (param instanceof String) {
                pstmt.setString(i + 1, (String) param);
            } else if (param instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) param);
            } else if (param instanceof Double) {
                pstmt.setDouble(i + 1, (Double) param);
            } else if (param instanceof Long) {
                pstmt.setLong(i + 1, (Long) param);
            } else if (param instanceof Boolean) {
                pstmt.setBoolean(i + 1, (Boolean) param);
            } else if (param instanceof Date) {
                pstmt.setDate(i + 1, (Date) param);
            } else if (param instanceof LocalDate) {
                pstmt.setDate(i + 1, Date.valueOf((LocalDate) param));
            } else if (param instanceof LocalDateTime) {
                pstmt.setTimestamp(i + 1, Timestamp.valueOf((LocalDateTime) param));
            } else if (param instanceof Timestamp) {
                pstmt.setTimestamp(i + 1, (Timestamp) param);
            } else {
                pstmt.setObject(i + 1, param);
            }
        }
    }

    /**
     * Check if database connection is active
     * @return true if connected, false otherwise
     */
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Test database connection
     * @return true if connection successful
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Begin transaction
     * @throws SQLException if transaction fails
     */
    public static void beginTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
    }

    /**
     * Commit transaction
     * @throws SQLException if commit fails
     */
    public static void commitTransaction() throws SQLException {
        getConnection().commit();
        getConnection().setAutoCommit(true);
    }

    /**
     * Rollback transaction
     * @throws SQLException if rollback fails
     */
    public static void rollbackTransaction() throws SQLException {
        getConnection().rollback();
        getConnection().setAutoCommit(true);
    }
}