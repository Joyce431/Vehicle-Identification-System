package joyce.vehicleidentificationsystem1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.*;
import java.time.LocalDate;

public class PoliceController {

    @FXML private GridPane Gpane;
    @FXML private Button addViolation;
    @FXML private TableColumn<ViolationRecord, String> descriptionCol;
    @FXML private Label fineAmount;
    @FXML private TableColumn<ViolationRecord, Double> fineAmountCol;
    @FXML private TextField fineAmountField;
    @FXML private GridPane gridpane;
    @FXML private HBox hbox1;
    @FXML private Label history;
    @FXML private Label make;
    @FXML private Label makeLabel;
    @FXML private Label model;
    @FXML private Label modelLabel;
    @FXML private TableColumn<PoliceReport, String> officerNameCol;
    @FXML private Label owner;
    @FXML private Label ownerLabel;
    @FXML private Label police;
    @FXML private TableView<PoliceReport> policeReportTable;
    @FXML private TableColumn<PoliceReport, String> reportTypeCol;
    @FXML private Button search;
    @FXML private TextField searchField;
    @FXML private Separator separator;
    @FXML private Label status;
    @FXML private TableColumn<ViolationRecord, String> statusCol;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TitledPane titlepane1;
    @FXML private TitledPane titlepane2;
    @FXML private Label v1;
    @FXML private VBox vbox1;
    @FXML private TableColumn<ViolationRecord, LocalDate> violationDateCol;
    @FXML private TableView<ViolationRecord> violationHistoryTable;
    @FXML private TableColumn<ViolationRecord, String> violationTypeCol;
    @FXML private ComboBox<String> violationTypeCombo;
    @FXML private Label year;
    @FXML private Label yearLabel;

    private ObservableList<ViolationRecord> violations = FXCollections.observableArrayList();
    private ObservableList<PoliceReport> reports = FXCollections.observableArrayList();
    private int currentVehicleId = -1;
    private String currentVehicleReg = "";

    @FXML
    public void initialize() {
        // Setup violation table columns
        violationTypeCol.setCellValueFactory(new PropertyValueFactory<>("violationType"));
        violationDateCol.setCellValueFactory(new PropertyValueFactory<>("violationDate"));
        fineAmountCol.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Setup police report table columns
        reportTypeCol.setCellValueFactory(new PropertyValueFactory<>("reportType"));
        officerNameCol.setCellValueFactory(new PropertyValueFactory<>("officerName"));

        // Setup combo boxes
        violationTypeCombo.getItems().addAll("Speeding", "Parking Violation", "Red Light Violation",
                "Driving Without License", "No Insurance", "Drunk Driving");
        violationTypeCombo.setValue("Speeding");

        statusCombo.getItems().addAll("All", "Paid", "Unpaid", "Pending Court");
        statusCombo.setValue("All");

        // Load police reports from database
        loadPoliceReportsFromDatabase();

        // Setup search
        setupSearch();

        // Status combo listener
        statusCombo.setOnAction(e -> filterViolationsByStatus());
    }

