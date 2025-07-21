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
    private boolean isPlacingIntersection = false;

    public enum intersectionType {
        TRAFFIC_LIGHT, ROUNDABOUT
    }

    @FXML
    public void initialize()
    {
        this.simulationService = new SimulationService();

        timeLabel.textProperty().bind(simulationService.simulationTimeProperty().asString("Time: %d s"));

        drawRoad();
        setupInitialCar();

        intersectionTypeComboBox.getItems().setAll("Traffic Light", "Roundabout");
        intersectionTypeComboBox.getSelectionModel().selectFirst();

        simulationPane.setOnMouseClicked(this::handlePaneClick);

        simulationService.getIntersections().addListener((ListChangeListener<IIntersection>) c ->
        {
            while (c.next())
            {
                if (c.wasAdded())
                {
                    for (IIntersection added : c.getAddedSubList())
                    {
                        createViewForIntersection(added);
                    }
                }
                // handle c.wasRemoved() for deletion logic
            }
        });
    }

    private void handlePaneClick(MouseEvent event)
    {
        if (isPlacingIntersection)
        {
            double x = event.getX();
            double y = event.getY();
            String selectedType = intersectionTypeComboBox.getValue();

            Optional<IIntersection> newIntersection = promptForIntersectionSettings(selectedType, x, y);

            newIntersection.ifPresent(IIntersection ->
            {
                simulationService.addIntersection(IIntersection);
            });

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

    private void createViewForIntersection(IIntersection intersection)
    {
        Node view = null;
        if (intersection instanceof TrafficLightIntersection)
        {
            Rectangle lightView = new Rectangle(10, 40);
            lightView.setFill(Color.DARKSLATEGRAY);
            // TODO: bind fill to the lightState properties
            view = lightView;
        } else if (intersection instanceof Roundabout)
        {
            Circle roundaboutView = new Circle(30, Color.DARKGRAY);
            roundaboutView.setStroke(Color.WHITE);
            view = roundaboutView;
        }

        if (view != null)
        {
            view.layoutXProperty().bind(intersection.positionXProperty());
            view.layoutYProperty().bind(intersection.positionYProperty());
            simulationPane.getChildren().add(view);

            Node finalView = view;
            view.setOnMouseClicked(event ->
            {
                if (!isPlacingIntersection)
                {
                    // prevent editing while in placement moe
                    // TODO: implement live editing dialog
                    System.out.println("Editing " + intersection.getClass().getSimpleName());
                    event.consume();
                }
            });
        }
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
