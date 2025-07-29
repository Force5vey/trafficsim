package trafficsim.core.events;

public interface AppliableCommand extends ModelCommandEvent
{
    void apply();
}