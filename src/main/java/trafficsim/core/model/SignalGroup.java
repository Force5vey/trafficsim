package trafficsim.core.model;

import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class SignalGroup
{
    private final UUID id;
    private final ObjectProperty<TrafficLightState> state;

    public SignalGroup()
    {
        this.id = UUID.randomUUID();
        this.state = new SimpleObjectProperty<>(TrafficLightState.RED);
    }

    public UUID getId()
    {
        return id;
    }

    public TrafficLightState getState()
    {
        return state.get();
    }

    public void setState(TrafficLightState state)
    {
        this.state.set(state);
    }

    public ObjectProperty<TrafficLightState> stateProperty()
    {
        return state;
    }
}