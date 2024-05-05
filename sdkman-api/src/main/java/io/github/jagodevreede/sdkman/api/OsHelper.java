package io.github.jagodevreede.sdkman.api;

import io.github.jagodevreede.sdkman.api.files.ProcessStarter;

import java.io.File;
import java.io.IOException;

public class OsHelper {

    public static String getOs() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static String getPlatformName() {
        String result;
        if (getOs().contains("windows")) {
            result = "windows";
        } else if (isMac()) {
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
        if (isWindows()) {
            return ";";
        }
        return ":";
    }

    public static boolean hasShell() {
        return !getPathSeparator().equals(";");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static String getGlobalPath() {
        if (OsHelper.isWindows()) {
            try {
                String pathQuery = ProcessStarter.runInGetOutput(new File("./"), "reg.exe", "query", "HKCU\\Environment", "/v", "Path");
                String[] split = pathQuery.split("\\s");
                System.out.println(split[split.length - 1].trim());
            }catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static boolean isMac() {
        return getOs().contains("mac");
    }
}
