package io.github.jagodevreede.sdkman.api;

import io.github.jagodevreede.sdkman.api.files.ProcessStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

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
                String pathQuery = ProcessStarter.runInGetOutput(new File("./"), "reg.exe", "query", "HKCU\\Environment", "/v", "Path");
                String[] split = pathQuery.split("\\s");

                return split[split.length - 1].trim();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (IllegalStateException e) {
                // this can happen if there is no path set in the users environment variables
                log.debug("Failed to get global path: {}", e.getMessage());
                return "";
            }
        }
        return null;
    }

    public static String setGlobalPath(String path) {
        if (OsHelper.isWindows()) {
            try {
                // Call with get output, as the output of the command will be "Task completed successfully"
                ProcessStarter.runInGetOutput(new File("./"), "reg.exe", "add", "HKCU\\Environment", "/v", "Path", "/t", "REG_EXPAND_SZ", "/d", path, "/f");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static boolean isMac() {
        return getOs().contains("mac");
    }
}
