module com.kjr21362.text_editor {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.kjianxin.texteditor to javafx.fxml;
    exports com.kjianxin.texteditor;
}