package io.github.jagodevreede.sdkman.api.parser;

import io.github.jagodevreede.sdkman.api.domain.CandidateVersion;
import org.junit.jupiter.api.Test;

import static io.github.jagodevreede.sdkman.api.parser.TestHelper.loadFileFromResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VersionListParserTest {

    @Test
    void parseJava_small() throws Exception {
        String resource = loadFileFromResource("api_examples/candidates/java/linux/versions/list/small.txt");
        var javaVersions = VersionListParser.parse(resource);
        assertThat(javaVersions).isNotNull().hasSize(5);

        assertThat(javaVersions.get(0).vendor()).isEqualTo("Corretto");
        assertThat(javaVersions.get(0).version()).isEqualTo("22");
        assertThat(javaVersions.get(0).dist()).isEqualTo("amzn");
        assertThat(javaVersions.get(0).identifier()).isEqualTo("22-amzn");
    }

    @Test
    void parseJava() throws Exception {
        String resource = loadFileFromResource("api_examples/candidates/java/linux/versions/list/out.txt");
        var javaVersions = VersionListParser.parse(resource);
        assertThat(javaVersions).isNotNull().hasSize(165);
    }

    /**
     * Since August 2026 the sdkman api renders the java list without the 'Dist' and 'Status' columns
     */
    @Test
    void parseJava_fourColumns_small() throws Exception {
        String resource = loadFileFromResource("api_examples/candidates/java/linux/versions/list/small-4-columns.txt");
        var javaVersions = VersionListParser.parse(resource);
        assertThat(javaVersions).isNotNull().hasSize(5);

        assertThat(javaVersions.get(0)).isEqualTo(new CandidateVersion("Corretto", "26.0.2", "amzn", "26.0.2-amzn", false, false));
        // vendor is only rendered on the first row of a vendor group
        assertThat(javaVersions.get(1)).isEqualTo(new CandidateVersion("Corretto", "21.0.12", "amzn", "21.0.12-amzn", false, false));
        assertThat(javaVersions.get(2)).isEqualTo(new CandidateVersion("Dragonwell", "17.0.16", "albba", "17.0.16-albba", false, false));
        assertThat(javaVersions.get(3)).isEqualTo(new CandidateVersion("Zulu", "26.0.2", "zulu", "26.0.2-zulu", false, false));
        // dist is derived from the identifier, the version itself may contain dots and suffixes
        assertThat(javaVersions.get(4)).isEqualTo(new CandidateVersion("Zulu", "17.0.19.crac", "zulu", "17.0.19.crac-zulu", false, false));
    }

    @Test
    void parseJava_fourColumns() throws Exception {
        String resource = loadFileFromResource("api_examples/candidates/java/linux/versions/list/out-4-columns.txt");
        var javaVersions = VersionListParser.parse(resource);
        assertThat(javaVersions).isNotNull().hasSize(92);
        // no header, separator or footer lines leaked into the result
        assertThat(javaVersions).allSatisfy(version -> {
            assertThat(version.vendor()).isNotBlank().doesNotContain("|");
            assertThat(version.version()).isNotBlank().doesNotContain("|", "=", "-----", "Version");
            assertThat(version.identifier()).isNotBlank().endsWith("-" + version.dist());
        });
        assertThat(javaVersions).extracting(CandidateVersion::vendor).containsOnly(
                "Corretto", "GraalVM CE", "GraalVM Oracle", "Java.net", "JetBrains", "Liberica", "Liberica NIK",
                "Mandrel", "Microsoft", "Oracle", "SapMachine", "Semeru", "Temurin", "Tencent", "Zulu");
        assertThat(javaVersions).contains(
                new CandidateVersion("GraalVM CE", "25.3.4+1.r25", "graalce", "25.3.4+1.r25-graalce", false, false),
                new CandidateVersion("Java.net", "28.0.0+ea.13", "open", "28.0.0+ea.13-open", false, false),
                new CandidateVersion("Liberica", "8.0.504-fx+1", "librca", "8.0.504-fx+1-librca", false, false),
                new CandidateVersion("Zulu", "8.0.502.fx", "zulu", "8.0.502.fx-zulu", false, false));
    }

    @Test
    void parseJava_windowsLineEndings() throws Exception {
        String resource = loadFileFromResource("api_examples/candidates/java/linux/versions/list/small-4-columns.txt")
                .replace("\n", "\r\n");
        var javaVersions = VersionListParser.parse(resource);
        assertThat(javaVersions).hasSize(5);
        assertThat(javaVersions.get(4)).isEqualTo(new CandidateVersion("Zulu", "17.0.19.crac", "zulu", "17.0.19.crac-zulu", false, false));
    }

    /**
     * Cells wider than their column end with a '>' marker
     */
    @Test
    void parseJava_truncatedCells() {
        String response = "====\nAvailable Java Versions\n====\n"
                + " Vendor   | Use | Version            | Identifier\n"
                + "----\n"
                + " Liberica |     | 21.0.12-crac+1.2.> | 21.0.12-crac+1.2.3.4-librca\n"
                + "          |     | 21.0.12-crac+1.2.> | 21.0.12-crac+1.2.3.4.5.6.7.10-libr>\n"
                + "====\n";
        var javaVersions = VersionListParser.parse(response);
        assertThat(javaVersions).hasSize(2);
        // truncated version recovered from the identifier
        assertThat(javaVersions.get(0)).isEqualTo(new CandidateVersion("Liberica", "21.0.12-crac+1.2.3.4", "librca", "21.0.12-crac+1.2.3.4-librca", false, false));
        // truncated identifier can not be recovered
        assertThat(javaVersions.get(1)).isEqualTo(new CandidateVersion("Liberica", "21.0.12-crac+1.2.>", "libr>", "21.0.12-crac+1.2.3.4.5.6.7.10-libr>", false, false));
    }

    @Test
    void parseJava_unknownColumns() {
        String response = "====\nAvailable Java Versions\n====\n Vendor | Use | Something | Else\n----\n Corretto | | 26.0.2 | 26.0.2-amzn\n====\n";
        assertThatThrownBy(() -> VersionListParser.parse(response))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Vendor | Use | Something | Else");
    }

    @Test
    void parseOther() throws Exception {
        String resource = loadFileFromResource("api_examples/candidates/maven/versions/list/out.txt");
        var javaVersions = VersionListParser.parse(resource);
        assertThat(javaVersions).isNotNull().hasSize(42);
    }
}
