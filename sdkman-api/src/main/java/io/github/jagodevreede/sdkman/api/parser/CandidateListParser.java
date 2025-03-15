package io.github.jagodevreede.sdkman.api.parser;

import io.github.jagodevreede.sdkman.api.domain.Candidate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CandidateListParser {
    private static final Pattern CANDIDATE_PATTERN = Pattern.compile("---\\r*\\n(.+?)\\(.*?\\r*\\n\\r*\\n(.*?)\\$ sdk install(.*?)\\r*\\n", Pattern.MULTILINE | Pattern.DOTALL);

    private CandidateListParser() {
    }

    public static List<Candidate> parse(String response) {
        var result = new ArrayList<Candidate>();
        Matcher matcher = CANDIDATE_PATTERN.matcher(response);

        while (matcher.find()) {
            String name = matcher.group(1).trim();
            String description = matcher.group(2).trim().replace("\n", " ");
            String id = matcher.group(3).trim();
            result.add(new Candidate(id, name, description));
        }

        return result;
    }
}
