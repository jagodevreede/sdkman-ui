package io.github.jagodevreede.sdkman.api.parser;

import io.github.jagodevreede.sdkman.api.domain.JavaVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class VersionListParser {
    private static final String VENDOR_HEADER_NAME = "Vendor";

    private static final Pattern JAVA_VERSION_PATTERN = Pattern.compile("(.*?)\\|(.*?)\\|(.*?)\\|(.*?)\\|(.*?)\\|(.*)");

    public static List<JavaVersion> parseJava(String response) {
        var result = new ArrayList<JavaVersion>();
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
            result.add(new JavaVersion(vendor, version, dist, identifier, false));
        }
        return result;
    }
}
