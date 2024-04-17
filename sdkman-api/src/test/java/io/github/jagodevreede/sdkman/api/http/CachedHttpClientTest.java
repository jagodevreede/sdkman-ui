package io.github.jagodevreede.sdkman.api.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.PrintWriter;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachedHttpClientTest {

    @TempDir
    File tempDir;

    @Mock
    HttpClient httpClient;

    CachedHttpClient subject;

    @BeforeEach
    void injectMocks() {
        subject = new CachedHttpClient(tempDir.getAbsolutePath(), Duration.of(1, ChronoUnit.HOURS), httpClient);
    }

    @Test
    void getReadsFromCache() throws Exception {
        String url = "http://example.com/test";
        try (PrintWriter out = new PrintWriter(new File(tempDir, "http___example_com_test"))) {
            out.print("test");
        }
        var result = subject.get(url, false);

        assertThat(result).isEqualTo("test");

        verifyNoInteractions(httpClient);
    }

    @Test
    void getCallsHttpIfNoCacheFile() throws Exception {
        String url = "http://example.com/test";
        String expectedResponse = "test from http";
        HttpResponse<String> httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(httpResponse.body()).thenReturn(expectedResponse);

        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(httpResponse);

        var result = subject.get(url, false);

        assertThat(result).isEqualTo("test from http");

        verify(httpClient).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void getCallsHttpIfCacheFileToOld() throws Exception {
        String url = "http://example.com/test";
        File cacheFile = new File(tempDir, "http___example_com_test");
        try (PrintWriter out = new PrintWriter(cacheFile)) {
            out.print("test");
        }
        cacheFile.setLastModified(System.currentTimeMillis() - Duration.of(1, ChronoUnit.HOURS).toMillis() - 1);
        String expectedResponse = "test from http";
        HttpResponse<String> httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(httpResponse.body()).thenReturn(expectedResponse);

        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(httpResponse);

        var result = subject.get(url, false);

        assertThat(result).isEqualTo("test from http");

        verify(httpClient).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void getDoesCallsHttpIfCacheFileToOldButIsOffline() throws Exception {
        String url = "http://example.com/test";
        File cacheFile = new File(tempDir, "http___example_com_test");
        try (PrintWriter out = new PrintWriter(cacheFile)) {
            out.print("test");
        }
        cacheFile.setLastModified(System.currentTimeMillis() - Duration.of(1, ChronoUnit.HOURS).toMillis() - 1);

        var result = subject.get(url, true);

        assertThat(result).isEqualTo("test");

        verifyNoInteractions(httpClient);
    }
}