package io.github.jagodevreede.sdkmanui;

import io.github.jagodevreede.sdkman.api.OsHelper;
import io.github.jagodevreede.sdkman.api.domain.Candidate;
import io.github.jagodevreede.sdkmanui.controller.MainScreenController;
import io.github.jagodevreede.sdkmanui.install.UiInstaller;
import io.github.jagodevreede.sdkmanui.service.GlobalExceptionHandler;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import io.github.jagodevreede.sdkmanui.updater.AutoUpdater;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import static io.github.jagodevreede.sdkmanui.view.Images.appIcon;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final ServiceRegistry SERVICE_REGISTRY = ServiceRegistry.INSTANCE;

    @Override
    public void start(Stage stage) throws Exception {
        Parameters params = getParameters();
        List<String> paramatersList = params.getRaw();
        if (paramatersList.contains("--no-console")) {
            System.setOut(outputFile("stdout.log"));
            System.setErr(outputFile("stderr.log"));
        }
        logger.debug("Starting SDKman UI");
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
        if (!ConfigurationUtil.preCheck(stage)) {
            logger.warn("Failed pre-check");
            return;
        }
        SERVICE_REGISTRY.getApi().registerShutdownHook();
        if (handleArguments(paramatersList)) {
            stage.close();
            return;
        }
        Future<List<Candidate>> futureCandidates = SERVICE_REGISTRY.getApi().getCandidates();
        setApplicationIconImage(stage);
        loadServiceRegistry();

        MainScreenController mainScreenController = MainScreenController.getInstance();
        List<Candidate> candidateList = futureCandidates.get();
        mainScreenController.setCandidates(candidateList);

        UiInstaller.getInstance().ifPresent(UiInstaller::checkInstalled);
        AutoUpdater.getInstance().ifPresent(AutoUpdater::checkForUpdate);
        if (paramatersList.contains("--update-complete")) {
            mainScreenController.showToast("Update installation complete");
            UiInstaller.getInstance().ifPresent(UiInstaller::updateScriptAndVersion);
        }
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
            SERVICE_REGISTRY.getPopupView().showWarning("Invalid arguments: " + list);
        }
        return false;
    }

    private static boolean checkArgument(String argument, String... checks) {
        return Arrays.stream(checks).anyMatch(argument::equalsIgnoreCase);
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
            // This code does not work in graal native, see https://github.com/oracle/graal/issues/8273
            try {
                final java.awt.Toolkit defaultToolkit = java.awt.Toolkit.getDefaultToolkit();
                final URL imageResource = getClass().getResource("/images/sdkman_ui_icon.png");
                final java.awt.Image image = defaultToolkit.getImage(imageResource);

                final java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                //set icon for mac os (and other systems which do support this method)
                taskbar.setIconImage(image);
            } catch (final UnsupportedOperationException e) {
                logger.debug("The os does not support: 'taskbar.setIconImage'");
            } catch (final SecurityException e) {
                logger.debug("There was a security exception for: 'taskbar.setIconImage'");
            } catch (final UnsatisfiedLinkError e) {
                logger.debug("No awt support in native image...");
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