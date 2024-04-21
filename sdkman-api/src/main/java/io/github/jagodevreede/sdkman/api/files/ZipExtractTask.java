package io.github.jagodevreede.sdkman.api.files;

import io.github.jagodevreede.sdkman.api.SdkManApi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class ZipExtractTask {

    private ZipExtractTask() {
        // no instantiation
    }

    public static void extract(File zipFile, File destination) {
        try {
            File tempDir = new File(SdkManApi.DEFAULT_SDKMAN_HOME, "tmp/out");
            FileUtil.deleteRecursively(tempDir);
            FileUtil.deleteRecursively(destination);
            tempDir.mkdirs();

            // unzip -oq "${SDKMAN_DIR}/tmp/${candidate}-${version}.zip" -d "${SDKMAN_DIR}/tmp/out"
            ProcessStarter.run("unzip", "-oq", zipFile.getAbsolutePath(), "-d", tempDir.getAbsolutePath());

            File sourceExtractedFolder = tempDir.listFiles()[0];
            Files.move(sourceExtractedFolder.toPath(), destination.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
