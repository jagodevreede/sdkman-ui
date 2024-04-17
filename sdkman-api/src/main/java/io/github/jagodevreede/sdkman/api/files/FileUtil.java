package io.github.jagodevreede.sdkman.api.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public final class FileUtil {
    public static void deleteRecursively(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteRecursively(c);
            }
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }
}
