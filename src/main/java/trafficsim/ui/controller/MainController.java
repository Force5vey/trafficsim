// src/main/java/trafficsim/ui/controller/MainController.java

package trafficsim.ui.controller;

import java.util.Optional;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import trafficsim.core.model.Intersection;
import trafficsim.core.sim.SimulationEngine;
import trafficsim.ui.adapter.IntersectionUtil;
import trafficsim.ui.controller.helpers.InteractionModeManager;
import trafficsim.ui.controller.helpers.PropertiesPanelManager;
import trafficsim.ui.controller.helpers.SimulationActionHandler;
import trafficsim.ui.controller.helpers.InteractionModeManager.Mode;
import trafficsim.ui.view.SimulationRenderer;

import trafficsim.ui.controller.helpers.DefaultLayoutHelper;

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

    private InteractionModeManager modeManager;
    private PropertiesPanelManager panelManager;
    private SimulationActionHandler actionHandler;

    private Object selectedForEdit = null;
    private boolean isSimStopped = true;

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

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now)
            {
                double time = engine.simulationTimeSeconds();
                timeLabel.setText(String.format("Time: %.1fs", time));
            }
        };
        timer.start();

        simulationStackPane.sceneProperty().addListener((obs, oldScene, newScene) ->
        {
            if (newScene != null)
            {
                this.modeManager = new InteractionModeManager(newScene);
                this.panelManager = new PropertiesPanelManager(propertiesPane, propertiesGrid, validationLabel,
                        editButtonsBox, deleteButton);
                this.actionHandler = new SimulationActionHandler(engine, simulationRenderer);

                // --- Initial Road Network Setup --- //
                DefaultLayoutHelper.setupDefaultLayout(this.actionHandler, this.engine.roadNetwork());

                resetToNormalMode();
            }
        });
    }

    public Mode getCurrentMode()
    {
        return modeManager != null ? modeManager.getCurrentMode() : Mode.NORMAL;
    }

    private void setMode(Mode newMode)
    {
        if (modeManager == null)
            return;

        modeManager.setMode(newMode);
        panelManager.setupForMode(newMode, selectedForEdit);

        addIntersectionButton.setText(newMode == Mode.PLACING_INTERSECTION ? "Done" : "Add Intersection");
        addRoadButton.setText(newMode == Mode.PLACING_ROAD ? "Done" : "Add Road");
        addCarButton.setText(newMode == Mode.PLACING_CAR ? "Done" : "Add Car");

        boolean isAdding = newMode == Mode.PLACING_INTERSECTION || newMode == Mode.PLACING_ROAD
                || newMode == Mode.PLACING_CAR;

        addIntersectionButton.setDisable(isAdding && newMode != Mode.PLACING_INTERSECTION);
        addRoadButton.setDisable(isAdding && newMode != Mode.PLACING_ROAD);
        addCarButton.setDisable(isAdding && newMode != Mode.PLACING_CAR);
    }

    private void resetToNormalMode()
    {
        selectedForEdit = null;
        setMode(Mode.NORMAL);
        if (isSimStopped)
        {
            updateStopButtonState();
        }
    }

    @FXML
    private void handleAddIntersectionRequest()
    {
        setMode(modeManager.getCurrentMode() == Mode.PLACING_INTERSECTION ? Mode.NORMAL : Mode.PLACING_INTERSECTION);
    }

    @FXML
    private void handleAddRoadRequest()
    {
        setMode(getCurrentMode() == Mode.PLACING_ROAD ? Mode.NORMAL : Mode.PLACING_ROAD);
    }

    @FXML
    private void handleAddCarRequest()
    {
        setMode(getCurrentMode() == Mode.PLACING_CAR ? Mode.NORMAL : Mode.PLACING_CAR);
    }

    private void handlePaneClick(MouseEvent event)
    {
        if (event.getTarget() != simulationStackPane || getCurrentMode() != Mode.PLACING_INTERSECTION)
        {
            return;
        }

        double xWorld = event.getX() / IntersectionUtil.PX_PER_M;
        double yWorld = event.getY() / IntersectionUtil.PX_PER_M;

        if (!isFarEnough(event.getX(), event.getY()))
        {
            panelManager.setValidationMessage(
                    "Too close to another intersection (min " + MIN_PLACEMENT_DISTANCE + " px).", true);
            return;
        }

        panelManager.createIntersectionFromUI(xWorld, yWorld).ifPresent(i ->
        {
            actionHandler.addIntersection(i);
            panelManager.setValidationMessage("", false);
        });
    }

    public void onIntersectionPickedForRoad(Intersection picked)
    {
        if (getCurrentMode() != Mode.PLACING_ROAD)
            return;

        Intersection first = modeManager.getFirstPickedForRoad();

        if (first == null)
        {
            modeManager.setFirstPickedForRoad(picked);
            panelManager.setValidationMessage("First intersection selected. Pick a second one.", false);
        } else
        {
            if (first == picked)
            {
                panelManager.setValidationMessage("Cannot connect an intersection to itself.", true);
                return;
            }
            panelManager.validateRoadSpeed().ifPresent(speed ->
            {
                actionHandler.addRoad(first, picked, speed);
                modeManager.setFirstPickedForRoad(null);
                panelManager.setValidationMessage("Road created. Pick another intersection.", false);
            });
        }
    }

    public void onIntersectionPickedForCar(Intersection picked)
    {
        if (getCurrentMode() != Mode.PLACING_CAR)
        {
            return;
        }

        panelManager.createCarFromUI(engine.roadNetwork()).ifPresent(car ->
        {
            actionHandler.addCar(car, picked);
            if (engine.roadNetwork().outgoing(picked).isEmpty())
            {
                panelManager.setValidationMessage("Selected intersection has no outgoing road.", true);
            } else
            {
                panelManager.setValidationMessage("Car added. Click another intersection.", false);
            }
        });
    }

    public void selectForEditing(Object item)
    {
        if (getCurrentMode() != Mode.NORMAL)
        {
            return;
        }
        selectedForEdit = item;
        setMode(Mode.EDITING);
    }

    @FXML
    private void handleApplyEdit()
    {
        if (panelManager.applyChanges(selectedForEdit))
        {
            resetToNormalMode();
        }
    }

    @FXML
    private void handleCancelEdit()
    {
        resetToNormalMode();
    }

    @FXML
    private void handleDelete()
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete this item?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            actionHandler.deleteItem(selectedForEdit);
            resetToNormalMode();
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

    private void updateStopButtonState()
    {
        if (isSimStopped)
        {
            stopButton.setText("Clear");
        } else
        {
            stopButton.setText("Stop");
        }
    }

    @FXML
    private void handleStart()
    {
        engine.start();
        isSimStopped = false;
        updateStopButtonState();
    }

    @FXML
    private void handlePause()
    {
        engine.pause();
    }

    @FXML
    private void handleStop()
    {
        if (isSimStopped)
        {
            actionHandler.clearAll();
        } else
        {
            engine.stop();
            isSimStopped = true;
            updateStopButtonState();
        }
    }
}