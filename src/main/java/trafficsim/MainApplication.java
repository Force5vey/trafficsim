package trafficsim;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class MainApplication extends Application
{
    // private static Scene scene;
    @Override
    public void start(Stage stage) throws IOException
    {
        // Parent root = loadFXML("primary");

        // scene = new Scene(loadFXML("primary"), 800, 600);

        // JMetro jMetro = new JMetro(Style.DARK);
        // jMetro.setScene(scene);

        // stage.setTitle("Traffic Sim");
        // stage.setScene(scene);
        // stage.show();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/trafficsim/view/MainView.fxml"));

        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 1280, 720);

        JMetro jMetro = new JMetro(Style.DARK);
        jMetro.setScene(scene);

        stage.setTitle("Traffic Sim");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args)
    {
        launch();
    }
}