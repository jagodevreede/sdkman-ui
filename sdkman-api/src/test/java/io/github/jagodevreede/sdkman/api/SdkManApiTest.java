package io.github.jagodevreede.sdkman.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class SdkManApiTest {

    @TempDir
    File sdkmanHome;

    @Test
    void downloadUsesTheBroker() {
        var api = new SdkManApi(sdkmanHome.getAbsolutePath());

        var task = api.download("java", "25.3.4+1.r25-graalce");

        assertThat(task.getUrl()).isEqualTo("https://broker.sdkman.io/download/java/25.3.4+1.r25-graalce/" + OsHelper.getPlatformName());
    }
}
