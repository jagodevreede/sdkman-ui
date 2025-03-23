package io.github.jagodevreede.sdkman.api.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class FileUtil {
    public static void deleteRecursively(File f) throws IOException {
        if (Files.isSymbolicLink(f.toPath())) {
            f.delete();
            return;
        }
        if (!f.exists()) {
            return;
        }
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteRecursively(c);
            }
        }
        f.delete();
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

    public static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation) throws IOException {
        Files.walk(Paths.get(sourceDirectoryLocation)).forEach(source -> {
            Path destination = Paths.get(destinationDirectoryLocation, source.toString().substring(sourceDirectoryLocation.length()));
            try {
                Files.copy(source, destination);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void makeAccessible(final File f) {
        if (!f.exists()) {
            return;
        }
        f.setWritable(true);
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                makeAccessible(c);
            }
        }
    }
}
