package io.github.jagodevreede.sdkman.api.http;

import io.github.jagodevreede.sdkman.api.ProgressInformation;
import io.github.jagodevreede.sdkman.api.files.FileUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipTask implements CancelableTask {
    private final File zipFile;
    private final File destination;
    private boolean cancelled = false;
    private ProgressInformation progressInformation;

    public UnzipTask(File zipFile, File destination) {
        this.zipFile = zipFile;
        this.destination = destination;
        this.destination.mkdirs();
    }

    public void unzip() {
        int unzippedFiles = 0;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            final int totalLength = zis.available();
            int totalRead = 0;
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                File f = new File(destination.getCanonicalPath(), ze.getName());
                if (ze.isDirectory()) {
                    f.mkdirs();
                    continue;
                }
                f.getParentFile().mkdirs();

                try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f))) {
                    unzippedFiles++;
                    final byte[] buf = new byte[1024];
                    int bytesRead;

                    while (-1 != (bytesRead = zis.read(buf))) {
                        if (isCancelled()) {
                            fos.close();
                            FileUtil.deleteRecursively(destination);
                            return;
                        }
                        fos.write(buf, 0, bytesRead);
                        totalRead += bytesRead;
                        if (progressInformation != null) {
                            progressInformation.publishProgress(totalRead * 100 / totalLength);
                        }
                    }

                } catch (final IOException ioe) {
                    f.delete();
                    throw ioe;
                }
            }
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
        if (unzippedFiles == 0) {
            throw new IllegalStateException("No files were unzipped");
        }
    }

    private boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }

    public void setProgressInformation(ProgressInformation progressInformation) {
        this.progressInformation = progressInformation;
    }

}
