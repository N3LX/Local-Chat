package com.n3lx.ui.dialogwindows;

import com.n3lx.chat.client.Client;
import com.n3lx.chat.util.serverscanner.ServerScanner;
import com.n3lx.ui.ChatController;
import com.n3lx.ui.util.Preferences;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionWindow extends DialogWindow {

    private static final double VERTICAL_SPACING = 15;
    private static final double HORIZONTAL_SPACING = 10;
    private static final double WINDOW_PADDING = 15;

    private TextField usernameTextField;
    private ListView<String> serverSelectionList;

    public ConnectionWindow(Stage parentStage) {
        super(parentStage);
    }

    protected void createUI() {
        GridPane windowContent = new GridPane();
        windowContent.setVgap(VERTICAL_SPACING);
        windowContent.setHgap(HORIZONTAL_SPACING);
        windowContent.setPadding(new Insets(WINDOW_PADDING));

        GridPane selectionPane = new GridPane();
        selectionPane.setVgap(VERTICAL_SPACING);
        selectionPane.setHgap(HORIZONTAL_SPACING);
        createServerSelectionRow(selectionPane);
        createUsernameSetupRow(selectionPane);
        windowContent.addColumn(0, selectionPane);

        createButtonRow(windowContent);

        Scene scene = new Scene(windowContent);
        windowStage.setScene(scene);
        windowStage.sizeToScene();
        windowStage.setResizable(false);

        windowStage.setTitle("Connect to a server");
        scene.getStylesheets().addAll("stylesheet.css", Preferences.getThemeCssPath());
    }

    private void createServerSelectionRow(GridPane parentPane) {
        Label serverSelectionLabel = new Label("Select server");
        serverSelectionList = new ListView<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                while (!windowStage.isShowing()) {
                    Thread.sleep(50);
                }
                while (windowStage.isShowing()) {
                    List<String> onlineServers = ServerScanner.getListOfOnlineServers();
                    Platform.runLater(() -> {
                        serverSelectionList.getItems().clear();
                        serverSelectionList.getItems().addAll(onlineServers);
                    });
                    Thread.sleep(5000);
                }
            } catch (InterruptedException ignored) {

            }
        });
        executor.shutdown();

        parentPane.addColumn(0, serverSelectionLabel);
        parentPane.addColumn(1, serverSelectionList);
    }

    private void createUsernameSetupRow(GridPane parentPane) {
        Label usernameLabel = new Label("Your username");
        usernameTextField = new TextField();

        parentPane.addColumn(0, usernameLabel);
        parentPane.addColumn(1, usernameTextField);
    }

    private void createButtonRow(GridPane parentPane) {
        HBox buttonRow = new HBox();
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.setSpacing(HORIZONTAL_SPACING);

        Button connectButton = new Button("Connect");
        connectButton.setOnAction(actionEvent -> {
            String server = serverSelectionList.getSelectionModel().getSelectedItem();
            String username = usernameTextField.getText();
            if (validateInput(server, username)) {
                Client client = new Client(server, username
                        , ChatController.getInstance().getChatBox()
                        , ChatController.getInstance().getUserListBox());
                ChatController.getInstance().startChat(client);
                windowStage.close();
            } else {
                showAlert();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(actionEvent -> windowStage.close());

        buttonRow.getChildren().addAll(connectButton, cancelButton);
        parentPane.addColumn(0, buttonRow);
    }


    private boolean validateInput(String server, String username) {
        //All fields must be filled/selected
        if (username.equals("")) {
            return false;
        }
        if (server == null) {
            return false;
        }

        //Username with only white space is not valid either
        if (username.chars().distinct().count() == 1 && username.contains(" ")) {
            return false;
        }

        return true;
    }

    private void showAlert() {
        String alertMessage = "Incorrect input!\n\n" +
                "Username cannot be empty or contain \":\" character.\n" +
                "Ensure that you selected a server from the list.";
        AlertWindow alertWindow = new AlertWindow(windowStage, alertMessage);
        alertWindow.getWindow().show();
    }

}
