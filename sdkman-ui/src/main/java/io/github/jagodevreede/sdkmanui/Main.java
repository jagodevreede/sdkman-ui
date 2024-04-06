package io.github.jagodevreede.sdkmanui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL mainFxml = Main.class.getClassLoader().getResource("main.fxml");
        Parent root = FXMLLoader.load(mainFxml);

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