package trafficsim.ui.controller;

import java.util.List;
import java.util.Optional;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import trafficsim.ui.adapter.UnitConverter;
import trafficsim.core.model.Car;
import trafficsim.core.model.Intersection;
import trafficsim.core.model.Road;
import trafficsim.core.model.Roundabout;
import trafficsim.core.model.SignalisedIntersection;
import trafficsim.core.sim.SimulationEngine;
import trafficsim.ui.adapter.IntersectionUtil;
import trafficsim.ui.view.SimulationRenderer;
import trafficsim.ui.view.intersection.IntersectionView;
import trafficsim.ui.view.intersection.SignalisedIntersectionView;

public class MainController
{
    private static final double MIN_PLACEMENT_DISTANCE = 50.0;

    @FXML
    private Pane simulationStackPane;
    @FXML
    private Pane intersectionPane;
    @FXML
    private Pane roadPane;
    @FXML
    private Pane carPane;
    @FXML
    private Pane lightPane;

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
    @FXML
    private Button addCarButton;

    @FXML
    private TitledPane propertiesPane;
    @FXML
    private GridPane propertiesGrid;
    @FXML
    private Label validationLabel;

    private SimulationEngine engine;
    private SimulationRenderer simulationRenderer;

    public enum InteractionMode {
        NORMAL, PLACING_INTERSECTION, PLACING_ROAD, PLACING_CAR
    }

    private InteractionMode currentMode = InteractionMode.NORMAL;
    private Intersection firstPicked = null; // for roads

    private ComboBox<String> intersectionTypeCombo;
    private Label param1Label, param2Label;
    private TextField param1Field, param2Field;
    private Label roadSpeedLabel;
    private TextField roadSpeedField;
    private Label carMaxSpeedLabel, carAccelLabel;
    private TextField carMaxSpeedField, carAccelField;

    private Label propertiesPlaceholderLabel;

