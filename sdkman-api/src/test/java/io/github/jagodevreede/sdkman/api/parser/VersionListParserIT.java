package io.github.jagodevreede.sdkman.api.parser;

import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkman.api.domain.Candidate;
import io.github.jagodevreede.sdkman.api.domain.CandidateVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests that hit the live SDKMan API.
 * These run in the verify phase via maven-failsafe-plugin, not during unit test phase.
 */
class VersionListParserIT {

    @Test
    void parseJavaIntegration_liveApi() throws IOException, InterruptedException {
        var tempDir = Files.createTempDirectory("sdkman-integration-test").toString();
        var api = new SdkManApi(tempDir);
        var javaVersions = api.getVersions("java");

        assertThat(javaVersions).isNotNull();
        assertThat(javaVersions).isNotEmpty();
        assertThat(javaVersions).hasSizeGreaterThan(50);

        javaVersions.forEach(cv -> {
            assertThat(cv.vendor()).isNotBlank();
            assertThat(cv.version()).isNotBlank();
        });

        var vendors = javaVersions.stream().map(CandidateVersion::vendor).distinct().toList();
        assertThat(vendors).contains("Corretto", "Temurin", "Zulu");
    }

    @Test
    void candidatesIntegration_liveApi() throws IOException, InterruptedException, ExecutionException {
        var tempDir = Files.createTempDirectory("sdkman-integration-test").toString();
        var api = new SdkManApi(tempDir);
        var candidates = api.getCandidates().get();

        assertThat(candidates).isNotNull();
        assertThat(candidates).isNotEmpty();
        assertThat(candidates).hasSizeGreaterThan(30);

        var ids = candidates.stream().map(Candidate::id).toList();
        assertThat(ids).contains("java", "groovy", "kotlin", "gradle", "maven");
    }
}
