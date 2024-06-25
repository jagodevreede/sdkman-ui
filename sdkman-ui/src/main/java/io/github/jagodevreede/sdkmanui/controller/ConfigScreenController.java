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

    @FXML
    TextField zipExecutablePath;
    @FXML
    TextField unzipExecutablePath;
    @FXML
    TextField tarExecutablePath;
    @FXML
    Text symlinkState;

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
        final boolean hasSymlink = Boolean.parseBoolean(properties.getProperty("hasSymlink"));

        zipExecutablePath.setText(zipExecutablePropertyPath);
        unzipExecutablePath.setText(unzipExecutablePropertyPath);
        tarExecutablePath.setText(tarExecutablePropertyPath);

        if (canCreateSymlink) {
            if (hasSymlink) {
                symlinkState.setText("connected");
            } else {
                symlinkState.setText("no connection");
            }
        } else {
            symlinkState.setText("n/a");
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

    public void retrySymlinkCreation() throws IOException {
        final SdkManUiPreferences sdkManUiPreferences = SdkManUiPreferences.load();
        if (checkSymlink()) {
            symlinkState.setText("connected");
            sdkManUiPreferences.hasSymlink = true;
        } else {
            symlinkState.setText("no connection");
            sdkManUiPreferences.hasSymlink = false;
        }
    }

    public void saveAndCloseConfigWindow() throws IOException {
        final SdkManUiPreferences sdkManUiPreferences = SdkManUiPreferences.load();
        sdkManUiPreferences.zipExecutable = zipExecutablePath.getText();
        sdkManUiPreferences.unzipExecutable = unzipExecutablePath.getText();
        sdkManUiPreferences.tarExecutable = tarExecutablePath.getText();

        sdkManUiPreferences.save();

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
