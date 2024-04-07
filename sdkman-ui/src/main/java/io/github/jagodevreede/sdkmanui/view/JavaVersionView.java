package io.github.jagodevreede.sdkmanui.view;

import io.github.jagodevreede.sdkman.api.domain.JavaVersion;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class JavaVersionView {

    private final SimpleStringProperty vendor;
    private final SimpleStringProperty version;
    private final SimpleStringProperty dist;
    private final SimpleStringProperty identifier;
    private final HBox actions;
    private final CheckBox installed;
    private final Button globalAction;
    private final Button useAction;

    public JavaVersionView(JavaVersion javaVersion, String globalIdentifierInUse) {
        globalAction = createImageButton("/images/global_icon.png", javaVersion.identifier().equals(globalIdentifierInUse), (event) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Set global SDK");
            alert.setHeaderText("Are you sure that you want to set " + javaVersion.identifier() + " as your global SDK?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                // ... user chose OK
            }
        });
        useAction = createImageButton("/images/use_icon.png", false, (event) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Set local SDK");
            alert.setHeaderText("Are you sure that you want to set " + javaVersion.identifier() + " as your local SDK?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                // ... user chose OK
            }
        });
        this.vendor = new SimpleStringProperty(javaVersion.vendor());
        this.version = new SimpleStringProperty(javaVersion.version());
        this.dist = new SimpleStringProperty(javaVersion.dist());
        this.identifier = new SimpleStringProperty(javaVersion.identifier());
        this.actions = new HBox(
                globalAction,
                useAction
        );
        this.installed = new CheckBox();
        this.installed.setSelected(javaVersion.installed());
    }

    private Button createImageButton(String imagePath, boolean disabled, EventHandler<? super MouseEvent> eventHandler) {
        Button button = new Button();
        button.setDisable(disabled);
        ImageView globalActionImage = new ImageView();
        globalActionImage.setImage(new Image(getClass().getResourceAsStream(imagePath)));
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

    public SimpleStringProperty vendorProperty() {
        return vendor;
    }

    public String getVersion() {
        return version.get();
    }

    public SimpleStringProperty versionProperty() {
        return version;
    }

    public String getDist() {
        return dist.get();
    }

    public SimpleStringProperty distProperty() {
        return dist;
    }

    public String getIdentifier() {
        return identifier.get();
    }

    public SimpleStringProperty identifierProperty() {
        return identifier;
    }

    public CheckBox getInstalled() {
        return installed;
    }

    public HBox getActions() {
        return actions;
    }

}
