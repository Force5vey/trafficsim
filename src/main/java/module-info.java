module force5dev
{
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jfxtras.styles.jmetro;

    opens trafficsim.controller to javafx.fxml;

    exports trafficsim;
    exports trafficsim.controller;
}
