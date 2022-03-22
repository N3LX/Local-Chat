package com.n3lx.ui.dialogwindows;

import javafx.stage.Modality;
import javafx.stage.Stage;

public abstract class DialogWindow {

    Stage parentStage;

    Stage windowStage;

    /*
    Make the constructor package-private so that the abstract class cannot be instantiated
    */
    DialogWindow(Stage parentStage) {
        this.parentStage = parentStage;
        windowStage = new Stage();
        anchorToParentStage();
    }

    private void anchorToParentStage() {
        windowStage.initOwner(parentStage);
        windowStage.initModality(Modality.APPLICATION_MODAL);
    }

    public abstract Stage getWindow();

}
