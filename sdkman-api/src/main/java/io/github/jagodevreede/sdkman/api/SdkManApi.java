package io.github.jagodevreede.sdkman.api;

import io.github.jagodevreede.sdkman.api.domain.Candidate;
import io.github.jagodevreede.sdkman.api.http.CachedHttpClient;
import io.github.jagodevreede.sdkman.api.parser.CandidateListParser;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.net.http.HttpClient.newHttpClient;

public class SdkManApi {

    public static final String BASE_URL = "https://api.sdkman.io/2";
    public static final Duration DEFAUL_CACHE_DURATION = Duration.of(1, ChronoUnit.HOURS);

    private final CachedHttpClient client;

    public SdkManApi(String baseFolder) {
        this.client = new CachedHttpClient(baseFolder + "/.http_cache", DEFAUL_CACHE_DURATION,newHttpClient());
    }

    public List<Candidate> getCandidates() throws Exception {
        String response = client.get(BASE_URL + "/candidates");
        return CandidateListParser.parse(response);
    }

}
