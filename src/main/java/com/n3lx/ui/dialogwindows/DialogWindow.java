package com.n3lx.ui.dialogwindows;

import javafx.stage.Modality;
import javafx.stage.Stage;

public abstract class DialogWindow {

    protected final Stage windowStage;

    /*
    Make the constructor package-private so that the abstract class cannot be instantiated
    */
    DialogWindow(Stage parentStage) {
        windowStage = new Stage();
        anchorToParentStage(parentStage);
        createUI();
    }

    private void anchorToParentStage(Stage parentStage) {
        windowStage.initOwner(parentStage);
        windowStage.initModality(Modality.APPLICATION_MODAL);
    }

    public Stage getWindow() {
        return windowStage;
    }

    protected abstract void createUI();

}
