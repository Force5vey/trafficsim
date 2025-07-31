/***************************************************************

- File:        CarAdapter.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Adapts Car model data for JavaFX property binding in the UI.

- Description:
- Provides observable properties for car position in pixels, allowing
- the UI to bind and update car visuals in response to simulation state.
- Converts world coordinates to pixel coordinates for rendering.

***************************************************************/

package trafficsim.ui.adapter;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;

import trafficsim.core.model.Car;
import trafficsim.core.model.Vec2;

public final class CarAdapter
{
    private static final double PX_PER_M = 10.0;

    private final Car model;
    private final ReadOnlyDoubleWrapper xPx = new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper yPx = new ReadOnlyDoubleWrapper();

    public CarAdapter(Car model)
    {
        this.model = model;
    }

    /**
    * Updates the adapter's x and y properties from the car model's current world position.
    * Converts world coordinates to pixel coordinates for UI rendering.
    */
    public void pullFromModel()
    {
        Vec2 p = model.worldPos();
        xPx.set(p.x * PX_PER_M);
        yPx.set(p.y * PX_PER_M);
    }

    /**
    * Returns a read-only property representing the car's x position in pixels.
    *
    * @return ReadOnlyDoubleProperty for the x coordinate.
    */
    public ReadOnlyDoubleProperty xProperty()
    {
        return xPx.getReadOnlyProperty();
    }

    /**
    * Returns a read-only property representing the car's y position in pixels.
    *
    * @return ReadOnlyDoubleProperty for the y coordinate.
    */
    public ReadOnlyDoubleProperty yProperty()
    {
        return yPx.getReadOnlyProperty();
    }
}
