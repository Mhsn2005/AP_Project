module JavaFX_Test {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.prefs;
    requires java.sql;

    opens org.example to javafx.fxml;

    exports org.example;
}
