package io.github.jagodevreede.sdkmanui.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

public class TaskRunner {
    public static void run(Task<?> task) {
        ProgressIndicator progressSpinner = ServiceRegistry.INSTANCE.getProgressSpinner();
        progressSpinner.setVisible(true);
        task.setOnSucceeded(e -> clearProgressSpinner());
        task.setOnFailed(e -> {
            clearProgressSpinner();
            new GlobalExceptionHandler().uncaughtException(Thread.currentThread(), task.getException());
        });
        var t = new Thread(task);
        t.setUncaughtExceptionHandler(new GlobalExceptionHandler());
        t.start();
    }
    public static void run(Runnable task) {
        run(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                task.run();
                return null;
            }
        });
    }

    private static void clearProgressSpinner() {
        ProgressIndicator progressSpinner = ServiceRegistry.INSTANCE.getProgressSpinner();
        Platform.runLater(() -> progressSpinner.setVisible(false));
    }
}
