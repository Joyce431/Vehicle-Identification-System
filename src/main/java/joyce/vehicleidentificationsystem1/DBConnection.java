package joyce.vehicleidentificationsystem1;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DBConnection {

    // Database connection parameters
    private static final String URL = "jdbc:postgresql://localhost:5432/vehicle_identification_system";
    private static final String DRIVER = "org.postgresql.Driver";

    private Connection conn;
    private String user;
    private String password;
    private PreparedStatement pstmt;

    // Constructor
    public DBConnection(String user, String password) {
        this.user = user;
        this.password = password;
        openConnection();
    }

    // Open database connection
    public void openConnection() {
        try {
            Class.forName(DRIVER);
            this.conn = DriverManager.getConnection(URL, this.user, this.password);
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Get connection
    public Connection getConn() {
        return conn;
    }

    // Get prepared statement
    public PreparedStatement getPstmt() {
        return pstmt;
    }

    // Set prepared statement with SQL
    public void setPstmt(String sql) {
        try {
            this.pstmt = this.getConn().prepareStatement(sql);
        } catch (SQLException e) {
            System.out.println("Error preparing statement: " + e.getMessage());
        }
    }

    // Set prepared statement with SQL and return generated keys
    public void setPstmtWithKeys(String sql) {
        try {
            this.pstmt = this.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            System.out.println("Error preparing statement: " + e.getMessage());
        }
    }

    // Set parameters for prepared statement
    public void setParameters(Object... params) {
        try {
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
        } catch (SQLException e) {
            System.out.println("Error setting parameters: " + e.getMessage());
        }
    }

    // Execute update (INSERT, UPDATE, DELETE)
    public int executeUpdate() {
        int result = -1;
        try {
            result = this.pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error executing update: " + e.getMessage());
        }
        return result;
    }

    // Execute update with parameters
    public int executeUpdate(String sql, Object... params) {
        int result = -1;
        try {
            setPstmt(sql);
            setParameters(params);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error executing update: " + e.getMessage());
        }
        return result;
    }

    // Execute insert and return generated key
    public int executeInsert(String sql, Object... params) {
        int generatedKey = -1;
        try {
            setPstmtWithKeys(sql);
            setParameters(params);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                generatedKey = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Error executing insert: " + e.getMessage());
        }
        return generatedKey;
    }

    // Execute query (SELECT)
    public ResultSet executeQuery(String sql, Object... params) {
        ResultSet rs = null;
        try {
            setPstmt(sql);
            setParameters(params);
            rs = pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
        return rs;
    }

    // Execute stored procedure
    public void executeProcedure(String procedureName, Object... params) {
        StringBuilder sql = new StringBuilder("{call " + procedureName + "(");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")}");

        try (CallableStatement cstmt = conn.prepareCall(sql.toString())) {
            for (int i = 0; i < params.length; i++) {
                cstmt.setObject(i + 1, params[i]);
            }
            cstmt.execute();
        } catch (SQLException e) {
            System.out.println("Error executing procedure: " + e.getMessage());
        }
    }

    // Close connection and statement
    public void closeConnection() {
        try {
            if (pstmt != null) {
                pstmt.close();
            }
            if (conn != null) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    // Check if connection is active
    public boolean isConnected() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // Test connection
    public boolean testConnection() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // Begin transaction
    public void beginTransaction() throws SQLException {
        conn.setAutoCommit(false);
    }

    // Commit transaction
    public void commitTransaction() throws SQLException {
        conn.commit();
        conn.setAutoCommit(true);
    }

    // Rollback transaction
    public void rollbackTransaction() throws SQLException {
        conn.rollback();
        conn.setAutoCommit(true);
    }
}