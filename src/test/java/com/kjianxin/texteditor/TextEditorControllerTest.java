package com.kjianxin.texteditor;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
class TextEditorControllerTest {

    @Start
    private void start(Stage stage) throws IOException {
        System.out.println("start");
        FXMLLoader fxmlLoader =
            new FXMLLoader(TextEditorApplication.class.getResource("text-editor-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);

        stage.setScene(scene);
        stage.show();
    }

    @Test
    void test(FxRobot robot) {
        System.out.println("test");
    }
}