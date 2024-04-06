package io.github.jagodevreede.sdkman.api.domain;

public record JavaVersion(String vendor, String version, String dist, String identifier, boolean installed) {
}
