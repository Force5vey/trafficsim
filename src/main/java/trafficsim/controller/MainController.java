package trafficsim.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import trafficsim.model.Car;
import trafficsim.service.SimulationService;

public class MainController
{
    @FXML
    private Pane simulationPane;
    @FXML
    private Button startButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button stopButton;
    @FXML
    private Label timeLabel;

    private SimulationService simulationService;

    @FXML
    public void initialize()
    {
        this.simulationService = new SimulationService();

        timeLabel.textProperty().bind(simulationService.simulationTimeProperty().asString("Time: %d s"));

        drawRoad();
        setupInitialCar();
    }

    private void drawRoad()
    {
        // Placeholder for future robust road drawing method    
        Rectangle road = new Rectangle(0, 335, 1280, 50);
        road.setFill(Color.GRAY);
        simulationPane.getChildren().add(road);
    }

    private void setupInitialCar()
    {
        Car carModel = new Car();
        carModel.setXPosition(50);
        carModel.setSpeed(25); // TODO: establish all speeds at MPH, roads will need mileage scale. Perhaps feet/minute for scaling drawings and then the dsplay speed can just be converted 

        simulationService.addCar(carModel);

        Rectangle carView = new Rectangle(40, 20);
        carView.setFill(Color.CORNFLOWERBLUE);

        carView.xProperty().bind(carModel.xPositionProperty());
        carView.setY(350);

        simulationPane.getChildren().add(carView);

        // Needs proper model management system integrated
    }

    @FXML
    private void handleStart()
    {
        simulationService.start();
    }

    @FXML
    private void handlePause()
    {
        simulationService.pause();
    }

    @FXML
    private void handleStop()
    {
        simulationService.stop();
        // need to reset car's visual position here
        // need better model-view synchronization on reset
    }
}
