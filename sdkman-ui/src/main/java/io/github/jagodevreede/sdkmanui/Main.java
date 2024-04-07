package io.github.jagodevreede.sdkmanui;

import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Objects;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage stage) throws Exception {
        logger.debug("Starting SDKMAN UI");
        ServiceRegistry.INSTANCE.setPrimaryStage(stage);

        URL mainFxml = Main.class.getClassLoader().getResource("main.fxml");
        Parent root = FXMLLoader.load(mainFxml);

        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/sdkman_ui_logo.png"))));

        Scene scene = new Scene(root, 800, 580);
        stage.setResizable(false);

        stage.setTitle("SDKMAN UI");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}