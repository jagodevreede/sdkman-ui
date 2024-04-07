package io.github.jagodevreede.sdkmanui.service;

import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkmanui.view.PopupView;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

import static io.github.jagodevreede.sdkman.api.SdkManApi.DEFAULT_SDKMAN_HOME;

public class ServiceRegistry {
    public static final ServiceRegistry INSTANCE = new ServiceRegistry();

    private SdkManApi api = new SdkManApi(DEFAULT_SDKMAN_HOME);
    private PopupView popupView;
    private Stage primaryStage;
    private ProgressIndicator progressSpinner;

    private ServiceRegistry() {
    }

    public SdkManApi getApi() {
        return api;
    }

    public void setApi(SdkManApi api) {
        this.api = api;
    }

    public void setPrimaryStage(Stage primaryStage) {
        popupView = new PopupView(primaryStage);
    }

    public PopupView getPopupView() {
        return popupView;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setProgressIndicator(ProgressIndicator progressSpinner) {
        this.progressSpinner = progressSpinner;
    }

    public ProgressIndicator getProgressSpinner() {
        return progressSpinner;
    }
}
