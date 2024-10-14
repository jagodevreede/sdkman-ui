package io.github.jagodevreede.sdkman.api.http;

import io.github.jagodevreede.sdkman.api.ProgressInformation;
import io.github.jagodevreede.sdkman.api.files.CancelableTask;
import io.github.jagodevreede.sdkman.api.files.PostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static io.github.jagodevreede.sdkman.api.OsHelper.isWindows;

public class DownloadTask implements CancelableTask {
    private static final Logger logger = LoggerFactory.getLogger(DownloadTask.class);
    private final String url;
    private final File tempFile;
    private final File destination;
    private final String identifier;
    private boolean cancelled = false;
    private ProgressInformation progressInformation;

    public DownloadTask(String url, File tempFile, File destination, String identifier) {
        this.url = url;
        this.tempFile = tempFile;
        this.destination = destination;
        this.identifier = identifier;
        // Create folders if this is the first download we do
        tempFile.getParentFile().mkdirs();
        destination.getParentFile().mkdirs();
        // remove any old tempfile if there
        tempFile.delete();
    }

    public void download() {
        if (destination.exists()) {
            logger.debug("File already exists: {}", destination.getAbsolutePath());
            return;
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL(this.url);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IllegalStateException("Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            final int fileLength = connection.getContentLength();
            try (InputStream input = connection.getInputStream(); OutputStream output = new FileOutputStream(tempFile)) {
                int lastUpdatedProgress = 0;
                byte[] data = new byte[16 * 1024]; // 16 KB
                int total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        return;
                    }
                    total += count;
                    if (fileLength > 0 && progressInformation != null) { // only if total length is known
                        int current = (total / 1024) * 100 / (fileLength/ 1024); // calculate using kb not bytes as we can overflow
                        // Don't over update, as every update is done in a separate thread (ui thread)
                        // So only update whole percentage
                        if (current > lastUpdatedProgress) {
                            lastUpdatedProgress = current;
                            progressInformation.publishProgress(current);
                        }
                    }
                    output.write(data, 0, count);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error in download: " + this.url, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (cancelled) {
                destination.delete();
                tempFile.delete();
            }
        }
        if (tempFile.exists()) {
            try {
                logger.debug("Downloaded {} to {}", this.url, tempFile.getAbsolutePath());
                postProcess();
                progressInformation.publishState("Moving download to destination");
                // Windows doesn't like atomic file moves, as it keeps the file in use
                if (isWindows()) {
                    Files.copy(tempFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    tempFile.delete();
                } else {
                    Files.move(tempFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Unable to move downloaded file to final destination", e);
            }
        }
    }

    private void postProcess() throws IOException {
        PostProcessor postProcessor = new PostProcessor(progressInformation);
        postProcessor.postProcess(tempFile, identifier);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }

    public void setProgressInformation(ProgressInformation progressInformation) {
        this.progressInformation = progressInformation;
    }
}
