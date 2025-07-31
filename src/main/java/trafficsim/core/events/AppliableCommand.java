/***************************************************************

- File:        AppliableCommand.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Interface for model command events that can be applied.

- Description:
- Defines a contract for events that encapsulate an operation to be
- applied to the simulation model. Used for command pattern events
- that modify simulation state when executed.

***************************************************************/

package trafficsim.core.events;

public interface AppliableCommand extends ModelCommandEvent
{
    void apply();
}