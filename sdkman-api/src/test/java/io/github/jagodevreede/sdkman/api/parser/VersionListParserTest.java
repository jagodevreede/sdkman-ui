package io.github.jagodevreede.sdkman.api.parser;

import io.github.jagodevreede.sdkman.api.domain.CandidateVersion;
import org.junit.jupiter.api.Test;

import static io.github.jagodevreede.sdkman.api.parser.TestHelper.loadFileFromResource;
import static org.assertj.core.api.Assertions.assertThat;

class VersionListParserTest {

    @Test
    void parseJava_small() throws Exception {
        String resource = loadFileFromResource("api_examples/candidates/java/linux/versions/list/small.txt");
        var javaVersions = VersionListParser.parse(resource);
        assertThat(javaVersions).isNotNull().hasSize(5);

        // 4-column format: no Dist column, so dist is null
        assertThat(javaVersions.get(0).vendor()).isEqualTo("Corretto");
        assertThat(javaVersions.get(0).version()).isEqualTo("27.0.0.0+33");
        assertThat(javaVersions.get(0).dist()).isNull();
        assertThat(javaVersions.get(0).identifier()).isEqualTo("27.0.0.0+33-amzn");
        assertThat(javaVersions.get(4).vendor()).isEqualTo("Zulu");
        assertThat(javaVersions.get(4).version()).isEqualTo("21.0.2");
        assertThat(javaVersions.get(4).identifier()).isEqualTo("21.0.2-zulu");
    }

    @Test
    void parseJava() throws Exception {
        String resource = loadFileFromResource("api_examples/candidates/java/linux/versions/list/out.txt");
        var javaVersions = VersionListParser.parse(resource);
        assertThat(javaVersions).isNotNull();
        // Live API returns ~123 versions (varies over time); verify we get a substantial list
        assertThat(javaVersions).hasSizeGreaterThan(100);
        // Verify key vendors are present
        var vendors = javaVersions.stream().map(CandidateVersion::vendor).distinct().toList();
        assertThat(vendors).contains("Corretto", "Temurin", "Zulu", "GraalVM CE", "GraalVM Oracle");
        // Verify dist is null (4-column format has no Dist column)
        assertThat(javaVersions).allSatisfy(cv -> assertThat(cv.dist()).isNull());
    }

    @Test
    void parseOther() throws Exception {
        String resource = loadFileFromResource("api_examples/candidates/maven/versions/list/out.txt");
        var javaVersions = VersionListParser.parse(resource);
        assertThat(javaVersions).isNotNull().hasSize(42);
    }
}
