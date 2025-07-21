package trafficsim.core.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Car
{
    private final DoubleProperty xPosition = new SimpleDoubleProperty(0);
    private final DoubleProperty speed = new SimpleDoubleProperty(0);

    private final DoubleProperty acceleration = new SimpleDoubleProperty(0);
    private double maxSpeed = 35; // m/s - again, need to update so this is all based on mph
    private double targetSpeed = 30;

    public void update(double deltaTime)
    {
        if (getSpeed() < targetSpeed)
        {
            setSpeed(Math.min(getSpeed() + acceleration.get() * deltaTime, targetSpeed));
        } else if (getSpeed() > targetSpeed)
        {
            setSpeed(Math.max(getSpeed() - acceleration.get() * deltaTime, targetSpeed));
        }

        double distance = getSpeed() * deltaTime;
        setXPosition(getXPosition() + distance);
    }

    public double getMaxSpeed()
    {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed)
    {
        this.maxSpeed = maxSpeed;
    }

    public double getTargetSpeed()
    {
        return targetSpeed;
    }

    public void setTargetSpeed(double targetSpeed)
    {
        this.targetSpeed = targetSpeed;
    }

    public double getAcceleration()
    {
        return acceleration.get();
    }

    public void setAcceleration(double acceleration)
    {
        this.acceleration.set(acceleration);
    }

    public DoubleProperty accelerationProperty()
    {
        return acceleration;
    }

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
