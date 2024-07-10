package io.github.jagodevreede.sdkman.api.files;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public enum ArchiveType {
    ZIP, TAR_GZ, TAR;

    public ArchiveInputStream<?> getInputStream(File file) throws IOException {
        return switch (this) {
            case ZIP -> new ZipArchiveInputStream(new FileInputStream(file));
            case TAR_GZ -> new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
            case TAR -> new TarArchiveInputStream(new FileInputStream(file));
        };
    }

    public static ArchiveType determineType(File file) throws IOException {
        for (ArchiveType archiveType : ArchiveType.values()) {
            if (tryRead(archiveType.getInputStream(file))) {
                return archiveType;
            }
        }
        throw new IllegalStateException("Unknown archive type");
    }

    private static boolean tryRead(ArchiveInputStream<?> inputStream) {
        try {
            inputStream.getNextEntry();
            return true;
        } catch (IOException e) {
            // Not a tar file then...
        }
        return false;
    }
}
