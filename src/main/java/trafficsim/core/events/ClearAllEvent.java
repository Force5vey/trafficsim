package trafficsim.core.events;

public final class ClearAllEvent implements ModelCommandEvent
{
    public static final ClearAllEvent INSTANCE = new ClearAllEvent();

    private ClearAllEvent()
    {
    }
}