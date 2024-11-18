package io.github.jagodevreede.sdkmanui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ApplicationVersion {

    public static ApplicationVersion INSTANCE = new ApplicationVersion();

    private static String version;
    private static String commitHash;

    private ApplicationVersion() {
        // hidden constructor as it is a static instance.
        version = readFile("version.txt");
        commitHash = readFile("commitHash.txt");
    }

    private static String readFile(String fileName) {
        try {
            InputStream resourceAsStream = ApplicationVersion.class.getClassLoader().getResourceAsStream(fileName);
            return new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "unknown";
        }
    }

    public String getVersion() {
        return version;
    }

    public String getCommitHash() {
        return commitHash;
    }

}
