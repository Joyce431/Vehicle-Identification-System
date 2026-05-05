package joyce.vehicleidentificationsystem1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.*;
import java.time.LocalDate;

public class WorkshopController {

    @FXML private TableColumn<ServiceRecordWorkshop, Double> costCol;
    @FXML private TextField costField;
    @FXML private GridPane gritpane;
    @FXML private Label history;
    @FXML private Label label1;
    @FXML private Label label2;
    @FXML private Label label3;
    @FXML private Label label4;
    @FXML private Label label5;
    @FXML private TextField makeField;
    @FXML private TextField modelField;
    @FXML private TextField ownerNameField;
    @FXML private TextField regNumberField;
    @FXML private Button register;
    @FXML private Label registered;
    @FXML private ScrollPane scrollpane;
    @FXML private Separator separator;
    @FXML private TableColumn<ServiceRecordWorkshop, LocalDate> serviceDateCol;
    @FXML private DatePicker serviceDatePicker;
    @FXML private TableColumn<ServiceRecordWorkshop, String> serviceDescCol;
    @FXML private TextArea serviceDescription;
    @FXML private TableView<ServiceRecordWorkshop> serviceHistoryTable;
    @FXML private TableColumn<ServiceRecordWorkshop, String> serviceTypeCol;
    @FXML private ComboBox<String> serviceTypeCombo;
    @FXML private Label text1;
    @FXML private TitledPane titlepane;
    @FXML private TitledPane titlepane2;
    @FXML private Label txt2;
    @FXML private Label txt3;
    @FXML private Label txt4;
    @FXML private Label txt5;
    @FXML private VBox vbox;
    @FXML private VBox vehicleListContainer;
    @FXML private ComboBox<String> vehicleSelect;
    @FXML private Label workshop;
    @FXML private TextField yearField;

    private ObservableList<ServiceRecordWorkshop> serviceRecords = FXCollections.observableArrayList();
    private ObservableList<VehicleWorkshop> registeredVehicles = FXCollections.observableArrayList();
    private DBConnection db;

