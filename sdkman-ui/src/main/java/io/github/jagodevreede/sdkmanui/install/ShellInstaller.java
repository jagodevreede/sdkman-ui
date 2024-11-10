package io.github.jagodevreede.sdkmanui.install;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static java.io.File.separator;

@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
class ShellInstaller extends UiInstaller {
    static final String BEGIN_MARKER = "# BEGIN SDKMAN-UI";
    static final String END_MARKER = "# END SDKMAN-UI";
    static final List<String> rcFiles = List.of(".bashrc", ".zshrc");

    @Override
    public void updateScriptAndVersion() {
        try {
            Files.copy(UiInstaller.class.getClassLoader()
                    .getResourceAsStream("sdkui.sh"), new File(installFolder, "sdkui.sh").toPath(), StandardCopyOption.REPLACE_EXISTING);
            new File(installFolder, "sdkui.sh").setExecutable(true, false);
            Files.copy(UiInstaller.class.getClassLoader()
                    .getResourceAsStream("update.sh"), new File(installFolder, "update.sh").toPath(), StandardCopyOption.REPLACE_EXISTING);
            new File(installFolder, "update.sh").setExecutable(true, false);
            Files.copy(UiInstaller.class.getClassLoader()
                    .getResourceAsStream("version.txt"), new File(installFolder, "version.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
            rcFiles.forEach(f -> {
                File rcFile = new File(System.getProperty("user.home") + separator + f);
                if (rcFile.exists()) {
                    addToRcFile(rcFile);
                }
            });
        } catch (IOException e) {
            SERVICE_REGISTRY.getPopupView().showError(e);
        }
    }

    void addToRcFile(File rcFile) {
        try (var templateStream = ShellInstaller.class.getResourceAsStream("/template/bash_shell")) {
            String template = new String(templateStream.readAllBytes(), StandardCharsets.UTF_8);
            Path filePath = rcFile.toPath();
            String content = Files.readString(filePath);

            if ((content.contains(BEGIN_MARKER) && !content.contains(END_MARKER)) || (!content.contains(BEGIN_MARKER) && content.contains(END_MARKER))) {
                SERVICE_REGISTRY.getPopupView()
                        .showError("Corrupt rc file " + rcFile.getAbsolutePath() + "\nUnable to add SDKMAN-UI");
            }
            if (!content.contains(BEGIN_MARKER)) {
                Files.writeString(filePath, content + "\n" + BEGIN_MARKER + "\n" + template + "\n" + END_MARKER);
            } else {
                String beforeContent = content.substring(0, content.indexOf(BEGIN_MARKER));
                String afterContent = content.substring(content.indexOf(END_MARKER) + END_MARKER.length());
                Files.writeString(filePath, beforeContent + BEGIN_MARKER + "\n" + template + "\n" + END_MARKER + afterContent);
            }
        } catch (IOException e) {
            SERVICE_REGISTRY.getPopupView().showError(e);
        }
    }
}
