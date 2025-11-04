package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainMenu extends Application {
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/MainMenu.fxml"));
            Parent root = loader.load();

            MainMenuController controller = loader.getController();
            controller.setStage(stage);

            Scene scene = new Scene(root, 600, 400);

            // Apply the stylesheet first
            scene.getStylesheets().add(getClass().getResource("/application/styles.css").toExternalForm());

            // Add dark theme to the root (this fixes the white background on launch)
            if (!root.getStyleClass().contains("dark")) {
                root.getStyleClass().add("dark");
            }

            stage.setTitle("Main Menu");
            stage.setScene(scene);
            stage.setMinWidth(600);
            stage.setMinHeight(400);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
