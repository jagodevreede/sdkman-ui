package io.github.jagodevreede.sdkmanui.updater;

import io.github.jagodevreede.sdkmanui.ApplicationVersion;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;

public class UpdateChecker {
    private static final Logger logger = LoggerFactory.getLogger(UpdateChecker.class);
    private final ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE;

    public void checkForUpdate() {
        String instanceVersion = ApplicationVersion.INSTANCE.getVersion();
        Thread updateThread = new Thread(() -> {
            GitHubRelease gitHubRelease = new GitHubRelease(serviceRegistry.getApi().getHttpCacheFolder(), HttpClient.newHttpClient());
            try {
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
}
