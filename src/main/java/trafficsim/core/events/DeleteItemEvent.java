/***************************************************************

- File:        DeleteItemEvent.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Event for deleting an item from the simulation.

- Description:
- Encapsulates a request to delete a simulation object (intersection,
- road, or car) and specifies a callback to be executed after deletion.
- Used for model mutation and UI synchronization.

***************************************************************/

package trafficsim.core.events;

import java.util.Objects;

public final class DeleteItemEvent implements ModelCommandEvent
{
    private final Object itemToDelete;
    private final Runnable postDeletionCallback;

    /**
    * Constructs a DeleteItemEvent for the specified item and post-deletion callback.
    *
    * @param itemToDelete         The simulation object to be deleted.
    * @param postDeletionCallBack The callback to run after deletion (must not be null).
    */
    public DeleteItemEvent(Object itemToDelete, Runnable postDeletionCallBack)
    {
        this.itemToDelete = itemToDelete;
        this.postDeletionCallback = Objects.requireNonNull(postDeletionCallBack);
    }

    /**
    * Returns the item to be deleted from the simulation.
    *
    * @return The object to delete.
    */
    public Object getItemToDelete()
    {
        return itemToDelete;
    }

    /**
    * Returns the callback to be executed after the item is deleted.
    *
    * @return The post-deletion Runnable callback.
    */
    public Runnable getPostDeletionCallback()
    {
        return postDeletionCallback;
    }
}