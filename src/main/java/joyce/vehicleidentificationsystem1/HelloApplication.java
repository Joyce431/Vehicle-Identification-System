package joyce.vehicleidentificationsystem1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Initialize database connection at startup
        DatabaseManager.getInstance();

        // Load login FXML
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 900, 700);

        // Apply CSS if exists

        try {
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS file not found, continuing without styles");
        }

        stage.setTitle("Vehicle Identification System - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void switchScene(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);

            try {
                scene.getStylesheets().add(HelloApplication.class.getResource("style.css").toExternalForm());
            } catch (Exception e) {
                // CSS file optional
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load " + fxml + ": " + e.getMessage());
        }
    }

    public static void switchScene(String fxml, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);

            try {
                scene.getStylesheets().add(HelloApplication.class.getResource("style.css").toExternalForm());
            } catch (Exception e) {
                // CSS file optional
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load " + fxml + ": " + e.getMessage());
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    private static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}