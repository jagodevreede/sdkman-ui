package io.github.jagodevreede.sdkman.api;

public class OsHelper {

    public static String getOs() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static String getPathSeparator() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return ";";
        }
        return ":";
    }
}
