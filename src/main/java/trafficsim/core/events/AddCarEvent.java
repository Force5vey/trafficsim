/***************************************************************

- File:        AddCarEvent.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Event for adding a car to the simulation.

- Description:
- Encapsulates a request to add a new car to the simulation model,
- specifying the car object and its spawn intersection. Provides
- equality and hashing for event handling.

***************************************************************/

package trafficsim.core.events;

import trafficsim.core.model.Car;
import trafficsim.core.model.Intersection;
import java.util.Objects;

public final class AddCarEvent implements ModelCommandEvent
{
    private final Car car;
    private final Intersection spawnPoint;

    /**
    * Constructs an AddCarEvent for the specified car and spawn point.
    *
    * @param car        The Car to be added to the simulation.
    * @param spawnPoint The Intersection where the car will spawn.
    */
    public AddCarEvent(Car car, Intersection spawnPoint)
    {
        this.car = car;
        this.spawnPoint = spawnPoint;
    }

    /**
    * Returns the car to be added to the simulation.
    *
    * @return The Car object.
    */
    public Car getCar()
    {
        return car;
    }

    /**
    * Returns the intersection where the car will spawn.
    *
    * @return The spawn Intersection.
    */
    public Intersection getSpawnPoint()
    {
        return spawnPoint;
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

        AddCarEvent that = (AddCarEvent) o;
        return Objects.equals(car, that.car) && Objects.equals(spawnPoint, that.spawnPoint);
    }

    /**
    * Computes the hash code for this event.
    *
    * @return The hash code.
    */
    @Override
    public int hashCode()
    {
        return Objects.hash(car, spawnPoint);
    }
}
