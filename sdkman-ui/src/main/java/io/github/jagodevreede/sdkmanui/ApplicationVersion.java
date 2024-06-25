package io.github.jagodevreede.sdkmanui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ApplicationVersion {

    public static ApplicationVersion INSTANCE = new ApplicationVersion();

    private static String version;

    private ApplicationVersion() {
        // hidden constructor as it is a static instance.

        try {
            InputStream resourceAsStream = ApplicationVersion.class.getClassLoader()
                .getResourceAsStream("version.txt");
            version = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            version = "unknown";
        }

    }

    public String getVersion() {
        return version;
    }

}
