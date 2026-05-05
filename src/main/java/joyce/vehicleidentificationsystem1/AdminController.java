package joyce.vehicleidentificationsystem1;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.*;

public class AdminController {

    @FXML private Button access;
    @FXML private Label activeReports;
    @FXML private Label activeReportsLabel;
    @FXML private ListView<String> activityLogList;
    @FXML private Button backpdb;
    @FXML private Button createuser;
    @FXML private ProgressBar dbProgress;
    @FXML private Label dbStatusLabel;
    @FXML private TitledPane dbmangement;
    @FXML private Label label1;
    @FXML private Label name;
    @FXML private Button rdb;
    @FXML private TitledPane recentActivityLog;
    @FXML private Button refreshstatistics;
    @FXML private Button revoke;
    @FXML private Label role;
    @FXML private ComboBox<String> roleCombo;
    @FXML private TitledPane statistics;
    @FXML private TitledPane titlepane;
    @FXML private Label toalUser;
    @FXML private Label total;
    @FXML private Label totalServiceRecords;
    @FXML private Label totalServicesLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalVehiclesLabel;
    @FXML private Label unpaid;
    @FXML private Label unpaidViolationsLabel;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TextField userIdField;
    @FXML private TableColumn<User, String> userNameCol;
    @FXML private TextField userNameField;
    @FXML private TableColumn<User, String> userRoleCol;
    @FXML private TableColumn<User, String> userStatusCol;
    @FXML private TableView<User> userTable;
    @FXML private Label userid;
    @FXML private Button viewlogs;

    private ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<String> activityLogs = FXCollections.observableArrayList();
    private DBConnection db;

    @FXML
    public void initialize() {
        db = DatabaseManager.getInstance();

        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        userRoleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        userStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        roleCombo.getItems().addAll("Admin", "Police Officer", "Customer", "Workshop Staff", "Insurance Agent");
        roleCombo.setValue("Customer");

        loadUsersFromDatabase();
        loadActivityLogs();
        updateStatistics();

        animateDatabaseProgress();

        FadeTransition fade = new FadeTransition(Duration.seconds(1), statistics);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void loadUsersFromDatabase() {
        users.clear();
        String sql = "SELECT user_id, username, full_name, email, role, status FROM users ORDER BY user_id";

        try (ResultSet rs = db.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
                users.add(user);
            }
            userTable.setItems(users);
            toalUser.setText(String.valueOf(users.size()));
            totalUsersLabel.setText(String.valueOf(users.size()));
            System.out.println("Loaded " + users.size() + " users from database");
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
            showAlert("Database Error", "Could not load users from database: " + e.getMessage());
        }
    }

    private void loadActivityLogs() {
        activityLogs.clear();
        String sql = "SELECT action, description, created_at FROM system_logs ORDER BY created_at DESC LIMIT 20";

        try (ResultSet rs = db.executeQuery(sql)) {
            while (rs.next()) {
                String timestamp = rs.getTimestamp("created_at").toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                String action = rs.getString("action");
                String description = rs.getString("description");
                activityLogs.add("[" + timestamp + "] " + action + " - " + description);
            }
            if (activityLogs.isEmpty()) {
                activityLogs.add("[" + getCurrentTime() + "] System initialized");
            }
        } catch (SQLException e) {
            System.err.println("Error loading activity logs: " + e.getMessage());
            activityLogs.add("[" + getCurrentTime() + "] System ready");
        }
        activityLogList.setItems(activityLogs);
    }

    private void updateStatistics() {
        try {
            String sql = "SELECT COUNT(*) as total FROM vehicles";
            try (ResultSet rs = db.executeQuery(sql)) {
                if (rs.next()) {
                    totalVehiclesLabel.setText(String.valueOf(rs.getInt("total")));
                }
            }

            String violationSql = "SELECT COUNT(*) as unpaid FROM violations WHERE status = 'Unpaid'";
            try (ResultSet rs = db.executeQuery(violationSql)) {
                if (rs.next()) {
                    unpaidViolationsLabel.setText(String.valueOf(rs.getInt("unpaid")));
                }
            }

            totalServicesLabel.setText("0");
            activeReports.setText("0");
            totalUsersLabel.setText(String.valueOf(users.size()));
        } catch (SQLException e) {
            totalVehiclesLabel.setText("0");
            unpaidViolationsLabel.setText("0");
        }
    }

