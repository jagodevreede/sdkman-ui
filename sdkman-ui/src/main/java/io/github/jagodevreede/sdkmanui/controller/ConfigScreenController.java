package io.github.jagodevreede.sdkmanui.controller;

import static io.github.jagodevreede.sdkman.api.SdkManUiPreferences.PROPERTY_LOCATION;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class ConfigScreenController implements Initializable {

    @FXML
    TextField zipExecutablePath;
    @FXML
    TextField unzipExecutablePath;
    @FXML
    TextField tarExecutablePath;

    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(PROPERTY_LOCATION));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final String zipExecutablePropertyPath = properties.getProperty("zipExecutable");
        final String unzipExecutablePropertyPath = properties.getProperty("unzipExecutable");
        final String tarExecutablePropertyPath = properties.getProperty("tarExecutable");
        zipExecutablePath.setText(zipExecutablePropertyPath);
        unzipExecutablePath.setText(unzipExecutablePropertyPath);
        tarExecutablePath.setText(tarExecutablePropertyPath);

    }

}
