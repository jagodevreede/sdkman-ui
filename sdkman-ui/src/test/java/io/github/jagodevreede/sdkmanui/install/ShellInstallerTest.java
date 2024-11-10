package io.github.jagodevreede.sdkmanui.install;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.github.jagodevreede.sdkmanui.install.ShellInstaller.BEGIN_MARKER;
import static io.github.jagodevreede.sdkmanui.install.ShellInstaller.END_MARKER;
import static org.assertj.core.api.Assertions.assertThat;

class ShellInstallerTest {
    @TempDir
    Path tempDir;
    Path tempFile;

    ShellInstaller subject = new ShellInstaller();

    @BeforeEach
    void createTempFile() throws IOException {
        tempFile = tempDir.resolve("test.txt");
        Files.writeString(tempFile, "Start of file\n");
    }

    @Test
    void checkWithCleanFile() throws IOException {
        subject.addToRcFile(tempFile.toFile());

        String fileContent = Files.readString(tempFile);

        assertThat(fileContent)
                .containsOnlyOnce("Start of file")
                .containsOnlyOnce(BEGIN_MARKER)
                .containsOnlyOnce("function")
                .containsOnlyOnce(END_MARKER);
    }

    @Test
    void checkUpdate() throws IOException {
        Files.writeString(tempFile, "Start of file\n" + BEGIN_MARKER + "\n" + "old stuff to be overwritten\n" + END_MARKER);

        subject.addToRcFile(tempFile.toFile());

        String fileContent = Files.readString(tempFile);

        assertThat(fileContent)
                .containsOnlyOnce("Start of file")
                .containsOnlyOnce(BEGIN_MARKER)
                .containsOnlyOnce("function")
                .containsOnlyOnce(END_MARKER);
    }
}