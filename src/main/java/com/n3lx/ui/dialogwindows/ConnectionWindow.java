package com.n3lx.ui.dialogwindows;

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

public class ConnectionWindow extends DialogWindow {

    private static final double VERTICAL_SPACING = 15;
    private static final double HORIZONTAL_SPACING = 10;
    private static final double WINDOW_PADDING = 15;

    public ConnectionWindow(Stage parentStage) {
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
        createServerSelectionRow(selectionPane);
        createUsernameSetupRow(selectionPane);
        windowContent.addColumn(0, selectionPane);

        createButtonRow(windowContent);

        Scene scene = new Scene(windowContent);
        windowStage.setScene(scene);
        windowStage.sizeToScene();
        windowStage.setResizable(false);

        windowStage.setTitle("Connect to a server");
        scene.getStylesheets().add("stylesheet.css");
    }

    private void createServerSelectionRow(GridPane parentPane) {
        Label serverSelectionLabel = new Label("Select server");
        ListView<String> serverSelectionList = new ListView<>();

        parentPane.addColumn(0, serverSelectionLabel);
        parentPane.addColumn(1, serverSelectionList);
    }

    private void createUsernameSetupRow(GridPane parentPane) {
        Label usernameLabel = new Label("Your username");
        TextField usernameTextField = new TextField();

        parentPane.addColumn(0, usernameLabel);
        parentPane.addColumn(1, usernameTextField);
    }

    private void createButtonRow(GridPane parentPane) {
        HBox buttonRow = new HBox();
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.setSpacing(HORIZONTAL_SPACING);

        Button connectButton = new Button("Connect");

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(actionEvent -> windowStage.close());

        buttonRow.getChildren().addAll(connectButton, cancelButton);
        parentPane.addColumn(0, buttonRow);
    }

}
