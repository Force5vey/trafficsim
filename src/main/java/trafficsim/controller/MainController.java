package trafficsim.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.text.NumberFormat;
import java.util.Optional;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;

import trafficsim.model.Car;
import trafficsim.service.SimulationService;
import trafficsim.model.IIntersection;
import trafficsim.model.Roundabout;
import trafficsim.model.TrafficLightIntersection;
import trafficsim.view.SimulationRenderer;

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
    @FXML
    private ComboBox<String> intersectionTypeComboBox;
    @FXML
    private Button addIntersectionButton;

    private SimulationService simulationService;
    private SimulationRenderer simulationRenderer;
    private boolean isPlacingIntersection = false;

    public enum intersectionType {
        TRAFFIC_LIGHT, ROUNDABOUT
    }

    @FXML
    public void initialize()
    {
        this.simulationService = new SimulationService();
        this.simulationRenderer = new SimulationRenderer(simulationPane, simulationService);

        timeLabel.textProperty().bind(simulationService.simulationTimeProperty().asString("Time: %d s"));

        setupInitialCar();

        intersectionTypeComboBox.getItems().setAll("Traffic Light", "Roundabout");
        intersectionTypeComboBox.getSelectionModel().selectFirst();

        simulationPane.setOnMouseClicked(this::handlePaneClick);

    }

    private void handlePaneClick(MouseEvent event)
    {
        if (isPlacingIntersection)
        {
            double x = event.getX();
            double y = event.getY();
            String selectedType = intersectionTypeComboBox.getValue();

            Optional<IIntersection> newIntersection = promptForIntersectionSettings(selectedType, x, y);

            newIntersection.ifPresent(simulationService::addIntersection);

            isPlacingIntersection = false;
            simulationPane.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }

    @FXML
    private void handleAddIntersectionRequest()
    {
        isPlacingIntersection = true;
        simulationPane.getScene().setCursor(javafx.scene.Cursor.CROSSHAIR);
    }

    private Optional<IIntersection> promptForIntersectionSettings(String type, double x, double y)
    {
        Dialog<IIntersection> dialog = new Dialog<>();
        dialog.setTitle("Configure Intersection");
        dialog.setHeaderText("Set properties for the new " + type);

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        if ("Traffic Light".equals(type))
        {
            TextField greenDuration = new TextField("10");
            TextField yellowDuration = new TextField("3");

            grid.add(new Label("Green Duration (s):"), 0, 0);
            grid.add(greenDuration, 1, 0);
            grid.add(new Label("Yellow Duration (s):"), 0, 1);
            grid.add(yellowDuration, 1, 1);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton ->
            {
                if (dialogButton == createButtonType)
                {
                    try
                    {
                        double green = Double.parseDouble(greenDuration.getText());
                        double yellow = Double.parseDouble(yellowDuration.getText());
                        return new TrafficLightIntersection(x, y, green, yellow);
                    } catch (NumberFormatException e)
                    {
                        return null;
                    }
                }
                return null;
            });
        } else if ("Roundabout".equals(type))
        {
            TextField speedLimit = new TextField("25");

            grid.add(new Label("Speed Limit (px/s):"), 0, 0);
            grid.add(speedLimit, 1, 0);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton ->
            {
                if (dialogButton == createButtonType)
                {
                    try
                    {
                        double speed = Double.parseDouble(speedLimit.getText());
                        return new Roundabout(x, y, speed);
                    } catch (NumberFormatException e)
                    {
                        return null;
                    }
                }
                return null;
            });
        }
        return dialog.showAndWait();
    }

    private void setupInitialCar()
    {
        Car carModel = new Car();
        carModel.setXPosition(50);
        carModel.setSpeed(25); // TODO: establish all speeds at MPH, roads will need mileage scale. Perhaps feet/minute for scaling drawings and then the dsplay speed can just be converted 

        simulationService.addCar(carModel);

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
