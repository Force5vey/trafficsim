/***************************************************************

- File:        ModelCommandEvent.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Marker interface for model command events.

- Description:
- Serves as a marker for events that command changes to the simulation
- model. Used to distinguish model-altering events from other simulation
- events in the event processing system.

***************************************************************/

package trafficsim.core.events;

/**
 * A marker interface for events that command a change to the simulation model.
 */
public interface ModelCommandEvent extends SimulationEvent
{

}