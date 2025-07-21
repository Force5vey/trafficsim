package trafficsim.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TrafficLightIntersection implements IIntersection
{
    // Position Properties
    private final DoubleProperty positionX = new SimpleDoubleProperty();
    private final DoubleProperty positionY = new SimpleDoubleProperty();

    // EW = east-west, NS = North-south
    private final ObjectProperty<TrafficLightState> lightStateEW = new SimpleObjectProperty<>(TrafficLightState.RED);
    private final ObjectProperty<TrafficLightState> lightStateNS = new SimpleObjectProperty<>(TrafficLightState.GREEN);

    // Durations - in seconds
    private double greenDuration;
    private double yellowDuration;
    private double redDuration;

    private double cycleTimer = 0;

    public TrafficLightIntersection(double x, double y, double green, double yellow)
    {
        this.positionX.set(x);
        this.positionY.set(y);
        this.greenDuration = green;
        this.yellowDuration = yellow;

        // Total cycle time for one direction to go from green to red
        this.redDuration = green + yellow;
    }

    @Override
    public void update(double deltaTime)
    {
        cycleTimer += deltaTime;

        double totalCycleTime = greenDuration + yellowDuration + redDuration;
        if (cycleTimer >= totalCycleTime)
        {
            cycleTimer -= totalCycleTime;
        }

        // State for primary direction
        TrafficLightState newNsState;
        if (cycleTimer < greenDuration)
        {
            newNsState = TrafficLightState.GREEN;
        } else if (cycleTimer < greenDuration + yellowDuration)
        {
            newNsState = TrafficLightState.YELLOW;
        } else
        {
            newNsState = TrafficLightState.RED;
        }

        // Other direction is derived
        TrafficLightState newEwState = (newNsState == TrafficLightState.RED) ? TrafficLightState.GREEN
                : TrafficLightState.RED;

        if (lightStateNS.get() != newNsState)
        {
            lightStateNS.set(newNsState);
        }
        if (lightStateEW.get() != newEwState)
        {
            lightStateEW.set(newEwState);
        }
    }

    @Override
    public double getPositionX()
    {
        return positionX.get();
    }

    @Override
    public DoubleProperty positionXProperty()
    {
        return positionX;
    }

    @Override
    public double getPositionY()
    {
        return positionY.get();
    }

    @Override
    public DoubleProperty positionYProperty()
    {
        return positionY;
    }

    public ObjectProperty<TrafficLightState> lightStateEWProperty()
    {
        return lightStateEW;
    }

    public ObjectProperty<TrafficLightState> lightStateNSProperty()
    {
        return lightStateNS;
    }

    public double getGreenDuration()
    {
        return greenDuration;
    }

    public void setGreenDuration(double d)
    {
        this.greenDuration = d;
    }

    public double getYellowDuration()
    {
        return yellowDuration;
    }

    public void setYellowDuration(double d)
    {
        this.yellowDuration = d;
    }

}
