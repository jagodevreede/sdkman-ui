package io.github.jagodevreede.sdkmanui;

import static io.github.jagodevreede.sdkman.api.OsHelper.isWindows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkman.api.SdkManUiPreferences;
import io.github.jagodevreede.sdkman.api.files.ProcessStarter;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ConfigurationUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationUtil.class);

    static boolean preCheck(Stage stage) throws IOException {
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

    static boolean checkSymlink() {
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

    static String testExecutable(String command, Stage stage) {
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
}
