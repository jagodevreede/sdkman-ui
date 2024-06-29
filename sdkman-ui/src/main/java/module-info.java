module sdkmanui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires sdkmanapi;
    requires org.slf4j;
    requires org.slf4j.simple;
    requires jdk.crypto.cryptoki;
    requires java.desktop;
    requires com.sun.jna.platform;
    requires java.net.http;
    requires com.google.gson;

    exports io.github.jagodevreede.sdkmanui;
    opens io.github.jagodevreede.sdkmanui to javafx.fxml;
    opens io.github.jagodevreede.sdkmanui.view to javafx.base;
    exports io.github.jagodevreede.sdkmanui.controller;
    opens io.github.jagodevreede.sdkmanui.controller to javafx.fxml;
}