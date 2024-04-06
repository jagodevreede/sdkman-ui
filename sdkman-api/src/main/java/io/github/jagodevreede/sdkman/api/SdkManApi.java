package io.github.jagodevreede.sdkman.api;

import io.github.jagodevreede.sdkman.api.domain.Candidate;
import io.github.jagodevreede.sdkman.api.http.CachedHttpClient;
import io.github.jagodevreede.sdkman.api.parser.CandidateListParser;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static java.net.http.HttpClient.newHttpClient;

public class SdkManApi {

    public static final String BASE_URL = "https://api.sdkman.io/2";
    public static final Duration DEFAUL_CACHE_DURATION = Duration.of(1, ChronoUnit.HOURS);
    private static final String SDKMAN_HOME = System.getProperty("user.home") + "/.sdkman";

    private final CachedHttpClient client;

    public SdkManApi(String baseFolder) {
        this.client = new CachedHttpClient(baseFolder + "/.http_cache", DEFAUL_CACHE_DURATION, newHttpClient());
    }

    public List<Candidate> getCandidates() throws Exception {
        String response = client.get(BASE_URL + "/candidates");
        return CandidateListParser.parse(response);
    }

    public List<String> getLocalInstalledVersions(String candidate) {
        var candidatesFolder = new File(SDKMAN_HOME + "/candidates/" + candidate);
        if (!candidatesFolder.exists()) {
            return List.of();
        }
        return List.of(Objects.requireNonNull(candidatesFolder.list((dir, name) -> new File(dir, name).isDirectory())));
    }

    private String getCurrentCandidateFromPath(String candidate) {
        var paths = System.getenv("PATH").split(OsHelper.getPathSeparator());
        var pathName = SDKMAN_HOME + "/candidates/" + candidate;
        for (var path : paths) {
            if (path.startsWith(pathName)) {
                return path.substring(pathName.length() + 1).replace("/bin", "");
            }
        }
        return null;
    }

    private String resolveCurrentVersion(String candidate) throws IOException {
        var candidatesFolder = new File(SDKMAN_HOME + "/candidates/" + candidate);
        var realPath = new File(candidatesFolder, "current").toPath().toRealPath().toString();
        return realPath.substring(candidatesFolder.getAbsolutePath().length() + 1).replace("/bin", "");
    }


    public static void main(String[] args) throws IOException {
        System.out.println(" ====== ");
        System.out.println(new SdkManApi(SDKMAN_HOME).getCurrentCandidateFromPath("java"));
        System.out.println(new SdkManApi(SDKMAN_HOME).getLocalInstalledVersions("java"));
        System.out.println(new SdkManApi(SDKMAN_HOME).resolveCurrentVersion("java"));

    }

}
