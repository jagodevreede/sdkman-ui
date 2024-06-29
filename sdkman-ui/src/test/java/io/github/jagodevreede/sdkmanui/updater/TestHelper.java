package io.github.jagodevreede.sdkmanui.updater;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TestHelper {
    public static String loadFileFromResource(String fileToLoad) throws IOException {
        try (var resource = TestHelper.class.getClassLoader().getResourceAsStream(fileToLoad)) {
            Objects.requireNonNull(resource);
            return new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
