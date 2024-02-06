package com.kjianxin.texteditor;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;

@ExtendWith({ApplicationExtension.class})
class TextEditorApplicationTest extends ApplicationTest {

    TextEditorApplication textEditorApplication;

    @Override
    public void start(Stage stage) throws Exception {
        textEditorApplication = new TextEditorApplication();
        textEditorApplication.start(stage);
    }

    @Test
    void testInitLineNumberColWhenStart(FxRobot robot) {
        // lookup is based on css id, not fx id
        VBox lineNumberCol = lookup("#lineNumberCol").queryAs(VBox.class);
        Assertions.assertThat(lineNumberCol.getChildren().size() == 1);

        Label label = (Label) lineNumberCol.getChildren().get(0);
        Assertions.assertThat(label.getTextFill().equals(Color.WHITE));
        Assertions.assertThat(label.getText().equals("1"));

    }

    @Test
    void test2(FxRobot robot) {
        System.out.println("test2");
    }

}