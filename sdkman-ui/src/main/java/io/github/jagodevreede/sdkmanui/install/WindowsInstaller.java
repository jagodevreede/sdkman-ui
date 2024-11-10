package io.github.jagodevreede.sdkmanui.install;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

class WindowsInstaller extends UiInstaller {

    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    @Override
    public void updateScriptAndVersion() {
        // REPLACE_EXISTING seems to fail on windows, so remove and copy
        new File(installFolder, "sdkui.cmd").delete();
        new File(installFolder, "update.cmd").delete();
        new File(installFolder, "version.txt").delete();
        try {
            Files.copy(UiInstaller.class.getClassLoader()
                    .getResourceAsStream("sdkui.cmd"), new File(installFolder, "sdkui.cmd").toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(UiInstaller.class.getClassLoader()
                    .getResourceAsStream("update.cmd"), new File(installFolder, "update.cmd").toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(UiInstaller.class.getClassLoader()
                    .getResourceAsStream("version.txt"), new File(installFolder, "version.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            SERVICE_REGISTRY.getPopupView().showError(e);
        }
    }
}
