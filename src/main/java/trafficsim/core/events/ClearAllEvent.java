/***************************************************************

- File:        ClearAllEvent.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Event for clearing all simulation state.

- Description:
- Represents a command to remove all simulation objects and reset
- the simulation state. Used to trigger a full reset of the model
- and simulation environment.

***************************************************************/

package trafficsim.core.events;

public final class ClearAllEvent implements ModelCommandEvent
{
    public static final ClearAllEvent INSTANCE = new ClearAllEvent();

    private ClearAllEvent()
    {
    }
}