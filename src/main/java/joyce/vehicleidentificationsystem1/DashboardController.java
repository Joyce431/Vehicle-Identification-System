package joyce.vehicleidentificationsystem1;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DashboardController {

    @FXML private VBox activityList;
    @FXML private ScrollPane activityScroll;
    @FXML private BorderPane borderpane;
    @FXML private Label complete;
    @FXML private Tab dashboard;
    @FXML private Button fadeButton;
    @FXML private TableColumn<Violation, Double> fineAmountCol;
    @FXML private HBox hbox;
    @FXML private TabPane mainTabPane;
    @FXML private MenuBar menubar;
    @FXML private Label recent;
    @FXML private Label registration;
    @FXML private Button shadowButton;
    @FXML private Label statistics;
    @FXML private TableColumn<Violation, String> statusCol;
    @FXML private Label statusLabel;
    @FXML private ProgressBar systemCapacity;
    @FXML private Label used;
    @FXML private VBox vbox;
    @FXML private VBox vbox2;
    @FXML private VBox vbox3;
    @FXML private Label vehicle;
    @FXML private Pagination vehiclePagination;
    @FXML private ProgressIndicator vehicleProgress;
    @FXML private TableView<Violation> violationTable;
    @FXML private TableColumn<Violation, String> violationTypeCol;
    @FXML private Label violations;
    @FXML private Label timeLabel;

    @FXML private ScrollPane dummyScrollPane;
    @FXML private VBox dummyContentBox;

    private ObservableList<Violation> violationsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        violationTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        fineAmountCol.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadViolationsData();
        violationTable.setItems(violationsList);

        updateStatistics();
        loadRecentActivities();
        setupVehiclePagination();
        setupProgressIndicators();
        setupVisualEffects();
        updateSystemCapacity();
        setupDummyScrollPane();

        if (statusLabel != null) {
            statusLabel.setText("System Ready - " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        updateTimeLabel();

        System.out.println("DashboardController initialized successfully!");
    }

    private void updateTimeLabel() {
        Thread timeThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> {
                        if (timeLabel != null) {
                            timeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        }
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        timeThread.setDaemon(true);
        timeThread.start();
    }

    private void setupDummyScrollPane() {
        if (dummyScrollPane == null) {
            System.out.println("dummyScrollPane is null - check FXML");
            return;
        }

        if (dummyContentBox == null) {
            dummyContentBox = new VBox(8);
            dummyContentBox.getStyleClass().add("dummy-content-box");
        }

        dummyContentBox.getChildren().clear();

        String[] dummyData = {
                "📚 Student: Tumo Koloi - Course: Computer Science - Grade: A",
                "📚 Student: Joyce Koetle - Course: Software Engineering - Grade: B+",
                "📚 Student: Moabi Lereng - Course: Database Systems - Grade: A-",
                "📚 Student: Sarah Williams - Course: Web Development - Grade: B",
                "📚 Student: David Brown - Course: Network Security - Grade: A",
                "📚 Student: Emily Davis - Course: Artificial Intelligence - Grade: A+",
                "📚 Student: Chris Wilson - Course: Mobile Apps - Grade: B-",
                "📚 Student: Jessica Taylor - Course: Cloud Computing - Grade: A",
                "📚 Student: Tsepo Leboto - Course: Data Science - Grade: B+",
                "📚 Student: Laura Anderson - Course: UX Design - Grade: A-",
                "📚 Student: Kevin Thomas - Course: Statistics - Grade: B",
                "📚 Student: Amanda Jackson - Course: Cyber Security - Grade: A",
                "📚 Student: Brian White - Course: IT Project Management - Grade: B+",
                "📚 Student: Nicole Harris - Course: System Analysis - Grade: A-",
                "📚 Student: Jason Martin - Course: Operating Systems - Grade: B",
                "📚 Student: Michelle Thompson - Course: Calculus - Grade: A",
                "📚 Student: Eric Garcia - Course: Computer Graphics - Grade: B+",
                "📚 Student: Stephanie Martinez - Course: Robotics - Grade: A-",
                "📚 Student: Daniel Robinson - Course: Object Oriented Programming - Grade: B",
                "📚 Student: Rebecca Sello - Course: Information System - Grade: A"
        };

        for (int i = 0; i < dummyData.length; i++) {
            HBox itemBox = new HBox(10);
            itemBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            itemBox.getStyleClass().add(i % 2 == 0 ? "dummy-item-even" : "dummy-item-odd");

            Label numberLabel = new Label(String.format("%02d.", i + 1));
            numberLabel.getStyleClass().add("dummy-number");

            Label contentLabel = new Label(dummyData[i]);
            contentLabel.getStyleClass().add("dummy-content");

            itemBox.getChildren().addAll(numberLabel, contentLabel);
            dummyContentBox.getChildren().add(itemBox);
        }

        dummyScrollPane.setContent(dummyContentBox);
        System.out.println("✓ ScrollPane with " + dummyData.length + " dummy student entries added successfully!");
    }

    private void loadViolationsData() {
        violationsList.clear();
        violationsList.add(new Violation("Speeding", 150.00, "Unpaid"));
        violationsList.add(new Violation("Parking Violation", 45.00, "Paid"));
        violationsList.add(new Violation("Red Light Violation", 250.00, "Unpaid"));
        violationsList.add(new Violation("No Insurance", 500.00, "Pending"));
        violationsList.add(new Violation("Driving Without License", 300.00, "Unpaid"));
    }

    private void updateStatistics() {
        if (vehicle != null) vehicle.setText("24");
        if (registration != null) registration.setText("18");
        if (violations != null) violations.setText("12");
        if (complete != null) complete.setText("156");
        if (used != null) used.setText("850/1000");
        if (systemCapacity != null) systemCapacity.setProgress(0.45);
        if (vehicleProgress != null) vehicleProgress.setProgress(0.65);
    }

    private void loadRecentActivities() {
        if (activityList != null) {
            activityList.getChildren().clear();

            String[] activities = {
                    "🚗 New vehicle registered: ABC123 - Toyota Camry",
                    "🔧 Service completed for vehicle XYZ789 - Oil Change",
                    "🚨 Violation reported for vehicle DEF456 - Speeding",
                    "📝 New customer registered: John Doe",
                    "💰 Fine payment received: $150.00",
                    "📄 Insurance policy renewed for vehicle GHI789"
            };

            for (int i = 0; i < activities.length; i++) {
                Label activityLabel = new Label(activities[i]);
                activityLabel.getStyleClass().add("activity-item");
                activityLabel.setMaxWidth(Double.MAX_VALUE);

                FadeTransition fade = new FadeTransition(Duration.millis(500), activityLabel);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                fade.setDelay(Duration.millis(i * 100));
                fade.play();

                activityList.getChildren().add(activityLabel);
            }
        }
    }

    private void setupVehiclePagination() {
        if (vehiclePagination != null) {
            vehiclePagination.setPageCount(10);
            vehiclePagination.setMaxPageIndicatorCount(5);
            vehiclePagination.setPageFactory(pageIndex -> {
                VBox box = new VBox(8);
                box.getStyleClass().add("pagination-box");

                Label vehicleLabel = getVehicleLabel(pageIndex);
                box.getChildren().add(vehicleLabel);

                Label pageLabel = new Label("Page " + (pageIndex + 1) + " of 10");
                pageLabel.getStyleClass().add("page-label");
                box.getChildren().add(pageLabel);

                return box;
            });
        }
    }

    private static Label getVehicleLabel(Integer pageIndex) {
        String[] vehiclesList = {
                "🚗 ABC123 - Toyota Camry (2020) - Owner: John Doe",
                "🚗 XYZ789 - Honda Civic (2019) - Owner: John Doe",
                "🚗 DEF456 - Ford Mustang (2021) - Owner: Jane Smith",
                "🚗 GHI789 - Tesla Model 3 (2022) - Owner: Bob Johnson",
                "🚗 JKL012 - BMW X5 (2020) - Owner: Alice Brown"
        };

        int index = pageIndex % vehiclesList.length;
        Label vehicleLabel = new Label(vehiclesList[index]);
        vehicleLabel.getStyleClass().add("vehicle-label");
        return vehicleLabel;
    }

    private void setupProgressIndicators() {
        new Thread(() -> {
            try {
                for (double i = 0; i <= 0.85; i += 0.01) {
                    final double progress = i;
                    javafx.application.Platform.runLater(() -> {
                        if (systemCapacity != null) systemCapacity.setProgress(progress);
                        if (vehicleProgress != null) vehicleProgress.setProgress(progress);
                    });
                    Thread.sleep(30);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupVisualEffects() {
        if (shadowButton != null) {
            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(10);
            dropShadow.setOffsetX(3);
            dropShadow.setOffsetY(3);
            dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));
            shadowButton.setEffect(dropShadow);

            shadowButton.setOnMouseEntered(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), shadowButton);
                st.setToX(1.05);
                st.setToY(1.05);
                st.play();
            });

            shadowButton.setOnMouseExited(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), shadowButton);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });
        }

        if (fadeButton != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), fadeButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.3);
            fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
        }
    }

    private void updateSystemCapacity() {
        new Thread(() -> {
            try {
                for (double i = 0; i <= 0.85; i += 0.02) {
                    final double progress = i;
                    javafx.application.Platform.runLater(() -> {
                        if (systemCapacity != null) systemCapacity.setProgress(progress);
                    });
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void reportV(ActionEvent actionEvent) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Report Violation");
        dialog.setHeaderText("Report a New Traffic Violation");
        dialog.getDialogPane().getStyleClass().add("violation-dialog");

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        TextField vehicleField = new TextField();
        vehicleField.setPromptText("Vehicle Registration Number");

        ComboBox<String> violationTypeCombo = new ComboBox<>();
        violationTypeCombo.getItems().addAll("Speeding", "Parking Violation", "Red Light Violation",
                "Driving Without License", "No Insurance", "Drunk Driving");
        violationTypeCombo.setValue("Speeding");

        TextField fineField = new TextField();
        fineField.setPromptText("Fine Amount");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description of violation");
        descArea.setPrefRowCount(3);

        content.getChildren().addAll(
                new Label("Vehicle Registration:"), vehicleField,
                new Label("Violation Type:"), violationTypeCombo,
                new Label("Fine Amount ($):"), fineField,
                new Label("Description:"), descArea
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String vehicle = vehicleField.getText().trim();
                String type = violationTypeCombo.getValue();
                String fineText = fineField.getText().trim();

                if (vehicle.isEmpty() || fineText.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please enter vehicle registration and fine amount!");
                    return;
                }

                try {
                    double fine = Double.parseDouble(fineText);

                    Violation newViolation = new Violation(type, fine, "Unpaid");
                    violationsList.add(0, newViolation);
                    violationTable.refresh();

                    addRecentActivity("🚨 Violation reported for vehicle " + vehicle + " - " + type);

                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Violation reported successfully!\nVehicle: " + vehicle +
                                    "\nType: " + type + "\nFine: $" + fine);

                    Button source = (Button) actionEvent.getSource();
                    source.setStyle("-fx-background-color: #2ecc71;");
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            javafx.application.Platform.runLater(() ->
                                    source.setStyle("-fx-background-color: #e74c3c;"));
                        } catch (InterruptedException e) {}
                    }).start();

                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid fine amount!");
                }
            }
        });
    }

    @FXML
    public void refreshV(ActionEvent actionEvent) {
        statusLabel.setText("Refreshing data...");

        new Thread(() -> {
            try {
                Thread.sleep(1000);

                javafx.application.Platform.runLater(() -> {
                    updateStatistics();
                    loadViolationsData();
                    violationTable.refresh();
                    addRecentActivity("🔄 Dashboard data refreshed at " +
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    setupDummyScrollPane();
                    setupVehiclePagination();
                    statusLabel.setText("System Ready - " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

                    Button source = (Button) actionEvent.getSource();
                    source.setStyle("-fx-background-color: #2ecc71;");
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            javafx.application.Platform.runLater(() ->
                                    source.setStyle("-fx-background-color: #3498db;"));
                        } catch (InterruptedException e) {}
                    }).start();

                    showAlert(Alert.AlertType.INFORMATION, "Refresh Complete", "All dashboard data has been refreshed successfully!");
                    System.out.println("✓ Dashboard data refreshed!");
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Refresh failed!");
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to refresh data!");
                });
            }
        }).start();
    }

    private void addRecentActivity(String activity) {
        if (activityList != null) {
            Label activityLabel = new Label(activity);
            activityLabel.getStyleClass().add("activity-item");
            activityLabel.setMaxWidth(Double.MAX_VALUE);

            FadeTransition fade = new FadeTransition(Duration.millis(500), activityLabel);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();

            activityList.getChildren().add(0, activityLabel);

            if (activityList.getChildren().size() > 10) {
                activityList.getChildren().remove(10, activityList.getChildren().size());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleExit(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Exit Application");
        alert.setContentText("Are you sure you want to exit?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                javafx.application.Platform.exit();
            }
        });
    }

    @FXML
    void loadAdminView(ActionEvent event) {
        HelloApplication.switchScene("admin.fxml", "Admin Panel");
    }

    @FXML
    void loadCustomerView(ActionEvent event) {
        HelloApplication.switchScene("customer.fxml", "Customer Management");
    }

    @FXML
    void loadPoliceView(ActionEvent event) {
        HelloApplication.switchScene("police.fxml", "Police Module");
    }

    @FXML
    void loadWorkshopView(ActionEvent event) {
        HelloApplication.switchScene("workshop.fxml", "Workshop Management");
    }

    @FXML
    void loadInsuranceView(ActionEvent event) {
        HelloApplication.switchScene("insurance.fxml", "Insurance Management");
    }

    @FXML
    void showAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Vehicle Identification System");
        alert.setContentText(
                "Version: 1.0\n\n" +
                        "Developed by: Vehicle Identification System Team\n\n" +
                        "Technologies Used:\n" +
                        "• JavaFX for Frontend\n" +
                        "• PostgreSQL for Database\n" +
                        "• JDBC for Database Connectivity\n\n" +
                        "© 2024 Vehicle Identification System"
        );
        alert.showAndWait();
    }

    @FXML
    void Logout(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout Confirmation");
        confirm.setHeaderText("Logout");
        confirm.setContentText("Are you sure you want to logout?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SessionManager.logout();
                HelloApplication.switchScene("login.fxml", "Login - Vehicle Identification System", 900, 700);
            }
        });
    }

    @FXML
    void Exit(ActionEvent event) {
        handleExit(event);
    }

    public void file(ActionEvent actionEvent) {
    }
}

