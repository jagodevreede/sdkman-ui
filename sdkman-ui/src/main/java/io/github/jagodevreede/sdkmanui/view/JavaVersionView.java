package io.github.jagodevreede.sdkmanui.view;

import io.github.jagodevreede.sdkman.api.domain.JavaVersion;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.CheckBox;

public class JavaVersionView {

    private final SimpleStringProperty vendor;
    private final SimpleStringProperty version;
    private final SimpleStringProperty dist;
    private final SimpleStringProperty identifier;
    private final SimpleStringProperty actions;
    private final CheckBox installed;

    public JavaVersionView(JavaVersion javaVersion) {
        this.vendor = new SimpleStringProperty(javaVersion.vendor());
        this.version = new SimpleStringProperty(javaVersion.version());
        this.dist = new SimpleStringProperty(javaVersion.dist());
        this.identifier = new SimpleStringProperty(javaVersion.identifier());
        this.actions = new SimpleStringProperty("TODO");
        this.installed = new CheckBox();
        this.installed.setSelected(javaVersion.installed());
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

    public String getActions() {
        return actions.get();
    }

    public SimpleStringProperty actionsProperty() {
        return actions;
    }
}
