package io.github.jagodevreede.sdkman.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SdkManUiPreferences {
    private static SdkManUiPreferences INSTACE;
    public static final File PROPERTY_LOCATION = new File(SdkManApi.DEFAULT_SDKMAN_HOME + "/etc/sdkmanui.preferences");
    public boolean offline;
    public boolean donePreCheck;
    public String unzipExecutable;
    public String zipExecutable;
    public String tarExecutable;
    public boolean canCreateSymlink;
    public boolean showInstalled;
    public boolean showAvailable;
    public boolean keepDownloadsAvailable;
    public boolean autoConfigurePaths;
    public boolean autoConfigureHome;

    private static SdkManUiPreferences load() throws IOException {
        PROPERTY_LOCATION.getParentFile().mkdirs();
        if (!PROPERTY_LOCATION.exists()) {
            PROPERTY_LOCATION.createNewFile();
        }
        Properties properties = new Properties();
        properties.load(new FileInputStream(PROPERTY_LOCATION));
        SdkManUiPreferences uiPreferences = new SdkManUiPreferences();
        uiPreferences.offline = Boolean.parseBoolean(properties.getProperty("offline", "false"));
        uiPreferences.donePreCheck = Boolean.parseBoolean(properties.getProperty("donePreCheck", "false"));
        uiPreferences.unzipExecutable = properties.getProperty("unzipExecutable", "unzip");
        uiPreferences.zipExecutable = properties.getProperty("zipExecutable", "zip");
        uiPreferences.tarExecutable = properties.getProperty("tarExecutable", "tar");
        uiPreferences.canCreateSymlink = Boolean.parseBoolean(properties.getProperty("canCreateSymlink", "true"));
        uiPreferences.showInstalled = Boolean.parseBoolean(properties.getProperty("showInstalled", "false"));
        uiPreferences.showAvailable = Boolean.parseBoolean(properties.getProperty("showAvailable", "false"));
        uiPreferences.keepDownloadsAvailable = Boolean.parseBoolean(properties.getProperty("keepDownloadsAvailable", "true"));
        uiPreferences.autoConfigurePaths = Boolean.parseBoolean(properties.getProperty("autoConfigurePaths", "true"));
        uiPreferences.autoConfigureHome = Boolean.parseBoolean(properties.getProperty("autoConfigureHome", "true"));
        return uiPreferences;
    }

    public static SdkManUiPreferences getInstance() {
        if (INSTACE == null) {
            try {
                INSTACE = load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return INSTACE;
    }

    public void saveQuite() {
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("offline", String.valueOf(offline));
        properties.setProperty("donePreCheck", String.valueOf(donePreCheck));
        properties.setProperty("unzipExecutable", unzipExecutable);
        properties.setProperty("zipExecutable", zipExecutable);
        properties.setProperty("tarExecutable", tarExecutable);
        properties.setProperty("canCreateSymlink", String.valueOf(canCreateSymlink));
        properties.setProperty("showInstalled", String.valueOf(showInstalled));
        properties.setProperty("showAvailable", String.valueOf(showAvailable));
        properties.setProperty("keepDownloadsAvailable", String.valueOf(keepDownloadsAvailable));
        properties.setProperty("autoConfigurePaths", String.valueOf(autoConfigurePaths));
        properties.setProperty("autoConfigureHome", String.valueOf(autoConfigureHome));
        properties.store(new FileOutputStream(PROPERTY_LOCATION), null);
    }
}
