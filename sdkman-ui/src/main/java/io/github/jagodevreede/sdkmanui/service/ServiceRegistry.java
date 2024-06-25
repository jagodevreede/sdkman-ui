package io.github.jagodevreede.sdkmanui.service;

import static io.github.jagodevreede.sdkman.api.SdkManApi.DEFAULT_SDKMAN_HOME;

import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkman.api.SdkManUiPreferences;
import io.github.jagodevreede.sdkmanui.view.PopupView;
import javafx.scene.control.ProgressIndicator;

public class ServiceRegistry {
    public static final ServiceRegistry INSTANCE = new ServiceRegistry();

    private SdkManApi api = new SdkManApi(DEFAULT_SDKMAN_HOME);
    private PopupView popupView = new PopupView();
    private ProgressIndicator progressSpinner;
    private SdkManUiPreferences sdkManUiPreferences;

    private ServiceRegistry() {
        sdkManUiPreferences = SdkManUiPreferences.getInstance();
    }

    public SdkManApi getApi() {
        return api;
    }

    public PopupView getPopupView() {
        return popupView;
    }

    public void setProgressIndicator(ProgressIndicator progressSpinner) {
        this.progressSpinner = progressSpinner;
    }

    public ProgressIndicator getProgressSpinner() {
        return progressSpinner;
    }

    public SdkManUiPreferences getSdkManUiPreferences() {
        return sdkManUiPreferences;
    }
}
