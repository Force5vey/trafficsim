/***************************************************************

- File:        Updatable.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Interface for simulation objects that require periodic updates.

- Description:
- Defines a contract for objects that participate in the simulation loop
- and need to update their state each tick (e.g., cars, roads, intersections).

***************************************************************/

package trafficsim.core.model;

/**
 * Updates the object's state for the given time step.
 *
 * @param dtSeconds The time step in seconds since the last update.
 */
public interface Updatable
{
    void update(double dtSeconds);
}
