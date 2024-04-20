package io.github.jagodevreede.sdkman.api.files;

import io.github.jagodevreede.sdkman.api.SdkManApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

public final class ZipExtractTask {

    public static void extract(File zipFile, File destination) {
        try {
            File tempDir = new File(SdkManApi.DEFAULT_SDKMAN_HOME, "tmp/out");
            FileUtil.deleteRecursively(tempDir);
            FileUtil.deleteRecursively(destination);
            tempDir.mkdirs();

            // unzip -oq "${SDKMAN_DIR}/tmp/${candidate}-${version}.zip" -d "${SDKMAN_DIR}/tmp/out"
            Process process = new ProcessBuilder("unzip", "-oq", zipFile.getAbsolutePath(), "-d", tempDir.getAbsolutePath()).start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }

            if (!stringBuilder.toString().trim().isEmpty()) {
                throw new IllegalStateException("Failure during extraction: " + stringBuilder.toString());
            }

            File sourceExtractedFolder = tempDir.listFiles()[0];

            Files.move(sourceExtractedFolder.toPath(), destination.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        var out = new File("/Users/jagodevreede/.sdkman/archives/test");
        FileUtil.deleteRecursively(out);
        ZipExtractTask.extract(new File("/Users/jagodevreede/.sdkman/archives/java-8.0.292.hs-adpt.zip"), out);
        //System.out.println(ArchiveType.determineType(new File("/Users/jagodevreede/.sdkman/archives/java-11.0.22-amzn.zip")));
        //new TarGzExtractTask(new File("/Users/jagodevreede/.sdkman/archives/java-11.0.22-amzn.zip"), out).extract();
    }
}
