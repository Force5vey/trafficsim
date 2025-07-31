/***************************************************************

- File:        MainApplication.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Main JavaFX application entry point for TrafficSim.

- Description:
- Initializes and launches the JavaFX application, loads the main FXML
- view, sets up the primary stage, and manages application shutdown.
- Integrates JMetro styling and connects the UI controller.

***************************************************************/

package trafficsim.app;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import trafficsim.ui.controller.MainController;

public class MainApplication extends Application
{
    private MainController controller;

    /**
     * Initializes and starts the JavaFX application.
     * Loads the main FXML view, sets up the scene and stage, and applies
     * the JMetro dark style. Stores a reference to the main controller.
     *
     * @param stage The primary stage for this application.
     * @throws IOException If the FXML resource cannot be loaded.
     */
    @Override
    public void start(Stage stage) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/trafficsim/view/MainView.fxml"));

        Parent root = fxmlLoader.load();

        this.controller = fxmlLoader.getController();

        Scene scene = new Scene(root, 1280, 720);

        JMetro jMetro = new JMetro(Style.DARK);
        jMetro.setScene(scene);

        stage.setTitle("Traffic Sim");
        stage.setScene(scene);
        stage.show();
    }

    /**
    * Called when the application is stopping.
    * Shuts down the simulation engine via the main controller.
    */
    @Override
    public void stop()
    {
        if (controller != null)
        {
            controller.shutdownEngine();
        }
    }

    public static void main(String[] args)
    {
        launch();
    }
}