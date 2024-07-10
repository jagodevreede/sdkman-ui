package io.github.jagodevreede.sdkmanui.updater;

import io.github.jagodevreede.sdkman.api.http.CachedHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static io.github.jagodevreede.sdkmanui.updater.TestHelper.loadFileFromResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubReleaseTest {

    @Mock
    CachedHttpClient cachedHttpClient;

    GitHubRelease subject;

    @BeforeEach
    void injectMocks() {
        subject = new GitHubRelease(cachedHttpClient);
    }

    @Test
    void getLatestRelease() throws IOException, InterruptedException {
        String httpResponse = loadFileFromResource("github_api_examples/releases/latest.json");
        when(cachedHttpClient.get(eq("https://api.github.com/repos/jagodevreede/sdkman-ui/releases/latest"), anyBoolean(), anyMap())).thenReturn(httpResponse);

        var result = subject.getLatestRelease();

        assertThat(result).isEqualTo("v0.5.1");
    }

    @Test
    void getLatestReleaseWithOutRelease() throws IOException, InterruptedException {
        String httpResponse = loadFileFromResource("github_api_examples/releases/no.json");
        when(cachedHttpClient.get(eq("https://api.github.com/repos/jagodevreede/sdkman-ui/releases/latest"), anyBoolean(), anyMap())).thenReturn(httpResponse);

        var result = subject.getLatestRelease();

        assertThat(result).isNull();
    }
}