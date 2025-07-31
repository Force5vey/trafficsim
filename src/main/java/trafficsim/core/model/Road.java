/***************************************************************

- File:        Road.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Represents a road segment between two intersections.

- Description:
- Stores the endpoints, length, and speed limit for a road in the simulation.
- Provides methods for accessing and updating road properties, and for
- participating in the simulation update loop.

***************************************************************/

package trafficsim.core.model;

public final class Road implements Updatable
{
    private final Intersection from, to;
    private final double length;
    private volatile double speedLimit;

    /**
     * Constructs a Road between two intersections with the specified length and speed limit.
     *
     * @param from       The starting Intersection.
     * @param to         The ending Intersection.
     * @param length     The length of the road in meters.
     * @param speedLimit The speed limit in meters per second.
     */
    public Road(Intersection from, Intersection to, double length, double speedLimit)
    {
        this.from = from;
        this.to = to;
        this.length = length;
        this.speedLimit = speedLimit;
    }

    /**
    * Returns the starting intersection of the road.
    *
    * @return The from Intersection.
    */
    public Intersection from()
    {
        return from;
    }

    /**
    * Returns the ending intersection of the road.
    *
    * @return The to Intersection.
    */
    public Intersection to()
    {
        return to;
    }

    /**
    * Returns the length of the road in meters.
    *
    * @return The road length.
    */
    public double length()
    {
        return length;
    }

    /**
    * Returns the speed limit of the road in meters per second.
    *
    * @return The speed limit.
    */
    public double speedLimit()
    {
        return speedLimit;
    }

    /**
    * Sets the speed limit of the road in meters per second.
    *
    * @param speedLimit The new speed limit.
    */
    public void setSpeedLimit(double speedLimit)
    {
        this.speedLimit = speedLimit;
    }

    /**
    * Updates the road state.
    * (No dynamic state to update; method present for interface compatibility.)
    *
    * @param deltaTime The time step in seconds.
    */
    @Override
    public void update(double deltaTime)
    {

    }
}
