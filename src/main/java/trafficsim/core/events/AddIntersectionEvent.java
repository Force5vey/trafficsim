/***************************************************************

- File:        AddIntersectionEvent.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Event for adding an intersection to the simulation.

- Description:
- Encapsulates a request to add a new intersection to the simulation
- model. Stores the intersection to be added and provides equality
- and hashing for event handling.

***************************************************************/

package trafficsim.core.events;

import trafficsim.core.model.Intersection;
import java.util.Objects;

public final class AddIntersectionEvent implements ModelCommandEvent
{
    private final Intersection intersection;

    /**
    * Constructs an AddIntersectionEvent for the specified intersection.
    *
    * @param intersection The Intersection to be added to the simulation.
    */
    public AddIntersectionEvent(Intersection intersection)
    {
        this.intersection = intersection;
    }

    /**
    * Returns the intersection to be added to the simulation.
    *
    * @return The Intersection object.
    */
    public Intersection getIntersection()
    {
        return intersection;
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

        AddIntersectionEvent that = (AddIntersectionEvent) o;
        return Objects.equals(intersection, that.intersection);
    }

    /**
    * Computes the hash code for this event.
    *
    * @return The hash code.
    */
    @Override
    public int hashCode()
    {
        return Objects.hash(intersection);
    }
}
