package com.n3lx.ui.dialogwindows;

import com.n3lx.chat.server.Server;
import com.n3lx.ui.ChatController;
import com.n3lx.ui.util.Preferences;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class HostWindow extends DialogWindow {

    private static final double VERTICAL_SPACING = 15;
    private static final double HORIZONTAL_SPACING = 10;
    private static final double WINDOW_PADDING = 15;

    private TextField serverNameTextField;
    private TextArea serverMessageTextArea;

    public HostWindow(Stage parentStage) {
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
        createServerNameRow(selectionPane);
        createServerMessageRow(selectionPane);
        windowContent.addColumn(0, selectionPane);

        createButtonRow(windowContent);

        Scene scene = new Scene(windowContent);
        windowStage.setScene(scene);
        windowStage.sizeToScene();
        windowStage.setResizable(false);

        windowStage.setTitle("Host a server");
        scene.getStylesheets().addAll("stylesheet.css", Preferences.getThemeCssPath());
    }

    private void createServerNameRow(GridPane parentPane) {
        Label serverNameLabel = new Label("Server name");
        serverNameTextField = new TextField();

        parentPane.addColumn(0, serverNameLabel);
        parentPane.addColumn(1, serverNameTextField);
    }

    private void createServerMessageRow(GridPane parentPane) {
        Label serverMessageLabel = new Label("Server welcome message");

        serverMessageTextArea = new TextArea();
        serverMessageTextArea.setPrefRowCount(10);

        parentPane.addColumn(0, serverMessageLabel);
        parentPane.addColumn(1, serverMessageTextArea);
    }

    private void createButtonRow(GridPane parentPane) {
        HBox buttonRow = new HBox();
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.setSpacing(HORIZONTAL_SPACING);

        Button connectButton = new Button("Host");
        connectButton.setOnAction(actionEvent -> {
            String serverName = serverNameTextField.getText();
            String serverMessage = serverMessageTextArea.getText();
            if (validateInput(serverName, serverMessage)) {
                Server server = new Server(serverName, serverMessage
                        , ChatController.getInstance().getChatBox()
                        , ChatController.getInstance().getUserListBox());
                ChatController.getInstance().startChat(server);
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

    private boolean validateInput(String serverName, String serverMessage) {
        //All fields must be filled
        if (serverName.equals("") || serverMessage.equals("")) {
            return false;
        }

        //Input with only white space is not valid either
        if (serverName.chars().distinct().count() == 1 && serverName.contains(" ")) {
            return false;
        }
        if (serverMessage.chars().distinct().count() == 1 && serverMessage.contains(" ")) {
            return false;
        }
        return true;
    }

    private void showAlert() {
        String alertMessage = "All fields cannot be empty!";
        AlertWindow alertWindow = new AlertWindow(windowStage, alertMessage);
        alertWindow.getWindow().show();
    }

}
