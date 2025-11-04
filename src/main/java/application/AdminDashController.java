package application;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class AdminDashController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button addButton;
    @FXML private Button removeButton;
    @FXML private Button toggleButton;
    @FXML private ListView<String> userList;

    private Stage stage;
    private String currentUser = "admin"; // Replace with actual logged-in username
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Map<String, Map<String, Object>> userMap = new HashMap<>();
    private static final String DATABASE_URL = "https://basicsecuritysystemdb-default-rtdb.asia-southeast1.firebasedatabase.app/users.json";

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    @FXML
    public void initialize() {
        loadUsers();

        userList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) onUserSelected(newVal);
            else clearSelection();
        });
    }

    private void loadUsers() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(DATABASE_URL)).GET().build();
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    String body = response.body();
                    if (body == null || body.equals("null")) return;

                    Platform.runLater(() -> {
                        userList.getItems().clear();
                        userMap.clear();

                        String trimmed = body.trim();
                        if (trimmed.length() < 2) return;
                        trimmed = trimmed.substring(1, trimmed.length() - 1); // remove { }

                        String[] entries = trimmed.split("},");
                        for (String entry : entries) {
                            if (!entry.endsWith("}")) entry += "}";
                            int keyEnd = entry.indexOf(':');
                            if (keyEnd == -1) continue;

                            String userJson = entry.substring(keyEnd + 1);

                            String username = extractValue(userJson, "username");
                            String email = extractValue(userJson, "email");
                            String password = extractValue(userJson, "password");
                            boolean isAdmin = userJson.contains("\"isAdmin\":true");

                            if (username.equals(currentUser)) continue; // skip current admin

                            Map<String, Object> data = new HashMap<>();
                            data.put("email", email);
                            data.put("password", password);
                            data.put("isAdmin", isAdmin);
                            userMap.put(username, data);

                            userList.getItems().add(username + (isAdmin ? " (Admin)" : ""));
                        }
                        clearSelection();
                    });
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onUserSelected(String selectedText) {
        String username = selectedText.replace(" (Admin)", "");
        Map<String, Object> data = userMap.get(username);

        if (data != null) {
            usernameField.setText(username);
            emailField.setText((String) data.get("email"));
            passwordField.setText((String) data.get("password"));
            addButton.setDisable(true);
            removeButton.setDisable(username.equals(currentUser));
            toggleButton.setDisable(username.equals(currentUser));
        }
    }

    private void clearSelection() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        userList.getSelectionModel().clearSelection();
        addButton.setDisable(false);
        removeButton.setDisable(true);
        toggleButton.setDisable(true);
    }

    @FXML
    private void handleAddUser() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Please fill all fields.");
            return;
        }

        if (userMap.containsKey(username)) {
            showAlert("Username already exists.");
            return;
        }

        boolean success = FirebaseConnection.sendData(email, username, password, false);
        if (success) {
            showAlert("User added successfully.");
            clearSelection();
            loadUsers();
        } else {
            showAlert("Failed to add user.");
        }
    }

    @FXML
    private void handleRemoveUser() {
        String selectedText = userList.getSelectionModel().getSelectedItem();
        if (selectedText == null) {
            showAlert("No user selected.");
            return;
        }

        String username = selectedText.replace(" (Admin)", "");
        if (username.equals(currentUser)) {
            showAlert("You cannot remove yourself.");
            return;
        }

        removeUser(username);
    }

    private void removeUser(String username) {
        try {
            HttpRequest getRequest = HttpRequest.newBuilder().uri(URI.create(DATABASE_URL)).GET().build();
            HttpResponse<String> response = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (body == null || body.equals("null")) {
                showAlert("No users found.");
                return;
            }

            String userKey = extractUserKey(body, username);
            if (userKey == null) {
                showAlert("User not found.");
                return;
            }

            String deleteUrl = DATABASE_URL.replace(".json", "/" + userKey + ".json");
            HttpRequest deleteRequest = HttpRequest.newBuilder().uri(URI.create(deleteUrl)).DELETE().build();
            httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

            Platform.runLater(() -> {
                showAlert("User removed.");
                clearSelection();
                loadUsers();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleToggleAdmin() {
        String selectedText = userList.getSelectionModel().getSelectedItem();
        if (selectedText == null) {
            showAlert("No user selected.");
            return;
        }

        String username = selectedText.replace(" (Admin)", "");
        if (username.equals(currentUser)) {
            showAlert("Cannot toggle your own admin status.");
            return;
        }

        Map<String, Object> data = userMap.get(username);
        if (data == null) return;

        boolean isAdmin = (boolean) data.getOrDefault("isAdmin", false);
        boolean newAdminStatus = !isAdmin;

        boolean success = FirebaseConnection.updateAdminStatus(username, newAdminStatus);
        if (success) {
            showAlert("Admin status updated.");
            clearSelection();
            loadUsers();
        } else {
            showAlert("Failed to update admin status.");
        }
    }

    @FXML
    private void handleReturn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/MainMenu.fxml"));
            Parent root = loader.load();

            // Pass current user to MainMenuController
            MainMenuController controller = loader.getController();
            controller.setStage(stage != null ? stage : (Stage) usernameField.getScene().getWindow());
            controller.setCurrentUser(currentUser, true);

            // Ensure dark background
            if (!root.getStyleClass().contains("dark")) {
                root.getStyleClass().add("dark");
            }

            Scene scene = new Scene(root, 600, 1200);
            String ss = getClass().getResource("/application/styles.css").toExternalForm();
            if (!scene.getStylesheets().contains(ss)) scene.getStylesheets().add(ss);

            Stage targetStage = stage != null ? stage : (Stage) usernameField.getScene().getWindow();
            targetStage.setScene(scene);
            targetStage.setMinWidth(600);
            targetStage.setMinHeight(600);
            targetStage.centerOnScreen();
            targetStage.setTitle("Main Menu");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Failed to return to main menu.");
        }
    }

    private static String extractValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end);
    }

    private String extractUserKey(String json, String username) {
        for (String entry : json.split("},")) {
            if (entry.contains("\"username\":\"" + username + "\"")) {
                int start = entry.indexOf('"') + 1;
                int end = entry.indexOf('"', start);
                return entry.substring(start, end);
            }
        }
        return null;
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