    private void loadPoliceReportsFromDatabase() {
        reports.clear();
        String sql = "SELECT report_id, report_type, description, officer_name FROM police_reports ORDER BY report_date DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PoliceReport report = new PoliceReport(
                        rs.getInt("report_id"),
                        rs.getString("report_type"),
                        rs.getString("description"),
                        rs.getString("officer_name")
                );
                reports.add(report);
            }
            policeReportTable.setItems(reports);

        } catch (SQLException e) {
            System.err.println("Error loading police reports: " + e.getMessage());
        }
    }

    private void setupSearch() {
        search.setOnAction(e -> searchVehicle());
        searchField.setOnAction(e -> searchVehicle());
    }

    @FXML
    private void searchVehicle() {
        String regNumber = searchField.getText().toUpperCase().trim();
        if (!regNumber.isEmpty()) {
            currentVehicleReg = regNumber;
            findVehicleInDatabase(regNumber);
        }
    }

    private void findVehicleInDatabase(String regNumber) {
        String sql = "SELECT v.vehicle_id, v.make, v.model, v.year, u.full_name as owner " +
                "FROM vehicles v " +
                "LEFT JOIN customers c ON v.owner_id = c.customer_id " +
                "LEFT JOIN users u ON c.user_id = u.user_id " +
                "WHERE v.registration_number = ?";

        try (PreparedStatement pstmt = DBConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, regNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                currentVehicleId = rs.getInt("vehicle_id");
                makeLabel.setText(rs.getString("make"));
                modelLabel.setText(rs.getString("model"));
                yearLabel.setText(String.valueOf(rs.getInt("year")));
                String ownerName = rs.getString("owner");
                ownerLabel.setText(ownerName != null ? ownerName : "Unknown");

                // Load violations for this vehicle
                loadViolationsForVehicle(currentVehicleId);

                // Enable add violation button
                addViolation.setDisable(false);

            } else {
                // Vehicle not found
                currentVehicleId = -1;
                makeLabel.setText("Not Found");
                modelLabel.setText("Not Found");
                yearLabel.setText("Not Found");
                ownerLabel.setText("Not Found");
                violationHistoryTable.setItems(FXCollections.observableArrayList());
                history.setText("No violations - Vehicle not found");

                // Disable add violation button
                addViolation.setDisable(true);

                showAlert("Info", "Vehicle '" + regNumber + "' not found in database. Please register the vehicle first.");
            }
        } catch (SQLException e) {
            System.err.println("Error searching vehicle: " + e.getMessage());
            makeLabel.setText("Error");
            modelLabel.setText("Error");
            yearLabel.setText("Error");
            ownerLabel.setText("Error");
            showAlert("Database Error", "Could not search for vehicle: " + e.getMessage());
        }
    }

    private void loadViolationsForVehicle(int vehicleId) {
        ObservableList<ViolationRecord> vehicleViolations = FXCollections.observableArrayList();
        String sql = "SELECT violation_id, violation_date, violation_type, fine_amount, status, description " +
                "FROM violations WHERE vehicle_id = ? ORDER BY violation_date DESC";

        try (PreparedStatement pstmt = DBConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, vehicleId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ViolationRecord violation = new ViolationRecord(
                        rs.getInt("violation_id"),
                        currentVehicleReg,
                        rs.getDate("violation_date").toLocalDate(),
                        rs.getString("violation_type"),
                        rs.getDouble("fine_amount"),
                        rs.getString("status"),
                        rs.getString("description")
                );
                vehicleViolations.add(violation);
            }

            if (vehicleViolations.isEmpty()) {
                history.setText("No violations for this vehicle");
            } else {
                history.setText("Violations: " + vehicleViolations.size());
            }
            violationHistoryTable.setItems(vehicleViolations);

        } catch (SQLException e) {
            System.err.println("Error loading vehicle violations: " + e.getMessage());
        }
    }

    @FXML
    void addViolation(ActionEvent event) {
        if (currentVehicleId == -1) {
            showAlert("Error", "Please search for a valid vehicle first!");
            return;
        }

        String type = violationTypeCombo.getValue();
        String fineText = fineAmountField.getText();

        if (fineText.isEmpty()) {
            showAlert("Error", "Please enter fine amount!");
            return;
        }

        try {
            double fine = Double.parseDouble(fineText);
            String officerName = SessionManager.getCurrentUsername();
            if (officerName == null) {
                officerName = "Unknown Officer";
            }

            // Insert violation into database
            String sql = "INSERT INTO violations (vehicle_id, violation_date, violation_type, fine_amount, status, description, officer_name) " +
                    "VALUES (?, ?, ?, ?, 'Unpaid', ?, ?)";

            try (PreparedStatement pstmt = DBConnection.getConnection().prepareStatement(sql)) {
                pstmt.setInt(1, currentVehicleId);
                pstmt.setDate(2, Date.valueOf(LocalDate.now()));
                pstmt.setString(3, type);
                pstmt.setDouble(4, fine);
                pstmt.setString(5, "Violation recorded for vehicle " + currentVehicleReg);
                pstmt.setString(6, officerName);
                pstmt.executeUpdate();

                showAlert("Success", "Violation reported successfully!");

                // Refresh the violations list
                loadViolationsForVehicle(currentVehicleId);

                fineAmountField.clear();

                // Animate button
                addViolation.setStyle("-fx-background-color: #2ecc71;");
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        javafx.application.Platform.runLater(() ->
                                addViolation.setStyle("-fx-background-color: #f44336;"));
                    } catch (InterruptedException ex) {}
                }).start();
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid fine amount!");
        } catch (SQLException e) {
            System.err.println("Error adding violation: " + e.getMessage());
            showAlert("Database Error", "Could not add violation: " + e.getMessage());
        }
    }

    @FXML
    void searchVehicle(ActionEvent event) {
        searchVehicle();
    }

    @FXML
    void statusCombo(ActionEvent event) {
        filterViolationsByStatus();
    }

    private void filterViolationsByStatus() {
        String selectedStatus = statusCombo.getValue();
        if (currentVehicleId == -1) {
            return;
        }

        ObservableList<ViolationRecord> currentList = violationHistoryTable.getItems();
        ObservableList<ViolationRecord> filtered = FXCollections.observableArrayList();

        if ("All".equals(selectedStatus)) {
            loadViolationsForVehicle(currentVehicleId);
        } else {
            for (ViolationRecord v : violationHistoryTable.getItems()) {
                if (v.getStatus().equals(selectedStatus)) {
                    filtered.add(v);
                }
            }
            violationHistoryTable.setItems(filtered);
        }
    }

    @FXML
    void violationTypeCombo(ActionEvent event) {
        String selected = violationTypeCombo.getValue();
        fineAmountField.setPromptText("Enter fine for " + selected);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

// ViolationRecord Class
class ViolationRecord {
    private int violationId;
    private String vehicleNumber;
    private LocalDate violationDate;
    private String violationType;
    private double fineAmount;
    private String status;
    private String description;

    public ViolationRecord(int id, String vehicle, LocalDate date, String type, double fine, String status, String desc) {
        this.violationId = id;
        this.vehicleNumber = vehicle;
        this.violationDate = date;
        this.violationType = type;
        this.fineAmount = fine;
        this.status = status;
        this.description = desc;
    }

    public int getViolationId() { return violationId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public LocalDate getViolationDate() { return violationDate; }
    public String getViolationType() { return violationType; }
    public double getFineAmount() { return fineAmount; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
}

// PoliceReport Class
class PoliceReport {
    private int reportId;
    private String reportType;
    private String description;
    private String officerName;

    public PoliceReport(int id, String type, String desc, String officer) {
        this.reportId = id;
        this.reportType = type;
        this.description = desc;
        this.officerName = officer;
    }

    public int getReportId() { return reportId; }
    public String getReportType() { return reportType; }
    public String getDescription() { return description; }
    public String getOfficerName() { return officerName; }
}