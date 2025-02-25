package io.github.jagodevreede.sdkman.api.parser;

import io.github.jagodevreede.sdkman.api.domain.Candidate;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static io.github.jagodevreede.sdkman.api.parser.TestHelper.loadFileFromResource;
import static org.assertj.core.api.Assertions.assertThat;

class CandidateListParserTest {
    private static final Logger LOG = LoggerFactory.getLogger(CandidateListParserTest.class);

    @Test
    void parse() throws Exception {
        String resource = TestHelper.readSdkManOutput();
        List<Candidate> candidates = CandidateListParser.parse(resource);
        assertThat(candidates).isNotNull();
        if (candidates.isEmpty()) {
            LOG.info("resource {}", resource);
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

            assertThat(candidates.get(0).id()).isEqualTo("activemq");
            assertThat(candidates.get(0).name()).isEqualTo("Apache ActiveMQ");
            assertThat(candidates.get(0).description()).contains("ubiquitous AMQP protocol");

            candidates.forEach( candidate -> {
                assertThat(candidate.id()).isNotNull().isNotEmpty();
                assertThat(candidate.name()).isNotNull().isNotEmpty();
                assertThat(candidate.description()).isNotNull().isNotEmpty();
            });
        }
    }

}
