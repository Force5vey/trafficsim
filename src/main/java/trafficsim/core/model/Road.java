package trafficsim.core.model;

public final class Road implements Updatable
{
    private final Intersection from, to;
    private final double length;
    private volatile double speedLimit;

    public Road(Intersection from, Intersection to, double length, double speedLimit)
    {
        this.from = from;
        this.to = to;
        this.length = length;
        this.speedLimit = speedLimit;
    }

    public Intersection from()
    {
        return from;
    }

    public Intersection to()
    {
        return to;
    }

    public double length()
    {
        return length;
    }

    public double speedLimit()
    {
        return speedLimit;
    }

    public void setSpeedLimit(double speedLimit)
    {
        this.speedLimit = speedLimit;
    }

    @Override
    public void update(double deltaTime)
    {

    }
}
