package com.kjianxin.texteditor;

import static com.kjianxin.texteditor.JavaKeywordHighlighter.computeHighlighting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.reactfx.EventStream;
import org.reactfx.Subscription;

/**
 * TextEditorController. To add more details.
 */
public class TextEditorController {
    @FXML
    @Getter
    @Setter
    private CodeArea textArea;

    @FXML
    @Getter
    @Setter
    private TextField searchText;

    @FXML
    @Getter
    @Setter
    private HBox findBar;

    @FXML
    @Getter
    @Setter
    private HBox replaceBar;

    @FXML
    @Getter
    @Setter
    private TextField replaceText;

    private int lastMatchStartIdx = -1;
    private boolean hasHighlight = false;

    @Getter
    private ExecutorService keywordHighlightExecutor = Executors.newSingleThreadExecutor();

    private EventStream<StyleSpans<Collection<String>>> keywordHighlightEventStream;

    private Subscription keywordHighlightSubscription;

    Pattern pattern;
    Matcher matcher;
    private String prevSearchText = "";

    private String prevTextAreaStr = "";

    private List<String> content = List.of();

    @Getter
    private File openedFile;

    @Setter
    @Getter
    private Stage stage;

    private FileTime lastModifiedTime;

    @FXML
    public void newFile(ActionEvent event) {
        textArea.clear();
        lastMatchStartIdx = -1;
        setStageTitle("untitled");
        openedFile = null;
    }

