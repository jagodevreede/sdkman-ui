package io.github.jagodevreede.sdkman.api.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class FileUtil {
    public static void deleteRecursively(File f) throws IOException {
        if (!f.exists()) {
            return;
        }
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteRecursively(c);
            }
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    public static File findRoot(File folderToSearchIn, String folderName) {
        Optional<File> binFolder = Arrays.stream(folderToSearchIn.listFiles(f -> f.isDirectory() && f.getName().equals(folderName))).findAny();
        if (binFolder.isPresent()) {
            return binFolder.get().getParentFile();
        }
        for (File dir : Objects.requireNonNull(folderToSearchIn.listFiles(File::isDirectory))) {
            File result = findRoot(dir, folderName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
