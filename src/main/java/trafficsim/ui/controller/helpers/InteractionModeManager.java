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

    public InteractionModeManager(Scene scene)
    {
        this.scene = scene;
        updateCursor();
    }

    public Mode getCurrentMode()
    {
        return currentMode;
    }

    public void setMode(Mode mode)
    {
        this.currentMode = mode;

        if (mode != Mode.PLACING_ROAD)
        {
            firstPickedForRoad = null;
        }
        updateCursor();
    }

    public Intersection getFirstPickedForRoad()
    {
        return firstPickedForRoad;
    }

    public void setFirstPickedForRoad(Intersection intersection)
    {
        this.firstPickedForRoad = intersection;
    }

    public void reset()
    {
        setMode(Mode.NORMAL);
    }

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
