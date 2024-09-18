package io.github.jagodevreede.sdkmanui.updater;

import com.google.gson.Gson;
import io.github.jagodevreede.sdkman.api.http.CachedHttpClient;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class GitHubRelease {
    public static final String RELEASES_LATEST = "https://api.github.com/repos/jagodevreede/sdkman-ui/releases/latest";
    private final CachedHttpClient cachedHttpClient;
    private final ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE;
    private final static Map<String, String> HEADERS = Map.of(
            "Accept", "application/vnd.github+json", "X-GitHub-Api-Version", "2022-11-28"
    );

    public GitHubRelease(String cacheFolder, HttpClient httpClient) {
        this(new CachedHttpClient(cacheFolder, Duration.of(1, ChronoUnit.DAYS), httpClient));
    }

    public GitHubRelease(CachedHttpClient cachedHttpClient) {
        this.cachedHttpClient = cachedHttpClient;
    }

    public String getLatestRelease() throws IOException, InterruptedException {
        boolean offline = serviceRegistry.getSdkManUiPreferences().offline;
        String data = cachedHttpClient.get(RELEASES_LATEST, offline, HEADERS);
        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(data, Map.class);
        data = map.get("tag_name");
        return data;
    }

    public List<String> getLatestReleaseDownloads() throws IOException, InterruptedException {
        boolean offline = serviceRegistry.getSdkManUiPreferences().offline;
        String data = cachedHttpClient.get(RELEASES_LATEST, offline, HEADERS);
        Gson gson = new Gson();
        Map<String, List<Map<String, String>>> map = gson.fromJson(data, Map.class);
        List<Map<String, String>> assets = map.get("assets");
        return assets.stream().map(asset -> asset.get("browser_download_url")).toList();
    }
}
