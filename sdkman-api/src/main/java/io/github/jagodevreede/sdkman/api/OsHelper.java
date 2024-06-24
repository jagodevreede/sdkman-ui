package io.github.jagodevreede.sdkman.api;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsHelper {

    private static final Logger log = LoggerFactory.getLogger(OsHelper.class);

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

    public static boolean hasShell() {
        return !isWindows();
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static String getGlobalPath() {
        if (OsHelper.isWindows()) {
            try {
                // Don't use reg.exe in windows as some policy's might forbid the execution of reg.exe
                String pathQuery = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Environment", "Path");
                String[] split = pathQuery.split("\\s");

                return split[split.length - 1].trim();
            } catch (IllegalStateException e) {
                // this can happen if there is no path set in the users environment variables
                log.debug("Failed to get global path: {}", e.getMessage());
                return "";
            }
        }
        return null;
    }

    public static void setGlobalEnvironment(String key, String value) {
        if (OsHelper.isWindows()) {
            Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "Environment", key, value);
        }
    }

    public static void setGlobalPath(String path) {
        setGlobalEnvironment("Path", path);
    }

    public static boolean isMac() {
        return getOs().contains("mac");
    }
}
