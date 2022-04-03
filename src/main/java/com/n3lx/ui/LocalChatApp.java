package com.n3lx.ui;


import com.n3lx.ui.dialogwindows.AlertWindow;
import com.n3lx.ui.dialogwindows.ConnectionWindow;
import com.n3lx.ui.dialogwindows.HostWindow;
import com.n3lx.ui.util.Preferences;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.DoubleStream;

public class LocalChatApp extends Application {

    private static final double WINDOW_MIN_WIDTH = 600;
    private static final double WINDOW_MIN_HEIGHT = 500;
    private static final double WINDOW_PADDING = 20;

    /**
     * This is a percentage value, should not exceed 100.
     */
    private static final double CHAT_PANE_TO_USER_LIST_RATIO = 75;

    private Stage mainStage;

    private Scene mainScene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;

        VBox root = new VBox();
        root.getChildren().add(createMenuBar());
        root.getChildren().add(createWindowContent());
        root.getChildren().forEach(node -> VBox.setVgrow(node, Priority.ALWAYS));

        mainScene = new Scene(root);
        stage.setScene(mainScene);
        stage.sizeToScene();
        stage.setMinHeight(WINDOW_MIN_HEIGHT);
        stage.setMinWidth(WINDOW_MIN_WIDTH);

        stage.setTitle("Local Chat");
        //The icon won't be visible in the dock on MacOS but it will work on Windows and Linux
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
        mainScene.getStylesheets().addAll("stylesheet.css", Preferences.getThemeCssPath());

        stage.show();
    }

    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();

        Menu chatMenu = new Menu("Chat");

        MenuItem chatMenuItem1 = new MenuItem("Connect...");
        chatMenuItem1.setOnAction(actionEvent -> new ConnectionWindow(mainStage).getWindow().show());

        MenuItem chatMenuItem2 = new MenuItem("Disconnect");
        chatMenuItem2.setDisable(true);

        MenuItem chatMenuItem3 = new MenuItem("Host...");
        chatMenuItem3.setOnAction(actionEvent -> new HostWindow(mainStage).getWindow().show());

        MenuItem chatMenuItem4 = new MenuItem("Stop hosting");
        chatMenuItem4.setDisable(true);

        chatMenu.getItems().addAll(chatMenuItem1, chatMenuItem2, new SeparatorMenuItem(), chatMenuItem3, chatMenuItem4);
        ChatController.getInstance().linkChatMenuBarButtons(chatMenuItem1, chatMenuItem2, chatMenuItem3, chatMenuItem4);

        Menu toolsMenu = new Menu("Tools");

        MenuItem toolsMenuItem1 = new MenuItem("Clear chat");
        toolsMenuItem1.setOnAction(actionEvent -> ChatController.getInstance().getChatBox().getItems().clear());

        MenuItem toolsMenuItem2 = new MenuItem("Save chat log to file...");
        toolsMenuItem2.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
            File targetFile = fileChooser.showSaveDialog(mainStage);

            boolean wasOperationSuccessful;
            try {
                FileWriter fileWriter = new FileWriter(targetFile);
                for (String line : ChatController.getInstance().getChatBox().getItems()) {
                    fileWriter.append(line).append("\n");
                }
                fileWriter.close();
                wasOperationSuccessful = true;
            } catch (IOException e) {
                wasOperationSuccessful = false;
            }

            String alertMessage;
            if (wasOperationSuccessful) {
                alertMessage = "File has been saved to chosen location";
            } else {
                alertMessage = "An error has occurred while trying to save a file to this location";
            }
            AlertWindow alert = new AlertWindow(mainStage, alertMessage);
            alert.getWindow().show();
        });

        toolsMenu.getItems().addAll(toolsMenuItem1, toolsMenuItem2);

        Menu settingsMenu = new Menu("Settings");

        Menu appearanceMenu = new Menu("Appearance");

        MenuItem appearanceMenuItem1 = new MenuItem("Light mode");
        MenuItem appearanceMenuItem2 = new MenuItem("Dark mode");

        appearanceMenuItem1.setOnAction(actionEvent -> {
            mainScene.getStylesheets().removeAll("stylesheet.css", Preferences.getThemeCssPath());
            Preferences.setTheme(Preferences.THEME.LIGHT);
            mainScene.getStylesheets().addAll("stylesheet.css", Preferences.getThemeCssPath());
            appearanceMenuItem1.setDisable(true);
            appearanceMenuItem2.setDisable(false);
        });

        appearanceMenuItem2.setOnAction(actionEvent -> {
            mainScene.getStylesheets().removeAll("stylesheet.css", Preferences.getThemeCssPath());
            Preferences.setTheme(Preferences.THEME.DARK);
            mainScene.getStylesheets().addAll("stylesheet.css", Preferences.getThemeCssPath());
            appearanceMenuItem2.setDisable(true);
            appearanceMenuItem1.setDisable(false);
        });

        //Disable one of the buttons based on which theme was set on startup
        switch (Preferences.getTheme()) {
            case LIGHT -> appearanceMenuItem1.setDisable(true);
            case DARK -> appearanceMenuItem2.setDisable(true);
        }

        appearanceMenu.getItems().addAll(appearanceMenuItem1, appearanceMenuItem2);

        CheckMenuItem settingsMenuItem1 = new CheckMenuItem("Show timestamps");
        settingsMenuItem1.setOnAction(actionEvent -> {
            Preferences.setAllowTimestamps(settingsMenuItem1.isSelected());
        });

        settingsMenu.getItems().addAll(appearanceMenu, settingsMenuItem1);

        menuBar.getMenus().addAll(chatMenu, toolsMenu, settingsMenu);
        return menuBar;
    }

    private GridPane createWindowContent() {
        GridPane windowContent = new GridPane();
        windowContent.setHgap(15);
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

        ChatController.getInstance().linkChatBox(chatBox);
        chatPane.getChildren().addAll(chatBox, createMessageBox());
        VBox.setVgrow(chatBox, Priority.ALWAYS);
        return chatPane;
    }

    private HBox createMessageBox() {
        HBox messageBox = new HBox();
        messageBox.setSpacing(5);
        messageBox.setAlignment(Pos.CENTER);

        TextField messageTextField = new TextField();
        messageTextField.setDisable(true);
        Button sendButton = new Button("Send");
        sendButton.setDisable(true);

        ChatController.getInstance().linkMessageBox(messageTextField, sendButton);
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

        ChatController.getInstance().linkUserListBox(userList);
        userListPane.getChildren().addAll(userListLabel, userList);
        userListPane.getChildren().forEach(node -> VBox.setVgrow(node, Priority.ALWAYS));
        return userListPane;
    }

    @Override
    public void stop() {
        ChatController.getInstance().stop();
    }

}
