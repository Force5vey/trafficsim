package trafficsim.ui.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.text.NumberFormat;
import java.util.Optional;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import trafficsim.core.model.Car;
import trafficsim.core.model.Intersection;
import trafficsim.core.model.Roundabout;
import trafficsim.core.model.SignalisedIntersection;
import trafficsim.core.model.TrafficLightState;
import trafficsim.core.sim.SimulationEngine;
import trafficsim.ui.view.SimulationRenderer;

public class MainController
{
    private static final double MIN_PLACEMENT_DISTANCE = 50.0;

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
    private Button addIntersectionButton;
    @FXML
    private Button addRoadButton;

    private SimulationEngine simulationService;
    private SimulationRenderer simulationRenderer;

    public enum InteractionMode {
        NORMAL, PLACING_INTERSECTION, PLACING_ROAD
    }

    private InteractionMode currentMode = InteractionMode.NORMAL;

    @FXML
    public void initialize()
    {
        this.simulationService = new SimulationEngine();
        this.simulationRenderer = new SimulationRenderer(simulationPane, simulationService, this);

        timeLabel.textProperty().bind(simulationService.simulationTimeProperty().asString("Time: %d s"));

        setupInitialCar();

        simulationPane.setOnMouseClicked(this::handlePaneClick);
    }

    public InteractionMode getCurrentMode()
    {
        return currentMode;
    }

    private void handlePaneClick(MouseEvent event)
    {
        if (currentMode == InteractionMode.PLACING_INTERSECTION)
        {
            double x = event.getX();
            double y = event.getY();

            for (Intersection existing : simulationService.getIntersections())
            {
                double distance = Math
                        .sqrt(Math.pow(existing.getPositionX() - x, 2) + Math.pow(existing.getPositionY() - y, 2));
                if (distance < MIN_PLACEMENT_DISTANCE)
                {
                    // TODO: have a visual clue that it is too close to an existing intersection
                    // probably just highlight the existing and just "ignore" the click
                    System.err.println("Cannot place intersection: Too close to another.");
                    return;
                }
            }

            Optional<Intersection> newIntersection = promptForIntersectionSettings(x, y);

            newIntersection.ifPresent(simulationService::addIntersection);

            currentMode = InteractionMode.NORMAL;
            simulationPane.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }

    @FXML
    private void handleAddIntersectionRequest()
    {
        currentMode = InteractionMode.PLACING_INTERSECTION;
        simulationPane.getScene().setCursor(javafx.scene.Cursor.CROSSHAIR);
    }

    @FXML
    private void handleAddRoadRequest()
    {
        // TODO: add road stuff
        System.out.println("Enterying road drawing mode...");
    }

    public void showEditIntersectionDialog(Intersection intersection)
    {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Intersection");
        dialog.setHeaderText("Update properties for " + intersection.getClass().getSimpleName());

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, deleteButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // dialog is based on intersection type
        if (intersection instanceof SignalisedIntersection)
        {
            SignalisedIntersection model = (SignalisedIntersection) intersection;
            TextField totalCycleField = new TextField(String.valueOf(model.getTotalCycleTime()));
            TextField yellowField = new TextField(String.valueOf(model.getYellowDuration()));

            grid.add(new Label("Total Cycle (s):"), 0, 0);
            grid.add(totalCycleField, 1, 0);
            grid.add(new Label("Yellow (s):"), 0, 1);
            grid.add(yellowField, 1, 1);

            dialog.setResultConverter(btn ->
            {
                if (btn == updateButtonType)
                {
                    try
                    {
                        model.setTotalCycleTime(Double.parseDouble(totalCycleField.getText()));
                        model.setYellowDuration(Double.parseDouble(yellowField.getText()));
                    } catch (NumberFormatException e)
                    {
                        System.err.println("Invalid number format in edit dialog.");
                    }
                }
                return btn == deleteButtonType;
            });
        } else if (intersection instanceof Roundabout)
        {
            Roundabout model = (Roundabout) intersection;
            TextField speed = new TextField(String.valueOf(model.getSpeedLimit()));
            grid.add(new Label("Speed (px/s):"), 0, 0);
            grid.add(speed, 1, 0);

            dialog.setResultConverter(btn ->
            {
                if (btn == updateButtonType)
                {
                    try
                    {
                        model.setSpeedLimit(Double.parseDouble(speed.getText()));
                    } catch (NumberFormatException e)
                    {
                        System.err.println("Invalid number format in edit dialog.");
                    }
                }
                return btn == deleteButtonType;
            });
        }

        dialog.getDialogPane().setContent(grid);
        Optional<Boolean> deleteRequested = dialog.showAndWait();

        if (deleteRequested.isPresent() && deleteRequested.get())
        {
            simulationService.removeIntersection(intersection);
        }
    }

    private Optional<Intersection> promptForIntersectionSettings(double x, double y)
    {
        Dialog<Intersection> dialog = new Dialog<>();
        dialog.setTitle("Configure Intersection");
        dialog.setHeaderText("Set properties for the new intersection.");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().setAll("Traffic Light", "Roundabout");

        TextField param1Field = new TextField();
        Label param1Label = new Label();
        TextField param2Field = new TextField();
        Label param2Label = new Label();

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeComboBox, 1, 0);

        grid.add(param1Label, 0, 1);
        grid.add(param1Field, 1, 1);
        grid.add(param2Label, 0, 2);
        grid.add(param2Field, 1, 2);

        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) ->
        {
            if ("Traffic Light".equals(newVal))
            {
                param1Label.setText("Total Cycle (s):");
                param1Field.setText("40");
                param2Label.setText("Yellow (s):");
                param2Field.setText("3");
                param2Label.setVisible(true);
                param2Field.setVisible(true);
            } else if ("Roundabout".equals(newVal))
            {
                param1Label.setText("Speed (px/s):");
                param1Field.setText("25");
                param2Label.setVisible(false);
                param2Field.setVisible(false);
            }
        });

        typeComboBox.getSelectionModel().selectFirst();
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton ->
        {
            if (dialogButton == createButtonType)
            {
                try
                {
                    String type = typeComboBox.getValue();
                    if ("Traffic Light".equals(type))
                    {
                        double totalTime = Double.parseDouble(param1Field.getText());
                        double yellow = Double.parseDouble(param2Field.getText());
                        SignalisedIntersection intersection = new SignalisedIntersection(x, y, totalTime, yellow);
                        return intersection;
                    } else if ("Roundabout".equals(type))
                    {
                        double speed = Double.parseDouble(param1Field.getText());
                        return new Roundabout(x, y, speed);
                    }
                } catch (NumberFormatException e)
                {
                    System.err.println("Invalid number format in dialog.");
                    return null;
                }
            }
            return null;
        });

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
