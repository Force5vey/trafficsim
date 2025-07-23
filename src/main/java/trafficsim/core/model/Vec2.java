package trafficsim.core.model;

public final class Vec2
{
    public final double x;
    public final double y;

    public Vec2(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public double distanceTo(Vec2 other)
    {
        double dx = x - other.x, dy = y - other.y;
        return Math.hypot(dx, dy);
    }
}