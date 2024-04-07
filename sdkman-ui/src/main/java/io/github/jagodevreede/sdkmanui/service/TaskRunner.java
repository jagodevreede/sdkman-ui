package io.github.jagodevreede.sdkmanui.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

public class TaskRunner {
    public static void run(Task task) {
        ProgressIndicator progressSpinner = ServiceRegistry.INSTANCE.getProgressSpinner();
        progressSpinner.setVisible(true);
        task.setOnSucceeded(e -> clearProgressSpinner());
        task.setOnFailed(e -> clearProgressSpinner());
        new Thread(task).start();
    }

    private static void clearProgressSpinner() {
        ProgressIndicator progressSpinner = ServiceRegistry.INSTANCE.getProgressSpinner();
        Platform.runLater(() -> progressSpinner.setVisible(false));
    }
}
