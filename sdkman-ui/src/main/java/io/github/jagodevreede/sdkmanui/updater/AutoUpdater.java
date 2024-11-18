package io.github.jagodevreede.sdkmanui.updater;

import io.github.jagodevreede.sdkman.api.OsHelper;
import io.github.jagodevreede.sdkman.api.ProgressInformation;
import io.github.jagodevreede.sdkman.api.http.DownloadTask;
import io.github.jagodevreede.sdkmanui.ApplicationVersion;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import io.github.jagodevreede.sdkmanui.service.TaskRunner;
import io.github.jagodevreede.sdkmanui.view.PopupView;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;

import static io.github.jagodevreede.sdkman.api.OsHelper.getOs;
import static io.github.jagodevreede.sdkman.api.OsHelper.isMac;

public abstract class AutoUpdater {
    private static final Logger logger = LoggerFactory.getLogger(AutoUpdater.class);
    protected final ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE;

    public static Optional<AutoUpdater> getInstance() {
        if (OsHelper.isWindows()) {
            return Optional.of(new AutoUpdaterWindows());
        }
        if (OsHelper.hasShell()) {
            return Optional.of(new AutoUpdaterShell());
        }
        return Optional.empty();
    }

    /**
     * Check if an update is available, and returns the version number of the update if it is, or null otherwise.
     */
    public void checkForUpdate() {
        String instanceVersion = ApplicationVersion.INSTANCE.getVersion();
        Thread updateThread = new Thread(() -> {
            try {
                GitHubRelease gitHubRelease = getLatestGitHubRelease();
                String latestRelease = gitHubRelease.getLatestRelease();
                if (latestRelease != null && !latestRelease.equals(instanceVersion)) {
                    serviceRegistry.getMainScreen().setUpdateAvailable(latestRelease);
                }
            } catch (IOException | InterruptedException e) {
                logger.warn("Unable to check for update: {}", e.getMessage());
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private GitHubRelease getLatestGitHubRelease() {
        return new GitHubRelease(serviceRegistry.getApi().getHttpCacheFolder(), HttpClient.newHttpClient());
    }

    abstract public Optional<String> getDownloadUrl(List<String> listOfUrls);

    public void runUpdate() {
        try {
            File tempFile = new File(serviceRegistry.getApi().getBaseFolder(), "tmp/ui-update.tmp");
            File destFile = new File(serviceRegistry.getApi().getBaseFolder(), "tmp/ui-update.bin");
            // Remove old first, as download could be interrupted
            tempFile.delete();
            destFile.delete();

            Optional<String> downloadUrl = getDownloadUrl(getLatestGitHubRelease().getLatestReleaseDownloads());
            if (!downloadUrl.isPresent()) {
                ServiceRegistry.INSTANCE.getPopupView()
                        .showError("Did not find the download url yet please visit the github page, and download the update manually");
                return;
            }
            DownloadTask downloadTask = new DownloadTask(downloadUrl.get(), tempFile, destFile, null);
            PopupView.ProgressWindow progressWindow = ServiceRegistry.INSTANCE.getPopupView()
                    .showProgress("Download of update in progress", downloadTask);
            ProgressInformation progressInformation = new ProgressInformation() {
                @Override
                public void publishProgress(int current) {
                    Platform.runLater(() -> progressWindow.progressBar().setProgress(current / 100.0));
                }

                @Override
                public void publishState(String state) {
                    Platform.runLater(() -> progressWindow.alert().setHeaderText(state));
                }
            };
            downloadTask.setProgressInformation(progressInformation);
            TaskRunner.run(() -> {
                downloadTask.download();

                Platform.runLater(() -> {
                    progressWindow.alert().close();
                    if (!downloadTask.isCancelled()) {
                        finalizeUpdate(tempFile);
                    }
                });
            });
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    abstract protected void finalizeUpdate(File tempFile);

    abstract public File getUpdateFile();

    public String getPlatformIdentifier() {
        String result;
        if (getOs().contains("windows")) {
            result = "windows";
        } else if (isMac()) {
            result = "osx";
        } else {
            result = "linux";
        }

        if (System.getProperty("os.arch").equals("arm64") || System.getProperty("os.arch").equals("aarch64")) {
            result += "_aarch64";
        } else {
            result += "_x86_64";
        }
        return result;
    }
}
