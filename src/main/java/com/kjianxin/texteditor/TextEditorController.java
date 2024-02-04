package com.kjianxin.texteditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ExecutionException;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

/**
 * TextEditorController. To add more details.
 */
public class TextEditorController {
    @FXML
    @Getter
    @Setter
    private TextArea textArea;

    private String prevTextAreaStr;

    @Getter
    private File openedFile;

    @Setter
    @Getter
    private Stage stage;

    private FileTime lastModifiedTime;

    /**
     * Open a file in the background and show the content in the text area.
     *
     * @param event event.
     */
    @FXML
    public void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        openedFile = fileChooser.showOpenDialog(null);
        if (openedFile != null) {
            loadFile(openedFile);
        }
    }

    /**
     * Save content in the text area to file.
     *
     * @param event event.
     */
    @FXML
    public void saveFile(ActionEvent event) {
        saveToFile();
    }

    public void saveToFile() {
        if (openedFile == null) {
            // Save to a new file
            FileChooser fileChooser = new FileChooser();
            openedFile = fileChooser.showSaveDialog(null);
        }
        if (openedFile != null) {
            try {
                setStageTitle(openedFile);

                FileWriter fileWriter = new FileWriter(openedFile);
                fileWriter.write(textArea.getText());
                prevTextAreaStr = textArea.getText();
                fileWriter.close();
            } catch (IOException e) {
                throw new RuntimeException("Error saving file.", e);
            }
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
                prevTextAreaStr = textArea.getText();
                setStageTitle(fileToOpen);
                openedFile = fileToOpen;

                lastModifiedTime = getFileLastModifiedTime(fileToOpen);
                scheduleFileModifiedCheck(openedFile);
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

    private static FileTime getFileLastModifiedTime(File fileToOpen) {
        try {
            return Files.readAttributes(fileToOpen.toPath(), BasicFileAttributes.class)
                .lastModifiedTime();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isFileModified() {
        if (openedFile == null) {
            return true;
        }

        FileTime lastModifiedNow = getFileLastModifiedTime(openedFile);
        return lastModifiedNow.compareTo(lastModifiedTime) > 0;
    }

    public boolean isFileEdited() {
        return !prevTextAreaStr.equals(textArea.getText());
    }

    private void setStageTitle(File openedFile) {
        stage.setTitle(openedFile.getName());
    }

    private ScheduledService<Boolean> createFileModifiedCheckService(File file) {
        ScheduledService<Boolean> scheduledService = new ScheduledService<>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        return isFileModified();
                    }
                };
            }
        };
        scheduledService.setPeriod(Duration.seconds(2));
        return scheduledService;
    }

    private void scheduleFileModifiedCheck(File file) {
        ScheduledService<Boolean> scheduledService = createFileModifiedCheckService(file);
        scheduledService.setOnSucceeded(workerStateEvent -> {
            if (scheduledService.getLastValue() == null) {
                return;
            }
            if (scheduledService.getLastValue()) {
                scheduledService.cancel();
                // notify change
                loadFile(file);
            }
        });
        scheduledService.start();
    }
}