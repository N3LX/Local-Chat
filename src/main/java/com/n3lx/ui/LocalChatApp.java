package com.n3lx.ui;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.stream.DoubleStream;

public class LocalChatApp extends Application {

    private static final double WINDOW_MIN_WIDTH = 600;
    private static final double WINDOW_MIN_HEIGHT = 500;
    private static final double WINDOW_PADDING = 20;
    private static final double CHAT_PANE_AND_USER_LIST_SPACING = 15;

    /**
     * This is a percentage value, should not exceed 100.
     */
    private static final double CHAT_PANE_TO_USER_LIST_RATIO = 75;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        VBox root = new VBox();
        root.getChildren().add(createMenuBar());
        root.getChildren().add(createWindowContent());
        root.getChildren().forEach(node -> VBox.setVgrow(node, Priority.ALWAYS));

        Scene scene = new Scene(root);
        stage.setTitle("Local Chat");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setMinHeight(WINDOW_MIN_HEIGHT);
        stage.setMinWidth(WINDOW_MIN_WIDTH);
        stage.show();
    }

    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();

        Menu chatMenu = new Menu("Chat");
        MenuItem chatMenuItem1 = new MenuItem("Connect...");
        MenuItem chatMenuItem2 = new MenuItem("Disconnect");
        MenuItem chatMenuItem3 = new MenuItem("Host...");
        MenuItem chatMenuItem4 = new MenuItem("Stop hosting");
        chatMenu.getItems().addAll(chatMenuItem1, chatMenuItem2, new SeparatorMenuItem(), chatMenuItem3, chatMenuItem4);

        Menu toolsMenu = new Menu("Tools");
        MenuItem toolsMenuItem1 = new MenuItem("Clear chat");
        MenuItem toolsMenuItem2 = new MenuItem("Save chat log to file...");
        toolsMenu.getItems().addAll(toolsMenuItem1, toolsMenuItem2);

        Menu settingsMenu = new Menu("Settings");

        Menu appearanceMenu = new Menu("Appearance");
        MenuItem appearanceMenuItem1 = new MenuItem("Light mode");
        MenuItem appearanceMenuItem2 = new MenuItem("Dark mode");
        appearanceMenu.getItems().addAll(appearanceMenuItem1, appearanceMenuItem2);

        MenuItem settingsMenuItem1 = new MenuItem("Show timestamps");
        settingsMenu.getItems().addAll(appearanceMenu, settingsMenuItem1);

        menuBar.getMenus().addAll(chatMenu, toolsMenu, settingsMenu);
        return menuBar;
    }

    private GridPane createWindowContent() {
        GridPane windowContent = new GridPane();
        windowContent.setHgap(CHAT_PANE_AND_USER_LIST_SPACING);
        windowContent.setPadding(new Insets(WINDOW_PADDING));

        var columnConstraints = DoubleStream
                .of(CHAT_PANE_TO_USER_LIST_RATIO, 100 - CHAT_PANE_TO_USER_LIST_RATIO)
                .mapToObj(percentage -> {
                    ColumnConstraints constraints = new ColumnConstraints();
                    constraints.setPercentWidth(percentage);
                    constraints.setFillWidth(true);
                    return constraints;
                })
                .toList();
        windowContent.getColumnConstraints().addAll(columnConstraints);

        var rowConstraints = new RowConstraints();
        rowConstraints.setVgrow(Priority.ALWAYS);
        windowContent.getRowConstraints().add(rowConstraints);

        windowContent.addColumn(0, createChatPane());
        windowContent.addColumn(1, createUserListPane());
        return windowContent;
    }

    private VBox createChatPane() {
        VBox chatPane = new VBox();
        chatPane.setSpacing(5);

        ListView<String> chatBox = new ListView<>();

        chatPane.getChildren().addAll(chatBox, createMessageBox());
        VBox.setVgrow(chatBox, Priority.ALWAYS);
        return chatPane;
    }

    private HBox createMessageBox() {
        HBox messageBox = new HBox();
        TextField messageTextField = new TextField();
        Button sendButton = new Button("Send");
        messageBox.getChildren().addAll(messageTextField, sendButton);
        messageBox.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
        return messageBox;
    }

    private VBox createUserListPane() {
        VBox userListPane = new VBox();
        userListPane.alignmentProperty().setValue(Pos.CENTER);
        userListPane.setSpacing(5);

        Label userListLabel = new Label("Connected users");
        ListView<String> userList = new ListView<>();

        userListPane.getChildren().addAll(userListLabel, userList);
        userListPane.getChildren().forEach(node -> VBox.setVgrow(node, Priority.ALWAYS));
        return userListPane;
    }

}
