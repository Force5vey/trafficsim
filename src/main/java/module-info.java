module trafficsim
{
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jfxtras.styles.jmetro;
    requires javafx.graphics;

    opens trafficsim.ui.controller to javafx.fxml;

    exports trafficsim.app;
    exports trafficsim.ui.controller;
}
