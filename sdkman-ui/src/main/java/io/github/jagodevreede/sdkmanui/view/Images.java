package io.github.jagodevreede.sdkmanui.view;

import javafx.scene.image.Image;

import java.util.Objects;

public final class Images {
    public static final String IMAGES_DIRECTORY = "/images/";

    public static final Image appIcon = new Image(Objects.requireNonNull(Images.class.getResourceAsStream(IMAGES_DIRECTORY + "sdkman_ui_logo.png")));
    public static final Image globalIcon = new Image(Objects.requireNonNull(Images.class.getResourceAsStream(IMAGES_DIRECTORY + "global_icon.png")));
    public static final Image useIcon = new Image(Objects.requireNonNull(Images.class.getResourceAsStream(IMAGES_DIRECTORY + "use_icon.png")));
    public static final Image checkIcon = new Image(Objects.requireNonNull(Images.class.getResourceAsStream(IMAGES_DIRECTORY + "check.png")));
    public static final Image installIcon = new Image(Objects.requireNonNull(Images.class.getResourceAsStream(IMAGES_DIRECTORY + "install.png")));
    public static final Image removeIcon = new Image(Objects.requireNonNull(Images.class.getResourceAsStream(IMAGES_DIRECTORY + "remove.png")));

    private Images() {
        // Private constructor, because this is a constrains class
    }
}
