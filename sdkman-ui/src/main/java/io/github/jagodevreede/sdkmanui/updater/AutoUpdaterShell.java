package io.github.jagodevreede.sdkmanui.updater;

import io.github.jagodevreede.sdkman.api.files.ZipExtractTask;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

class AutoUpdaterShell extends AutoUpdater {
    AutoUpdaterShell() {

    }

    @Override
    public Optional<String> getDownloadUrl(List<String> listOfUrls) {
        return listOfUrls.stream().filter(url -> url.contains(getPlatformIdentifier()) && url.contains("zip")).findFirst();
    }

    @Override
    public void finalizeUpdate(File tempFile) {
        ZipExtractTask.extract(tempFile, getUpdateFile());
        File installFolder = new File(serviceRegistry.getApi().getBaseFolder(), "ui");
        ProcessBuilder builder = new ProcessBuilder("bash", "update.sh");
        builder.directory(installFolder);
        try {
            builder.start();
            Platform.exit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public File getUpdateFile() {
        return new File(serviceRegistry.getApi().getBaseFolder(), "ui/sdkman-ui-update");
    }

}
