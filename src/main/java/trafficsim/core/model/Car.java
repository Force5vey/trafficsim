package trafficsim.core.model;

import java.util.List;
import java.util.Random;

public class Car implements Updatable
{
    private final double maxSpeed;
    private double acceleration;
    private final RoadNetwork net;
    private final Random rng = new Random();

    // Dynamic state
    private Road road;
    private double s;
    private double v;
    private double targetV;

    public Car(RoadNetwork net, double maxSpeed, double acceleration)
    {
        this.net = net;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.v = 0.0;
        this.targetV = 0.0;
    }

    // constructor for some defaults
    public Car(RoadNetwork net)
    {
        this(net, 35.0, 2.0);
    }

    public void attachTo(Road road, double offsetMeters)
    {
        this.road = road;
        this.s = Math.max(0.0, Math.min(offsetMeters, road.length()));
    }

    @Override
    public void update(double deltaTime)
    {
        if (road == null)
        {
            return;
        }

        decideTargetSpeed();

        if (Math.abs(v - targetV) < 1e-3)
        {
            v = targetV;
        } else if (v < targetV)
        {
            v = Math.min(v + acceleration * deltaTime, targetV);
        } else
        {
            v = Math.max(v - acceleration * deltaTime, targetV);
        }

        double ds = v * deltaTime;
        while (s + ds >= road.length())
        {
            ds -= (road.length() - s);
            enterNextRoad();
            if (road == null)
            {
                v = 0.0;
                return;
            }
        }
        s += ds;
    }

    private void decideTargetSpeed()
    {
        double remaining = road.length() - s;
        double timeToNode = remaining / Math.max(v, 0.1);
        Intersection dest = road.to();

        boolean canGo = dest.mayEnter(road, timeToNode);

        if (!canGo)
        {
            double stopDist = v * v / (2.0 * acceleration);
            if (stopDist >= remaining - 1.0)
            {
                targetV = 0.0;
                return;
            }
        }
        targetV = Math.min(maxSpeed, road.speedLimit());
    }

    private void enterNextRoad()
    {
        Intersection node = road.to();
        List<Road> outs = net.outgoing(node);
        if (outs.isEmpty())
        {
            road = null;
            return;
        }
        road = outs.get(rng.nextInt(outs.size()));
        s = 0.0;
    }

    public Vec2 worldPos()
    {
        double t = s / road.length();
        Vec2 a = road.from().position(), b = road.to().position();
        return new Vec2(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t);
    }

    public double headingRad()
    {
        Vec2 a = road.from().position(), b = road.to().position();
        return Math.atan2(b.y - a.y, b.x - a.x);
    }
}