    /**
     * Open a file in the background and show the content in the text area.
     *
     * @param event event.
     */
    @FXML
    public void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        //fileChooser.getExtensionFilters()
        //    .add(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        openedFile = fileChooser.showOpenDialog(null);
        if (openedFile != null) {
            loadFile(openedFile);
        }
    }

    /**
     * Replace keywords. Search the word and highlight it first (if not already highlighted),
     * then replace.
     *
     * @param event event.
     */
    @FXML
    public void replaceInFile(ActionEvent event) {
        replaceWithText(replaceText.getText());
    }

    private void replaceWithText(String text) {
        System.out.println("replace: " + text);
        if (lastMatchStartIdx == -1 || !hasHighlight) {
            findText(searchText.getText());
        } else {
            if (!text.isEmpty()) {
                textArea.replaceText(matcher.start(), matcher.end(), text);
                lastMatchStartIdx = matcher.end();
                findText(searchText.getText());
            }
        }
    }

    @FXML
    public void showFindBar(ActionEvent event) {
        findBar.setVisible(true);
    }

    @FXML
    public void hideFindBar(ActionEvent event) {
        findBar.setVisible(false);
    }

    @FXML
    public void showReplaceBar(ActionEvent event) {
        // find bar is required to find keyword for replacing
        findBar.setVisible(true);
        replaceBar.setVisible(true);
    }

    @FXML
    public void hideReplaceBar(ActionEvent event) {
        replaceBar.setVisible(false);
    }

    @FXML
    public void findInFile(ActionEvent event) {
        findText(searchText.getText());
    }

    public void findText(String text) {
        // TODO: Implement 'Find Previous' (search backwards)
        clearHighlight();

        if (text.isEmpty()) {
            return;
        }

        if (pattern == null || (!text.equals(prevSearchText))) {
            lastMatchStartIdx = -1;
            pattern = Pattern.compile(text, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
        }
        matcher = pattern.matcher(textArea.getText());
        int caretPos = textArea.getCaretPosition();
        int searchIdx = lastMatchStartIdx + 1;
        if ((caretPos < searchIdx) || (caretPos > searchIdx)) {
            searchIdx = caretPos;
        }

        if (matcher.find(searchIdx) || matcher.find()) {
            lastMatchStartIdx = matcher.start();
            prevSearchText = text;
            setHighlight(matcher.start(), matcher.end());

            textArea.moveTo(matcher.end());
            textArea.requestFollowCaret();
        } else {
            pattern = null;
            lastMatchStartIdx = -1;
            prevSearchText = "";
        }
    }

    public void setHighlight(int from, int to) {
        textArea.setStyle(from, to, Collections.singleton("highlight"));
        hasHighlight = true;
    }

    public void clearHighlight() {
        if (lastMatchStartIdx != -1) {
            textArea.setStyle(matcher.start(), matcher.end(), Collections.singleton("normal"));
            applyHighlighting(computeHighlighting(textArea.getText()));
            hasHighlight = false;
        }
    }

    /**
     * Save File action.
     *
     * @param event event.
     */
    @FXML
    public void saveFile(ActionEvent event) {
        saveToFile();
    }

    /**
     * Save content in the text area to file.
     */
    public void saveToFile() {
        if (openedFile == null) {
            // Save to a new file
            FileChooser fileChooser = new FileChooser();
            openedFile = fileChooser.showSaveDialog(null);
        }
        if (openedFile != null) {
            try {
                setStageTitle(openedFile.getName());

                FileWriter fileWriter = new FileWriter(openedFile);
                fileWriter.write(textArea.getText());
                prevTextAreaStr = textArea.getText();
                lastModifiedTime = getFileLastModifiedTime(openedFile);

                fileWriter.close();
            } catch (IOException e) {
                throw new RuntimeException("Error saving file.", e);
            }
        }
    }

    private void loadFile(File fileToOpen) {
        Task<List<String>> loadFileTask = fileLoaderTask(fileToOpen);
        loadFileTask.run();

        if (keywordHighlightSupported()) {
            keywordHighlightEventStream = textArea.multiPlainChanges()
                .successionEnds(java.time.Duration.ofMillis(100))
                .retainLatestUntilLater(keywordHighlightExecutor)
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(textArea.multiPlainChanges())
                .filterMap(t -> {
                    if (t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                });

            keywordHighlightSubscription =
                keywordHighlightEventStream.subscribe(this::applyHighlighting);
        } else {
            if (keywordHighlightSubscription != null) {
                keywordHighlightSubscription.unsubscribe();
            }
        }
    }

    private boolean keywordHighlightSupported() {
        return "java".equals(getFileExtension(openedFile).get());
    }

    private Task<List<String>> fileLoaderTask(File fileToOpen) {
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                /*BufferedReader bufferedReader = new BufferedReader(new FileReader(fileToOpen));
                StringBuilder fileContent = new StringBuilder();
                int c;
                while ((c = bufferedReader.read()) != -1) {
                    fileContent.append((char) c);
                }
                return fileContent.toString();*/
                Path path = fileToOpen.toPath();
                if (Files.exists(path)) {
                    try (Stream<String> stream = Files.lines(path)) {
                        content = stream.collect(Collectors.toList());
                    } catch (IOException e) {

                    }
                }
                return content;
            }
        };

        task.setOnSucceeded(workerStateEvent -> {
            System.out.println("fileLoaderTask succeeded");
            //textArea.setText(task.get());
            textArea.clear();
            for (int i = 0; i < content.size(); i++) {
                textArea.appendText(content.get(i));
                textArea.appendText(System.lineSeparator());
            }

            prevTextAreaStr = textArea.getText();

            setStageTitle(fileToOpen.getName());
            openedFile = fileToOpen;

            lastModifiedTime = getFileLastModifiedTime(fileToOpen);
            scheduleFileModifiedCheck(openedFile);
        });

        task.setOnFailed(workerStateEvent -> {
            //textArea.setText("Error opening file: " + fileToOpen.getAbsolutePath());
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

    /**
     * Check if the file is modified based on its last modified time.
     *
     * @return true if it's modified, otherwise false.
     */
    public boolean isFileModified() {
        if (openedFile == null) {
            return false;
        }

        FileTime lastModifiedNow = getFileLastModifiedTime(openedFile);
        return lastModifiedNow.compareTo(lastModifiedTime) > 0;
    }

    public boolean isFileEdited() {
        return !prevTextAreaStr.equals(textArea.getText());
    }

    private void setStageTitle(String title) {
        stage.setTitle(title);
    }

    private ScheduledService<Boolean> createFileModifiedCheckService(File file) {
        ScheduledService<Boolean> scheduledService = new ScheduledService<>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<>() {
                    @Override
                    protected Boolean call() {
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

    public Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = textArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<>() {
            @Override
            protected StyleSpans<Collection<String>> call() {
                return computeHighlighting(text);
            }
        };
        keywordHighlightExecutor.execute(task);
        return task;
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        textArea.setStyleSpans(0, highlighting);
    }

    public Optional<String> getFileExtension(File file) {
        String name = file.getName();
        return Optional.ofNullable(name)
            .filter(f -> f.contains("."))
            .map(f -> f.substring(name.lastIndexOf(".") + 1));
    }
}