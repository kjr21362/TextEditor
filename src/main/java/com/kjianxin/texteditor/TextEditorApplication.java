package com.kjianxin.texteditor;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * TextEditorApplication. To add more details.
 */
public class TextEditorApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TextEditorApplication.class.getResource("text-editor-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        ((TextEditorController) fxmlLoader.getController()).setStage(stage);

        stage.setTitle("untitled");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}