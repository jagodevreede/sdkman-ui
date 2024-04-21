package io.github.jagodevreede.sdkman.api.files;

import io.github.jagodevreede.sdkman.api.SdkManUiPreferences;

import java.io.File;
import java.io.IOException;

public class TarGzExtractTask {

    private TarGzExtractTask() {
        // no instantiation
    }

    public static void extract(File zipFile, File destination) {
        // tar zxf "$binary_input" -C "$work_dir"
        try {
            String tarExecutable = SdkManUiPreferences.load().tarExecutable;
            FileUtil.deleteRecursively(destination);
            destination.mkdirs();
            ProcessStarter.run(tarExecutable, "zxf", zipFile.getAbsolutePath(), "-C", destination.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
