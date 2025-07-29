package trafficsim.core.model;

import java.util.UUID;

public final class SignalGroup
{
    private final UUID id = UUID.randomUUID();
    private volatile TrafficLightState state = TrafficLightState.RED;

    public UUID id()
    {
        return id;
    }

    public TrafficLightState state()
    {
        return state;
    }

    public void setState(TrafficLightState state)
    {
        this.state = state;
    }
}