package io.github.jagodevreede.sdkman.api.http;

import io.github.jagodevreede.sdkman.api.ProgressInformation;
import io.github.jagodevreede.sdkman.api.files.FileUtil;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

class ExtractTask<E extends ArchiveEntry, T extends ArchiveInputStream<E>> implements CancelableTask {
    private static final Logger log = LoggerFactory.getLogger(ExtractTask.class);
    private final File destination;
    private boolean cancelled = false;
    private ProgressInformation progressInformation;

    protected ExtractTask(File destination) {
        this.destination = destination;
        this.destination.mkdirs();
    }

    protected void extract(T archiveInputStream, FileChannel channel) throws IOException {
        int unzippedFiles = 0;
        final int totalLength = archiveInputStream.available();
        int totalRead = 0;
        E ze;
        while ((ze = archiveInputStream.getNextEntry()) != null) {
            File f = new File(destination.getCanonicalPath(), ze.getName());
            if (ze.isDirectory()) {
                f.mkdirs();
                continue;
            }
            f.getParentFile().mkdirs();

            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f))) {
                unzippedFiles++;
                final byte[] buf = new byte[4096];
                int bytesRead;

                while (-1 != (bytesRead = archiveInputStream.read(buf))) {
                    if (isCancelled()) {
                        fos.close();
                        FileUtil.deleteRecursively(destination);
                        return;
                    }
                    fos.write(buf, 0, bytesRead);
                    if (progressInformation != null) {
                        progressInformation.publishProgress((int) (channel.position() * 100 / totalLength));
                    }
                }
            } catch (final IOException ioe) {
                f.delete();
                throw ioe;
            }
            if (unzippedFiles == 0) {
                throw new IllegalStateException("No files were unzipped");
            }
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
