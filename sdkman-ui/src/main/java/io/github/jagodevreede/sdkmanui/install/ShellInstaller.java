package io.github.jagodevreede.sdkmanui.install;

import io.github.jagodevreede.sdkmanui.ApplicationVersion;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ShellInstaller extends UiInstaller {
    private static final Logger logger = LoggerFactory.getLogger(ShellInstaller.class);
    private static final ServiceRegistry SERVICE_REGISTRY = ServiceRegistry.INSTANCE;
    private final File installFolder = new File(SERVICE_REGISTRY.getApi().getBaseFolder(), "ui");

    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    @Override
    public void updateScriptAndVersion() {
        try {
            Files.copy(ApplicationVersion.class.getClassLoader()
                    .getResourceAsStream("sdkui.sh"), new File(installFolder, "sdkui.sh").toPath(), StandardCopyOption.REPLACE_EXISTING);
            new File(installFolder, "sdkui.sh").setExecutable(true, false);
            Files.copy(ApplicationVersion.class.getClassLoader()
                    .getResourceAsStream("update.sh"), new File(installFolder, "update.sh").toPath(), StandardCopyOption.REPLACE_EXISTING);
            new File(installFolder, "update.sh").setExecutable(true, false);
            Files.copy(ApplicationVersion.class.getClassLoader()
                    .getResourceAsStream("version.txt"), new File(installFolder, "version.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            SERVICE_REGISTRY.getPopupView().showError(e);
        }
    }
}
