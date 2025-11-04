package application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {

    private Stage stage;

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private ProgressBar pwdStrengthBar;
    @FXML private Label pwdStrengthLabel;

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setTitle("Sign Up");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        emailField.setPromptText("name@example.com");
        usernameField.setPromptText("choose a username");
        passwordField.setPromptText("at least 4 characters");
        confirmPasswordField.setPromptText("repeat password");

        Platform.runLater(() -> emailField.requestFocus());

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> updatePasswordStrength(newVal));
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", "Please fill in all fields.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(AlertType.ERROR, "Validation Error", "Please enter a valid email address.");
            return;
        }

        if (password.length() < 4) {
            showAlert(AlertType.ERROR, "Validation Error", "Password must be at least 4 characters long.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(AlertType.ERROR, "Validation Error", "Passwords do not match.");
            return;
        }

        new Thread(() -> {
            try {
                if (FirebaseConnection.usernameExists(username)) {
                    Platform.runLater(() -> showAlert(AlertType.ERROR, "Validation Error", "Username already taken."));
                    return;
                }

                String hashed = hashPassword(password);
                boolean success = FirebaseConnection.sendData(email, username, hashed, false);

                if (success) {
                    Platform.runLater(() -> {
                        showAlert(AlertType.INFORMATION, "Success", "Sign Up Successful!");
                        handleBack(null);
                    });
                } else {
                    Platform.runLater(() -> showAlert(AlertType.ERROR, "Network Error", "Failed to send data. Try again."));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/MainMenu.fxml"));
            Parent root = loader.load();

            if (stage != null && stage.getScene() != null && stage.getScene().getRoot() != null) {
                if (stage.getScene().getRoot().getStyleClass().contains("light")) root.getStyleClass().add("light");
                else root.getStyleClass().add("dark");
            }

            MainMenuController controller = loader.getController();
            controller.setStage(stage);

            Scene scene = new Scene(root, Math.max(360, stage.getWidth()), Math.max(600, stage.getHeight()));
            scene.getStylesheets().add(getClass().getResource("/application/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Main Menu");

            if (stage != null && stage.getProperties().containsKey("prevWidth") && stage.getProperties().containsKey("prevHeight")) {
                Object pw = stage.getProperties().remove("prevWidth");
                Object ph = stage.getProperties().remove("prevHeight");
                if (pw instanceof Number && ph instanceof Number) {
                    stage.setWidth(((Number) pw).doubleValue());
                    stage.setHeight(((Number) ph).doubleValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert a = new Alert(type, message);
        a.setTitle(title);
        a.setHeaderText(null);
        a.initOwner(stage);
        a.showAndWait();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }

    private String hashPassword(String password) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }


    private void updatePasswordStrength(String pwd) {
        int score = getStrengthScore(pwd); // 0..4
        double progress = (double) score / 4.0;
        pwdStrengthBar.setProgress(progress);

        switch (score) {
            case 0, 1 -> {
                pwdStrengthLabel.setText("Weak");
                pwdStrengthLabel.setStyle("-fx-text-fill: #ff5c5c; -fx-font-weight: 600;");
                pwdStrengthBar.setStyle("-fx-accent: #ff5c5c;"); // red
            }
            case 2, 3 -> {
                pwdStrengthLabel.setText("Moderate");
                pwdStrengthLabel.setStyle("-fx-text-fill: #ffb74d; -fx-font-weight: 600;");
                pwdStrengthBar.setStyle("-fx-accent: #ffb74d;"); // orange
            }
            case 4 -> {
                pwdStrengthLabel.setText("Strong");
                pwdStrengthLabel.setStyle("-fx-text-fill: #61e786; -fx-font-weight: 600;");
                pwdStrengthBar.setStyle("-fx-accent: #61e786;"); // green
            }
            default -> {
                pwdStrengthLabel.setText("");
                pwdStrengthBar.setStyle("");
            }
        }
    }

    private int getStrengthScore(String pwd) {
        int score = 0;
        if (pwd == null || pwd.isEmpty()) return 0;
        if (pwd.length() >= 8) score++;
        if (pwd.matches(".*[A-Z].*") && pwd.matches(".*[a-z].*")) score++;
        if (pwd.matches(".*[0-9].*")) score++;
        if (pwd.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score++;
        return score;
    }
}
