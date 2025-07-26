// src/main/java/trafficsim/ui/controller/MainController.java

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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import trafficsim.core.model.Car;
import trafficsim.core.model.Intersection;
import trafficsim.core.model.Road;
import trafficsim.core.model.Roundabout;
import trafficsim.core.model.SignalisedIntersection;
import trafficsim.core.sim.SimulationEngine;
import trafficsim.ui.adapter.IntersectionUtil;
import trafficsim.ui.adapter.UnitConverter;
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
    @FXML
    private HBox editButtonsBox;
    @FXML
    private Button applyEditButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button cancelEditButton;

    private SimulationEngine engine;
    private SimulationRenderer simulationRenderer;

    public enum InteractionMode {
        NORMAL, PLACING_INTERSECTION, PLACING_ROAD, PLACING_CAR, EDITING,
    }

    private InteractionMode currentMode = InteractionMode.NORMAL;
    private Intersection firstPicked = null; // for roads
    private Object selectedForEdit = null;

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
        propertiesPlaceholderLabel = new Label("Select an 'Add' action or click an item to edit properties.");
        propertiesPlaceholderLabel.setWrapText(true);
        resetToNormalMode();

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
            resetToNormalMode();
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
            resetToNormalMode();
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
            resetToNormalMode();
        } else
        {
            setupMode(InteractionMode.PLACING_CAR);
        }
    }

    private void setupMode(InteractionMode mode)
    {
        resetToNormalMode();

        currentMode = mode;
        propertiesPane.setVisible(true);
        propertiesGrid.getChildren().clear();
        validationLabel.setText("");

        int row = 0;

        switch (mode) {
        case PLACING_INTERSECTION:
            addIntersectionButton.setText("Done");
            addRoadButton.setDisable(true);
            addCarButton.setDisable(true);
            propertiesGrid.add(new Label("Type:"), 0, row++, 2, 1);
            propertiesGrid.add(intersectionTypeCombo, 0, row++, 2, 1);
            propertiesGrid.add(param1Label, 0, row++, 2, 1);
            propertiesGrid.add(param1Field, 0, row++, 2, 1);
            propertiesGrid.add(param2Label, 0, row++, 2, 1);
            propertiesGrid.add(param2Field, 0, row++, 2, 1);
            intersectionTypeCombo.getSelectionModel().selectFirst();
            simulationStackPane.getScene().setCursor(Cursor.CROSSHAIR);
            break;
        case PLACING_ROAD:
            addRoadButton.setText("Done");
            addIntersectionButton.setDisable(true);
            addCarButton.setDisable(true);
            firstPicked = null;
            propertiesGrid.add(roadSpeedLabel, 0, row++, 2, 1);
            propertiesGrid.add(roadSpeedField, 0, row++, 2, 1);
            roadSpeedField.setText("35");
            validationLabel.setText("Select the first intersection.");
            simulationStackPane.getScene().setCursor(Cursor.CROSSHAIR);
            break;
        case PLACING_CAR:
            addCarButton.setText("Done");
            addIntersectionButton.setDisable(true);
            addRoadButton.setDisable(true);
            propertiesGrid.add(carMaxSpeedLabel, 0, row++, 2, 1);
            propertiesGrid.add(carMaxSpeedField, 0, row++, 2, 1);
            propertiesGrid.add(carAccelLabel, 0, row++, 2, 1);
            propertiesGrid.add(carAccelField, 0, row++, 2, 1);
            carMaxSpeedField.setText("30");
            carAccelField.setText("15.0");
            validationLabel.setText("Click an intersection to spawn a car.");
            simulationStackPane.getScene().setCursor(Cursor.CROSSHAIR);
            break;
        case EDITING:
            addIntersectionButton.setDisable(true);
            addRoadButton.setDisable(true);
            addCarButton.setDisable(true);
            editButtonsBox.setVisible(true);
            editButtonsBox.setManaged(true);
            break;
        case NORMAL:
            // Handled by resetToNormalMode()
            break;
        }
    }

    private void createPropertyControls()
    {
        intersectionTypeCombo = new ComboBox<>();
        intersectionTypeCombo.getItems().setAll("Traffic Light", "Roundabout");
        intersectionTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateIntersectionFields(newVal));
        param1Label = new Label();
        param1Field = new TextField();
        param2Label = new Label();
        param2Field = new TextField();

        roadSpeedLabel = new Label("Speed (MPH):");
        roadSpeedField = new TextField();

        carMaxSpeedLabel = new Label("Max Spd (MPH):");
        carMaxSpeedField = new TextField();
        carAccelLabel = new Label("0-60 Time (s)");
        carAccelField = new TextField();
    }

    private void resetToNormalMode()
    {
        currentMode = InteractionMode.NORMAL;
        selectedForEdit = null;

        addIntersectionButton.setText("Add Intersection");
        addRoadButton.setText("Add Road");
        addCarButton.setText("Add Car");

        addIntersectionButton.setDisable(false);
        addRoadButton.setDisable(false);
        addCarButton.setDisable(false);

        propertiesGrid.getChildren().clear();
        propertiesGrid.add(propertiesPlaceholderLabel, 0, 0, 2, 1);
        validationLabel.setText("");

        editButtonsBox.setVisible(false);
        editButtonsBox.setManaged(false);

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

    public void selectForEditing(Object item)
    {
        if (currentMode != InteractionMode.NORMAL)
        {
            return;
        }
        setupMode(InteractionMode.EDITING);
        selectedForEdit = item;
        populatePropertiesForEdit(item);
    }

    private void populatePropertiesForEdit(Object item)
    {
        propertiesGrid.getChildren().clear();
        deleteButton.setVisible(!(item instanceof Car));
        int row = 0;

        param1Label.setVisible(false);
        param1Field.setVisible(false);
        param2Label.setVisible(false);
        param2Field.setVisible(false);
        roadSpeedLabel.setVisible(false);
        roadSpeedField.setVisible(false);
        carMaxSpeedLabel.setVisible(false);
        carMaxSpeedField.setVisible(false);
        carAccelLabel.setVisible(false);
        carAccelField.setVisible(false);

        if (item instanceof SignalisedIntersection)
        {
            validationLabel.setText("Editing Signalised Intersection");
            SignalisedIntersection model = (SignalisedIntersection) item;

            param1Label.setText("Cycle (s):");
            param1Field.setText(String.format("%.1f", model.getTotalCycleTime()));
            propertiesGrid.add(param1Label, 0, row++, 2, 1);
            propertiesGrid.add(param1Field, 0, row++, 2, 1);

            param2Label.setText("Yellow (s):");
            param2Field.setText(String.format("%.1f", model.getYellowDuration()));
            param2Label.setVisible(true);
            param2Field.setVisible(true);

            propertiesGrid.add(param2Label, 0, row++, 2, 1);
            propertiesGrid.add(param2Field, 0, row++, 2, 1);
        } else if (item instanceof Roundabout)
        {
            validationLabel.setText("Editing Roundabout");
            Roundabout model = (Roundabout) item;
            double speedMph = UnitConverter.mpsToMph(model.getSpeedLimit());

            param1Label.setText("Speed (MPH):");
            param1Field.setText(String.format("%.1f", speedMph));
            param1Label.setVisible(true);
            param1Field.setVisible(true);

            propertiesGrid.add(param1Label, 0, row++, 2, 1);
            propertiesGrid.add(param1Field, 0, row++, 2, 1);
        } else if (item instanceof Road)
        {
            validationLabel.setText("Editing Road");
            Road model = (Road) item;
            double speedMph = UnitConverter.mpsToMph(model.speedLimit());

            roadSpeedLabel.setText("Speed (MPH):");
            roadSpeedField.setText(String.format("%.1f", speedMph));
            roadSpeedLabel.setVisible(true);
            roadSpeedLabel.setVisible(true);

            propertiesGrid.add(roadSpeedLabel, 0, row++, 2, 1);
            propertiesGrid.add(roadSpeedField, 0, row++, 2, 1);
        } else if (item instanceof Car)
        {
            validationLabel.setText("Editing Car");
            Car model = (Car) item;

            double maxSpdMph = UnitConverter.mpsToMph(model.getMaxSpeed());
            carMaxSpeedLabel.setText("Max Spd (MPH):");
            carMaxSpeedField.setText(String.format("%.1f", maxSpdMph));
            carMaxSpeedLabel.setVisible(true);
            carMaxSpeedLabel.setVisible(true);

            propertiesGrid.add(carMaxSpeedLabel, 0, row++, 2, 1);
            propertiesGrid.add(carMaxSpeedField, 0, row++, 2, 1);

            double accelMps2 = model.getAcceleration();
            String timeTo60Text = "";
            if (accelMps2 > 1e-6)
            {
                double timeTo60 = UnitConverter.MPH_60_IN_MPS / accelMps2;
                timeTo60Text = String.format("%.2f", timeTo60);
            }
            carAccelLabel.setText("0-60 Time (s)");
            carAccelField.setText(timeTo60Text);
            carAccelLabel.setVisible(true);
            carAccelField.setVisible(true);

            propertiesGrid.add(carAccelLabel, 0, row++, 2, 1);
            propertiesGrid.add(carAccelField, 0, row++, 2, 1);
        }
    }

    @FXML
    private void handleApplyEdit()
    {
        if (currentMode != InteractionMode.EDITING || selectedForEdit == null)
        {
            return;
        }
        boolean success = applyChanges(selectedForEdit);
        if (success)
        {
            resetToNormalMode();
        }
    }

    private boolean applyChanges(Object item)
    {
        try
        {
            if (item instanceof SignalisedIntersection)
            {
                SignalisedIntersection model = (SignalisedIntersection) item;
                double newTotalTime = Double.parseDouble(param1Field.getText());
                double newYellow = Double.parseDouble(param2Field.getText());
                if (newTotalTime <= 0 || newYellow <= 0 || newYellow >= newTotalTime)
                {
                    validationLabel.setText("Invalid times. Ensure total > yellow > 0.");
                    return false;
                }
                model.setTotalCycleTime(newTotalTime);
                model.setYellowDuration(newYellow);
            } else if (item instanceof Roundabout)
            {
                Roundabout model = (Roundabout) item;
                double newSpeedMph = Double.parseDouble(param1Field.getText());
                if (newSpeedMph <= 0)
                {
                    validationLabel.setText("Speed must be positive.");
                    return false;
                }
                model.setSpeedLimit(UnitConverter.mphToMps(newSpeedMph));
            } else if (item instanceof Road)
            {
                Road model = (Road) item;
                double newSpeedMph = Double.parseDouble(roadSpeedField.getText());
                if (newSpeedMph <= 0)
                {
                    validationLabel.setText("Speed must be positive.");
                    return false;
                }
                model.setSpeedLimit(UnitConverter.mphToMps(newSpeedMph));
            } else if (item instanceof Car)
            {
                Car model = (Car) item;
                double newMaxSpdMph = Double.parseDouble(carMaxSpeedField.getText());
                double newTimeTo60 = Double.parseDouble(carAccelField.getText());

                if (newMaxSpdMph <= 0 || newTimeTo60 <= 0)
                {
                    validationLabel.setText("Car properties must be positive.");
                    return false;
                }
                model.setMaxSpeed(UnitConverter.mphToMps(newMaxSpdMph));
                if (newTimeTo60 > 1e-6)
                {
                    double newAccelMps2 = UnitConverter.MPH_60_IN_MPS / newTimeTo60;
                    model.setAcceleration(newAccelMps2);
                }
            }
        } catch (NumberFormatException e)
        {
            validationLabel.setText("Invalid number format.");
            return false;
        }
        return true;
    }

    @FXML
    private void handleCancelEdit()
    {
        resetToNormalMode();
    }

    @FXML
    private void handleDelete()
    {
        if (currentMode != InteractionMode.EDITING || selectedForEdit == null)
        {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete this item?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            if (selectedForEdit instanceof Intersection)
            {
                deleteIntersection((Intersection) selectedForEdit);
            } else if (selectedForEdit instanceof Road)
            {
                engine.removeRoad((Road) selectedForEdit);
                simulationRenderer.removeRoad((Road) selectedForEdit);
            }
            // Car deletion is not supported
            resetToNormalMode();
        }
    }

    private void deleteIntersection(Intersection intersection)
    {
        List<Road> removedRoads = engine.removeIntersection(intersection);
        simulationRenderer.removeIntersection(intersection);
        for (Road road : removedRoads)
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
    }
}