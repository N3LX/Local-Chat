package com.n3lx.ui.dialogwindows;

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

    public HostWindow(Stage parentStage) {
        super(parentStage);
        createUI();
    }

    private void createUI() {
        GridPane windowContent = new GridPane();
        windowContent.setVgap(VERTICAL_SPACING);
        windowContent.setHgap(HORIZONTAL_SPACING);
        windowContent.setPadding(new Insets(WINDOW_PADDING));

        GridPane selectionPane = new GridPane();
        selectionPane.setVgap(VERTICAL_SPACING);
        selectionPane.setHgap(HORIZONTAL_SPACING);
        createServerNameRow(selectionPane);
        createUsernameSetupRow(selectionPane);
        createServerMessageRow(selectionPane);
        windowContent.addColumn(0, selectionPane);

        createButtonRow(windowContent);

        Scene scene = new Scene(windowContent);
        windowStage.setTitle("Host a server");
        windowStage.setScene(scene);
        windowStage.sizeToScene();
        windowStage.setResizable(false);
    }

    private void createServerNameRow(GridPane parentPane) {
        Label serverNameLabel = new Label("Server name");
        TextField serverNameTextField = new TextField();

        parentPane.addColumn(0, serverNameLabel);
        parentPane.addColumn(1, serverNameTextField);
    }

    private void createUsernameSetupRow(GridPane parentPane) {
        Label usernameLabel = new Label("Your username");
        TextField usernameTextField = new TextField();

        parentPane.addColumn(0, usernameLabel);
        parentPane.addColumn(1, usernameTextField);
    }

    private void createServerMessageRow(GridPane parentPane) {
        Label serverMessageLabel = new Label("Server welcome message");

        TextArea serverMessageTextArea = new TextArea();
        serverMessageTextArea.setPrefRowCount(10);

        parentPane.addColumn(0, serverMessageLabel);
        parentPane.addColumn(1, serverMessageTextArea);
    }

    private void createButtonRow(GridPane parentPane) {
        HBox buttonRow = new HBox();
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.setSpacing(HORIZONTAL_SPACING);

        Button connectButton = new Button("Host");

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(actionEvent -> windowStage.close());

        buttonRow.getChildren().addAll(connectButton, cancelButton);
        parentPane.addColumn(0, buttonRow);
    }

}
