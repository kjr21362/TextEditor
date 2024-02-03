package com.kjianxin.texteditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ExecutionException;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

/**
 * TextEditorController. To add more details.
 */
public class TextEditorController {
    @FXML
    private TextArea textArea;

    /**
     * Open a file in the background and show the content in the text area.
     * @param event
     */
    @FXML
    public void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File fileToOpen = fileChooser.showOpenDialog(null);
        if (fileToOpen != null) {
            loadFile(fileToOpen);
        }
    }

    private void loadFile(File fileToOpen) {
        Task<String> loadFileTask = fileLoaderTask(fileToOpen);
        loadFileTask.run();
    }

    private Task<String> fileLoaderTask(File fileToOpen) {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(fileToOpen));
                StringBuilder fileContent = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    fileContent.append(line);
                    fileContent.append("\n");
                }

                return fileContent.toString();
            }
        };

        task.setOnSucceeded(workerStateEvent -> {
            try {
                textArea.setText(task.get());
            } catch (InterruptedException | ExecutionException e) {
                textArea.setText("Error opening file: " + fileToOpen.getAbsolutePath());
                throw new RuntimeException("Error opening file.", e);
            }
        });
        task.setOnFailed(workerStateEvent -> {
            textArea.setText("Error opening file: " + fileToOpen.getAbsolutePath());
        });
        return task;
    }
}