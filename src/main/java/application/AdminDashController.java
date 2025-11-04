package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class AdminDashController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField fileNameField;

    private Stage stage;
    private String currentUserEmail;
    private boolean isAdmin;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCurrentUser(String email, boolean isAdmin) {
        this.currentUserEmail = email;
        this.isAdmin = isAdmin;
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        showInfo("Add User", "Add user functionality is not yet implemented.");
    }

    @FXML
    private void handleRemoveUser(ActionEvent event) {
        showInfo("Remove User", "Remove user functionality is not yet implemented.");
    }

    @FXML
    private void handleToggleAdmin(ActionEvent event) {
        showInfo("Toggle Admin", "Toggle admin functionality is not yet implemented.");
    }

    @FXML
    private void handleGenerateSample(ActionEvent event) {
        String fname = (fileNameField != null) ? fileNameField.getText().trim() : "sample.txt";
        if (fname.isEmpty()) fname = "sample.txt";
        showInfo("Generate Sample File", "Generated sample file: " + fname);
    }

    @FXML
    private void handleDecrypt(ActionEvent event) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Encrypted File (.enc)");
            javafx.stage.FileChooser.ExtensionFilter filter = new javafx.stage.FileChooser.ExtensionFilter("Encrypted files (*.enc)", "*.enc");
            chooser.getExtensionFilters().add(filter);
            java.io.File file = chooser.showOpenDialog(stage);
            if (file == null) return;

            Dialog<String> pwdDialog = new Dialog<>();
            pwdDialog.setTitle("Enter password");
            pwdDialog.setHeaderText("Enter the password used to encrypt the file");
            pwdDialog.initModality(Modality.APPLICATION_MODAL);

            ButtonType okButton = new ButtonType("Decrypt", ButtonData.OK_DONE);
            pwdDialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

            PasswordField pf = new PasswordField();
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Password:"), 0, 0);
            grid.add(pf, 1, 0);
            pwdDialog.getDialogPane().setContent(grid);

            pwdDialog.setResultConverter(button -> button == okButton ? pf.getText() : null);
            Optional<String> result = pwdDialog.showAndWait();
            if (result.isEmpty() || result.get().isEmpty()) {
                showInfo("Decrypt", "Decryption cancelled or empty password.");
                return;
            }

            String password = result.get();
            Path out = CryptoUtils.decryptFile(file.toPath(), password);
            showInfo("Decrypt", "File decrypted to:\n" + out.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Decryption failed: " + ex.getMessage());
        }
    }

    @FXML
    private void handleReturn(ActionEvent event) {
        try {
            Stage targetStage = this.stage;
            if (targetStage == null) {
                Object src = event.getSource();
                if (src instanceof javafx.scene.Node) {
                    targetStage = (Stage) ((javafx.scene.Node) src).getScene().getWindow();
                }
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/MainMenu.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            try {
                controller.getClass().getMethod("setStage", Stage.class).invoke(controller, targetStage);
            } catch (Exception ignored) {}

            if (currentUserEmail != null) {
                try {
                    controller.getClass().getMethod("setCurrentUser", String.class, boolean.class)
                        .invoke(controller, currentUserEmail, isAdmin);
                } catch (Exception ignored) {}
            }

            if (!root.getStyleClass().contains("dark")) {
                root.getStyleClass().add("dark");
            }

            Scene scene = new Scene(root, 600, 400);

            String ss = getClass().getResource("/application/styles.css").toExternalForm();
            if (!scene.getStylesheets().contains(ss)) scene.getStylesheets().add(ss);
            targetStage.setScene(scene);
            targetStage.setMinWidth(600);
            targetStage.setMinHeight(400);
            targetStage.setTitle("Main Menu");
            targetStage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Unable to return to main menu: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
