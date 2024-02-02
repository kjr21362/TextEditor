package com.kjianxin.texteditor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * HelloController. To add more details.
 */
public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}