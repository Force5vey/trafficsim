/***************************************************************

- File:        UpdateItemEvent.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Event for applying an update to a simulation item.

- Description:
- Encapsulates an update operation for a simulation object, using a
- Consumer to apply changes to the item. Used for model updates that
- require a command/event pattern.

***************************************************************/

package trafficsim.core.events;

import java.util.function.Consumer;

public final class UpdateItemEvent<T> implements AppliableCommand
{
    private final T item;
    private final Consumer<T> updater;

    /**
    * Constructs an UpdateItemEvent for the given item and updater.
    *
    * @param item    The item to be updated.
    * @param updater The Consumer that applies the update to the item.
    */
    public UpdateItemEvent(T item, Consumer<T> updater)
    {
        this.item = item;
        this.updater = updater;
    }

    /**
    * Applies the update operation to the item using the provided Consumer.
    */
    @Override
    public void apply()
    {
        updater.accept(item);
    }
}
