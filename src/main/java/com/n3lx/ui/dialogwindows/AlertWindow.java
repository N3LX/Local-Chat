package com.n3lx.ui.dialogwindows;

import com.n3lx.ui.util.Preferences;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class AlertWindow extends DialogWindow {

    private static final double VERTICAL_SPACING = 15;
    private static final double WINDOW_PADDING = 15;

    private String alertMessage;

    AlertWindow(Stage parentStage, String alertMessage) {
        super(parentStage);
        this.alertMessage = alertMessage;
        createUI();
    }

    private void createUI() {
        VBox windowContent = new VBox();
        windowContent.setAlignment(Pos.CENTER);
        windowContent.setSpacing(VERTICAL_SPACING);
        windowContent.setPadding(new Insets(WINDOW_PADDING));

        Label alertMessageLabel = new Label(alertMessage);
        alertMessageLabel.setTextAlignment(TextAlignment.CENTER);

        Button closeButton = new Button("OK");
        closeButton.setOnAction(actionEvent -> windowStage.close());

        windowContent.getChildren().addAll(alertMessageLabel, closeButton);

        Scene scene = new Scene(windowContent);
        windowStage.setScene(scene);
        windowStage.sizeToScene();
        windowStage.setResizable(false);

        windowStage.setTitle("Alert");
        scene.getStylesheets().addAll("stylesheet.css", Preferences.getThemeCssPath());
    }

}
