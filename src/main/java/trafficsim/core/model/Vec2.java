/***************************************************************

- File:        Vec2.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Represents a 2D vector or point in the simulation.

- Description:
- Immutable class for 2D coordinates, used for positions of intersections,
- roads, and cars in the simulation. Provides basic distance calculation.

***************************************************************/

package trafficsim.core.model;

public final class Vec2
{
    public final double x;
    public final double y;

    /**
    * Constructs a Vec2 with the specified x and y coordinates.
    *
    * @param x The x-coordinate.
    * @param y The y-coordinate.
    */
    public Vec2(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    /**
    * Calculates the Euclidean distance from this vector to another.
    *
    * @param other The other Vec2 to measure distance to.
    * @return      The distance between the two points.
    */
    public double distanceTo(Vec2 other)
    {
        double dx = x - other.x, dy = y - other.y;
        return Math.hypot(dx, dy);
    }
}