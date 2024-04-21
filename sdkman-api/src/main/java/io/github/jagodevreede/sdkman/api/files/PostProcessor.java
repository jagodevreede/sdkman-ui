package io.github.jagodevreede.sdkman.api.files;

import io.github.jagodevreede.sdkman.api.ProgressInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.github.jagodevreede.sdkman.api.files.ArchiveType.ZIP;

public class PostProcessor implements CancelableTask {
    private static final Logger logger = LoggerFactory.getLogger(PostProcessor.class);
    private final ProgressInformation progressInformation;
    private boolean canceled;

    public PostProcessor(ProgressInformation progressInformation) {
        this.progressInformation = progressInformation;
    }

    public void postProcess(File tempFile, String identifier) throws IOException {
        if (ArchiveType.determineType(tempFile) == ZIP) {
            logger.debug("Not post-processing zip file: {}", tempFile.getName());
            return;
        }
        publishState("Post-processing download");
        if (ArchiveType.determineType(tempFile) != ArchiveType.TAR_GZ) {
            logger.error("Got a {} but was expecting a tar.gz as a intermediate file", ArchiveType.determineType(tempFile));
            throw new IllegalStateException("Unknown archive type");
        }

        processTarGz(tempFile, identifier);
    }

    private void processTarGz(File tempFile, String identifier) throws IOException {
        File tempExtractFolder = new File(tempFile.getParent(), tempFile.getName().replaceAll("\\.", "") + "_extracted");
        try {
            TarGzExtractTask.extract(tempFile, tempExtractFolder);

            if (isCancelled()) {
                return;
            }

            publishState("Repackaging download");
            File rootFolder = FileUtil.findRoot(tempExtractFolder, "bin");
            File newRootFolder = new File(rootFolder.getParentFile(), identifier);
            Files.move(rootFolder.toPath(), newRootFolder.toPath());
            ZipDirectory.zip(newRootFolder, tempFile);
        } finally {
            publishState("Cleanup");
            FileUtil.deleteRecursively(tempExtractFolder);
        }
    }

    private void publishState(String state) {
        logger.debug("Now in state: {}", state);
        if (progressInformation != null) {
            progressInformation.publishState(state);
            progressInformation.publishProgress(-1);
        }
    }

    private boolean isCancelled() {
        return canceled;
    }

    @Override
    public void cancel() {
        canceled = true;
    }
}
