package io.github.jagodevreede.sdkman.api.parser;

import io.github.jagodevreede.sdkman.api.domain.Candidate;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static io.github.jagodevreede.sdkman.api.parser.TestHelper.loadFileFromResource;
import static org.assertj.core.api.Assertions.assertThat;

class CandidateListParserTest {

    @Test
    void parse() throws Exception {
        String resource = TestHelper.readSdkManOutput();
        List<Candidate> candidates = CandidateListParser.parse(resource);
        assertThat(candidates).isNotNull();
        if (candidates.isEmpty()) {
            throw new IllegalStateException("empty collection found: " +resource);
        }
        TestHelper.validateParsed(candidates);
    }

    @Test
    void parseForWindows() throws Exception {
        String resource = TestHelper.readSdkManOutput();
        List<Candidate> candidates = CandidateListParser.SdkManOutputTextParser.parse(resource);

        TestHelper.validateParsed(candidates);
    }

    private static class TestHelper {
        private static String readSdkManOutput() throws IOException {
            return loadFileFromResource("api_examples/candidates/list/out.txt");
        }

        private static void validateParsed(List<Candidate> candidates) {
            assertThat(candidates).hasSize(72);
            var candidate0 = candidates.get(0);
            var candidate71 = candidates.get(71);

            assertThat(candidate0.id()).isEqualTo("activemq");
            assertThat(candidate0.name()).isEqualTo("Apache ActiveMQ");
            assertThat(candidate0.description()).contains("ubiquitous AMQP protocol");

            candidates.forEach( candidate -> {
                assertThat(candidate.id()).isNotNull().isNotEmpty();
                assertThat(candidate.name()).isNotNull().isNotEmpty();
                assertThat(candidate.description()).isNotNull().isNotEmpty();
            });

            assertThat(candidate71.id()).isEqualTo("znai");
            assertThat(candidate71.name()).isEqualTo("Znai");
            assertThat(candidate71.description()).contains("beautiful User Guides with znai");

        }
    }

}
