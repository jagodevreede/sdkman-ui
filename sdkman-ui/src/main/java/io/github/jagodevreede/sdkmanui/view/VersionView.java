package io.github.jagodevreede.sdkmanui.view;

import static io.github.jagodevreede.sdkmanui.view.Images.checkIcon;
import static io.github.jagodevreede.sdkmanui.view.Images.globalIcon;
import static io.github.jagodevreede.sdkmanui.view.Images.installIcon;
import static io.github.jagodevreede.sdkmanui.view.Images.removeIcon;
import static io.github.jagodevreede.sdkmanui.view.Images.useIcon;

import java.io.IOException;
import java.util.Optional;

import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkman.api.domain.CandidateVersion;
import io.github.jagodevreede.sdkmanui.MainScreenController;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class VersionView {

    private final SimpleStringProperty vendor;
    private final SimpleStringProperty version;
    private final SimpleStringProperty dist;
    private final SimpleStringProperty identifier;
    private final HBox installed = new HBox();
    private final HBox actions = new HBox();
    private boolean isInstalled;
    private final ImageView installedImage = createImageView(checkIcon);
    private final CheckBox available;
    private final Button installAction;
    private final Button removeAction;
    private final Button globalAction;
    private final Button useAction;
    private final MainScreenController controller;

    public VersionView(CandidateVersion candidateVersion, String globalVersionInUse, String pathVersionInUse, MainScreenController controller) {
        this.controller = controller;
        installAction = createImageButton(installIcon, getInstallActionEventHandler(candidateVersion));
        removeAction = createImageButton(removeIcon, getRemoveActionEventHandler(candidateVersion));
        globalAction = createImageButton(globalIcon, getGlobalActionEventHandler(candidateVersion));
        useAction = createImageButton(useIcon, getUseActionEventHandler(candidateVersion));
        configureActionsColumn();
        vendor = new SimpleStringProperty(candidateVersion.vendor());
        version = new SimpleStringProperty(candidateVersion.version());
        dist = new SimpleStringProperty(candidateVersion.dist());
        identifier = new SimpleStringProperty(candidateVersion.identifier());
        available = new CheckBox();
        configureInstalledColumn();
        update(candidateVersion, globalVersionInUse, pathVersionInUse);
    }

    private void configureActionsColumn() {
        installAction.setTooltip(new Tooltip("Install this version."));
        removeAction.setTooltip(new Tooltip("Remove this version."));
        globalAction.setTooltip(new Tooltip("Use this version as global SDK."));
        useAction.setTooltip(new Tooltip("Use this version as local SDK."));
        actions.setAlignment(Pos.CENTER_RIGHT);
    }

    private void configureInstalledColumn() {
        installed.setAlignment(Pos.CENTER);
        installedImage.setFitHeight(20.0);
        installedImage.setFitWidth(20.0);
    }

    public void update(CandidateVersion candidateVersion, String globalVersionInUse, String pathVersionInUse) {
        isInstalled = candidateVersion.installed();
        available.setDisable(true);
        available.setSelected(candidateVersion.available());
        globalAction.setDisable(candidateVersion.identifier().equals(globalVersionInUse));
        useAction.setDisable(candidateVersion.identifier().equals(pathVersionInUse));

        installed.getChildren().clear();
        actions.getChildren().clear();
        if (isInstalled) {
            installed.getChildren().add(installedImage);
            actions.getChildren().add(globalAction);
            actions.getChildren().add(useAction);
            actions.getChildren().add(removeAction);
        } else {
            actions.getChildren().add(installAction);
        }
    }

    private EventHandler<MouseEvent> getInstallActionEventHandler(CandidateVersion candidateVersion) {
        return event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Install SDK");
            alert.setHeaderText("Are you sure that you want to install " + candidateVersion.identifier() + "?");

            ButtonType buttonNo = new ButtonType("No");
            ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonNo, buttonYes);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == buttonYes) {
                controller.download(controller.getSelectedCandidate(), candidateVersion.identifier(), true);
            }
            alert.close();
        };
    }

    private EventHandler<MouseEvent> getRemoveActionEventHandler(CandidateVersion candidateVersion) {
        return event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Remove SDK");
            alert.setHeaderText("Are you sure that you want to remove installation of " + candidateVersion.identifier() + "?");

            ButtonType buttonNo = new ButtonType("No");
            ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonNo, buttonYes);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == buttonYes) {
                controller.uninstall(controller.getSelectedCandidate(), candidateVersion.identifier());
            }
            alert.close();
        };
    }

    private EventHandler<MouseEvent> getGlobalActionEventHandler(CandidateVersion candidateVersion) {
        return event -> {
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
                    api.changeGlobal(controller.getSelectedCandidate(), candidateVersion.identifier());
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

    private EventHandler<MouseEvent> getUseActionEventHandler(CandidateVersion candidateVersion) {
        return event -> {
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
        ImageView globalActionImage = createImageView(image);
        button.setGraphic(globalActionImage);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(10.0);
        button.setMaxHeight(10.0);
        button.setOnMouseClicked(eventHandler);
        return button;
    }

    private static ImageView createImageView(Image image) {
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setFitHeight(10.0);
        imageView.setFitWidth(10.0);
        return imageView;
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

    public HBox getInstalled() { return installed; }

    public boolean isInstalled() {
        return isInstalled;
    }

    public CheckBox getAvailable() {
        return available;
    }

    public HBox getActions() {
        return actions;
    }

}
