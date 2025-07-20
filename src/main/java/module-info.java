module force5dev {
    requires javafx.controls;
    requires javafx.fxml;

    opens force5dev to javafx.fxml;
    exports force5dev;
}
