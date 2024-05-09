module sdkmanui {
    requires javafx.controls;
    requires javafx.fxml;
    requires sdkmanapi;
    requires org.slf4j;
    requires org.slf4j.simple;
    requires jdk.crypto.cryptoki;
    requires java.desktop;

    exports io.github.jagodevreede.sdkmanui;
    opens io.github.jagodevreede.sdkmanui to javafx.fxml;
    opens io.github.jagodevreede.sdkmanui.view to javafx.base;
}