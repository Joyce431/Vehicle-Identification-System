module joyce.vehicleidentificationsystem1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens joyce.vehicleidentificationsystem1 to javafx.fxml;
    exports joyce.vehicleidentificationsystem1;
}