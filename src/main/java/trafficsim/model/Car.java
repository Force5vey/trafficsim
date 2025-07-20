package trafficsim.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Car
{
    private final DoubleProperty xPosition = new SimpleDoubleProperty(0);
    private final DoubleProperty speed = new SimpleDoubleProperty(0);

    public double getXPosition()
    {
        return xPosition.get();
    }

    public DoubleProperty xPositionProperty()
    {
        return xPosition;
    }

    public void setXPosition(double xPosition)
    {
        this.xPosition.set(xPosition);
    }

    public double getSpeed()
    {
        return speed.get();
    }

    public DoubleProperty speedProperty()
    {
        return speed;
    }

    public void setSpeed(double speed)
    {
        this.speed.set(speed);
    }
}
