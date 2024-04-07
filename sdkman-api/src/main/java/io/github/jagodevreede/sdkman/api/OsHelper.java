package io.github.jagodevreede.sdkman.api;

public class OsHelper {

    public static String getOs() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static String getPlatformName() {
        String result;
        if (getOs().contains("windows")) {
            result = "windows";
        } else if (getOs().contains("mac")) {
            result = "darwin";
        } else {
            result = "linux";
        }

        if (System.getProperty("os.arch").equals("arm64")) {
            result += "arm64";
        } else {
            result += "x64";
        }
        return result;
    }

    public static String getPathSeparator() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return ";";
        }
        return ":";
    }
}
