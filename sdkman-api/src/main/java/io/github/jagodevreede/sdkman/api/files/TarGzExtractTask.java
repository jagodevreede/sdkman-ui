package io.github.jagodevreede.sdkman.api.files;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TarGzExtractTask extends ExtractTask<TarArchiveEntry, TarArchiveInputStream> {
    private final File zipFile;

    public TarGzExtractTask(File zipFile, File destination) {
        super(destination);
        this.zipFile = zipFile;
    }

    @Override
    public void extract() {
        try (FileInputStream fis = new FileInputStream(zipFile);
             TarArchiveInputStream zis = new TarArchiveInputStream(new GzipCompressorInputStream(fis))) {
            extract(zis, fis.getChannel());
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
