package trafficsim.core.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Roundabout implements Intersection
{
    private final DoubleProperty positionX = new SimpleDoubleProperty();
    private final DoubleProperty positionY = new SimpleDoubleProperty();
    private double speedLimit; // Temp pixels/sec

    public Roundabout(double x, double y, double speedLimit)
    {
        this.positionX.set(x);
        this.positionY.set(y);
        this.speedLimit = speedLimit;
    }

    @Override
    public void update(double deltaTime)
    {
        // no time based state, cars approach, and the update will be on the cars
        // holding this incase other updates need to happen later.
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

    public double getSpeedLimit()
    {
        return speedLimit;
    }

    public void setSpeedLimit(double speedLimit)
    {
        this.speedLimit = speedLimit;
    }
}
