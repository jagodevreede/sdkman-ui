package io.github.jagodevreede.sdkman.api.files;

import io.github.jagodevreede.sdkman.api.ProgressInformation;

import java.io.File;
import java.io.IOException;

public class ZipDirectory implements CancelableTask {
    private final File outputFile;
    private boolean cancelled;
    private ProgressInformation progressInformation;

    public ZipDirectory(File outputFile) throws IOException {
        this.outputFile = outputFile;
    }

    public void zip(File folderToZip) throws IOException {

    }

    private boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

}