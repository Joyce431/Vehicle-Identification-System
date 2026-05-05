package joyce.vehicleidentificationsystem1;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.sql.*;
import java.time.LocalDate;

public class CustomerController {

    @FXML private Label addressLabel;
    @FXML private Label customerNameLabel;
    @FXML private TableView<CustomerVehicle> customerVehiclesTable;
    @FXML private Label emailLabel;
    @FXML private ProgressIndicator engineHealth;
    @FXML private Label history;
    @FXML private ProgressBar insuranceValidity;
    @FXML private VBox maintenance;
    @FXML private ProgressBar maintenanceStatus;
    @FXML private TableColumn<CustomerVehicle, String> makeCol;
    @FXML private Label memberSinceLabel;
    @FXML private TableColumn<CustomerVehicle, String> modelCol;
    @FXML private ProgressIndicator overallRating;
    @FXML private Label phoneLabel;
    @FXML private TableColumn<CustomerQuery, String> queryDateCol;
    @FXML private TableView<CustomerQuery> queryHistoryTable;
    @FXML private Pagination queryPagination;
    @FXML private TableColumn<CustomerQuery, String> queryStatusCol;
    @FXML private TextArea queryTextArea;
    @FXML private TableColumn<CustomerQuery, String> queryTextCol;
    @FXML private TableColumn<CustomerQuery, String> queryVehicleCol;
    @FXML private TableColumn<CustomerVehicle, String> regNumberCol;
    @FXML private TableColumn<CustomerQuery, String> responseTextCol;
    @FXML private Button submit;
    @FXML private Label vehicle;
    @FXML private ComboBox<String> vehicleSelect;
    @FXML private TableColumn<CustomerVehicle, String> vehicleStatusCol;
    @FXML private Label vehicles;
    @FXML private TableColumn<CustomerVehicle, Integer> yearCol;

    private ObservableList<CustomerVehicle> vehiclesList = FXCollections.observableArrayList();
    private ObservableList<CustomerQuery> queriesList = FXCollections.observableArrayList();
    private int currentCustomerId = -1;
    private User currentUser;
    private DBConnection db;

    @FXML
    public void initialize() {
        // Get database connection
        db = DatabaseManager.getInstance();

        // Setup table columns using PropertyValueFactory
        regNumberCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        makeCol.setCellValueFactory(new PropertyValueFactory<>("make"));
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        vehicleStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        queryDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        queryVehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleNumber"));
        queryTextCol.setCellValueFactory(new PropertyValueFactory<>("queryText"));
        responseTextCol.setCellValueFactory(new PropertyValueFactory<>("response"));
        queryStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Get logged in user
        currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            System.out.println("CustomerController: Loading data for user: " + currentUser.getUsername());
            loadCustomerData();
            loadVehiclesFromDatabase();
            loadQueryHistoryFromDatabase();
        } else {
            System.out.println("CustomerController: No logged in user found!");
        }

