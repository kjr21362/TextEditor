package com.kjianxin.texteditor;

import java.io.IOException;
import java.util.Optional;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * TextEditorApplication. To add more details.
 */
public class TextEditorApplication extends Application {
    TextEditorController textEditorController;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TextEditorApplication.class.getResource("text-editor-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        if (textEditorController == null) {
            textEditorController = fxmlLoader.getController();
        }
        textEditorController.setStage(stage);
        textEditorController.initLineNumberCol();

        stage.setTitle("untitled");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(windowEvent -> {
            if (textEditorController.isFileModified() || textEditorController.isFileEdited()) {
                Alert alert = new Alert(Alert.AlertType.NONE);
                alert.getButtonTypes().remove(ButtonType.OK);
                alert.getButtonTypes().add(ButtonType.YES);
                alert.getButtonTypes().add(ButtonType.NO);
                alert.getButtonTypes().add(ButtonType.CANCEL);
                alert.setTitle("Quit application");
                alert.setContentText("Do you want to save the changes?");

                Optional<ButtonType> choice = alert.showAndWait();
                if (choice.isPresent()) {
                    if (choice.get().equals(ButtonType.YES)) {
                        textEditorController.saveToFile();
                    } else if (choice.get().equals(ButtonType.CANCEL)) {
                        windowEvent.consume();
                    }
                }
            }

        });

        textEditorController.getTextArea().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.BACK_SPACE) {
                //System.out.println("keyCode enter|back_space");
                int nLines = textEditorController.getTextArea().getText().split(System.lineSeparator(), -1).length;
                textEditorController.generateLineNumberCol(nLines);
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}