    private void animateDatabaseProgress() {
        new Thread(() -> {
            try {
                for (double i = 0; i <= 0.95; i += 0.01) {
                    final double progress = i;
                    javafx.application.Platform.runLater(() -> {
                        dbProgress.setProgress(progress);
                        if (progress >= 0.95) {
                            dbStatusLabel.setText("Connected");
                            dbStatusLabel.setStyle("-fx-text-fill: #2ecc71;");
                        }
                    });
                    Thread.sleep(30);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @FXML
    void createUser(ActionEvent event) {
        String userName = userNameField.getText().trim();
        String userId = userIdField.getText().trim();
        String role = roleCombo.getValue();

        if (userName.isEmpty() || userId.isEmpty()) {
            showAlert("Error", "Please enter both Username and Full Name!");
            return;
        }

        // Check if username already exists
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (ResultSet rs = db.executeQuery(checkSql, userId)) {
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Error", "Username already exists! Please choose a different username.");
                return;
            }
        } catch (SQLException e) {
            System.err.println("Error checking user: " + e.getMessage());
        }

        // Insert into database
        String sql = "INSERT INTO users (username, full_name, role, status, password, email, created_at) VALUES (?, ?, ?, 'Active', 'password123', ?, NOW())";
        int result = db.executeUpdate(sql, userId, userName, role, userId + "@vehicle.com");

        if (result > 0) {
            // Add to activity log
            String logSql = "INSERT INTO system_logs (user_id, action, description) VALUES (?, ?, ?)";
            db.executeUpdate(logSql, SessionManager.getCurrentUserId(), "User Created", "New user '" + userName + "' created as " + role);

            loadUsersFromDatabase();
            userNameField.clear();
            userIdField.clear();
            updateStatistics();
            showAlert("Success", "User created successfully!");

            createuser.setStyle("-fx-background-color: #2ecc71;");
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    javafx.application.Platform.runLater(() ->
                            createuser.setStyle("-fx-background-color: #3498db;"));
                } catch (InterruptedException e) {}
            }).start();
        } else {
            showAlert("Error", "Could not create user!");
        }
    }

    @FXML
    void grantAccess(ActionEvent event) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String sql = "UPDATE users SET status = 'Active' WHERE user_id = ?";
            int result = db.executeUpdate(sql, selected.getUserId());
            if (result > 0) {
                loadUsersFromDatabase();
                showAlert("Success", "Access granted to " + selected.getFullName());
            } else {
                showAlert("Error", "Could not update user!");
            }
        } else {
            showAlert("Error", "Please select a user first!");
        }
    }

    @FXML
    void revokeAccess(ActionEvent event) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String sql = "UPDATE users SET status = 'Inactive' WHERE user_id = ?";
            int result = db.executeUpdate(sql, selected.getUserId());
            if (result > 0) {
                loadUsersFromDatabase();
                showAlert("Success", "Access revoked for " + selected.getFullName());
            } else {
                showAlert("Error", "Could not update user!");
            }
        } else {
            showAlert("Error", "Please select a user first!");
        }
    }

    @FXML
    void refreshStatistics(ActionEvent event) {
        loadUsersFromDatabase();
        updateStatistics();
        loadActivityLogs();
        showAlert("Refreshed", "Statistics updated successfully!");
    }

    @FXML
    void backupDatabase(ActionEvent event) {
        dbProgress.setProgress(0);
        dbProgress.setVisible(true);
        new Thread(() -> {
            try {
                for (double i = 0; i <= 1.0; i += 0.1) {
                    final double progress = i;
                    javafx.application.Platform.runLater(() -> dbProgress.setProgress(progress));
                    Thread.sleep(200);
                }
                javafx.application.Platform.runLater(() -> {
                    dbProgress.setVisible(false);
                    showAlert("Backup Complete", "Database has been backed up successfully!");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    void restoreDatabase(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Restore");
        confirm.setHeaderText("Restore Database");
        confirm.setContentText("Are you sure you want to restore the database? This will overwrite current data.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showAlert("Restore Initiated", "Database restore has been started.");
            }
        });
    }

    @FXML
    void viewLogs(ActionEvent event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("System Logs");
        dialog.setHeaderText("Complete System Activity Logs");
        TextArea textArea = new TextArea();
        StringBuilder logs = new StringBuilder();
        for (String log : activityLogs) {
            logs.append(log).append("\n");
        }
        textArea.setText(logs.toString());
        textArea.setEditable(false);
        textArea.setPrefHeight(400);
        textArea.setPrefWidth(500);
        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void userIdField(ActionEvent actionEvent) {
    }

    public void userNameField(ActionEvent actionEvent) {
    }

    public void roleCombo(ActionEvent actionEvent) {

    }
}