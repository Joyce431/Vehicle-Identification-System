package joyce.vehicleidentificationsystem1;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class InsuranceController {

    @FXML private GridPane Gpne;
    @FXML private TitledPane add;
    @FXML private TextField companyField;
    @FXML private ProgressIndicator coverageIndicator;
    @FXML private DatePicker expiryDate;
    @FXML private HBox hbox;
    @FXML private Label inrecords;
    @FXML private Label instatus;
    @FXML private Label insurance;
    @FXML private Pagination insurancePagination;
    @FXML private ProgressBar insuranceProgress;
    @FXML private TableView<InsurancePolicyData> insuranceTable;
    @FXML private Label overall;
    @FXML private TextField policyField;
    @FXML private Separator sep;
    @FXML private TableColumn<InsurancePolicyData, String> tcol1;
    @FXML private TableColumn<InsurancePolicyData, String> tcol2;
    @FXML private TableColumn<InsurancePolicyData, String> tcol3;
    @FXML private TableColumn<InsurancePolicyData, String> tcol4;
    @FXML private Label validation;
    @FXML private VBox vbox;
    @FXML private VBox vbox2;
    @FXML private ComboBox<String> vehicleCombo;

    private ObservableList<InsurancePolicyData> policies = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup table columns using lambda expressions (more reliable)
        if (tcol1 != null) {
            tcol1.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPolicyNumber()));
            tcol2.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getVehicleNumber()));
            tcol3.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCompany()));
            tcol4.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getExpiryDateString()));
        }

        // Load vehicles from database
        loadVehiclesFromDatabase();

        // Load insurance policies from database
        loadInsurancePoliciesFromDatabase();

        // Setup pagination
        setupPagination();

        // Animate progress
        animateProgress();

        // Update statistics
        updateStatistics();

        System.out.println("InsuranceController initialized successfully!");
    }

    private void loadVehiclesFromDatabase() {
        vehicleCombo.getItems().clear();
        String sql = "SELECT registration_number, make, model FROM vehicles WHERE status = 'Active'";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String vehicleInfo = rs.getString("registration_number") + " - " +
                        rs.getString("make") + " " +
                        rs.getString("model");
                vehicleCombo.getItems().add(vehicleInfo);
            }

            if (!vehicleCombo.getItems().isEmpty()) {
                vehicleCombo.setValue(vehicleCombo.getItems().get(0));
            }

            System.out.println("Loaded " + vehicleCombo.getItems().size() + " vehicles");

        } catch (SQLException e) {
            System.err.println("Error loading vehicles: " + e.getMessage());
            // Fallback sample data
            vehicleCombo.getItems().addAll("ABC123 - Toyota Camry", "XYZ789 - Honda Civic", "DEF456 - Ford Mustang");
            vehicleCombo.setValue("ABC123 - Toyota Camry");
        }
    }

    private void loadInsurancePoliciesFromDatabase() {
        policies.clear();
        String sql = "SELECT ip.policy_number, v.registration_number, ip.provider, ip.end_date " +
                "FROM insurance_policies ip " +
                "JOIN vehicles v ON ip.vehicle_id = v.vehicle_id " +
                "ORDER BY ip.policy_id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                InsurancePolicyData policy = new InsurancePolicyData(
                        rs.getString("policy_number"),
                        rs.getString("registration_number"),
                        rs.getString("provider"),
                        rs.getDate("end_date").toLocalDate()
                );
                policies.add(policy);
            }

            insuranceTable.setItems(policies);
            if (inrecords != null) {
                inrecords.setText(String.valueOf(policies.size()));
            }

            System.out.println("Loaded " + policies.size() + " insurance policies");

        } catch (SQLException e) {
            System.err.println("Error loading insurance policies: " + e.getMessage());
            // Add sample data if no policies exist
            addSamplePolicies();
        }
    }

    private void addSamplePolicies() {
        policies.add(new InsurancePolicyData("POL-001", "ABC123", "Allianz", LocalDate.of(2025, 1, 15)));
        policies.add(new InsurancePolicyData("POL-002", "XYZ789", "Zurich", LocalDate.of(2025, 3, 20)));
        policies.add(new InsurancePolicyData("POL-003", "DEF456", "AXA", LocalDate.of(2024, 12, 10)));
        insuranceTable.setItems(policies);
        if (inrecords != null) {
            inrecords.setText(String.valueOf(policies.size()));
        }
    }

    private void setupPagination() {
        if (insurancePagination != null) {
            insurancePagination.setPageCount(Math.max(1, (policies.size() + 2) / 3));
            insurancePagination.setMaxPageIndicatorCount(5);
            insurancePagination.setPageFactory(pageIndex -> {
                VBox box = new VBox(5);
                box.setStyle("-fx-padding: 10;");
                int start = pageIndex * 3;
                for (int i = start; i < Math.min(start + 3, policies.size()); i++) {
                    InsurancePolicyData p = policies.get(i);
                    Label label = new Label("📄 " + p.getPolicyNumber() + " - " + p.getVehicleNumber() + " - " + p.getCompany());
                    label.setStyle("-fx-padding: 5; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");
                    box.getChildren().add(label);
                }
                if (box.getChildren().isEmpty()) {
                    box.getChildren().add(new Label("No insurance policies found"));
                }
                return box;
            });
        }
    }

    private void animateProgress() {
        new Thread(() -> {
            try {
                for (double i = 0; i <= 0.75; i += 0.01) {
                    final double progress = i;
                    javafx.application.Platform.runLater(() -> {
                        if (insuranceProgress != null) {
                            insuranceProgress.setProgress(progress);
                        }
                        if (coverageIndicator != null) {
                            coverageIndicator.setProgress(progress);
                        }
                    });
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateStatistics() {
        long activePolicies = policies.stream()
                .filter(p -> p.getExpiryDate().isAfter(LocalDate.now()))
                .count();

        if (instatus != null) {
            instatus.setText(String.valueOf(activePolicies));
        }

        long expiringSoon = policies.stream()
                .filter(p -> ChronoUnit.DAYS.between(LocalDate.now(), p.getExpiryDate()) <= 30 &&
                        p.getExpiryDate().isAfter(LocalDate.now()))
                .count();

        if (insurance != null) {
            insurance.setText(String.valueOf(expiringSoon));
        }

        if (overall != null) {
            if (policies.size() > 0) {
                double percentage = (double) activePolicies / policies.size() * 100;
                overall.setText(String.format("%.0f%%", percentage));
            } else {
                overall.setText("0%");
            }
        }
    }

    @FXML
    void addInsurance(ActionEvent event) {
        String selectedVehicle = vehicleCombo.getValue();
        String company = companyField.getText().trim();
        String policyNumber = policyField.getText().trim();
        LocalDate expiry = expiryDate.getValue();

        if (selectedVehicle == null || company.isEmpty() || policyNumber.isEmpty() || expiry == null) {
            if (validation != null) {
                validation.setText("Please fill all fields!");
                validation.setStyle("-fx-text-fill: red;");
            }
            return;
        }

        String regNumber = selectedVehicle.split(" ")[0];

        try {
            // Get vehicle ID
            String vehicleSql = "SELECT vehicle_id FROM vehicles WHERE registration_number = ?";
            PreparedStatement vehicleStmt = DBConnection.getConnection().prepareStatement(vehicleSql);
            vehicleStmt.setString(1, regNumber);
            ResultSet vehicleRs = vehicleStmt.executeQuery();
            int vehicleId = vehicleRs.next() ? vehicleRs.getInt("vehicle_id") : -1;

            if (vehicleId == -1) {
                if (validation != null) {
                    validation.setText("Vehicle not found in database!");
                    validation.setStyle("-fx-text-fill: red;");
                }
                return;
            }

            // Insert into database
            String sql = "INSERT INTO insurance_policies (policy_number, vehicle_id, provider, policy_type, start_date, end_date, premium_amount, status) " +
                    "VALUES (?, ?, ?, 'Comprehensive', ?, ?, 0, 'Active')";
            PreparedStatement pstmt = DBConnection.getConnection().prepareStatement(sql);
            pstmt.setString(1, policyNumber);
            pstmt.setInt(2, vehicleId);
            pstmt.setString(3, company);
            pstmt.setDate(4, Date.valueOf(LocalDate.now()));
            pstmt.setDate(5, Date.valueOf(expiry));
            pstmt.executeUpdate();

            // Add to local list
            InsurancePolicyData newPolicy = new InsurancePolicyData(policyNumber, regNumber, company, expiry);
            policies.add(0, newPolicy);
            insuranceTable.refresh();

            // Clear fields
            companyField.clear();
            policyField.clear();
            expiryDate.setValue(null);

            if (validation != null) {
                validation.setText("Insurance policy added successfully!");
                validation.setStyle("-fx-text-fill: green;");
            }

            updateStatistics();
            if (inrecords != null) {
                inrecords.setText(String.valueOf(policies.size()));
            }
            setupPagination(); // Refresh pagination

            // Animate effect
            FadeTransition fade = new FadeTransition(Duration.millis(500), validation);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setDelay(Duration.seconds(2));
            fade.play();

        } catch (SQLException e) {
            System.err.println("Error adding insurance: " + e.getMessage());
            if (validation != null) {
                validation.setText("Database error: " + e.getMessage());
                validation.setStyle("-fx-text-fill: red;");
            }
        }
    }
}

// InsurancePolicyData Class
class InsurancePolicyData {
    private String policyNumber;
    private String vehicleNumber;
    private String company;
    private LocalDate expiryDate;

    public InsurancePolicyData(String policy, String vehicle, String company, LocalDate expiry) {
        this.policyNumber = policy;
        this.vehicleNumber = vehicle;
        this.company = company;
        this.expiryDate = expiry;
    }

    public String getPolicyNumber() { return policyNumber; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getCompany() { return company; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getExpiryDateString() {
        return expiryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}