package application;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainMenuController {

    private Stage stage;
    private String currentUserEmail;
    private boolean isAdmin;

    @FXML
    private Button loginButton;

    @FXML
    private Button signUpButton;

    @FXML
    private Button adminButton;

    @FXML
    private Button fileEncryptButton;

    @FXML
    private Button exitButton;

    @FXML
    private Label loggedInLabel;

    private static final String DATABASE_URL =
        "https://basicsecuritysystemdb-default-rtdb.asia-southeast1.firebasedatabase.app/users.json";

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCurrentUser(String email, boolean admin) {
        this.currentUserEmail = email;
        this.isAdmin = admin;
        if (loggedInLabel != null) {
            loggedInLabel.setVisible(true);
            loggedInLabel.setText("Logged in as: " + email + (admin ? " - Admin" : ""));
        }
        configureMenu(admin);
    }

    @FXML
    public void initialize() {
        if (adminButton != null) adminButton.setVisible(false);
        if (fileEncryptButton != null) fileEncryptButton.setVisible(false);
        if (loggedInLabel != null) loggedInLabel.setVisible(false);
    }

    private void configureMenu(boolean isAdmin) {
        if (loginButton != null) {
            loginButton.setVisible(true);
            loginButton.setPrefWidth(180);
        }
        if (signUpButton != null) {
            signUpButton.setVisible(true);
            signUpButton.setPrefWidth(180);
        }

        if (currentUserEmail != null) {
            if (fileEncryptButton != null) {
                fileEncryptButton.setVisible(true);
                fileEncryptButton.setPrefWidth(180);
            }

            if (adminButton != null && isAdmin) {
                adminButton.setVisible(true);
                adminButton.setPrefWidth(180);
                adminButton.setText("Admin Panel");
            } else if (adminButton != null) {
                adminButton.setVisible(false);
            }
        } else {
            if (fileEncryptButton != null) {
                fileEncryptButton.setVisible(false);
            }
            if (adminButton != null) {
                adminButton.setVisible(false);
            }
        }
    }

    @FXML
    private void handleLogin(ActionEvent e) {
        // If already logged in, warn the user that continuing will log them out
        if (currentUserEmail != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "You are currently logged in as: " + currentUserEmail + (isAdmin ? " - Admin" : "") +
                "\nContinuing to Login will log you out. Continue?",
                ButtonType.YES, ButtonType.NO);
            confirm.initModality(Modality.APPLICATION_MODAL);
            confirm.setHeaderText(null);
            confirm.setTitle("Confirm Log Out");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    // perform logout and go to login
                    currentUserEmail = null;
                    isAdmin = false;
                    if (loggedInLabel != null) loggedInLabel.setVisible(false);
                    configureMenu(false);
                    switchTo("/application/Login.fxml", "Login");
                }
            });
        } else {
            switchTo("/application/Login.fxml", "Login");
        }
    }

    @FXML
    private void handleSignUp(ActionEvent e) {
        // If already logged in, warn the user that continuing will log them out
        if (currentUserEmail != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "You are currently logged in as: " + currentUserEmail + (isAdmin ? " - Admin" : "") +
                "\nContinuing to Sign Up will log you out. Continue?",
                ButtonType.YES, ButtonType.NO);
            confirm.initModality(Modality.APPLICATION_MODAL);
            confirm.setHeaderText(null);
            confirm.setTitle("Confirm Log Out");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    // perform logout and go to sign up
                    currentUserEmail = null;
                    isAdmin = false;
                    if (loggedInLabel != null) loggedInLabel.setVisible(false);
                    configureMenu(false);
                    switchTo("/application/SignUp.fxml", "Sign Up");
                }
            });
        } else {
            switchTo("/application/SignUp.fxml", "Sign Up");
        }
    }

    @FXML
    private void handleAdmin(ActionEvent e) {
        if (currentUserEmail == null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Please log in first.", ButtonType.OK);
            a.initModality(Modality.APPLICATION_MODAL);
            a.setHeaderText(null);
            a.setTitle("Info");
            a.showAndWait();
            return;
        }
        switchTo("/application/AdminDash.fxml", "Admin Panel");
    }

    @FXML
    private void handleFileEncrypt(ActionEvent e) {
    switchTo("/application/FileEncrypt.fxml", "File Encryption");
    }

    @FXML
    private void handleExit(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?", ButtonType.YES, ButtonType.NO);
        a.initModality(Modality.APPLICATION_MODAL);
        a.setHeaderText(null);
        a.setTitle("Exit");
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                if (stage != null) stage.close();
                else System.exit(0);
            }
        });
    }

    private void switchTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Apply consistent dark theme every time
            root.getStyleClass().add("dark");
            root.getStylesheets().add(getClass().getResource("/application/styles.css").toExternalForm());

            Object controller = loader.getController();

            try {
                controller.getClass().getMethod("setStage", Stage.class).invoke(controller, stage);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}

            if (currentUserEmail != null) {
                try {
                    try {
                        controller.getClass().getMethod("setCurrentUser", String.class, boolean.class)
                            .invoke(controller, currentUserEmail, this.isAdmin);
                    } catch (NoSuchMethodException ex) {
                        controller.getClass().getMethod("setCurrentUser", String.class)
                            .invoke(controller, currentUserEmail);
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
            }

            // Prefer a scene size specified by the FXML root pref size when available
            double prefW = root.prefWidth(-1);
            double prefH = root.prefHeight(-1);
            double sceneW = prefW > 0 ? prefW : Math.max(360, stage.getWidth());
            double sceneH = prefH > 0 ? prefH : Math.max(600, stage.getHeight());
            Scene scene = new Scene(root, sceneW, sceneH);
            scene.getStylesheets().add(getClass().getResource("/application/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Unable to load screen: " + fxmlPath, ButtonType.OK);
            a.initModality(Modality.APPLICATION_MODAL);
            a.setHeaderText(null);
            a.showAndWait();
        }
    }
}