    @FXML
    public void initialize()
    {
        this.engine = new SimulationEngine();
        this.simulationRenderer = new SimulationRenderer(intersectionPane, roadPane, carPane, lightPane, engine, this);

        intersectionPane.setPickOnBounds(false);
        roadPane.setPickOnBounds(false);
        carPane.setPickOnBounds(false);
        lightPane.setPickOnBounds(false);

        simulationStackPane.setOnMouseClicked(this::handlePaneClick);

        createPropertyControls();
        propertiesPlaceholderLabel = new Label("Select an 'Add' action above to configure properties.");
        propertiesPlaceholderLabel.setWrapText(true);
        propertiesGrid.add(propertiesPlaceholderLabel, 0, 0, 2, 1); // Span 2 columns
        propertiesPane.setVisible(true);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now)
            {
                double time = engine.simulationTimeSeconds();
                timeLabel.setText(String.format("Time: %.1fs", time));
            }
        };
        timer.start();
    }

    public InteractionMode getCurrentMode()
    {
        return currentMode;
    }

    private void handlePaneClick(MouseEvent event)
    {
        if (event.getTarget() != simulationStackPane)
        {
            return;
        }

        if (currentMode == InteractionMode.PLACING_INTERSECTION)
        {
            double xWorld = event.getX() / IntersectionUtil.PX_PER_M;
            double yWorld = event.getY() / IntersectionUtil.PX_PER_M;

            if (!isFarEnough(event.getX(), event.getY()))
            {
                validationLabel.setText("Too close to another intersection (min " + MIN_PLACEMENT_DISTANCE + " px).");
                return;
            }

            Optional<Intersection> opt = createIntersectionFromUI(xWorld, yWorld);
            opt.ifPresent(i ->
            {
                engine.addIntersection(i);
                simulationRenderer.onIntersectionAdded(i);
                validationLabel.setText("");
            });
        }
    }

    @FXML
    private void handleAddIntersectionRequest()
    {
        if (currentMode == InteractionMode.PLACING_INTERSECTION)
        {
            resetToAddMode();
        } else
        {
            setupMode(InteractionMode.PLACING_INTERSECTION);
        }
    }

    @FXML
    private void handleAddRoadRequest()
    {
        if (currentMode == InteractionMode.PLACING_ROAD)
        {
            resetToAddMode();
        } else
        {
            setupMode(InteractionMode.PLACING_ROAD);
        }
    }

    @FXML
    private void handleAddCarRequest()
    {
        if (currentMode == InteractionMode.PLACING_CAR)
        {
            resetToAddMode();
        } else
        {
            setupMode(InteractionMode.PLACING_CAR);
        }
    }

    private void setupMode(InteractionMode mode)
    {
        resetToAddMode();

        currentMode = mode;
        propertiesPane.setVisible(true);
        propertiesGrid.getChildren().clear();
        validationLabel.setText("");
        simulationStackPane.getScene().setCursor(Cursor.CROSSHAIR);

        switch (mode) {
        case PLACING_INTERSECTION:
            addIntersectionButton.setText("Done");
            addRoadButton.setDisable(true);
            addCarButton.setDisable(true);
            propertiesGrid.add(new Label("Type:"), 0, 0, 2, 1);
            propertiesGrid.add(intersectionTypeCombo, 0, 1, 2, 1);
            propertiesGrid.add(param1Label, 0, 2, 2, 1);
            propertiesGrid.add(param1Field, 0, 3, 2, 1);
            propertiesGrid.add(param2Label, 0, 4, 2, 1);
            propertiesGrid.add(param2Field, 0, 5, 2, 1);
            intersectionTypeCombo.getSelectionModel().selectFirst();
            break;
        case PLACING_ROAD:
            addRoadButton.setText("Done");
            addIntersectionButton.setDisable(true);
            addCarButton.setDisable(true);
            firstPicked = null;
            propertiesGrid.add(roadSpeedLabel, 0, 0, 2, 1);
            propertiesGrid.add(roadSpeedField, 0, 1, 2, 1);
            roadSpeedField.setText("35");
            validationLabel.setText("Select the first intersection.");
            break;
        case PLACING_CAR:
            addCarButton.setText("Done");
            addIntersectionButton.setDisable(true);
            addRoadButton.setDisable(true);
            propertiesGrid.add(carMaxSpeedLabel, 0, 0, 2, 1);
            propertiesGrid.add(carMaxSpeedField, 0, 1, 2, 1);
            propertiesGrid.add(carAccelLabel, 0, 2, 2, 1);
            propertiesGrid.add(carAccelField, 0, 3, 2, 1);
            carMaxSpeedField.setText("30");
            carAccelField.setText("15.0");
            validationLabel.setText("Click an intersection to spawn a car.");
            break;
        case NORMAL:
            // nothing for now
            break;
        }
    }

    private void createPropertyControls()
    {
        // Intersection controls
        intersectionTypeCombo = new ComboBox<>();
        intersectionTypeCombo.getItems().setAll("Traffic Light", "Roundabout");
        intersectionTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateIntersectionFields(newVal));
        param1Label = new Label();
        param1Field = new TextField();
        param2Label = new Label();
        param2Field = new TextField();

        // Road controls
        roadSpeedLabel = new Label("Speed (MPH):");
        roadSpeedField = new TextField();

        // Car controls
        carMaxSpeedLabel = new Label("Max Spd (MPH):");
        carMaxSpeedField = new TextField();
        carAccelLabel = new Label("0-60 Time (s)");
        carAccelField = new TextField();
    }

    private void resetToAddMode()
    {
        currentMode = InteractionMode.NORMAL;

        addIntersectionButton.setText("Add Intersection");
        addRoadButton.setText("Add Road");
        addCarButton.setText("Add Car");

        addIntersectionButton.setDisable(false);
        addRoadButton.setDisable(false);
        addCarButton.setDisable(false);

        propertiesGrid.getChildren().clear();
        propertiesGrid.add(propertiesPlaceholderLabel, 0, 0, 2, 1);
        validationLabel.setText("");

        firstPicked = null;

        if (simulationStackPane.getScene() != null)
        {
            simulationStackPane.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    private void updateIntersectionFields(String type)
    {
        if ("Traffic Light".equals(type))
        {
            param1Label.setText("Cycle (s):");
            param1Field.setText("20");
            param2Label.setText("Yellow (s):");
            param2Field.setText("3");
            param2Label.setVisible(true);
            param2Field.setVisible(true);
        } else if ("Roundabout".equals(type))
        {
            param1Label.setText("Speed (MPH):");
            param1Field.setText("15");
            param2Label.setVisible(false);
            param2Field.setVisible(false);
        }
    }

    public void onIntersectionPickedForRoad(Intersection picked)
    {
        if (currentMode != InteractionMode.PLACING_ROAD)
            return;

        if (firstPicked == null)
        {
            firstPicked = picked;
            validationLabel.setText("First intersection selected. Pick a second one.");
            return;
        }

        if (firstPicked == picked)
        {
            validationLabel.setText("Cannot connect an intersection to itself. Pick a different one.");
            return;
        }

        Intersection secondPicked = picked;
        Optional<Double> speedOpt = validateRoadSpeed();

        speedOpt.ifPresent(speed ->
        {
            double length = firstPicked.position().distanceTo(secondPicked.position());
            Road road1 = new Road(firstPicked, secondPicked, length, speed);
            engine.addRoad(road1);
            simulationRenderer.onRoadAdded(road1);

            Road road2 = new Road(secondPicked, firstPicked, length, speed);
            engine.addRoad(road2);
            simulationRenderer.onRoadAdded(road2);

            firstPicked = null;
            validationLabel.setText("Road created. Pick the first intersection for the next road.");
        });
    }

    public void onIntersectionPickedForCar(Intersection picked)
    {
        if (currentMode != InteractionMode.PLACING_CAR)
            return;

        Optional<Car> carOpt = createCarFromUI();
        carOpt.ifPresent(car ->
        {
            List<Road> outs = engine.roadNetwork().outgoing(picked);
            if (outs.isEmpty())
            {
                validationLabel.setText("Selected intersection has no outgoing road.");
                return;
            }
            engine.addVehicle(car, outs.get(0), 0.0);
            simulationRenderer.onCarAdded(car);
            validationLabel.setText("Car added. Click another intersection.");
        });
    }

    private Optional<Intersection> createIntersectionFromUI(double x, double y)
    {
        validationLabel.setText("");
        try
        {
            String type = intersectionTypeCombo.getValue();
            if ("Traffic Light".equals(type))
            {
                double totalTime = Double.parseDouble(param1Field.getText());
                double yellow = Double.parseDouble(param2Field.getText());
                if (totalTime <= 0 || yellow <= 0 || yellow >= totalTime)
                {
                    validationLabel.setText("Invalid times. Ensure total > yellow > 0.");
                    return Optional.empty();
                }
                return Optional.of(new SignalisedIntersection(x, y, totalTime, yellow));
            } else if ("Roundabout".equals(type))
            {
                double speedMph = Double.parseDouble(param1Field.getText());
                if (speedMph <= 0)
                {
                    validationLabel.setText("Speed must be positive.");
                    return Optional.empty();
                }
                double speedMps = UnitConverter.mphToMps(speedMph);
                return Optional.of(new Roundabout(x, y, speedMps));
            }
        } catch (NumberFormatException e)
        {
            validationLabel.setText("Invalid number format in intersection properties.");
            return Optional.empty();
        }
        return Optional.empty();
    }

    private Optional<Double> validateRoadSpeed()
    {
        validationLabel.setText("");
        try
        {
            double speedMph = Double.parseDouble(roadSpeedField.getText());
            if (speedMph <= 0)
            {
                validationLabel.setText("Speed must be positive.");
                return Optional.empty();
            }
            return Optional.of(UnitConverter.mphToMps(speedMph));
        } catch (NumberFormatException e)
        {
            validationLabel.setText("Invalid number for speed.");
            return Optional.empty();
        }
    }

    private Optional<Car> createCarFromUI()
    {
        validationLabel.setText("");
        try
        {
            double vMph = Double.parseDouble(carMaxSpeedField.getText());
            double timeTo60 = Double.parseDouble(carAccelField.getText());
            if (vMph <= 0 || timeTo60 <= 0)
            {
                validationLabel.setText("Car properties must be positive.");
                return Optional.empty();
            }
            double vMps = UnitConverter.mphToMps(vMph);
            double aMps2 = UnitConverter.MPH_60_IN_MPS / timeTo60;
            return Optional.of(new Car(engine.roadNetwork(), vMps, aMps2));
        } catch (NumberFormatException e)
        {
            validationLabel.setText("Invalid number format in car properties.");
            return Optional.empty();
        }
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

        if (intersection instanceof SignalisedIntersection)
        {
            grid.add(new Label("Editing is not fully implemented for this type."), 0, 0);
            dialog.setResultConverter(btn -> btn == deleteButtonType);
        } else if (intersection instanceof Roundabout)
        {
            Roundabout model = (Roundabout) intersection;
            double speedMph = UnitConverter.mpsToMph(model.getSpeedLimit());
            TextField speed = new TextField(String.format("%.1f", speedMph));
            grid.add(new Label("Speed (MPH):"), 0, 0);
            grid.add(speed, 1, 0);

            dialog.setResultConverter(btn ->
            {
                if (btn == updateButtonType)
                {
                    try
                    {
                        double newSpeedMph = Double.parseDouble(speed.getText());
                        model.setSpeedLimit(UnitConverter.mphToMps(newSpeedMph));
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
            List<Road> removeRoads = engine.removeIntersection(intersection);
            simulationRenderer.removeIntersection(intersection);
            for (Road road : removeRoads)
            {
                simulationRenderer.removeRoad(road);
                Intersection neighbor = road.to().equals(intersection) ? road.from() : road.to();

                if (neighbor instanceof SignalisedIntersection)
                {
                    SignalisedIntersection sigNeighbor = (SignalisedIntersection) neighbor;
                    sigNeighbor.unregisterIncomingRoad(road);

                    IntersectionView viewMgr = simulationRenderer.getIntersectionView(neighbor);
                    if (viewMgr instanceof SignalisedIntersectionView)
                    {
                        ((SignalisedIntersectionView) viewMgr).removeSignalForRoad(road, lightPane);
                    }
                }
            }
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
        double speedMph = UnitConverter.mpsToMph(road.speedLimit());
        TextField spd = new TextField(String.format("%.1f", speedMph));
        g.add(new Label("Speed (MPH):"), 0, 0);
        g.add(spd, 1, 0);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(btn ->
        {
            if (btn == update)
            {
                double newSpeedMph = Double.parseDouble(spd.getText());
                road.setSpeedLimit(UnitConverter.mphToMps(newSpeedMph));
            }
            return btn == delete;
        });

        if (dialog.showAndWait().orElse(false))
        {
            engine.removeRoad(road);
            simulationRenderer.removeRoad(road);
        }
    }

    public void showEditCarDialog(Car car)
    {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Car");
        ButtonType apply = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(apply, ButtonType.CANCEL);

        GridPane g = new GridPane();
        double maxSpdMph = UnitConverter.mpsToMph(car.getMaxSpeed());
        g.setHgap(10);
        g.setVgap(10);
        g.setPadding(new Insets(20, 150, 10, 10));
        TextField maxSpd = new TextField(String.format("%.1f", maxSpdMph));

        double accelMps2 = car.getAcceleration();
        String timeTo60Text = "";
        if (accelMps2 > 1e-6)
        {
            double timeTo60 = UnitConverter.MPH_60_IN_MPS / accelMps2;
            timeTo60Text = String.format("%.2f", timeTo60);
        }

        TextField accel = new TextField(timeTo60Text);
        g.add(new Label("Max Spd (MPH):"), 0, 0);
        g.add(maxSpd, 1, 0);
        g.add(new Label("0-60 Time (s)"), 0, 1);
        g.add(accel, 1, 1);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(btn ->
        {
            if (btn == apply)
            {
                try
                {
                    double newMaxSpdMph = Double.parseDouble(maxSpd.getText());
                    car.setMaxSpeed(UnitConverter.mphToMps(newMaxSpdMph));

                    double newTimeTo60 = Double.parseDouble(accel.getText());
                    if (newTimeTo60 > 1e-6)
                    {
                        double newAccelMps2 = UnitConverter.MPH_60_IN_MPS / newTimeTo60;
                        car.setAcceleration(newAccelMps2);
                    }

                } catch (NumberFormatException e)
                {
                    System.err.println("Bad number");
                }
            }
            return null;
        });
        dialog.showAndWait();
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