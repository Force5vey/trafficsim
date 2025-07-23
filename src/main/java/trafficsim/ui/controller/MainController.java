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
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;

import trafficsim.core.model.*;
import trafficsim.core.model.Car;
import trafficsim.core.model.Intersection;
import trafficsim.core.model.Roundabout;
import trafficsim.core.model.SignalisedIntersection;
import trafficsim.core.model.TrafficLightState;
import trafficsim.core.sim.SimulationEngine;
import trafficsim.ui.adapter.IntersectionUtil;
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

    private SimulationEngine engine;
    private SimulationRenderer simulationRenderer;

    public enum InteractionMode {
        NORMAL, PLACING_INTERSECTION, PLACING_ROAD
    }

    private InteractionMode currentMode = InteractionMode.NORMAL;
    private Intersection firstPicked = null;

    @FXML
    public void initialize()
    {
        this.engine = new SimulationEngine();
        this.simulationRenderer = new SimulationRenderer(simulationPane, engine, this);

        // timeLabel.textProperty().bind(engine.simulationTimeProperty().asString("Time: %d s"));

        simulationPane.setOnMouseClicked(this::handlePaneClick);
    }

    public InteractionMode getCurrentMode()
    {
        return currentMode;
    }

    private void handlePaneClick(MouseEvent event)
    {
        if (currentMode != InteractionMode.PLACING_INTERSECTION)
        {
            return;
        }

        double xWorld = event.getX() / IntersectionUtil.PX_PER_M;
        double yWorld = event.getY() / IntersectionUtil.PX_PER_M;

        if (!isFarEnough(event.getX(), event.getY()))
        {
            showWarning("Too close to another intersection (min " + MIN_PLACEMENT_DISTANCE + " px).");
            return;
        }

        Optional<Intersection> opt = promptForIntersectionSettings(xWorld, yWorld);
        opt.ifPresent(i ->
        {
            engine.addIntersection(i);
            simulationRenderer.onIntersectionAdded(i);
        });

        currentMode = InteractionMode.NORMAL;
        simulationPane.getScene().setCursor(Cursor.DEFAULT);
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
        currentMode = InteractionMode.PLACING_ROAD;
        firstPicked = null;
        simulationPane.getScene().setCursor(Cursor.CROSSHAIR);
    }

    public void onIntersectionPickedForRoad(Intersection picked)
    {
        if (firstPicked == null)
        {
            firstPicked = picked;
            return;
        }

        // second intersection
        Intersection secondPicked = picked;
        Optional<Road> opt = promptForRoadSettings(firstPicked, secondPicked);
        opt.ifPresent(r ->
        {
            engine.addRoad(r);
            simulationRenderer.onRoadAdded(r);
        });

        currentMode = InteractionMode.NORMAL;
        firstPicked = null;
        simulationPane.getScene().setCursor(Cursor.DEFAULT);
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
            // TextField totalCycleField = new TextField(String.valueOf(model.getTotalCycleTime()));
            // TextField yellowField = new TextField(String.valueOf(model.getYellowDuration()));

            grid.add(new Label("Total Cycle (s):"), 0, 0);
            // grid.add(totalCycleField, 1, 0);
            grid.add(new Label("Yellow (s):"), 0, 1);
            // grid.add(yellowField, 1, 1);

            dialog.setResultConverter(btn ->
            {
                if (btn == updateButtonType)
                {
                    try
                    {
                        // model.setTotalCycleTime(Double.parseDouble(totalCycleField.getText()));
                        // model.setYellowDuration(Double.parseDouble(yellowField.getText()));
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
            engine.removeIntersection(intersection);
        }
    }

    public void showEditRoadDialog(Road road)
    {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Road");
        ButtonType update = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        ButtonType delete = new ButtonType("Delete", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(update, delete, ButtonType.CANCEL);

        GridPane g = new GridPane();
        TextField spd = new TextField(String.valueOf(road.speedLimit()));
        g.add(new Label("Speed (m/s):"), 0, 0);
        g.add(spd, 1, 0);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(btn ->
        {
            if (btn == update)
            {
                road.setSpeedLimit(Double.parseDouble(spd.getText()));
            }
            return btn == delete;
        });

        if (dialog.showAndWait().orElse(false))
        {
            engine.removeRoad(road);
            simulationRenderer.removeRoad(road);
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

    private Optional<Road> promptForRoadSettings(Intersection a, Intersection b)
    {
        Dialog<Road> dialog = new Dialog<>();
        dialog.setTitle("New Road");
        ButtonType ok = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(10);
        TextField speed = new TextField("35");
        g.add(new Label("Speed limit (m/s):"), 0, 0);
        g.add(speed, 1, 0);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(btn ->
        {
            if (btn == ok)
            {
                double length = a.position().distanceTo(b.position());
                double velocity = Double.parseDouble(speed.getText());
                return new Road(a, b, length, velocity);
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private boolean isFarEnough(double newPxX, double newPxY)
    {
        for (Intersection i : simulationRenderer.getIntersections())
        {
            double dx = newPxX - IntersectionUtil.toPx(i.position().x);
            double dy = newPxY - IntersectionUtil.toPx(i.position().y);
            if (Math.hypot(dx, dy) < MIN_PLACEMENT_DISTANCE)
            {
                return false;
            }
        }
        return true;
    }

    private void showWarning(String msg)
    {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    @FXML
    private void handleStart()
    {
        engine.start();
    }

    @FXML
    private void handlePause()
    {
        engine.pause();
    }

    @FXML
    private void handleStop()
    {
        engine.stop();
        // need to reset car's visual position here
        // need better model-view synchronization on reset
    }
}
