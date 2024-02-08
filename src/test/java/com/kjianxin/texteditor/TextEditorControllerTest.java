package com.kjianxin.texteditor;

import javafx.stage.Stage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;

@ExtendWith({ApplicationExtension.class})
class TextEditorControllerTest extends ApplicationTest {

    TextEditorApplication textEditorApplication;
    TextEditorController textEditorController;

    @Override
    public void start(Stage stage) throws Exception {
        textEditorApplication = new TextEditorApplication();
        textEditorApplication.start(stage);
        textEditorController = textEditorApplication.textEditorController;
    }
}