/***************************************************************

- File:        AddRoadEvent.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Event for adding a road to the simulation model.

- Description:
- Encapsulates a request to add a new road segment to the simulation.
- Stores the road to be added and provides equality and hashing for
- event handling.

***************************************************************/

package trafficsim.core.events;

import trafficsim.core.model.Road;
import java.util.Objects;

public final class AddRoadEvent implements ModelCommandEvent
{
    private final Road road;

    /**
    * Constructs an AddRoadEvent for the specified road.
    *
    * @param road The Road to be added to the simulation.
    */
    public AddRoadEvent(Road road)
    {
        this.road = road;
    }

    /**
    * Returns the road to be added to the simulation.
    *
    * @return The Road object.
    */
    public Road getRoad()
    {
        return road;
    }

    /**
    * Checks if this event is equal to another object.
    *
    * @param o The object to compare with.
    * @return  True if the objects are equal, false otherwise.
    */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        AddRoadEvent that = (AddRoadEvent) o;
        return Objects.equals(road, that.road);
    }

    /**
    * Computes the hash code for this event.
    *
    * @return The hash code.
    */
    @Override
    public int hashCode()
    {
        return Objects.hash(road);
    }
}
