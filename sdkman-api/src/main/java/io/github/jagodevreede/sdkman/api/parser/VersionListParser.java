package io.github.jagodevreede.sdkman.api.parser;

import io.github.jagodevreede.sdkman.api.domain.CandidateVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VersionListParser {
    private static final String VENDOR_HEADER_NAME = "Vendor";
    private static final String VERSION_HEADER_NAME = "Version";
    private static final String DIST_HEADER_NAME = "Dist";
    private static final String IDENTIFIER_HEADER_NAME = "Identifier";
    private static final String COLUMN_SEPARATOR = "|";
    private static final String TABLE_END = "=====";
    private static final String TRUNCATION_MARKER = ">";

    private static final Pattern COLUMN_SEPARATOR_PATTERN = Pattern.compile("\\|");
    private static final Pattern OTHER_VERSION_PATTERN = Pattern.compile("(.+?)\\s+(.+?)\\s+(.+?)\\s");

    public static List<CandidateVersion> parse(String response) {
        List<String> lines = response.lines().collect(Collectors.toList());
        int headerIndex = findJavaHeader(lines);
        if (headerIndex >= 0) {
            return parseJava(lines, headerIndex);
        }
        var result = parseOther(response);
        result.sort(CandidateVersion::compareTo);
        return result;
    }

    private static List<CandidateVersion> parseOther(String response) {
        var result = new ArrayList<CandidateVersion>();
        String headerLessResponse = removeHeader(response);
        var matcher = OTHER_VERSION_PATTERN.matcher(headerLessResponse);
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String version = matcher.group(i).trim();
                if (version.startsWith(TABLE_END)) {
                    // we have reached the end
                    return result;
                }
                if (!version.isEmpty()) {
                    result.add(new CandidateVersion(null, version, null, version, false, false));
                }
            }
        }
        return result;
    }

    private static String removeHeader(String response) {
        // remove first 3 lines as the header is always in the first 3 lines
        return response.lines().skip(3).collect(Collectors.joining("\n"));
    }

    /**
     * The java list is rendered as a table with '|' separated columns, the first column being the vendor.
     * Up to August 2026 the header was 'Vendor | Use | Version | Dist | Status | Identifier', since then it
     * is 'Vendor | Use | Version | Identifier'. Columns are therefore located by name in the header row,
     * instead of relying on their position or count.
     *
     * @return the index of the java table header line, or -1 if the response is not a java table
     */
    private static int findJavaHeader(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains(COLUMN_SEPARATOR) && VENDOR_HEADER_NAME.equals(splitColumns(line).get(0))) {
                return i;
            }
        }
        return -1;
    }

    private static List<CandidateVersion> parseJava(List<String> lines, int headerIndex) {
        String header = lines.get(headerIndex);
        List<String> columns = splitColumns(header);
        int vendorColumn = columns.indexOf(VENDOR_HEADER_NAME);
        int versionColumn = columns.indexOf(VERSION_HEADER_NAME);
        int identifierColumn = columns.indexOf(IDENTIFIER_HEADER_NAME);
        int distColumn = columns.indexOf(DIST_HEADER_NAME); // optional, no longer present since August 2026
        if (versionColumn < 0 || identifierColumn < 0) {
            throw new IllegalStateException("Unexpected java version list header: " + header.trim());
        }
        var result = new ArrayList<CandidateVersion>();
        String lastVendor = null;
        for (String line : lines.subList(headerIndex + 1, lines.size())) {
            if (line.startsWith(TABLE_END)) {
                // end of the table, only the footer follows
                break;
            }
            List<String> cells = splitColumns(line);
            if (cells.size() != columns.size()) {
                // the ---- separator line between the header and the first row
                continue;
            }
            String vendor = cells.get(vendorColumn);
            if (!vendor.isEmpty()) {
                lastVendor = vendor;
            } else {
                // the vendor is only rendered on the first row of each vendor group
                vendor = lastVendor;
            }
            String identifier = cells.get(identifierColumn);
            String dist = distColumn >= 0 ? cells.get(distColumn) : distFromIdentifier(identifier);
            String version = version(cells.get(versionColumn), identifier, dist);
            result.add(new CandidateVersion(vendor, version, dist, identifier, false, false));
        }
        return result;
    }

    /**
     * Cells wider than their column are truncated and end with a '>' marker. The identifier is
     * '&lt;version&gt;-&lt;dist&gt;' and its column is wider, so a truncated version is recovered from it
     * when possible. A truncated identifier can not be recovered and is left as is.
     */
    private static String version(String version, String identifier, String dist) {
        if (!version.endsWith(TRUNCATION_MARKER) || identifier.endsWith(TRUNCATION_MARKER) || dist == null) {
            return version;
        }
        return identifier.substring(0, identifier.length() - dist.length() - 1);
    }

    /**
     * The dist is the suffix of the identifier, e.g. 'amzn' for '21.0.12-amzn', which is
     * what the removed 'Dist' column used to contain.
     */
    private static String distFromIdentifier(String identifier) {
        int index = identifier.lastIndexOf('-');
        if (index < 0) {
            return null;
        }
        return identifier.substring(index + 1);
    }

    private static List<String> splitColumns(String line) {
        var cells = new ArrayList<String>();
        for (String cell : COLUMN_SEPARATOR_PATTERN.split(line, -1)) {
            cells.add(cell.trim());
        }
        return cells;
    }
}
