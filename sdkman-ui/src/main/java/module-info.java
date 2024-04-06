module sdkmanui {
    requires javafx.controls;
    requires javafx.fxml;
    requires sdkmanapi;

    exports io.github.jagodevreede.sdkmanui;
    opens io.github.jagodevreede.sdkmanui to javafx.fxml;
    opens io.github.jagodevreede.sdkmanui.view to javafx.base;
}