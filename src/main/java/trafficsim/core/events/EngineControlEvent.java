package trafficsim.core.events;

import java.util.Objects;

public final class EngineControlEvent implements SimulationEvent
{
    public enum ControlType {
        START, PAUSE, STOP
    }

    private final ControlType type;

    public EngineControlEvent(ControlType type)
    {
        this.type = type;
    }

    public ControlType getType()
    {
        return type;
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

        EngineControlEvent that = (EngineControlEvent) o;
        return type == that.type;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type);
    }
}
