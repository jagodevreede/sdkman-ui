package io.github.jagodevreede.sdkmanui.install;

import io.github.jagodevreede.sdkman.api.OsHelper;

import java.util.Optional;

public interface UiInstaller {

    static Optional<UiInstaller> getInstance() {
        if (OsHelper.isWindows()) {
            return Optional.of(new WindowsInstaller());
        }
        return Optional.empty();
    }

    void updateScriptAndVersion();

    void checkInstalled();
}
