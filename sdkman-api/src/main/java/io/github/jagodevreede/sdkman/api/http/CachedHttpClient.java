package io.github.jagodevreede.sdkman.api.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class CachedHttpClient {
    private static Logger logger = LoggerFactory.getLogger(CachedHttpClient.class);

    private final File cacheFolder;
    private final Duration cacheDuration;
    private final HttpClient httpClient;

    public CachedHttpClient(String cacheFolder, Duration cacheDuration, HttpClient httpClient) {
        this.cacheFolder = new File(cacheFolder);
        this.cacheDuration = cacheDuration;
        this.httpClient = httpClient;
    }

    public String get(String url) throws IOException, InterruptedException {
        var cacheFile = new File(cacheFolder, url.replaceAll("[^a-zA-Z0-9]", "_"));
        var fromCache = loadFromCache(cacheFile);
        if (fromCache != null) {
            logger.debug("Loaded from cache: {}", url);
            return fromCache;
        }
        HttpRequest getRequest = HttpRequest.newBuilder().uri(java.net.URI.create(url)).build();
        var response = httpClient.send(getRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
        try (var cacheOutputStream = new java.io.FileOutputStream(cacheFile)) {
            var bytes = response.body().getBytes();
            cacheOutputStream.write(bytes);
            logger.debug("Loaded and saved to cache: {}", url);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private String loadFromCache(File cacheFile) throws IOException {
        if (!cacheFile.getParentFile().exists()) {
            logger.info("Creating (http) cache folder: {}", cacheFile.getParentFile().getAbsolutePath());
            cacheFile.getParentFile().mkdirs();
        }
        if (cacheFile.exists()) {
            // get age of file
            long age = System.currentTimeMillis() - cacheFile.lastModified();
            if (age < cacheDuration.toMillis()) {
                try (var resource = new FileInputStream(cacheFile)) {
                    return new String(resource.readAllBytes(), StandardCharsets.UTF_8);
                }
            } else {
                logger.debug("Cached file too old: {}", cacheFile.getName());
            }
        }
        return null;
    }
}
