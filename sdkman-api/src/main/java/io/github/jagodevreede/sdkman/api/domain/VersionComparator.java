package io.github.jagodevreede.sdkman.api.domain;

import java.util.Comparator;
import java.util.regex.Pattern;

public class VersionComparator implements Comparator<String> {
    private final static Pattern SPLIT_REGEX = Pattern.compile("[.-]");

    @Override
    public int compare(String v1, String v2) {
        String[] parts1 = SPLIT_REGEX.split(v1);
        String[] parts2 = SPLIT_REGEX.split(v2);

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            String p1 = i < parts1.length ? parts1[i] : "0";
            String p2 = i < parts2.length ? parts2[i] : "0";

            int result = comparePart(p1, p2);
            if (result != 0) return result;
        }
        return 0;
    }

    private static int comparePart(String p1, String p2) {
        boolean isNum1 = p1.matches("\\d+");
        boolean isNum2 = p2.matches("\\d+");

        if (isNum1 && isNum2) {
            return Integer.compare(Integer.parseInt(p1), Integer.parseInt(p2));
        } else if (isNum1) {
            return -1;
        } else if (isNum2) {
            return 1;
        } else {
            return p1.compareTo(p2);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
