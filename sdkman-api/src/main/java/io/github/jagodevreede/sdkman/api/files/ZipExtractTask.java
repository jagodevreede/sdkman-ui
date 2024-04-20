package io.github.jagodevreede.sdkman.api.files;

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

    @Override
    public void extract() {
        try (FileInputStream fis = new FileInputStream(zipFile);
             ZipArchiveInputStream zis = new ZipArchiveInputStream(fis)) {
            extract(zis, fis.getChannel());
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static void main(String[] args) throws IOException {
        var out = new File("/Users/jagodevreede/.sdkman/archives/test");
        FileUtil.deleteRecursively(out);
        //new ZipExtractTask(new File("/Users/jagodevreede/.sdkman/archives/java-8.0.292.hs-adpt.zip"), out).extract();
        System.out.println(ArchiveType.determineType(new File("/Users/jagodevreede/.sdkman/archives/java-11.0.22-amzn.zip")));
        new TarGzExtractTask(new File("/Users/jagodevreede/.sdkman/archives/java-11.0.22-amzn.zip"), out).extract();
    }
}
