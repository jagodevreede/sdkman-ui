package io.github.jagodevreede.sdkman.api.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessStarter {
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

    public static void runIn(File workingFolder, String... args) throws IOException {
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

        if (!stdOut.isEmpty()) {
            throw new IllegalStateException("Failure during extraction: " + stdOut);
        }
        if (!stdErr.isEmpty()) {
            throw new IllegalStateException("Failure during extraction: " + stdErr);
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
