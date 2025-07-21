package trafficsim.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.application.Platform;

public class Intersection
{
    private final ObjectProperty<TrafficLightState> lightState = new SimpleObjectProperty<>(TrafficLightState.RED);

    private double positionX;
    private double greenDuration;
    private double yellowDuration;
    private double redDuration;
    private double cycleTimer = 0;

    public void update(double deltaTime)
    {
        cycleTimer += deltaTime;

        double totalCycleTime = greenDuration + yellowDuration + redDuration;
        if (cycleTimer > totalCycleTime)
        {
            cycleTimer -= totalCycleTime;
        }

        TrafficLightState newState;
        if (cycleTimer < greenDuration)
        {
            newState = TrafficLightState.GREEN;
        } else if (cycleTimer < greenDuration + yellowDuration)
        {
            newState = TrafficLightState.YELLOW;
        } else
        {
            newState = TrafficLightState.RED;
        }

        if (getLightState() != newState)
        {
            Platform.runLater(() -> setLightState(newState));
        }
    }

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
