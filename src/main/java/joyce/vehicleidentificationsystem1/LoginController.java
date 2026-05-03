package joyce.vehicleidentificationsystem1;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.sql.*;

public class LoginController {

    @FXML private BorderPane borderpane;
    @FXML private VBox vbox1;
    @FXML private VBox vbox2;
    @FXML private VBox vbox3;
    @FXML private VBox vbox4;
    @FXML private VBox vbox5;
    @FXML private VBox role;
    @FXML private Label label1;
    @FXML private Label userid;
    @FXML private Label password;
    @FXML private Label label;
    @FXML private Label register;
    @FXML private Label statusLabel;
    @FXML private TextField userIdField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Button login;

    @FXML
    public void initialize() {
        if (roleCombo != null) {
            roleCombo.getItems().addAll("Admin", "Police Officer", "Customer", "Workshop Staff", "Insurance Agent");
            roleCombo.setValue("Admin");
        }

        if (vbox1 != null) {
            FadeTransition fade = new FadeTransition(Duration.seconds(1), vbox1);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }

        System.out.println("LoginController initialized!");
        showAllUsers();
    }

    private void showAllUsers() {
        try {
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT user_id, username, full_name, role, status FROM users");

            System.out.println("\n=== USERS IN DATABASE ===");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("user_id") +
                        ", Username: " + rs.getString("username") +
                        ", Name: " + rs.getString("full_name") +
                        ", Role: " + rs.getString("role") +
                        ", Status: " + rs.getString("status"));
            }
            System.out.println("========================\n");

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    @FXML
    void login(ActionEvent event) {
        String userId = userIdField.getText().trim();
        String pwd = passwordField.getText().trim();
        String selectedRole = roleCombo != null ? roleCombo.getValue() : "Admin";

        if (userId.isEmpty() || pwd.isEmpty()) {
            if (statusLabel != null) {
                statusLabel.setText("Please enter User ID/Email and Password!");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
            return;
        }

        User authenticatedUser = authenticateFromDatabase(userId, pwd, selectedRole);

        if (authenticatedUser != null) {
            if (statusLabel != null) {
                statusLabel.setText("Welcome " + authenticatedUser.getFullName() + "! Redirecting...");
                statusLabel.setStyle("-fx-text-fill: green;");
            }

            SessionManager.setCurrentUser(authenticatedUser);

            if (login != null) {
                login.setStyle("-fx-background-color: #2ecc71;");
            }

            String dashboard = getDashboardForRole(authenticatedUser.getRole());

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    Platform.runLater(() -> {
                        HelloApplication.switchScene(dashboard, authenticatedUser.getRole() + " Dashboard - Vehicle Identification System");
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            if (statusLabel != null) {
                statusLabel.setText("Invalid credentials! User not found or account is inactive.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
            passwordField.clear();
        }
    }

    private User authenticateFromDatabase(String usernameOrEmail, String password, String role) {
        String sql = "SELECT user_id, username, full_name, email, phone, address, role, status FROM users WHERE (username = ? OR email = ?) AND password = ? AND role = ? AND status = 'Active'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail);
            pstmt.setString(3, password);
            pstmt.setString(4, role);

            System.out.println("Attempting login: " + usernameOrEmail + " as " + role);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    user.setAddress(rs.getString("address"));
                    user.setRole(rs.getString("role"));
                    user.setStatus(rs.getString("status"));

                    System.out.println("✓ Login successful for: " + user.getFullName());
                    return user;
                } else {
                    System.out.println("✗ No active user found with: " + usernameOrEmail + " and role: " + role);
                }
            }
        } catch (SQLException e) {
            System.err.println("Auth error: " + e.getMessage());
        }
        return null;
    }

    // FIXED: Admin now goes to dashboard.fxml instead of admin.fxml
    private String getDashboardForRole(String role) {
        switch(role) {
            case "Admin": return "dashboard.fxml";        // CHANGED: admin.fxml → dashboard.fxml
            case "Police Officer": return "police.fxml";
            case "Customer": return "customer.fxml";
            case "Workshop Staff": return "workshop.fxml";
            case "Insurance Agent": return "insurance.fxml";
            default: return "dashboard.fxml";
        }
    }

    @FXML
    void handleRegister(MouseEvent event) {
        HelloApplication.switchScene("register.fxml", "Register - Vehicle Identification System", 800, 700);
    }
}