package io.github.jagodevreede.sdkman.api.files;

import java.io.File;
import java.io.IOException;

public class ZipDirectory {
    private ZipDirectory() {
        // no instantiation
    }

    public static void zip(File folderToZip, File outputFile) throws IOException {
        outputFile.delete();
        //  zip -qyr "$zip_output" "Java-22.1.0.1.r17-gln"
        ProcessStarter.runIn(folderToZip.getParentFile(), "zip", "-qyr", outputFile.getAbsolutePath(), folderToZip.getName());
    }

}