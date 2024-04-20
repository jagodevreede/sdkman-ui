package io.github.jagodevreede.sdkman.api.files;

import io.github.jagodevreede.sdkman.api.OsHelper;
import io.github.jagodevreede.sdkman.api.ProgressInformation;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

abstract class ExtractTask<E extends ArchiveEntry, T extends ArchiveInputStream<E>> implements CancelableTask {
    private static final Logger log = LoggerFactory.getLogger(ExtractTask.class);
    private final File destination;
    private boolean cancelled = false;
    private ProgressInformation progressInformation;

    protected ExtractTask(File destination) {
        this.destination = destination;
        this.destination.mkdirs();
    }

    abstract void extract();

    protected void extract(T archiveInputStream, FileChannel channel) throws IOException {
        int unzippedFiles = 0;
        E ze;
        boolean isFirst = true;
        String topFolder = destination.getName();
        while ((ze = archiveInputStream.getNextEntry()) != null) {
            if (isFirst) {
                topFolder = ze.getName();
                isFirst = false;
                continue;
            }
            File f = new File(destination.getCanonicalPath(), removeTopFolder(ze.getName(), topFolder));

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
                        progressInformation.publishProgress((int) (channel.position() * 100 / channel.size()));
                    }
                }
                if (OsHelper.hasShell()) {

                 //   Files.setPosixFilePermissions(f.toPath(), PermissionUtils.permissionsFromMode(ze.getUnixMode()));
                    if (ze instanceof ZipArchiveEntry zae) {
                            System.out.println(ze.getName() + " " + ZipExtraInformation.fromField(zae.getExtra()));
                    }
                    if (ze instanceof TarArchiveEntry zae) {
                        System.out.println(ze.getName() + " " + zae.getMode());
                        // 420 is normaal     110100100    -rwxr-xr-
                        // 292 is md??        100100100
                        // 493 is LICIENSE    111101101
                        // 493 is /bin/java
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

    // 5554090003df417f60b09f5861757806000102f5010114
    // 555409000347417f60b09f5861757806000102f5010114

    private String removeTopFolder(String name, String topFolder) {
        if (name.startsWith(topFolder)) {
            return name.substring(topFolder.length());
        }
        return name;
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
