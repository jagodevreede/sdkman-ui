package io.github.jagodevreede.sdkmanui.install;

import io.github.jagodevreede.sdkman.api.OsHelper;
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
import java.util.Optional;

public abstract class UiInstaller {
    private static final Logger logger = LoggerFactory.getLogger(UiInstaller.class);
    protected final ServiceRegistry SERVICE_REGISTRY = ServiceRegistry.INSTANCE;
    protected final File installFolder = new File(SERVICE_REGISTRY.getApi().getBaseFolder(), "ui");

    public static Optional<UiInstaller> getInstance() {
        if (OsHelper.isWindows()) {
            return Optional.of(new WindowsInstaller());
        }
        if (OsHelper.isMac()) {
            return Optional.of(new ShellInstaller());
        }
        return Optional.empty();
    }

    abstract public void updateScriptAndVersion();

    public void checkInstalled() {
        final String applicationVersion = ApplicationVersion.INSTANCE.getVersion();
        final String currentInstalledUIVersion = SERVICE_REGISTRY.getApi().getCurrentInstalledUIVersion();
        if (!applicationVersion.equals(currentInstalledUIVersion)) {
            logger.info("Running a different UI version {} then the one installed {}", applicationVersion, currentInstalledUIVersion);
            install();
        }
    }

    public void install() {
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
                if (!OsHelper.isWindows() && !new File(SERVICE_REGISTRY.getApi().getBaseFolder()).exists()) {
                    SERVICE_REGISTRY.getPopupView()
                            .showConfirmation("SDKman not installed", "Its highly recommended to install SDKman first before installing SDKman UI\nAre you sure you want to install SDKman UI now?",  () -> doInstall(currentRunningFolder, installedExecutable, currentExecutable));
                } else {
                    SERVICE_REGISTRY.getPopupView()
                            .showConfirmation("Installation", "Do you want to " + (installedExecutable.exists() ? "update" : "install") + " SDKman UI?", () -> doInstall(currentRunningFolder, installedExecutable, currentExecutable));
                }
            }
        } catch (URISyntaxException e) {
            logger.warn("Failed to check if installed, assuming so");
        }
    }

    protected void doInstall(File currentRunningFolder, File installedExecutable, File currentExecutable) {
        try {
            installFolder.mkdirs();
            boolean configured = SERVICE_REGISTRY.getApi().addSkdmanUiToGlobalEnvironmentPath();

            // REPLACE_EXISTING seems to fail on windows, so remove and copy
            boolean oldVersion = installedExecutable.delete();
            Files.copy(currentExecutable.toPath(), installedExecutable.toPath(), StandardCopyOption.REPLACE_EXISTING);
            updateScriptAndVersion();

            StringBuilder confirmationMessage = new StringBuilder("SDKman UI has been ");
            if (oldVersion) {
                confirmationMessage.append("updated");
            } else {
                confirmationMessage.append("installed");
            }
            String tmpDir = System.getProperty("java.io.tmpdir");
            if (!currentRunningFolder.getAbsolutePath().startsWith(tmpDir)) {
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
    }
}
