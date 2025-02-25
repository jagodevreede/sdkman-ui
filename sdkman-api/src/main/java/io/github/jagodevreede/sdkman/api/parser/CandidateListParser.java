package io.github.jagodevreede.sdkman.api.parser;

import io.github.jagodevreede.sdkman.api.domain.Candidate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public final class CandidateListParser {

    private CandidateListParser() {
    }

    public static List<Candidate> parse(String response) {
        // NB: on windows the regular expression parser fails.

        if (isWindows()) {
            return SdkManOutputTextParser.parse(response);
        }

        return LinuxTextParser.parse(response);
    }

    static class SdkManOutputTextParser {
        private SdkManOutputTextParser(){

        }
        static List<Candidate>  parse(String response) {
            List<Candidate> result = new ArrayList<>();
            String[] lines = response.split("\n");
            CandidateBuilder current = null;
            for (String line: lines) {
                var lineParser = new LineParser(line);

                if (lineParser.isStartLine()) {
                    if (current!=null) {
                        result.add(current.build());
                    }
                    current = new CandidateBuilder();
                } else
                if (lineParser.isNameLine()&&current!=null) {
                    current.name = lineParser.readName();
                } else
                if (lineParser.isEndLine()&&current!=null) {
                    current.id = lineParser.readId();
                }
                else {
                    if (current!=null) {
                        current.sb.append(line).append(" ");
                    }
                }
            }
            return result;
        }
        private static class CandidateBuilder {
            private String id;
            private String name;
            private final StringBuilder sb = new StringBuilder();

            public Candidate build() {
                return new Candidate(requireNonNull(id,"id should not be null"), requireNonNull(name,"name should not be null"), sb.toString());
            }
        }
        private static class LineParser {
            private final String line;
            LineParser(String line) {
                this.line = line;
            }
            public boolean isStartLine() {
                return line.contains("----");
            }
            public boolean isNameLine() {
                return line.contains("https://") ||line.contains("http://");
            }
            public boolean isEndLine() {
                return line.contains("$ sdk install ");
            }
            public String readName() {
                return line.substring(0, line.indexOf("(")).trim();
            }
            public String readId() {
                int index = line.indexOf("$ sdk install ") +14;
                return line.substring(index).trim();
            }
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private static class LinuxTextParser {
        private static final Pattern CANDIDATE_PATTERN = Pattern.compile("---\\n(.+?)\\(.*?\\n\\n(.*?)\\$ sdk install(.*?)\\n", Pattern.MULTILINE | Pattern.DOTALL);

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
}
