package trafficsim.core.model;

public final class Roundabout implements Intersection
{
    private final Vec2 position;
    private double speedLimit;

    public Roundabout(double x, double y, double speedLimit)
    {
        this.position = new Vec2(x, y);
        this.speedLimit = speedLimit;
    }

    @Override
    public Vec2 position()
    {
        return position;
    }

    @Override
    public void update(double deltaTime)
    {
        // no time based state, cars approach, and the update will be on the cars
        // holding this incase other updates need to happen later.
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