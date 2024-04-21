package io.github.jagodevreede.sdkman.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SdkManUiPreferences {
    public static final File PROPERTY_LOCATION = new File(SdkManApi.DEFAULT_SDKMAN_HOME + "/etc/sdkmanui.preferences");
    public boolean offline;
    public boolean donePreCheck;
    public String unzipExecutable;
    public String zipExecutable;
    public String tarExecutable;

    public static SdkManUiPreferences load() throws IOException {
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
        return uiPreferences;
    }

    public void save() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("offline", String.valueOf(offline));
        properties.setProperty("donePreCheck", String.valueOf(donePreCheck));
        properties.setProperty("unzipExecutable", unzipExecutable);
        properties.setProperty("zipExecutable", zipExecutable);
        properties.setProperty("tarExecutable", tarExecutable);
        properties.store(new FileOutputStream(PROPERTY_LOCATION), null);
    }
}
