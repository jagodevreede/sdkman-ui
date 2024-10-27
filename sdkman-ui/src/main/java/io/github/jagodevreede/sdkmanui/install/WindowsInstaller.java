package io.github.jagodevreede.sdkmanui.install;

import io.github.jagodevreede.sdkmanui.ApplicationVersion;
import io.github.jagodevreede.sdkmanui.Main;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

class WindowsInstaller implements UiInstaller {
    private static final Logger logger = LoggerFactory.getLogger(WindowsInstaller.class);
    private static final ServiceRegistry SERVICE_REGISTRY = ServiceRegistry.INSTANCE;
    private final File installFolder = new File(SERVICE_REGISTRY.getApi().getBaseFolder(), "ui");

    @Override
    public void updateScriptAndVersion() {
        // REPLACE_EXISTING seems to fail on windows, so remove and copy
        new File(installFolder, "sdkui.cmd").delete();
        new File(installFolder, "update.cmd").delete();
        new File(installFolder, "version.txt").delete();
        try {
            Files.copy(ApplicationVersion.class.getClassLoader()
                    .getResourceAsStream("sdkui.cmd"), new File(installFolder, "sdkui.cmd").toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(ApplicationVersion.class.getClassLoader()
                    .getResourceAsStream("update.cmd"), new File(installFolder, "update.cmd").toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(ApplicationVersion.class.getClassLoader()
                    .getResourceAsStream("version.txt"), new File(installFolder, "version.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            SERVICE_REGISTRY.getPopupView().showError(e);
        }
    }

    @Override
    public void checkInstalled() {
        final String applicationVersion = ApplicationVersion.INSTANCE.getVersion();
        final String currentInstalledUIVersion = SERVICE_REGISTRY.getApi().getCurrentInstalledUIVersion();
        if (!applicationVersion.equals(currentInstalledUIVersion)) {
            logger.info("Running a different UI version {} then the one installed {}", applicationVersion, currentInstalledUIVersion);
            install();
        }
    }

    private void install() {
        try {
            URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
            File currentExecutable = Paths.get(location.toURI()).toFile();
            if (!currentExecutable.isFile()) {
                logger.info("Not running executable, so unable to install");
                // Probably dev mode, or failed to get location, we can't check for installation
                return;
            }
            File currentRunningFolder = currentExecutable.getParentFile();
            if (!currentRunningFolder.equals(installFolder)) {
                File installedExecutable = new File(installFolder, currentExecutable.getName());
                SERVICE_REGISTRY.getPopupView().showConfirmation("Installation", "Do you want to " + (installedExecutable.exists() ? "update" : "install") + " SDKMAN UI?", () -> {
                    try {
                        installFolder.mkdirs();
                        boolean configured = SERVICE_REGISTRY.getApi().configureEnvironmentPath();

                        // REPLACE_EXISTING seems to fail on windows, so remove and copy
                        boolean oldVersion = installedExecutable.delete();
                        Files.copy(currentExecutable.toPath(), installedExecutable.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        updateScriptAndVersion();

                        StringBuilder confirmationMessage = new StringBuilder("SDKMAN UI has been ");
                        if (oldVersion) {
                            confirmationMessage.append("updated");
                        } else {
                            confirmationMessage.append("installed");
                        }
                        String tmpdir = System.getProperty("java.io.tmpdir");
                        if (!currentRunningFolder.getAbsolutePath().startsWith(tmpdir)) {
                            confirmationMessage.append(",\nyou can now remove ");
                            confirmationMessage.append(currentExecutable.getAbsolutePath());
                        }

                        if (configured) {
                            confirmationMessage.append("\nyou need to relogin to be able to use `sdkui` from the command line.");
                        }
                        SERVICE_REGISTRY.getPopupView().showInformation(confirmationMessage.toString());
                    } catch (IOException e) {
                        SERVICE_REGISTRY.getPopupView().showError(e);
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (URISyntaxException e) {
            logger.warn("Failed to check if installed, assuming so");
        }
    }
}
