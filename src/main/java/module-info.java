module com.kjianxin.text_editor {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires org.fxmisc.richtext;
    requires reactfx;


    opens com.kjianxin.texteditor to javafx.fxml;
    exports com.kjianxin.texteditor;
}