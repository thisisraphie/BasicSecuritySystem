package application;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private TextField email;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField ConfirmPasswordField;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setTitle("Login");
        this.stage.setFullScreen(true);
        
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        System.out.println("Attempted login with username: " + username);
        // TODO: Add authentication logic here
    }

    @FXML
    private void handleBack() {
        MainMenu mainMenu = new MainMenu();
        mainMenu.start(stage);
    }
}