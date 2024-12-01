package io.github.jagodevreede.sdkmanui.controller;

import io.github.jagodevreede.sdkman.api.domain.CandidateVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class MainScreenControllerTest {

    public static final CandidateVersion CANDIDATE_VERSION = new CandidateVersion("vendor", "version", "dist", "identifier", true, true);
    private MainScreenController subject = new MainScreenController();

    @Test
    void isCandidateVersionIncludedInSearch_shouldShowAllIfSearchFieldIsNull() {
        subject.isCandidateVersionIncludedInSearch(null, CANDIDATE_VERSION);
    }

    @ValueSource(strings = {
            "",
            " ",
            "vendor ",
            "version",
            "ver",
            "identifier",
            " VENDOR ",
            "vendor version",
    })
    @ParameterizedTest(name = "search = {0}")
    void isCandidateVersionIncludedInSearch_shouldFilterOnVendorAndIdentifierAndVersion(String searchTerm) {
        assertThat(subject.isCandidateVersionIncludedInSearch(searchTerm, CANDIDATE_VERSION)).isTrue();
    }
}