/***************************************************************

- File:        InteractionModeManager.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Manages the current user interaction mode for the TrafficSim UI.

- Description:
- Tracks and updates the current interaction mode (normal, placing, editing, etc.)
- and manages related UI state such as cursor changes and selection memory for
- multi-step actions like road placement.

***************************************************************/

package trafficsim.ui.controller.helpers;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import trafficsim.core.model.Intersection;

public class InteractionModeManager
{
    public enum Mode {
        NORMAL, PLACING_INTERSECTION, PLACING_ROAD, PLACING_CAR, EDITING
    }

    private Mode currentMode = Mode.NORMAL;
    private Intersection firstPickedForRoad = null;
    private final Scene scene;

    /**
    * Constructs an InteractionModeManager for the given JavaFX Scene.
    * Initializes the mode and sets the initial cursor.
    *
    * @param scene The JavaFX Scene to manage interaction modes for.
    */
    public InteractionModeManager(Scene scene)
    {
        this.scene = scene;
        updateCursor();
    }

    /**
    * Returns the current interaction mode.
    *
    * @return The current Mode.
    */
    public Mode getCurrentMode()
    {
        return currentMode;
    }

    /**
    * Sets the current interaction mode and updates the UI cursor.
    * Resets road selection if not in road placement mode.
    *
    * @param mode The new interaction mode to set.
    */
    public void setMode(Mode mode)
    {
        this.currentMode = mode;

        if (mode != Mode.PLACING_ROAD)
        {
            firstPickedForRoad = null;
        }
        updateCursor();
    }

    /**
    * Returns the first intersection selected for road placement, if any.
    *
    * @return The first picked Intersection, or null if none.
    */
    public Intersection getFirstPickedForRoad()
    {
        return firstPickedForRoad;
    }

    /**
    * Sets the first intersection selected for road placement.
    *
    * @param intersection The Intersection to set as first picked.
    */
    public void setFirstPickedForRoad(Intersection intersection)
    {
        this.firstPickedForRoad = intersection;
    }

    /**
    * Resets the interaction mode to NORMAL.
    */
    public void reset()
    {
        setMode(Mode.NORMAL);
    }

    /**
    * Updates the mouse cursor based on the current interaction mode.
    * Sets crosshair for placement modes and default for others.
    */
    private void updateCursor()
    {
        if (scene == null)
        {
            return;
        }

        switch (currentMode) {
        case PLACING_INTERSECTION:
        case PLACING_ROAD:
        case PLACING_CAR:
            scene.setCursor(Cursor.CROSSHAIR);
            break;
        case NORMAL:
        case EDITING:
        default:
            scene.setCursor(Cursor.DEFAULT);
            break;
        }
    }
}
