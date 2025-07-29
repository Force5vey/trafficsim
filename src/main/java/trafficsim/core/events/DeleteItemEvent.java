package trafficsim.core.events;

import java.util.Objects;

public final class DeleteItemEvent implements ModelCommandEvent
{
    private final Object itemToDelete;
    private final Runnable postDeletionCallback;

    public DeleteItemEvent(Object itemToDelete, Runnable postDeletionCallBack)
    {
        this.itemToDelete = itemToDelete;
        this.postDeletionCallback = Objects.requireNonNull(postDeletionCallBack);
    }

    public Object getItemToDelete()
    {
        return itemToDelete;
    }

    public Runnable getPostDeletionCallback()
    {
        return postDeletionCallback;
    }
}