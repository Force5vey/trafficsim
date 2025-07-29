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

        DeleteItemEvent that = (DeleteItemEvent) o;
        return Objects.equals(itemToDelete, that.itemToDelete);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(itemToDelete);
    }
}
