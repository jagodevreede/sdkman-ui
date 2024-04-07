package io.github.jagodevreede.sdkman.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OsHelperTest {

    @Test
    void getPlatformName() {
        assertThat(OsHelper.getPlatformName()).isNotNull();
    }
}