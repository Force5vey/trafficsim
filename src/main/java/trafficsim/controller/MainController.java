package trafficsim.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MainController
{
    @FXML
    private Button someButton;

    @FXML
    public void initialize()
    {
        System.out.println("MainController initialized");
    }
}
