package io.github.jagodevreede.sdkman.api.domain;

public record JavaVersion(String vendor, String version, String dist, String identifier, boolean installed,
                          boolean available) implements Comparable {

    public JavaVersion(JavaVersion javaVersion, boolean installed, boolean available) {
        this(javaVersion.vendor, javaVersion.version, javaVersion.dist, javaVersion.identifier, installed, available);
    }

    @Override
    public int compareTo(Object o) {
        int cmp;
        // first by vendor then version, then identifier
        if (this.dist != null && ((JavaVersion) o).dist != null) {
            cmp = this.dist.compareTo(((JavaVersion) o).dist);
            if (cmp != 0) return cmp;
        }
        cmp = this.version.compareTo(((JavaVersion) o).version);
        if (cmp != 0) return cmp;

        cmp = this.identifier.compareTo(((JavaVersion) o).identifier);
        return cmp;
    }
}
