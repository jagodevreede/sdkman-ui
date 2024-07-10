package io.github.jagodevreede.sdkmanui;

import io.github.jagodevreede.sdkman.api.OsHelper;
import io.github.jagodevreede.sdkmanui.controller.MainScreenController;
import io.github.jagodevreede.sdkmanui.service.GlobalExceptionHandler;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import io.github.jagodevreede.sdkmanui.updater.UpdateChecker;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import static io.github.jagodevreede.sdkmanui.view.Images.appIcon;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final ServiceRegistry SERVICE_REGISTRY = ServiceRegistry.INSTANCE;

    @Override
    public void start(Stage stage) throws Exception {
        Parameters params = getParameters();
        List<String> list = params.getRaw();
        if (list.size() == 1 && list.get(0).equalsIgnoreCase("--no-console")) {
            System.setOut(outputFile("stdout.log"));
            System.setErr(outputFile("stderr.log"));
        }
        logger.debug("Starting SDKMAN UI");
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
        if (!ConfigurationUtil.preCheck(stage)) {
            logger.warn("Failed pre-check");
            return;
        }
        SERVICE_REGISTRY.getApi().registerShutdownHook();

        if (handleArguments(list)) {
            Platform.exit();
            return;
        }
        setApplicationIconImage(stage);
        loadServiceRegistry();

        MainScreenController.getInstance();

        checkInstalled();
        new UpdateChecker().checkForUpdate();
    }

    private boolean handleArguments(List<String> list) throws IOException {
        if (!list.isEmpty()) {
            if (list.size() == 3 && checkArgument(list.get(0), "u", "use")) {
                SERVICE_REGISTRY.getApi().changeLocal(list.get(1), list.get(2));
                return true;
            }
            if (list.size() == 3 && checkArgument(list.get(0), "d", "default")) {
                SERVICE_REGISTRY.getApi().changeGlobal(list.get(1), list.get(2));
                return true;
            }
            if (list.size() >= 1 && checkArgument(list.get(0), "e", "env")) {
                if (list.size() == 1 || checkArgument(list.get(1), "u", "use")) {
                    SERVICE_REGISTRY.getApi().useEnv();
                    return true;
                }
                if (list.size() >= 2 && checkArgument(list.get(1), "i", "install")) {
                    SERVICE_REGISTRY.getApi().initEnv();
                    return true;
                }
                if (list.size() >= 2 && checkArgument(list.get(1), "c", "clear")) {
                    SERVICE_REGISTRY.getApi().clearEnv();
                    return true;
                }
            }
            logger.warn("Invalid arguments: {}", list);
        }
        return false;
    }

    private static boolean checkArgument(String argument, String... checks) {
        return Arrays.stream(checks).anyMatch(s -> argument.equalsIgnoreCase(s));
    }

    private void checkInstalled() {
        final String applicationVersion = ApplicationVersion.INSTANCE.getVersion();
        final String currentInstalledUIVersion = SERVICE_REGISTRY.getApi().getCurrentInstalledUIVersion();
        if (!applicationVersion.equals(currentInstalledUIVersion)) {
            logger.info("Running a different UI version {} then the one installed {}", applicationVersion, currentInstalledUIVersion);
            install();
        }
    }

    private static void install() {
        try {
            URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
            File currentExecutable = Paths.get(location.toURI()).toFile();
            if (!currentExecutable.isFile()) {
                logger.info("Not running executable, so unable to install");
                // Probably dev mode, or failed to get location, we can't check for installation
                return;
            }
            File currentRunningFolder = currentExecutable.getParentFile();
            File installFolder = new File(SERVICE_REGISTRY.getApi().getBaseFolder(), "ui");
            if (!currentRunningFolder.equals(installFolder)) {
                SERVICE_REGISTRY.getPopupView().showConfirmation("Installation", "Do you want to install/update SDKMAN UI?", () -> {
                    installFolder.mkdirs();
                    boolean configured = SERVICE_REGISTRY.getApi().configureEnvironmentPath();
                    try {
                        Files.copy(currentExecutable.toPath(), new File(installFolder, currentExecutable.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        Files.copy(ApplicationVersion.class.getClassLoader().getResourceAsStream("sdkui.cmd"), new File(installFolder, "sdkui.cmd").toPath(), StandardCopyOption.REPLACE_EXISTING);
                        Files.copy(ApplicationVersion.class.getClassLoader().getResourceAsStream("version.txt"), new File(installFolder, "version.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    StringBuilder confirmationMessage = new StringBuilder("SDKMAN UI has been installed, you can now remove ");
                    confirmationMessage.append(currentExecutable.getAbsolutePath());
                    if (configured) {
                        confirmationMessage.append("\nyou need to relogin to be able to use `sdkui` from the command line.");
                    }
                    SERVICE_REGISTRY.getPopupView().showInformation(confirmationMessage.toString());
                });
            }
        } catch (URISyntaxException e) {
            logger.warn("Failed to check if installed, assuming so");
        }
    }

    private void loadServiceRegistry() {
        Thread loaderThread = new Thread(() -> {
            // Load the preferences, then everything is ready to go
            SERVICE_REGISTRY.getSdkManUiPreferences();
        });
        loaderThread.setDaemon(true);
        loaderThread.setName("ServiceRegistry loader");
        loaderThread.start();
    }

    private void setApplicationIconImage(Stage stage) {
        if (!OsHelper.isMac()) {
            // Only for mac other os are not needed
            stage.getIcons().add(appIcon);
            return;
        }
        Thread loaderThread = new Thread(() -> {
            final java.awt.Toolkit defaultToolkit = java.awt.Toolkit.getDefaultToolkit();
            final URL imageResource = getClass().getResource("/images/sdkman_ui_logo.png");
            final java.awt.Image image = defaultToolkit.getImage(imageResource);

            final java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();

            try {
                //set icon for mac os (and other systems which do support this method)
                taskbar.setIconImage(image);
            } catch (final UnsupportedOperationException e) {
                logger.debug("The os does not support: 'taskbar.setIconImage'");
            } catch (final SecurityException e) {
                logger.debug("There was a security exception for: 'taskbar.setIconImage'");
            }
        });
        loaderThread.setDaemon(true);
        loaderThread.setName("Osx dock icon loader");
        loaderThread.start();
    }

    protected PrintStream outputFile(String name) throws FileNotFoundException {
        return new PrintStream(new BufferedOutputStream(new FileOutputStream(name)), true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}