        setupVehicleCombo();
        setupQueryPagination();
        animateProgressIndicators();
        setupMaintenanceInfo();
    }

    private void loadCustomerData() {
        String sql = "SELECT u.full_name, u.email, u.phone, u.address, u.created_at FROM users u WHERE u.user_id = ?";

        try (ResultSet rs = db.executeQuery(sql, currentUser.getUserId())) {
            if (rs.next()) {
                customerNameLabel.setText(rs.getString("full_name"));
                emailLabel.setText(rs.getString("email"));
                phoneLabel.setText(rs.getString("phone"));
                addressLabel.setText(rs.getString("address"));
                if (rs.getDate("created_at") != null) {
                    memberSinceLabel.setText(rs.getDate("created_at").toLocalDate().getYear() + "");
                } else {
                    memberSinceLabel.setText("2024");
                }
                System.out.println("Customer data loaded for: " + rs.getString("full_name"));
            }
            rs.close();

            // Get customer ID
            String customerSql = "SELECT customer_id FROM customers WHERE user_id = ?";
            try (ResultSet customerRs = db.executeQuery(customerSql, currentUser.getUserId())) {
                if (customerRs.next()) {
                    currentCustomerId = customerRs.getInt("customer_id");
                    System.out.println("Customer ID: " + currentCustomerId);
                } else {
                    // Create customer record if not exists
                    String insertSql = "INSERT INTO customers (user_id, customer_type) VALUES (?, 'Regular')";
                    currentCustomerId = db.executeInsert(insertSql, currentUser.getUserId());
                    System.out.println("Created new customer record with ID: " + currentCustomerId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not load customer data: " + e.getMessage());
        }
    }

    private void loadVehiclesFromDatabase() {
        vehiclesList.clear();
        String sql = "SELECT registration_number, make, model, year, color, status FROM vehicles WHERE owner_id = ?";

        try (ResultSet rs = db.executeQuery(sql, currentCustomerId)) {
            while (rs.next()) {
                CustomerVehicle cv = new CustomerVehicle(
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("status")
                );
                vehiclesList.add(cv);
                System.out.println("Added vehicle: " + rs.getString("registration_number"));
            }
            customerVehiclesTable.setItems(vehiclesList);
            vehicles.setText(String.valueOf(vehiclesList.size()));

            if (vehiclesList.isEmpty()) {
                System.out.println("No vehicles found for customer ID: " + currentCustomerId);
                Label placeholder = new Label("No vehicles registered. Please contact admin to add vehicles.");
                placeholder.setStyle("-fx-text-fill: #e74c3c; -fx-padding: 10;");
                customerVehiclesTable.setPlaceholder(placeholder);
                vehicleSelect.setPromptText("No vehicles available");
                vehicle.setText("No vehicles");
            } else {
                setupVehicleCombo();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not load vehicles: " + e.getMessage());
        }
    }

    private void loadQueryHistoryFromDatabase() {
        queriesList.clear();
        String sql = "SELECT cq.query_id, cq.vehicle_id, cq.query_date, cq.query_text, cq.response_text, cq.status, v.registration_number " +
                "FROM customer_queries cq " +
                "JOIN vehicles v ON cq.vehicle_id = v.vehicle_id " +
                "WHERE cq.customer_id = ? ORDER BY cq.query_date DESC";

        try (ResultSet rs = db.executeQuery(sql, currentCustomerId)) {
            while (rs.next()) {
                queriesList.add(new CustomerQuery(
                        rs.getString("registration_number"),
                        rs.getDate("query_date").toString(),
                        rs.getString("query_text"),
                        rs.getString("response_text") != null ? rs.getString("response_text") : "Pending",
                        rs.getString("status")
                ));
            }
            queryHistoryTable.setItems(queriesList);
            history.setText(String.valueOf(queriesList.size()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupVehicleCombo() {
        vehicleSelect.getItems().clear();
        for (CustomerVehicle v : vehiclesList) {
            vehicleSelect.getItems().add(v.getRegistrationNumber() + " - " + v.getMake() + " " + v.getModel());
        }
        if (!vehicleSelect.getItems().isEmpty()) {
            vehicleSelect.setValue(vehicleSelect.getItems().get(0));
            String firstVehicle = vehicleSelect.getValue();
            String regNumber = firstVehicle.split(" ")[0];
            for (CustomerVehicle v : vehiclesList) {
                if (v.getRegistrationNumber().equals(regNumber)) {
                    vehicle.setText(v.getMake() + " " + v.getModel());
                    break;
                }
            }
        } else {
            vehicleSelect.setPromptText("No vehicles available");
            vehicle.setText("No vehicles");
        }
    }

    private void setupQueryPagination() {
        queryPagination.setPageCount(Math.max(1, (queriesList.size() + 1) / 2));
        queryPagination.setMaxPageIndicatorCount(5);
        queryPagination.setPageFactory(pageIndex -> {
            VBox box = new VBox(5);
            box.setStyle("-fx-padding: 10;");
            if (queriesList.isEmpty()) {
                Label label = new Label("No queries yet. Submit your first query above!");
                label.setStyle("-fx-padding: 5; -fx-text-fill: #7f8c8d;");
                box.getChildren().add(label);
            } else {
                int start = pageIndex * 2;
                for (int i = start; i < Math.min(start + 2, queriesList.size()); i++) {
                    CustomerQuery q = queriesList.get(i);
                    String displayText = q.getQueryText().length() > 50 ?
                            q.getQueryText().substring(0, 50) + "..." : q.getQueryText();
                    Label label = new Label("📝 " + q.getVehicleNumber() + ": " + displayText);
                    label.setStyle("-fx-padding: 5; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");
                    box.getChildren().add(label);
                }
            }
            return box;
        });
    }

    private void animateProgressIndicators() {
        new Thread(() -> {
            try {
                for (double i = 0; i <= 0.85; i += 0.01) {
                    final double progress = i;
                    javafx.application.Platform.runLater(() -> {
                        engineHealth.setProgress(progress);
                        maintenanceStatus.setProgress(progress);
                        insuranceValidity.setProgress(0.75);
                        overallRating.setProgress(0.92);
                    });
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupMaintenanceInfo() {
        Label maintenanceLabel = new Label("🔧 Last Service: March 15, 2024\n📅 Next Service Due: June 15, 2024\n🛞 Tire Condition: Good\n⚡ Battery Health: 85%");
        maintenanceLabel.setWrapText(true);
        maintenanceLabel.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");
        maintenance.getChildren().clear();
        maintenance.getChildren().add(maintenanceLabel);
    }

    @FXML
    void submitQuery(ActionEvent event) {
        String selectedVehicle = vehicleSelect.getValue();
        String queryText = queryTextArea.getText();

        if (selectedVehicle == null || selectedVehicle.equals("No vehicles available") || vehiclesList.isEmpty()) {
            showAlert("Warning", "No vehicles available. Please contact admin to add vehicles.");
            return;
        }

        if (queryText.isEmpty()) {
            showAlert("Warning", "Please enter your query!");
            return;
        }

        String regNumber = selectedVehicle.split(" ")[0];
        System.out.println("Submitting query for vehicle: " + regNumber);

        try {
            // Get vehicle ID
            String vehicleSql = "SELECT vehicle_id FROM vehicles WHERE registration_number = ?";
            try (ResultSet vehicleRs = db.executeQuery(vehicleSql, regNumber)) {
                if (!vehicleRs.next()) {
                    showAlert("Error", "Vehicle '" + regNumber + "' not found in database!");
                    return;
                }
                int vehicleId = vehicleRs.getInt("vehicle_id");

                // Insert query
                String sql = "INSERT INTO customer_queries (customer_id, vehicle_id, query_text, priority, status, query_date) VALUES (?, ?, ?, 'Normal', 'Pending', ?)";
                int rowsAffected = db.executeUpdate(sql, currentCustomerId, vehicleId, queryText, Date.valueOf(LocalDate.now()));

                if (rowsAffected > 0) {
                    loadQueryHistoryFromDatabase();
                    queryTextArea.clear();
                    showAlert("Success", "Your query has been submitted successfully!");
                    setupQueryPagination();

                    submit.setStyle("-fx-background-color: #2ecc71;");
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            javafx.application.Platform.runLater(() ->
                                    submit.setStyle("-fx-background-color: #3498db;"));
                        } catch (InterruptedException e) {}
                    }).start();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not submit query: " + e.getMessage());
        }
    }

    @FXML
    void vehicleSelect(ActionEvent event) {
        String selected = vehicleSelect.getValue();
        if (selected != null && !selected.equals("No vehicles available") && !vehiclesList.isEmpty()) {
            String regNumber = selected.split(" ")[0];
            for (CustomerVehicle v : vehiclesList) {
                if (v.getRegistrationNumber().equals(regNumber)) {
                    vehicle.setText(v.getMake() + " " + v.getModel());
                    break;
                }
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class CustomerVehicle {
        private String registrationNumber;
        private String make;
        private String model;
        private int year;
        private String status;

        public CustomerVehicle(String reg, String make, String model, int year, String status) {
            this.registrationNumber = reg;
            this.make = make;
            this.model = model;
            this.year = year;
            this.status = status;
        }

        public String getRegistrationNumber() { return registrationNumber; }
        public String getMake() { return make; }
        public String getModel() { return model; }
        public int getYear() { return year; }
        public String getStatus() { return status; }
    }

    public static class CustomerQuery {
        private String vehicleNumber;
        private String date;
        private String queryText;
        private String response;
        private String status;

        public CustomerQuery(String vehicle, String date, String query, String response, String status) {
            this.vehicleNumber = vehicle;
            this.date = date;
            this.queryText = query;
            this.response = response;
            this.status = status;
        }

        public String getVehicleNumber() { return vehicleNumber; }
        public String getDate() { return date; }
        public String getQueryText() { return queryText; }
        public String getResponse() { return response; }
        public String getStatus() { return status; }
    }
}