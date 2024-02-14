package com.kjianxin.texteditor;

import java.util.Optional;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.fxmisc.richtext.LineNumberFactory;

/**
 * TextEditorApplication. To add more details.
 */
public class TextEditorApplication extends Application {
    TextEditorController textEditorController;

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader =
            new FXMLLoader(TextEditorApplication.class.getResource("text-editor-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        scene.getStylesheets().add(TextEditorApplication.class.getResource("textarea.css").toExternalForm());
        if (textEditorController == null) {
            textEditorController = fxmlLoader.getController();
        }
        textEditorController.setStage(stage);
        textEditorController.getTextArea().setParagraphGraphicFactory(LineNumberFactory.get(textEditorController.getTextArea()));

        textEditorController.getFindBar().setVisible(false);
        textEditorController.getReplaceBar().setVisible(false);
        textEditorController.getReplaceBar().managedProperty().bind(textEditorController.getReplaceBar().visibleProperty());
        textEditorController.getFindBar().managedProperty().bind(textEditorController.getFindBar().visibleProperty());

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

        textEditorController.getSearchText().setOnKeyTyped(e -> {
            textEditorController.findText(textEditorController.getSearchText().getText());
        });

        textEditorController.getTextArea().setOnMouseClicked(e -> {
            textEditorController.clearHighlight();
        });
    }

    public static void main(String[] args) {
        launch();
    }
}