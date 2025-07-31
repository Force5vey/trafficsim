/***************************************************************

- File:        Intersection.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Interface for intersections in the simulation.

- Description:
- Defines the contract for intersection objects, including position
- retrieval, update logic, and traffic signal state queries. Implemented
- by specific intersection types such as roundabouts and signalised intersections.

***************************************************************/

package trafficsim.core.model;

public interface Intersection extends Updatable
{
    /**
    * Returns the position of the intersection as a Vec2.
    *
    * @return The position vector.
    */
    Vec2 position();

    /**
    * Returns the traffic light state for a given incoming road.
    * Default implementation returns GREEN (no signal).
    *
    * @param incoming The incoming Road.
    * @return         The TrafficLightState for the road.
    */
    default TrafficLightState getSignalStateFor(Road incoming)
    {
        return TrafficLightState.GREEN;
    }
}
