/***************************************************************

- File:        SignalGroup.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Represents a group of traffic signals for an incoming road.

- Description:
- Encapsulates the state and unique identifier for a signal group
- associated with a specific incoming road at a signalised intersection.
- Provides methods to get and set the current traffic light state.

***************************************************************/

package trafficsim.core.model;

import java.util.UUID;

public final class SignalGroup
{
    private final UUID id = UUID.randomUUID();
    private volatile TrafficLightState state = TrafficLightState.RED;

    /**
    * Returns the unique identifier for this signal group.
    *
    * @return UUID of the signal group.
    */
    public UUID id()
    {
        return id;
    }

    /**
    * Returns the current traffic light state of this signal group.
    *
    * @return The current TrafficLightState.
    */
    public TrafficLightState state()
    {
        return state;
    }

    /**
    * Sets the traffic light state for this signal group.
    *
    * @param state The new TrafficLightState.
    */
    public void setState(TrafficLightState state)
    {
        this.state = state;
    }
}