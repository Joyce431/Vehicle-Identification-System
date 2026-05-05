package joyce.vehicleidentificationsystem1;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.sql.*;

public class RegisterController {

    @FXML private VBox Vbox1;
    @FXML private TextArea addressField;
    @FXML private BorderPane borderpane;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField fullNameField;
    @FXML private GridPane gridpane;
    @FXML private HBox hbox;
    @FXML private Label labelRegister;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneField;
    @FXML private Button register;
    @FXML private ComboBox<String> roleCombo;
    @FXML private ScrollPane scrollpane;
    @FXML private Label statusLabel;
    @FXML private VBox vbox2;

    private DBConnection db;

    @FXML
    public void initialize() {
        db = DatabaseManager.getInstance();

        roleCombo.getItems().addAll("Customer", "Police Officer", "Workshop Staff", "Insurance Agent");
        roleCombo.setValue("Customer");

        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> validatePasswords());
        passwordField.textProperty().addListener((obs, old, newVal) -> validatePasswords());

        FadeTransition fade = new FadeTransition(Duration.seconds(1), Vbox1);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void validatePasswords() {
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (!pass.isEmpty() && !confirm.isEmpty()) {
            if (!pass.equals(confirm)) {
                statusLabel.setText("Passwords do not match!");
                statusLabel.setStyle("-fx-text-fill: red;");
                register.setDisable(true);
            } else if (pass.length() < 6) {
                statusLabel.setText("Password must be at least 6 characters!");
                statusLabel.setStyle("-fx-text-fill: red;");
                register.setDisable(true);
            } else {
                statusLabel.setText("✓ Password valid");
                statusLabel.setStyle("-fx-text-fill: green;");
                register.setDisable(false);
            }
        } else {
            statusLabel.setText("");
            register.setDisable(false);
        }
    }

    @FXML
    void register(ActionEvent event) {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();
        String role = roleCombo.getValue();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            statusLabel.setText("Please fill all fields!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match!");
            return;
        }

        if (password.length() < 6) {
            statusLabel.setText("Password must be at least 6 characters!");
            return;
        }

        if (!email.contains("@")) {
            statusLabel.setText("Please enter a valid email address!");
            return;
        }

        String username = email.split("@")[0];

        try {
            // Check if user already exists
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
            try (ResultSet rs = db.executeQuery(checkSql, username, email)) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    statusLabel.setText("Username or email already exists!");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    return;
                }
            }

            // Insert user
            String sql = "INSERT INTO users (username, password, full_name, email, phone, address, role, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending')";
            int userId = db.executeInsert(sql, username, password, fullName, email, phone, address, role);

            // If role is Customer, create customer record
            if (role.equals("Customer") && userId > 0) {
                db.executeUpdate("INSERT INTO customers (user_id) VALUES (?)", userId);
            }

            statusLabel.setText("Registration successful! Awaiting admin approval. Redirecting...");
            statusLabel.setStyle("-fx-text-fill: green;");
            register.setStyle("-fx-background-color: #2ecc71;");

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> {
                        HelloApplication.switchScene("login.fxml", "Login - Vehicle Identification System", 900, 700);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            statusLabel.setText("Registration failed: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    void handleBackToLogin(MouseEvent event) {
        HelloApplication.switchScene("login.fxml", "Login - Vehicle Identification System", 900, 700);
    }
}