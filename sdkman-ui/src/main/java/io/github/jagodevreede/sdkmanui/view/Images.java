package io.github.jagodevreede.sdkmanui.view;

import javafx.scene.image.Image;

import java.util.Objects;

final class Images {
    private static final String IMAGES_DIRECTORY = "/images/";

    static final Image globalIcon = new Image(Objects.requireNonNull(Images.class.getResourceAsStream(IMAGES_DIRECTORY + "global_icon.png")));
    static final Image useIcon = new Image(Objects.requireNonNull(Images.class.getResourceAsStream(IMAGES_DIRECTORY + "use_icon.png")));
    static final Image checkIcon = new Image(Objects.requireNonNull(Images.class.getResourceAsStream(IMAGES_DIRECTORY + "check.png")));
    static final Image installIcon = new Image(Objects.requireNonNull(Images.class.getResourceAsStream(IMAGES_DIRECTORY + "install.png")));
    static final Image removeIcon = new Image(Objects.requireNonNull(Images.class.getResourceAsStream(IMAGES_DIRECTORY + "remove.png")));

    private Images() {
        // Private constructor, because this is a constrains class
    }
}
