/***************************************************************

- File:        MainController.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     JavaFX controller for the main TrafficSim application window.

- Description:
- Handles user interactions, UI state management, and communication
- between the simulation engine and the user interface. Manages
- simulation controls, editing modes, and property panels.

***************************************************************/

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
import javafx.scene.control.ToggleButton;

import trafficsim.core.events.EngineControlEvent;
import trafficsim.core.events.EngineControlEvent.ControlType;
import trafficsim.core.events.ModelCommandEvent;
import trafficsim.core.model.Intersection;
import trafficsim.core.sim.SimulationEngine;
import trafficsim.ui.adapter.IntersectionUtil;
import trafficsim.ui.controller.helpers.InteractionModeManager;
import trafficsim.ui.controller.helpers.PropertiesPanelManager;
import trafficsim.ui.controller.helpers.SimulationActionHandler;
import trafficsim.ui.controller.helpers.InteractionModeManager.Mode;
import trafficsim.ui.view.SimulationRenderer;
import trafficsim.ui.controller.helpers.BackgroundHelper;
import trafficsim.ui.controller.helpers.DefaultLayoutHelper;

public class MainController
{
    private static final double MIN_PLACEMENT_DISTANCE = 50.0;

    @FXML
    private Pane backgroundPane;
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
    @FXML
    private ToggleButton toggleBubblesButton;

    private SimulationEngine engine;
    private SimulationRenderer simulationRenderer;

    private InteractionModeManager modeManager;
    private PropertiesPanelManager panelManager;
    private SimulationActionHandler actionHandler;

    private Object selectedForEdit = null;
    private boolean isSimStopped = true;

    /**
    * Initializes the controller after the FXML fields are injected.
    * Sets up the simulation engine, renderer, UI event handlers, and default layout.
    */
    @FXML
    public void initialize()
    {
        BackgroundHelper.setupBackground(backgroundPane);

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

    /**
    * Returns the current interaction mode of the UI.
    *
    * @return The current InteractionModeManager.Mode.
    */
    public Mode getCurrentMode()
    {
        return modeManager != null ? modeManager.getCurrentMode() : Mode.NORMAL;
    }

    /**
    * Sets the current interaction mode and updates UI controls accordingly.
    *
    * @param newMode The new interaction mode to set.
    */
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

    /**
    * Resets the UI to normal mode and updates the stop button state if needed.
    */
    private void resetToNormalMode()
    {
        selectedForEdit = null;
        setMode(Mode.NORMAL);
        if (isSimStopped)
        {
            updateStopButtonState();
        }
    }

    /**
    * Handles the request to enter or exit intersection placement mode.
    * Toggles the mode and updates UI controls.
    */
    @FXML
    private void handleAddIntersectionRequest()
    {
        setMode(modeManager.getCurrentMode() == Mode.PLACING_INTERSECTION ? Mode.NORMAL : Mode.PLACING_INTERSECTION);
    }

    /**
    * Handles the request to enter or exit road placement mode.
    * Toggles the mode and updates UI controls.
    */
    @FXML
    private void handleAddRoadRequest()
    {
        setMode(getCurrentMode() == Mode.PLACING_ROAD ? Mode.NORMAL : Mode.PLACING_ROAD);
    }

    /**
    * Handles the request to enter or exit car placement mode.
    * Toggles the mode and updates UI controls.
    */
    @FXML
    private void handleAddCarRequest()
    {
        setMode(getCurrentMode() == Mode.PLACING_CAR ? Mode.NORMAL : Mode.PLACING_CAR);
    }

    /**
    * Handles toggling the visibility of all car data bubbles in the simulation.
    */
    @FXML
    private void handleToggleBubbles()
    {
        boolean show = toggleBubblesButton.isSelected();
        simulationRenderer.setAllBubblesVisible(show);
        toggleBubblesButton.setText(show ? "Hide Bubbles" : "Show Bubbles");
    }

    /**
    * Handles mouse clicks on the simulation pane for intersection placement.
    * Places a new intersection if in the correct mode and location is valid.
    *
    * @param event The MouseEvent representing the click.
    */
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

    /**
    * Handles the selection of an intersection as the endpoint for a new road.
    * Manages the two-step road creation process.
    *
    * @param picked The intersection selected by the user.
    */
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

    /**
    * Handles the selection of an intersection as the spawn point for a new car.
    *
    * @param picked The intersection selected by the user.
    */
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

    /**
    * Selects an item (intersection, road, or car) for editing if in normal mode.
    *
    * @param item The model object to edit.
    */
    public void selectForEditing(Object item)
    {
        if (getCurrentMode() != Mode.NORMAL)
        {
            return;
        }
        selectedForEdit = item;
        setMode(Mode.EDITING);
    }

    /**
    * Applies edits made in the properties panel to the selected item.
    * Posts the update event to the simulation engine.
    */
    @FXML
    private void handleApplyEdit()
    {
        Optional<ModelCommandEvent> updateEvent = panelManager.createUpdateEvent(selectedForEdit);

        updateEvent.ifPresent(event ->
        {
            engine.postEvent(event);
            resetToNormalMode();
        });
    }

    /**
    * Cancels the current edit operation and resets the UI to normal mode.
    */
    @FXML
    private void handleCancelEdit()
    {
        resetToNormalMode();
    }

    /**
    * Handles deletion of the currently selected item after user confirmation.
    * Posts the deletion event to the simulation engine.
    */
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

    /**
    * Checks if a new intersection placement is far enough from existing intersections.
    *
    * @param newPxX The x-coordinate in pixels.
    * @param newPxY The y-coordinate in pixels.
    * @return True if the placement is valid, false otherwise.
    */
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

    /**
    * Updates the stop button's label based on the simulation state.
    */
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

    /**
    * Handles the start button action. Starts the simulation.
    */
    @FXML
    private void handleStart()
    {
        engine.postEvent(new EngineControlEvent(ControlType.START));
        isSimStopped = false;
        updateStopButtonState();
    }

    /**
    * Handles the pause button action. Pauses the simulation.
    */
    @FXML
    private void handlePause()
    {
        engine.postEvent(new EngineControlEvent(ControlType.PAUSE));
    }

    /**
    * Handles the stop/clear button action. Stops or clears the simulation depending on state.
    */
    @FXML
    private void handleStop()
    {
        if (isSimStopped)
        {
            actionHandler.clearAll();
        } else
        {
            engine.postEvent(new EngineControlEvent(ControlType.STOP));
            isSimStopped = true;
            updateStopButtonState();
        }
    }

    /**
    * Shuts down the simulation engine and releases resources.
    * Called when the application is closing.
    */
    public void shutdownEngine()
    {
        if (engine != null)
        {
            engine.shutdown();
        }
    }
}