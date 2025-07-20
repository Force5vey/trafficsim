package trafficsim.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Intersection
{
    private final ObjectProperty<TrafficLightState> lightState = new SimpleObjectProperty<>(TrafficLightState.RED);

    public TrafficLightState getLightState()
    {
        return lightState.get();
    }

    public ObjectProperty<TrafficLightState> lightStateProperty()
    {
        return lightState;
    }

    public void setLightState(TrafficLightState state)
    {
        this.lightState.set(state);
    }
}
