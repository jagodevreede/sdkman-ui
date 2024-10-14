package io.github.jagodevreede.sdkman.api.files;

import io.github.jagodevreede.sdkman.api.SdkManUiPreferences;

import java.io.File;
import java.io.IOException;

public class ZipDirectory {
    private ZipDirectory() {
        // no instantiation
    }

    public static void zip(File folderToZip, File outputFile) throws IOException {
        String zipExecutable = SdkManUiPreferences.getInstance().zipExecutable;
        outputFile.delete();
        //  zip -qyr "$zip_output" "Java-22.1.0.1.r17-gln" (the -y is not supported on windows)
        ProcessStarter.runIn(folderToZip.getParentFile(), zipExecutable, "-qr", outputFile.getAbsolutePath(), folderToZip.getName());
    }

}