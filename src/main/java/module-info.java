module com.kjianxin.text_editor {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;


    opens com.kjianxin.texteditor to javafx.fxml;
    exports com.kjianxin.texteditor;
}