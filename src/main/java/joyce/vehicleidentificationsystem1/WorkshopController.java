package joyce.vehicleidentificationsystem1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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

    @FXML
    public void initialize() {
        // Setup service history table columns
        serviceTypeCol.setCellValueFactory(new PropertyValueFactory<>("serviceType"));
        serviceDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        serviceDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));

        // Setup service type combo
        serviceTypeCombo.getItems().addAll("Oil Change", "Tire Rotation", "Brake Service",
                "Engine Tune-up", "Transmission Service", "Battery Replacement", "AC Service");
        serviceTypeCombo.setValue("Oil Change");

        // Setup vehicle select combo
        setupVehicleCombo();

        // Load sample service records
        loadServiceRecords();

        // Load registered vehicles
        loadRegisteredVehicles();

        // Setup date picker default
        serviceDatePicker.setValue(LocalDate.now());
    }

    private void setupVehicleCombo() {
        vehicleSelect.getItems().addAll("ABC123 - Toyota Camry", "XYZ789 - Honda Civic", "DEF456 - Ford Mustang");
        vehicleSelect.setValue("ABC123 - Toyota Camry");

        vehicleSelect.setOnAction(e -> {
            String selected = vehicleSelect.getValue();
            if (selected != null) {
                String regNumber = selected.split(" ")[0];
                loadVehicleDetails(regNumber);
            }
        });
    }

    private void loadVehicleDetails(String regNumber) {
        // Sample vehicle details - in real app, fetch from database
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

    private void loadServiceRecords() {
        serviceRecords.add(new ServiceRecordWorkshop("Oil Change", LocalDate.of(2024, 3, 15),
                "Regular oil change and filter replacement", 89.99));
        serviceRecords.add(new ServiceRecordWorkshop("Tire Rotation", LocalDate.of(2024, 3, 10),
                "Rotated all four tires", 49.99));
        serviceRecords.add(new ServiceRecordWorkshop("Brake Service", LocalDate.of(2024, 3, 5),
                "Front brake pads replacement", 299.99));
        serviceRecords.add(new ServiceRecordWorkshop("Engine Tune-up", LocalDate.of(2024, 2, 28),
                "Complete engine diagnostic and tune-up", 450.00));

        serviceHistoryTable.setItems(serviceRecords);
        history.setText(String.valueOf(serviceRecords.size()));
    }

    private void loadRegisteredVehicles() {
        registeredVehicles.add(new VehicleWorkshop("ABC123", "Toyota", "Camry", 2020, "John Doe"));
        registeredVehicles.add(new VehicleWorkshop("XYZ789", "Honda", "Civic", 2019, "John Doe"));
        registeredVehicles.add(new VehicleWorkshop("DEF456", "Ford", "Mustang", 2021, "Jane Smith"));

        // Display in VBox
        for (VehicleWorkshop v : registeredVehicles) {
            Label vehicleLabel = new Label("🚗 " + v.getRegistrationNumber() + " - " + v.getMake() + " " + v.getModel() + " (" + v.getYear() + ")");
            vehicleLabel.setStyle("-fx-padding: 5; -fx-background-color: #ecf0f1; -fx-background-radius: 5;");
            vehicleLabel.setMaxWidth(Double.MAX_VALUE);
            vehicleListContainer.getChildren().add(vehicleLabel);
        }
        registered.setText(String.valueOf(registeredVehicles.size()));
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

            ServiceRecordWorkshop newRecord = new ServiceRecordWorkshop(serviceType, serviceDate, description, cost);
            serviceRecords.add(0, newRecord);
            serviceHistoryTable.refresh();

            // Clear fields
            costField.clear();
            serviceDescription.clear();
            serviceDatePicker.setValue(LocalDate.now());

            showAlert("Success", "Service record added successfully!");

            // Animate register button
            register.setStyle("-fx-background-color: #2ecc71;");
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    javafx.application.Platform.runLater(() ->
                            register.setStyle("-fx-background-color: #3498db;"));
                } catch (InterruptedException ex) {}
            }).start();

        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid cost amount!");
        }
    }

    @FXML
    void registerVehicle(ActionEvent event) {
        String regNumber = regNumberField.getText();
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
            VehicleWorkshop newVehicle = new VehicleWorkshop(regNumber, make, model, year, ownerName);
            registeredVehicles.add(newVehicle);

            // Add to display
            Label vehicleLabel = new Label("🚗 " + regNumber + " - " + make + " " + model + " (" + year + ")");
            vehicleLabel.setStyle("-fx-padding: 5; -fx-background-color: #ecf0f1; -fx-background-radius: 5;");
            vehicleLabel.setMaxWidth(Double.MAX_VALUE);
            vehicleListContainer.getChildren().add(vehicleLabel);

            // Add to combo box
            vehicleSelect.getItems().add(regNumber + " - " + make + " " + model);

            // Clear fields
            regNumberField.clear();
            makeField.clear();
            modelField.clear();
            yearField.clear();
            ownerNameField.clear();

            registered.setText(String.valueOf(registeredVehicles.size()));
            showAlert("Success", "Vehicle registered successfully!");

        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid year format!");
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

// Data Models
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