    @FXML
    public void initialize() {
        db = DatabaseManager.getInstance();

        // Use lambda expressions instead of PropertyValueFactory to avoid reflection issues
        serviceTypeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getServiceType()));
        serviceDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDate()));
        serviceDescCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
        costCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getCost()).asObject());

        serviceTypeCombo.getItems().addAll("Oil Change", "Tire Rotation", "Brake Service",
                "Engine Tune-up", "Transmission Service", "Battery Replacement", "AC Service");
        serviceTypeCombo.setValue("Oil Change");

        setupVehicleCombo();
        loadServiceRecords();
        loadRegisteredVehicles();
        serviceDatePicker.setValue(LocalDate.now());
    }

    private void setupVehicleCombo() {
        vehicleSelect.getItems().clear();

        // Load vehicles from database
        String sql = "SELECT registration_number, make, model FROM vehicles WHERE status = 'Active'";
        try (ResultSet rs = db.executeQuery(sql)) {
            while (rs.next()) {
                String vehicleInfo = rs.getString("registration_number") + " - " +
                        rs.getString("make") + " " +
                        rs.getString("model");
                vehicleSelect.getItems().add(vehicleInfo);
            }
        } catch (SQLException e) {
            System.err.println("Error loading vehicles: " + e.getMessage());
            // Fallback sample data
            vehicleSelect.getItems().addAll("ABC123 - Toyota Camry", "XYZ789 - Honda Civic", "DEF456 - Ford Mustang");
        }

        if (!vehicleSelect.getItems().isEmpty()) {
            vehicleSelect.setValue(vehicleSelect.getItems().get(0));
            String selected = vehicleSelect.getValue();
            if (selected != null) {
                String regNumber = selected.split(" ")[0];
                loadVehicleDetails(regNumber);
            }
        }

        vehicleSelect.setOnAction(e -> {
            String selected = vehicleSelect.getValue();
            if (selected != null) {
                String regNumber = selected.split(" ")[0];
                loadVehicleDetails(regNumber);
            }
        });
    }

    private void loadVehicleDetails(String regNumber) {
        String sql = "SELECT v.make, v.model, v.year, u.full_name as owner FROM vehicles v " +
                "LEFT JOIN customers c ON v.owner_id = c.customer_id " +
                "LEFT JOIN users u ON c.user_id = u.user_id " +
                "WHERE v.registration_number = ?";

        try (ResultSet rs = db.executeQuery(sql, regNumber)) {
            if (rs.next()) {
                makeField.setText(rs.getString("make"));
                modelField.setText(rs.getString("model"));
                yearField.setText(String.valueOf(rs.getInt("year")));
                ownerNameField.setText(rs.getString("owner") != null ? rs.getString("owner") : "Unknown");
            }
        } catch (SQLException e) {
            System.err.println("Error loading vehicle details: " + e.getMessage());
            // Fallback to sample data
            if (regNumber.equals("ABC123")) {
                makeField.setText("Toyota");
                modelField.setText("Camry");
                yearField.setText("2020");
                ownerNameField.setText("John Doe");
            } else if (regNumber.equals("XYZ789")) {
                makeField.setText("Honda");
                modelField.setText("Civic");
                yearField.setText("2019");
                ownerNameField.setText("John Doe");
            } else if (regNumber.equals("DEF456")) {
                makeField.setText("Ford");
                modelField.setText("Mustang");
                yearField.setText("2021");
                ownerNameField.setText("Jane Smith");
            }
        }
    }

    private void loadServiceRecords() {
        serviceRecords.clear();
        String sql = "SELECT sr.service_type, sr.service_date, sr.description, sr.cost " +
                "FROM service_records sr " +
                "JOIN vehicles v ON sr.vehicle_id = v.vehicle_id " +
                "ORDER BY sr.service_date DESC";

        try (ResultSet rs = db.executeQuery(sql)) {
            while (rs.next()) {
                ServiceRecordWorkshop record = new ServiceRecordWorkshop(
                        rs.getString("service_type"),
                        rs.getDate("service_date").toLocalDate(),
                        rs.getString("description"),
                        rs.getDouble("cost")
                );
                serviceRecords.add(record);
            }
            serviceHistoryTable.setItems(serviceRecords);
            history.setText("Total: " + serviceRecords.size() + " records");
        } catch (SQLException e) {
            System.err.println("Error loading service records: " + e.getMessage());
            // Fallback sample data
            serviceRecords.add(new ServiceRecordWorkshop("Oil Change", LocalDate.of(2024, 3, 15),
                    "Regular oil change and filter replacement", 89.99));
            serviceRecords.add(new ServiceRecordWorkshop("Tire Rotation", LocalDate.of(2024, 3, 10),
                    "Rotated all four tires", 49.99));
            serviceRecords.add(new ServiceRecordWorkshop("Brake Service", LocalDate.of(2024, 3, 5),
                    "Front brake pads replacement", 299.99));
            serviceRecords.add(new ServiceRecordWorkshop("Engine Tune-up", LocalDate.of(2024, 2, 28),
                    "Complete engine diagnostic and tune-up", 450.00));
            serviceHistoryTable.setItems(serviceRecords);
            history.setText("Total: " + serviceRecords.size() + " records");
        }
    }

    private void loadRegisteredVehicles() {
        registeredVehicles.clear();
        vehicleListContainer.getChildren().clear();

        String sql = "SELECT v.registration_number, v.make, v.model, v.year, u.full_name as owner " +
                "FROM vehicles v " +
                "LEFT JOIN customers c ON v.owner_id = c.customer_id " +
                "LEFT JOIN users u ON c.user_id = u.user_id " +
                "WHERE v.status = 'Active' " +
                "ORDER BY v.registration_number";

        try (ResultSet rs = db.executeQuery(sql)) {
            while (rs.next()) {
                VehicleWorkshop vehicle = new VehicleWorkshop(
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("owner") != null ? rs.getString("owner") : "Unknown"
                );
                registeredVehicles.add(vehicle);

                // Add to display as HBox for better layout
                HBox itemBox = new HBox(10);
                itemBox.setAlignment(Pos.CENTER_LEFT);
                itemBox.setStyle("-fx-padding: 8; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-margin: 2;");

                Label vehicleLabel = new Label("🚗 " + vehicle.getRegistrationNumber() + " - " +
                        vehicle.getMake() + " " + vehicle.getModel() + " (" + vehicle.getYear() + ")");
                vehicleLabel.setStyle("-fx-font-size: 13px;");

                Label ownerLabel = new Label("Owner: " + vehicle.getOwnerName());
                ownerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

                VBox textBox = new VBox(2);
                textBox.getChildren().addAll(vehicleLabel, ownerLabel);

                itemBox.getChildren().add(textBox);
                vehicleListContainer.getChildren().add(itemBox);
            }
            registered.setText("Total: " + registeredVehicles.size() + " vehicles registered");
        } catch (SQLException e) {
            System.err.println("Error loading registered vehicles: " + e.getMessage());
            // Fallback sample data
            String[][] sampleVehicles = {
                    {"ABC123", "Toyota", "Camry", "2020", "John Doe"},
                    {"XYZ789", "Honda", "Civic", "2019", "John Doe"},
                    {"DEF456", "Ford", "Mustang", "2021", "Jane Smith"},
                    {"GHI789", "BMW", "X5", "2022", "Bob Johnson"},
                    {"JKL012", "Mercedes", "C200", "2021", "Alice Brown"}
            };

            for (String[] v : sampleVehicles) {
                VehicleWorkshop vehicle = new VehicleWorkshop(v[0], v[1], v[2], Integer.parseInt(v[3]), v[4]);
                registeredVehicles.add(vehicle);

                HBox itemBox = new HBox(10);
                itemBox.setAlignment(Pos.CENTER_LEFT);
                itemBox.setStyle("-fx-padding: 8; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");

                Label vehicleLabel = new Label("🚗 " + vehicle.getRegistrationNumber() + " - " +
                        vehicle.getMake() + " " + vehicle.getModel() + " (" + vehicle.getYear() + ")");
                vehicleLabel.setStyle("-fx-font-size: 13px;");

                Label ownerLabel = new Label("Owner: " + vehicle.getOwnerName());
                ownerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

                VBox textBox = new VBox(2);
                textBox.getChildren().addAll(vehicleLabel, ownerLabel);
                itemBox.getChildren().add(textBox);
                vehicleListContainer.getChildren().add(itemBox);
            }
            registered.setText("Total: " + registeredVehicles.size() + " vehicles registered");
        }
    }

    @FXML
    void addServiceRecord(ActionEvent event) {
        String selectedVehicle = vehicleSelect.getValue();
        String serviceType = serviceTypeCombo.getValue();
        String costText = costField.getText();
        String description = serviceDescription.getText();
        LocalDate serviceDate = serviceDatePicker.getValue();

        if (selectedVehicle == null) {
            showAlert("Error", "Please select a vehicle!");
            return;
        }

        if (costText.isEmpty()) {
            showAlert("Error", "Please enter service cost!");
            return;
        }

        try {
            double cost = Double.parseDouble(costText);
            String regNumber = selectedVehicle.split(" ")[0];
            String technician = SessionManager.getCurrentUsername() != null ?
                    SessionManager.getCurrentUsername() : "Unknown Technician";

            // Insert into database
            String sql = "INSERT INTO service_records (vehicle_id, service_date, service_type, description, cost, technician_name, status) " +
                    "SELECT vehicle_id, ?, ?, ?, ?, ?, 'Completed' FROM vehicles WHERE registration_number = ?";
            int result = db.executeUpdate(sql, Date.valueOf(serviceDate), serviceType, description, cost, technician, regNumber);

            if (result > 0) {
                ServiceRecordWorkshop newRecord = new ServiceRecordWorkshop(serviceType, serviceDate, description, cost);
                serviceRecords.add(0, newRecord);
                serviceHistoryTable.refresh();
                serviceHistoryTable.scrollTo(0);

                history.setText("Total: " + serviceRecords.size() + " records");

                costField.clear();
                serviceDescription.clear();
                serviceDatePicker.setValue(LocalDate.now());

                showAlert("Success", "Service record added successfully!");

                // Animate button
                Button source = (Button) event.getSource();
                source.setStyle("-fx-background-color: #2ecc71;");
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        javafx.application.Platform.runLater(() ->
                                source.setStyle("-fx-background-color: #2196F3;"));
                    } catch (InterruptedException ex) {}
                }).start();
            } else {
                showAlert("Error", "Failed to add service record!");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid cost amount!");
        }
    }

    @FXML
    void registerVehicle(ActionEvent event) {
        String regNumber = regNumberField.getText().toUpperCase();
        String make = makeField.getText();
        String model = modelField.getText();
        String yearText = yearField.getText();
        String ownerName = ownerNameField.getText();

        if (regNumber.isEmpty() || make.isEmpty() || model.isEmpty() || yearText.isEmpty()) {
            showAlert("Error", "Please fill all vehicle details!");
            return;
        }

        try {
            int year = Integer.parseInt(yearText);

            // Check if vehicle already exists
            String checkSql = "SELECT COUNT(*) FROM vehicles WHERE registration_number = ?";
            try (ResultSet rs = db.executeQuery(checkSql, regNumber)) {
                if (rs.next() && rs.getInt(1) > 0) {
                    showAlert("Error", "Vehicle with registration number " + regNumber + " already exists!");
                    return;
                }
            }

            // Insert into database
            String sql = "INSERT INTO vehicles (registration_number, make, model, year, owner_id, status) " +
                    "SELECT ?, ?, ?, ?, customer_id, 'Active' FROM customers WHERE user_id = ?";
            int result = db.executeUpdate(sql, regNumber, make, model, year, SessionManager.getCurrentUserId());

            if (result > 0) {
                VehicleWorkshop newVehicle = new VehicleWorkshop(regNumber, make, model, year, ownerName);
                registeredVehicles.add(0, newVehicle);

                // Add to display
                HBox itemBox = new HBox(10);
                itemBox.setAlignment(Pos.CENTER_LEFT);
                itemBox.setStyle("-fx-padding: 8; -fx-background-color: #e8f4f8; -fx-background-radius: 5;");

                Label vehicleLabel = new Label("🚗 " + regNumber + " - " + make + " " + model + " (" + year + ")");
                vehicleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

                Label ownerLabel = new Label("Owner: " + ownerName);
                ownerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

                VBox textBox = new VBox(2);
                textBox.getChildren().addAll(vehicleLabel, ownerLabel);
                itemBox.getChildren().add(textBox);

                vehicleListContainer.getChildren().add(0, itemBox);

                vehicleSelect.getItems().add(regNumber + " - " + make + " " + model);
                if (vehicleSelect.getItems().size() == 1) {
                    vehicleSelect.setValue(regNumber + " - " + make + " " + model);
                }

                regNumberField.clear();
                makeField.clear();
                modelField.clear();
                yearField.clear();
                ownerNameField.clear();

                registered.setText("Total: " + registeredVehicles.size() + " vehicles registered");
                showAlert("Success", "Vehicle registered successfully!");

                // Animate button
                Button source = (Button) event.getSource();
                source.setStyle("-fx-background-color: #2ecc71;");
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        javafx.application.Platform.runLater(() ->
                                source.setStyle("-fx-background-color: #4CAF50;"));
                    } catch (InterruptedException ex) {}
                }).start();
            } else {
                showAlert("Error", "Failed to register vehicle. Please check if you are logged in as a customer.");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid year format!");
        } catch (SQLException e) {
            System.err.println("Error registering vehicle: " + e.getMessage());
            showAlert("Database Error", "Could not register vehicle: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

// Data Models - Make them public static for better access
class ServiceRecordWorkshop {
    private String serviceType;
    private LocalDate date;
    private String description;
    private double cost;

    public ServiceRecordWorkshop(String type, LocalDate date, String desc, double cost) {
        this.serviceType = type;
        this.date = date;
        this.description = desc;
        this.cost = cost;
    }

    public String getServiceType() { return serviceType; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public double getCost() { return cost; }
}

class VehicleWorkshop {
    private String registrationNumber;
    private String make;
    private String model;
    private int year;
    private String ownerName;

    public VehicleWorkshop(String reg, String make, String model, int year, String owner) {
        this.registrationNumber = reg;
        this.make = make;
        this.model = model;
        this.year = year;
        this.ownerName = owner;
    }

    public String getRegistrationNumber() { return registrationNumber; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public String getOwnerName() { return ownerName; }
}