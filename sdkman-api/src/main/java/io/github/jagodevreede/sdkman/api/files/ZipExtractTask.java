package io.github.jagodevreede.sdkman.api.files;

import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkman.api.SdkManUiPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;

public final class ZipExtractTask {
    private static Logger logger = LoggerFactory.getLogger(ZipExtractTask.class);

    private ZipExtractTask() {
        // no instantiation
    }

    public static void extract(File zipFile, File destination) {
        final SdkManUiPreferences sdkManUiPreferences = SdkManUiPreferences.getInstance();
        try {
            String unzipExecutable = sdkManUiPreferences.unzipExecutable;
            File tempDir = new File(SdkManApi.DEFAULT_SDKMAN_HOME, "tmp/out");
            FileUtil.deleteRecursively(tempDir);
            FileUtil.deleteRecursively(destination);
            tempDir.mkdirs();

            // unzip -oq "${SDKMAN_DIR}/tmp/${candidate}-${version}.zip" -d "${SDKMAN_DIR}/tmp/out"
            ProcessStarter.run(unzipExecutable, "-oq", zipFile.getAbsolutePath(), "-d", tempDir.getAbsolutePath());

            File sourceExtractedFolder = tempDir.listFiles()[0];
            destination.getParentFile().mkdirs();
            FileUtil.makeAccessible(sourceExtractedFolder);
            try {
                Files.move(sourceExtractedFolder.toPath(), destination.toPath());
            } catch (AccessDeniedException accessDeniedException) {
                logger.info("Could not move reverting to copy {}", accessDeniedException.getMessage());
                FileUtil.copyDirectory(sourceExtractedFolder.getAbsolutePath(), destination.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (!sdkManUiPreferences.keepDownloadsAvailable) {
                zipFile.delete();
            }
        }
    }
}
