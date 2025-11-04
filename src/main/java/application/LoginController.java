package application;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setTitle("Login");
        
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter a username and password.");
            return;
        }

        new Thread(() -> {
            FirebaseConnection.getUser(username, new FirebaseConnection.OnUserFetchListener() {
                @Override
                public void onSuccess(String email, String storedHash, boolean isAdmin) {
                    String inputHash = hashPassword(password);
                    Platform.runLater(() -> {
                        if (inputHash.equals(storedHash)) {
                            showSuccess("Login Successful!");
                            handleLoginSuccess(email, isAdmin);
                        } else {
                            showError("Incorrect password.");
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    Platform.runLater(() -> showError("User not found or error: " + error));
                }
            });
        }).start();
    }

    @FXML
    private void handleBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/application/MainMenu.fxml"));
            javafx.scene.Parent root = loader.load();

            root.getStyleClass().add("dark");
            

            Object controller = loader.getController();
            try {
                controller.getClass().getMethod("setStage", Stage.class).invoke(controller, stage);
            } catch (Exception ignored) {}

            javafx.scene.Scene scene = new javafx.scene.Scene(root, Math.max(360, stage.getWidth()), Math.max(600, stage.getHeight()));
            scene.getStylesheets().add(getClass().getResource("/application/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Main Menu");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void handleLoginSuccess(String email, boolean isAdmin) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/application/MainMenu.fxml"));
            javafx.scene.Parent root = loader.load();
            root.getStyleClass().add("dark");

            MainMenuController controller = loader.getController();
            controller.setStage(stage);
            controller.setCurrentUser(email, isAdmin);

            javafx.scene.Scene scene = new javafx.scene.Scene(root, Math.max(360, stage.getWidth()), Math.max(600, stage.getHeight()));
            scene.getStylesheets().add(getClass().getResource("/application/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Main Menu");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error returning to main menu");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}