package io.github.jagodevreede.sdkman.api.http;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ZipExtractTask extends ExtractTask<ZipArchiveEntry, ZipArchiveInputStream> {
    private final File zipFile;

    public ZipExtractTask(File zipFile, File destination) {
        super(destination);
        this.zipFile = zipFile;
    }

    public void unzip() {
        try (FileInputStream fis = new FileInputStream(zipFile);
             ZipArchiveInputStream zis = new ZipArchiveInputStream(fis)) {
            extract(zis, fis.getChannel());
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
