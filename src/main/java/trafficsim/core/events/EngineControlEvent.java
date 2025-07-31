/***************************************************************

- File:        EngineControlEvent.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Event for controlling simulation engine state.

- Description:
- Represents commands to control the simulation engine, such as
- starting, pausing, or stopping the simulation. Encapsulates the
- control type and provides equality and hashing for event handling.

***************************************************************/

package trafficsim.core.events;

import java.util.Objects;

public final class EngineControlEvent implements SimulationEvent
{
    public enum ControlType {
        START, PAUSE, STOP
    }

    private final ControlType type;

    /**
    * Constructs an EngineControlEvent with the specified control type.
    *
    * @param type The control action to be performed (START, PAUSE, STOP).
    */
    public EngineControlEvent(ControlType type)
    {
        this.type = type;
    }

    /**
     * Returns the type of engine control action for this event.
     *
     * @return The control type (START, PAUSE, or STOP).
     */
    public ControlType getType()
    {
        return type;
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

        EngineControlEvent that = (EngineControlEvent) o;
        return type == that.type;
    }

    /**
    * Computes the hash code for this event.
    *
    * @return The hash code.
    */
    @Override
    public int hashCode()
    {
        return Objects.hash(type);
    }
}
