package io.github.jagodevreede.sdkman.api.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class VersionComparatorTest {
    private final VersionComparator comparator = new VersionComparator();

    @ParameterizedTest
    @CsvSource({
            "1.0.0, 1.0.1",
            "1.0, 1.1",
            "1.3, 1.12",
            "1, 2",
            "1.0.0-rc.1, 1.0.0-rc.2",
            "rc1, rc2",
            "1.0, 1.0.1",
            "1, 1.0.1",
    })
    void compare(String v1, String v2) {
        assertThat(comparator.compare(v1, v2)).as("compare v1 to v2").isEqualTo(-1);
        assertThat(comparator.compare(v2, v1)).as("compare v2 to v1").isEqualTo(1);
        assertThat(comparator.compare(v1, v1)).as("compare v1 to v1").isEqualTo(0);
        assertThat(comparator.compare(v2, v2)).as("compare v2 to v2").isEqualTo(0);
    }

}