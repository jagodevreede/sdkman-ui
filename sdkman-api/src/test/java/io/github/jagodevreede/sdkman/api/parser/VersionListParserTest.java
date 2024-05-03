package io.github.jagodevreede.sdkman.api.parser;

import org.junit.jupiter.api.Test;

import static io.github.jagodevreede.sdkman.api.parser.TestHelper.loadFileFromResource;
import static org.assertj.core.api.Assertions.assertThat;

class VersionListParserTest {

    @Test
    void parseJava_small() throws Exception {
        String resource = loadFileFromResource("api_examples/candidates/java/linux/versions/list/small.txt");
        var javaVersions = VersionListParser.parse(resource);
        assertThat(javaVersions)
                .isNotNull()
                .hasSize(5);

        assertThat(javaVersions.get(0).vendor()).isEqualTo("Corretto");
        assertThat(javaVersions.get(0).version()).isEqualTo("22");
        assertThat(javaVersions.get(0).dist()).isEqualTo("amzn");
        assertThat(javaVersions.get(0).identifier()).isEqualTo("22-amzn");
    }

    @Test
    void parseJava() throws Exception {
        String resource = loadFileFromResource("api_examples/candidates/java/linux/versions/list/out.txt");
        var javaVersions = VersionListParser.parse(resource);
        assertThat(javaVersions)
                .isNotNull()
                .hasSize(165);
    }

    @Test
    void parseOther() throws Exception {
        String resource = loadFileFromResource("api_examples/candidates/maven/versions/list/out.txt");
        var javaVersions = VersionListParser.parse(resource);
        assertThat(javaVersions)
                .isNotNull()
                .hasSize(42);
    }
}