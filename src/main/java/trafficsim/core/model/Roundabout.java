/***************************************************************

- File:        Roundabout.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Represents a roundabout intersection in the simulation.

- Description:
- Implements the Intersection interface for roundabouts, storing position
- and speed limit. Provides methods for updating state (no-op), and for
- getting and setting the speed limit.

***************************************************************/

package trafficsim.core.model;

public final class Roundabout implements Intersection
{
    private final Vec2 position;
    private volatile double speedLimit;

    /**
    * Constructs a Roundabout at the specified position with a given speed limit.
    *
    * @param x          The x-coordinate of the roundabout.
    * @param y          The y-coordinate of the roundabout.
    * @param speedLimit The speed limit in meters per second.
    */
    public Roundabout(double x, double y, double speedLimit)
    {
        this.position = new Vec2(x, y);
        this.speedLimit = speedLimit;
    }

    /**
    * Returns the position of the roundabout as a Vec2.
    *
    * @return The position vector.
    */
    @Override
    public Vec2 position()
    {
        return position;
    }

    /**
    * Updates the roundabout state.
    * (No dynamic state to update; method present for interface compatibility.)
    *
    * @param deltaTime The time step in seconds.
    */
    @Override
    public void update(double deltaTime)
    {
        // no time based state, cars approach, and the update will be on the cars
        // holding this incase other updates need to happen later.
    }

    /**
    * Returns the speed limit of the roundabout in meters per second.
    *
    * @return The speed limit.
    */
    public double getSpeedLimit()
    {
        return speedLimit;
    }

    /**
    * Sets the speed limit of the roundabout in meters per second.
    *
    * @param speedLimit The new speed limit.
    */
    public void setSpeedLimit(double speedLimit)
    {
        this.speedLimit = speedLimit;
    }
}