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
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Optional;

public class FileEncryptController {

    @FXML private javafx.scene.control.Button btnSelectFile;
    @FXML private javafx.scene.control.Button btnEncrypt;
    @FXML private javafx.scene.control.Button btnDecrypt;
    @FXML private TextArea outputArea;

    private Stage stage;
    private String currentUserEmail;
    private boolean isAdmin;
    private File selectedFile;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleDecrypt(ActionEvent event) {
        // Determine the encrypted file to decrypt.
        // If a file is already selected and it ends with .enc, use it.
        // If a non-.enc file is selected, prefer the corresponding file in ./generated/<name>.enc if it exists.
        // Otherwise prompt the user to pick a .enc file.
        File file = null;
        if (selectedFile != null) {
            if (selectedFile.getName().toLowerCase().endsWith(".enc")) {
                file = selectedFile;
            } else {
                Path candidate = Path.of(System.getProperty("user.dir"), "generated", selectedFile.getName() + ".enc");
                if (Files.exists(candidate)) {
                    file = candidate.toFile();
                    appendLog("Found corresponding encrypted file: " + file.getAbsolutePath());
                }
            }
        }

        if (file == null) {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select File to Decrypt");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Encrypted files (*.enc)", "*.enc"));
            file = chooser.showOpenDialog(stage);
            if (file == null) {
                appendLog("No file selected.");
                return;
            }
        }

        // Prompt for password
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
            appendLog("Decryption cancelled or empty password.");
            return;
        }

        String password = result.get();
        try {
            appendLog("Decrypting: " + file.getName() + " ...");
            Path outPath = CryptoUtils.decryptFile(file.toPath(), password);
            appendLog("Decrypted to: " + outPath.toString());
            showInfo("Decrypt", "File decrypted successfully:\n" + outPath.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            appendLog("Decryption failed: " + ex.getMessage());
            showError("Decryption failed: " + ex.getMessage());
        }
    }

    public void setCurrentUser(String email, boolean isAdmin) {
        this.currentUserEmail = email;
        this.isAdmin = isAdmin;
    }

    @FXML
    private void handleSelectFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select File to Encrypt");
        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            appendLog("No file selected.");
            return;
        }
        selectedFile = file;
        appendLog("Selected: " + file.getAbsolutePath());
    }

    @FXML
    private void handleEncrypt(ActionEvent event) {
        if (selectedFile == null) {
            appendLog("No file selected to encrypt.");
            showInfo("Encrypt", "Please select a file first.");
            return;
        }

        // Prompt for password using a secure PasswordField dialog
        Dialog<String> pwdDialog = new Dialog<>();
        pwdDialog.setTitle("Enter password");
        pwdDialog.setHeaderText("Enter a password to encrypt the file");
        pwdDialog.initModality(Modality.APPLICATION_MODAL);

        ButtonType okButton = new ButtonType("Encrypt", ButtonData.OK_DONE);
        pwdDialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        PasswordField pf = new PasswordField();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Password:"), 0, 0);
        grid.add(pf, 1, 0);
        pwdDialog.getDialogPane().setContent(grid);

        // Convert result
        pwdDialog.setResultConverter(button -> {
            if (button == okButton) return pf.getText();
            return null;
        });

        Optional<String> result = pwdDialog.showAndWait();
        if (result.isEmpty() || result.get().isEmpty()) {
            appendLog("Encryption cancelled or empty password.");
            return;
        }

        String password = result.get();

        try {
            appendLog("Encrypting: " + selectedFile.getName() + " ...");

            byte[] input = Files.readAllBytes(selectedFile.toPath());

            // Parameters
            SecureRandom rnd = new SecureRandom();
            byte[] salt = new byte[16];
            rnd.nextBytes(salt);
            byte[] iv = new byte[12]; // GCM recommended 12 bytes
            rnd.nextBytes(iv);

            // Derive key with PBKDF2
            int iterations = 65536;
            int keyLen = 128; // bits
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLen);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = skf.generateSecret(spec).getEncoded();
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            byte[] cipherText = cipher.doFinal(input);

            // Write out: [salt(16)][iv(12)][ciphertext]
            // Save into a generated/ folder inside the project working directory
            Path outPath = Path.of(System.getProperty("user.dir"), "generated", selectedFile.getName() + ".enc");
            Files.createDirectories(outPath.getParent());
            byte[] outBytes = new byte[salt.length + iv.length + cipherText.length];
            System.arraycopy(salt, 0, outBytes, 0, salt.length);
            System.arraycopy(iv, 0, outBytes, salt.length, iv.length);
            System.arraycopy(cipherText, 0, outBytes, salt.length + iv.length, cipherText.length);

            Files.write(outPath, outBytes);

            appendLog("Encrypted to: " + outPath.toString());
            showInfo("Encrypt", "File encrypted successfully:\n" + outPath.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            appendLog("Encryption failed: " + ex.getMessage());
            showError("Encryption failed: " + ex.getMessage());
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

            if (!root.getStyleClass().contains("dark")) root.getStyleClass().add("dark");
            Scene scene = new Scene(root, 800, 400);
            String ss = getClass().getResource("/application/styles.css").toExternalForm();
            if (!scene.getStylesheets().contains(ss)) scene.getStylesheets().add(ss);
            targetStage.setScene(scene);
            targetStage.setMinWidth(800);
            targetStage.setMinHeight(400);
            targetStage.setTitle("Main Menu");
            targetStage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Unable to return to main menu: " + ex.getMessage());
        }
    }

    private void appendLog(String msg) {
        if (outputArea != null) {
            outputArea.appendText(msg + "\n");
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
