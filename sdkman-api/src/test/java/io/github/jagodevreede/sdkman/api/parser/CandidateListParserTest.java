package io.github.jagodevreede.sdkman.api.parser;

import org.junit.jupiter.api.Test;

import static io.github.jagodevreede.sdkman.api.parser.TestHelper.loadFileFromResource;
import static org.assertj.core.api.Assertions.assertThat;

class CandidateListParserTest {

    @Test
    void parse() throws Exception {
        String resource = loadFileFromResource("api_examples/candidates/list/out.txt");
        var candidates = CandidateListParser.parse(resource);
        assertThat(candidates).isNotNull().hasSize(72);

        assertThat(candidates.get(0).id()).isEqualTo("activemq");
        assertThat(candidates.get(0).name()).isEqualTo("Apache ActiveMQ");
        assertThat(candidates.get(0).description()).contains("ubiquitous AMQP protocol");
    }

}