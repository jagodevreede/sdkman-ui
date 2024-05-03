package io.github.jagodevreede.sdkman.api.parser;

import io.github.jagodevreede.sdkman.api.domain.CandidateVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VersionListParser {
    private static final String VENDOR_HEADER_NAME = "Vendor";

    private static final Pattern JAVA_VERSION_PATTERN = Pattern.compile("(.*?)\\|(.*?)\\|(.*?)\\|(.*?)\\|(.*?)\\|(.*)");
    private static final Pattern OTHER_VERSION_PATTERN = Pattern.compile("(.+?)\\s+(.+?)\\s+(.+?)\\s");

    public static List<CandidateVersion> parse(String response) {
        var matcher = JAVA_VERSION_PATTERN.matcher(response);
        if (matcher.find()) {
            return parseJava(response);
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
                if (version.startsWith("=====")) {
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

    private static List<CandidateVersion> parseJava(String response) {
        var result = new ArrayList<CandidateVersion>();
        var matcher = JAVA_VERSION_PATTERN.matcher(response);
        String lastVendor = null;
        while (matcher.find()) {
            String vendor = matcher.group(1).trim();
            if (VENDOR_HEADER_NAME.equals(vendor)) {
                continue;
            }
            if (!vendor.trim().isEmpty()) {
                lastVendor = vendor;
            } else {
                vendor = lastVendor;
            }
            String version = matcher.group(3).trim();
            String dist = matcher.group(4).trim();
            String identifier = matcher.group(6).trim();
            result.add(new CandidateVersion(vendor, version, dist, identifier, false, false));
        }
        return result;
    }
}
