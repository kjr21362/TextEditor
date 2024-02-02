module com.kjr21362.text_editor {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.kjr21362.text_editor to javafx.fxml;
    exports com.kjr21362.text_editor;
}