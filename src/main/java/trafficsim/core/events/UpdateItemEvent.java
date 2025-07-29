package trafficsim.core.events;

import java.util.function.Consumer;

public final class UpdateItemEvent<T> implements AppliableCommand
{
    private final T item;
    private final Consumer<T> updater;

    public UpdateItemEvent(T item, Consumer<T> updater)
    {
        this.item = item;
        this.updater = updater;
    }

    @Override
    public void apply()
    {
        updater.accept(item);
    }
}
