module application {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    exports application;
    opens application to javafx.fxml;
}