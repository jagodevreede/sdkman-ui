package io.github.jagodevreede.sdkmanui.view;

import static io.github.jagodevreede.sdkmanui.view.Images.globalIcon;
import static io.github.jagodevreede.sdkmanui.view.Images.useIcon;

import java.io.IOException;
import java.util.Optional;

import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkman.api.domain.CandidateVersion;
import io.github.jagodevreede.sdkmanui.MainScreenController;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class VersionView {

    private final SimpleStringProperty vendor;
    private final SimpleStringProperty version;
    private final SimpleStringProperty dist;
    private final SimpleStringProperty identifier;
    private final HBox actions;
    private final CheckBox installed;
    private final CheckBox available;
    private final Button globalAction;
    private final Button useAction;
    private final MainScreenController controller;

    public VersionView(CandidateVersion candidateVersion, String globalVersionInUse, String pathVersionInUse, MainScreenController controller) {
        this.controller = controller;
        globalAction = createImageButton(globalIcon, globalEventHandler(candidateVersion));
        useAction = createImageButton(useIcon, useEventHandler(candidateVersion));
        this.vendor = new SimpleStringProperty(candidateVersion.vendor());
        this.version = new SimpleStringProperty(candidateVersion.version());
        this.dist = new SimpleStringProperty(candidateVersion.dist());
        this.identifier = new SimpleStringProperty(candidateVersion.identifier());
        if (candidateVersion.installed()) {
            this.actions = new HBox(
                    globalAction,
                    useAction
            );
        } else {
            this.actions = new HBox();
        }
        this.installed = new CheckBox();
        this.available = new CheckBox();
        update(candidateVersion, globalVersionInUse, pathVersionInUse);
    }

    public void update(CandidateVersion candidateVersion, String globalVersionInUse, String pathVersionInUse) {
        this.installed.setSelected(candidateVersion.installed());
        this.installed.selectedProperty().addListener(installedChangeListener(candidateVersion));
        this.available.setDisable(true);
        this.available.setSelected(candidateVersion.available());
        globalAction.setDisable(candidateVersion.identifier().equals(globalVersionInUse));
        useAction.setDisable(candidateVersion.identifier().equals(pathVersionInUse));
    }

    private ChangeListener<Boolean> installedChangeListener(CandidateVersion candidateVersion) {
        return (observable, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(newValue)) {
                controller.downloadAndInstall(controller.getSelectedCandidate(), candidateVersion.identifier());
            } else {
                controller.uninstall(controller.getSelectedCandidate(), candidateVersion.identifier());
            }
        };
    }

    private EventHandler<MouseEvent> globalEventHandler(CandidateVersion candidateVersion) {
        return (event) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Set global SDK");
            alert.setHeaderText("Are you sure that you want to set " + candidateVersion.identifier() + " as your global SDK?");

            ButtonType buttonTypeCancel = new ButtonType("Cancel");

            ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType buttonYesAndClose = new ButtonType("Yes, and close", ButtonBar.ButtonData.YES);

            alert.getButtonTypes().setAll(buttonTypeCancel, buttonYes, buttonYesAndClose);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && (result.get() == buttonYesAndClose || result.get() == buttonYes)) {
                SdkManApi api = ServiceRegistry.INSTANCE.getApi();
                try {
                    api.changeGlobal("java", candidateVersion.identifier());
                } catch (IOException e) {
                    ServiceRegistry.INSTANCE.getPopupView().showError(e);
                }
                if (result.get() == buttonYesAndClose) {
                    Platform.exit();
                } else {
                    controller.loadData();
                }
            }
            alert.close();
        };
    }

    private EventHandler<MouseEvent> useEventHandler(CandidateVersion candidateVersion) {
        return (event) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Set local SDK");
            alert.setHeaderText("Are you sure that you want to set " + candidateVersion.identifier() + " as your local SDK?");

            ButtonType buttonTypeCancel = new ButtonType("Cancel");

            ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType buttonYesAndClose = new ButtonType("Yes, and close", ButtonBar.ButtonData.YES);

            alert.getButtonTypes().setAll(buttonTypeCancel, buttonYes, buttonYesAndClose);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && (result.get() == buttonYesAndClose || result.get() == buttonYes)) {
                SdkManApi api = ServiceRegistry.INSTANCE.getApi();
                try {
                    api.createExitScript("java", candidateVersion.identifier());
                } catch (IOException e) {
                    ServiceRegistry.INSTANCE.getPopupView().showError(e);
                }
                if (result.get() == buttonYesAndClose) {
                    Platform.exit();
                } else {
                    controller.loadData();
                }
            }
            alert.close();
        };
    }

    private Button createImageButton(Image image, EventHandler<? super MouseEvent> eventHandler) {
        Button button = new Button();
        ImageView globalActionImage = new ImageView();
        globalActionImage.setImage(image);
        globalActionImage.setFitHeight(10.0);
        globalActionImage.setFitWidth(10.0);
        button.setGraphic(globalActionImage);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(10.0);
        button.setMaxHeight(10.0);
        button.setOnMouseClicked(eventHandler);
        return button;
    }

    public String getVendor() {
        return vendor.get();
    }

    public String getVersion() {
        return version.get();
    }

    public String getDist() {
        return dist.get();
    }

    public String getIdentifier() {
        return identifier.get();
    }

    public CheckBox getInstalled() {
        return installed;
    }

    public CheckBox getAvailable() {
        return available;
    }

    public HBox getActions() {
        return actions;
    }

}
