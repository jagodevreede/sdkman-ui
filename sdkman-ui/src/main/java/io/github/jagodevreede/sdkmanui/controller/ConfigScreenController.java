package io.github.jagodevreede.sdkmanui.controller;

import static io.github.jagodevreede.sdkman.api.OsHelper.isWindows;
import static io.github.jagodevreede.sdkman.api.SdkManUiPreferences.PROPERTY_LOCATION;
import static io.github.jagodevreede.sdkmanui.ConfigurationUtil.checkSymlink;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import io.github.jagodevreede.sdkman.api.SdkManUiPreferences;
import io.github.jagodevreede.sdkman.api.files.ProcessStarter;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ConfigScreenController implements Initializable {
    final String SYMLINK_CAPABLE = "Capable";
    final String SYMLINK_NOT_CAPABLE = "Not capable";

    @FXML
    TextField zipExecutablePath;
    @FXML
    TextField unzipExecutablePath;
    @FXML
    TextField tarExecutablePath;
    @FXML
    Text symlinkCapability;

    @FXML
    Button closeConfigButton;

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
        final boolean canCreateSymlink = Boolean.parseBoolean(properties.getProperty("canCreateSymlink"));

        zipExecutablePath.setText(zipExecutablePropertyPath);
        unzipExecutablePath.setText(unzipExecutablePropertyPath);
        tarExecutablePath.setText(tarExecutablePropertyPath);

        if (canCreateSymlink) {
            symlinkCapability.setText(SYMLINK_CAPABLE);
        } else {
            symlinkCapability.setText(SYMLINK_NOT_CAPABLE);
        }
    }

    public void browseZipExecutablePath() {
        final String path = this.browsePath("zip", new Stage());
        zipExecutablePath.setText(path);
    }

    public void browseUnzipExecutablePath() {
        final String path = this.browsePath("unzip", new Stage());
        unzipExecutablePath.setText(path);
    }

    public void browseTarExecutablePath() {
        final String path = this.browsePath("tar", new Stage());
        tarExecutablePath.setText(path);
    }

    public void checkSymlinkCapability() {
        final SdkManUiPreferences sdkManUiPreferences = ServiceRegistry.INSTANCE.getSdkManUiPreferences();
        if (checkSymlink()) {
            symlinkCapability.setText(SYMLINK_CAPABLE);
            sdkManUiPreferences.canCreateSymlink = true;
        } else {
            symlinkCapability.setText(SYMLINK_NOT_CAPABLE);
            sdkManUiPreferences.canCreateSymlink = false;
        }
    }

    public void saveAndCloseConfigWindow() {
        final SdkManUiPreferences sdkManUiPreferences = ServiceRegistry.INSTANCE.getSdkManUiPreferences();

        sdkManUiPreferences.zipExecutable = zipExecutablePath.getText();
        sdkManUiPreferences.unzipExecutable = unzipExecutablePath.getText();
        sdkManUiPreferences.tarExecutable = tarExecutablePath.getText();

        try {
            sdkManUiPreferences.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.closeConfigWindow();
    }

    public void closeConfigWindow() {
        Stage stage = (Stage) closeConfigButton.getScene().getWindow();
        stage.close();
    }

    private String browsePath(String command, Stage stage) {
        FileChooser fileChooser = new FileChooser();

        if (isWindows()) {
            fileChooser.setInitialDirectory(new File("./"));
        } else {
            fileChooser.setInitialDirectory(new File("/usr/bin"));
        }
        fileChooser.setTitle("Where is the " + command + " executable");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(command, command + (isWindows() ? ".exe" : "")));
        fileChooser.setInitialFileName(command);
        File file = fileChooser.showOpenDialog(stage);
        if (file == null || !ProcessStarter.testIfAvailable(file.getAbsolutePath())) {
            String name = file != null ? file.getAbsolutePath() : command;
            ServiceRegistry.INSTANCE.getPopupView().showInformation("Failed to verify " + name, Alert.AlertType.INFORMATION);
            return null;
        }
        return file.getAbsolutePath();
    }
}
