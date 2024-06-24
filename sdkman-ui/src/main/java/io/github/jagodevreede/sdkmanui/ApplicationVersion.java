package io.github.jagodevreede.sdkmanui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class ApplicationVersion {

    public static ApplicationVersion INSTANCE = new ApplicationVersion();

    private static String version;

    private ApplicationVersion() {
        // hidden constructor as it is a static instance.

        try {
            final List<String> lines = Files.readAllLines(Paths.get(ApplicationVersion.class.getClassLoader().getResource("version.txt").toURI()));
            version = lines.get(0);
        } catch (IOException | URISyntaxException e) {
            version = "unknown";
        }

    }

    public String getVersion() {
        return version;
    }

}
