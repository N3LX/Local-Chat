package com.n3lx;


import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LocalChatApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        //Placeholder code for now
        var root = new Group();
        var scene = new Scene(root, 600, 300);

        stage.setTitle("Local Chat");
        stage.setScene(scene);
        stage.show();
    }
}
