package io.github.jagodevreede.sdkman.api.http;

import io.github.jagodevreede.sdkman.api.ProgressInformation;
import io.github.jagodevreede.sdkman.api.files.ArchiveType;
import io.github.jagodevreede.sdkman.api.files.CancelableTask;
import io.github.jagodevreede.sdkman.api.files.FileUtil;
import io.github.jagodevreede.sdkman.api.files.TarGzExtractTask;
import io.github.jagodevreede.sdkman.api.files.ZipDirectory;
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
import java.util.List;
import java.util.Map;

import static io.github.jagodevreede.sdkman.api.files.ArchiveType.ZIP;

public class DownloadTask implements CancelableTask {
    private static Logger logger = LoggerFactory.getLogger(DownloadTask.class);
    private final String url;
    private final File tempFile;
    private final File destination;
    private boolean cancelled = false;
    private ProgressInformation progressInformation;
    private Map<String, List<String>> headerFields;

    public DownloadTask(String url, File tempFile, File destination) {
        this.url = url;
        this.tempFile = tempFile;
        this.destination = destination;
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
            headerFields = connection.getHeaderFields();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IllegalStateException("Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();
            try (InputStream input = connection.getInputStream();
                 OutputStream output = new FileOutputStream(tempFile)) {

                byte[] data = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return;
                    }
                    total += count;
                    if (fileLength > 0 && progressInformation != null) { // only if total length is known
                        progressInformation.publishProgress((int) (total * 100 / fileLength));
                    }
                    output.write(data, 0, count);
                }
            }

            postProcess();
            progressInformation.publishState("Moving download to destination");
            Files.move(tempFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new IllegalStateException("Error in download", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (cancelled) {
                destination.delete();
            }
        }
    }

    private void postProcess() throws IOException {
        if (ArchiveType.determineType(tempFile) != ZIP) {
            progressInformation.publishState("Post-processing download");
            progressInformation.publishProgress(-1);
            if (ArchiveType.determineType(tempFile) == ArchiveType.TAR_GZ) {
                File tempExtractFolder = new File(tempFile.getParent(), tempFile.getName().replaceAll("\\.", "") + "_extracted");
                TarGzExtractTask extractTask = new TarGzExtractTask(tempFile, tempExtractFolder);
                extractTask.setProgressInformation(progressInformation);
                extractTask.extract();

                progressInformation.publishState("Repackaging download");
                File rootFolder = FileUtil.findRoot(tempExtractFolder, "bin");
                new ZipDirectory(tempFile).zip(rootFolder);

                progressInformation.publishState("Cleanup");
                FileUtil.deleteRecursively(tempExtractFolder);
            } else {
                throw new IllegalStateException("Unknown archive type");
            }
        }
    }

    private boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }

    public Map<String, List<String>> getHeaderFields() {
        return headerFields;
    }

    public void setProgressInformation(ProgressInformation progressInformation) {
        this.progressInformation = progressInformation;
    }
}
