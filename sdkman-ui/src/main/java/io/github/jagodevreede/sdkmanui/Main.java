package io.github.jagodevreede.sdkmanui;

import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkman.api.SdkManUiPreferences;
import io.github.jagodevreede.sdkman.api.files.ProcessStarter;
import io.github.jagodevreede.sdkmanui.service.GlobalExceptionHandler;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static io.github.jagodevreede.sdkman.api.OsHelper.isWindows;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage stage) throws Exception {
        logger.debug("Starting SDKMAN UI");
        if (!preCheck(stage)) {
            logger.warn("Failed pre-check");
            return;
        }
        ServiceRegistry.INSTANCE.getApi().registerShutdownHook();

        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());

        URL mainFxml = Main.class.getClassLoader().getResource("main.fxml");
        Parent root = FXMLLoader.load(mainFxml);

        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/sdkman_ui_logo.png"))));

        Scene scene = new Scene(root, 800, 580);
        stage.setResizable(false);

        stage.setTitle("SDKMAN UI");
        stage.setScene(scene);
        stage.show();
    }

    private boolean preCheck(Stage stage) throws IOException {
        SdkManUiPreferences sdkManUiPreferences = ServiceRegistry.INSTANCE.getSdkManUiPreferences();
        if (!sdkManUiPreferences.donePreCheck) {
            String unzipExecutable = testExecutable("unzip", stage);
            if (unzipExecutable == null) {
                return false;
            }
            String zipExecutable = testExecutable("zip", stage);
            if (zipExecutable == null) {
                return false;
            }
            if (!isWindows()) {
                // other than windows can create symlinks
                sdkManUiPreferences.canCreateSymlink = true;
                String tarExecutable = testExecutable("tar", stage);
                if (tarExecutable == null) {
                    return false;
                }
                sdkManUiPreferences.tarExecutable = tarExecutable;
            } else {
                sdkManUiPreferences.canCreateSymlink = checkSymlink();
            }
            sdkManUiPreferences.unzipExecutable = unzipExecutable;
            sdkManUiPreferences.zipExecutable = zipExecutable;
            sdkManUiPreferences.donePreCheck = true;
            sdkManUiPreferences.save();
        }
        return true;
    }

    private boolean checkSymlink() {
        File sourceFolder = new File(SdkManApi.DEFAULT_SDKMAN_HOME, "/tmp/src");
        sourceFolder.mkdirs();
        File targetFolder = new File(SdkManApi.DEFAULT_SDKMAN_HOME, "/tmp/target");
        if (targetFolder.exists()) {
            targetFolder.delete();
        }
        try {
            Files.createSymbolicLink(sourceFolder.toPath(), targetFolder.toPath());
            return true;
        } catch (IOException e) {
            logger.trace("Failed to create symlink", e);
            logger.info("Unable to make symlinks, using copies instead");
        } finally {
            targetFolder.delete();
            sourceFolder.delete();
        }
        return false;
    }

    public String testExecutable(String command, Stage stage) {
        logger.debug("Testing for {}", command);
        if (!ProcessStarter.testIfAvailable(command)) {
            logger.info("{} is not on path", command);
            ServiceRegistry.INSTANCE.getPopupView().showInformation(command + " is not available,\n" +
                    "Please install it or in the next dialog point to where it is.");
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
        return command;
    }

    public static void main(String[] args) {
        launch(args);
    }
}