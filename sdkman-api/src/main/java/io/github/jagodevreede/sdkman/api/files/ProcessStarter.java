package io.github.jagodevreede.sdkman.api.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessStarter {
    private static final Logger log = LoggerFactory.getLogger(ProcessStarter.class);

    public static String streamToString(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = br.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString().trim();
    }

    public static String runInGetOutput(File workingFolder, String... args) throws IOException {
        log.debug("Running in {}: {}", workingFolder, String.join(" ", args));
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(workingFolder);
        pb.environment().put("LANG", "en_US.UTF-8");
        pb.environment().put("LC_ALL", "en_US.UTF-8");
        Process process = pb.start();
        String stdOut = streamToString(process.getInputStream());
        String stdErr = streamToString(process.getErrorStream());

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        if (!stdErr.isEmpty()) {
            throw new IllegalStateException("Failure during extraction: " + stdErr);
        }
        return stdOut;
    }

    public static void runIn(File workingFolder, String... args) throws IOException {
        String stdOut = runInGetOutput(workingFolder, args);
        if (!stdOut.isEmpty()) {
            throw new IllegalStateException("Failure during extraction: " + stdOut);
        }
    }

    public static void run(String... args) throws IOException {
        runIn(new File("."), args);
    }

    public static boolean testIfAvailable(String command) {
        ProcessBuilder pb = new ProcessBuilder(command, "--version");
        try {
            Process process = pb.start();
            streamToString(process.getInputStream());
            streamToString(process.getErrorStream());

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            return false;
        }
        return true;
    }
}
