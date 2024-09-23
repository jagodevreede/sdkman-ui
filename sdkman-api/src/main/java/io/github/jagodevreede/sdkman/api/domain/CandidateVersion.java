package io.github.jagodevreede.sdkman.api.domain;

public record CandidateVersion(String vendor, String version, String dist, String identifier, boolean installed,
                               boolean available) implements Comparable {

    public CandidateVersion(CandidateVersion candidateVersion, boolean installed, boolean available) {
        this(candidateVersion.vendor, candidateVersion.version, candidateVersion.dist, candidateVersion.identifier, installed, available);
    }

    @Override
    public int compareTo(Object o) {
        int cmp;
        // first by vendor then version, then identifier
        if (this.dist != null && ((CandidateVersion) o).dist != null) {
            cmp = this.dist.compareTo(((CandidateVersion) o).dist);
            if (cmp != 0) return cmp;
        }
        cmp = this.version.compareTo(((CandidateVersion) o).version) * -1; // We want to sort in descending order
        if (cmp != 0) return cmp;

        cmp = this.identifier.compareTo(((CandidateVersion) o).identifier);
        return cmp;
    }
}
