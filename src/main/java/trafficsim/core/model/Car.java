package trafficsim.core.model;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Car implements Updatable
{
    private static final double STOP_LINE_OFFSET_METERS = 7.0;

    private double maxSpeed;
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

    private boolean shouldStopForLight()
    {
        if (road == null)
        {
            return true;
        }
        Intersection dest = road.to();
        TrafficLightState lightState = dest.getSignalStateFor(road);

        if (lightState == TrafficLightState.GREEN)
        {
            return false;
        }

        if (lightState == TrafficLightState.RED)
        {
            return true;
        }

        double stopLine = Math.max(0, road.length() - STOP_LINE_OFFSET_METERS);
        double remainingDistanceToStopLine = stopLine - s;
        double requiredStoppingDistance = (v * v) / (2.0 * acceleration);

        return requiredStoppingDistance <= remainingDistanceToStopLine;
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

        double potentialNewS = s + v * deltaTime;
        boolean stopIsRequired = shouldStopForLight();

        if (stopIsRequired)
        {
            double stopLine = Math.max(0, road.length() - STOP_LINE_OFFSET_METERS);
            if (potentialNewS >= stopLine)
            {
                s = stopLine;
                v = 0;
            } else
            {
                s = potentialNewS;
            }
        } else
        {
            if (potentialNewS >= road.length())
            {
                double overflowDistance = potentialNewS - road.length();
                enterNextRoad();
                if (road != null)
                {
                    s = overflowDistance;
                    v = Math.min(v, road.speedLimit());
                } else
                {
                    s = 0;
                    v = 0;
                }
            } else
            {
                s = potentialNewS;
            }
        }
    }

    private void decideTargetSpeed()
    {
        if (shouldStopForLight())
        {
            double stopLine = Math.max(0, road.length() - STOP_LINE_OFFSET_METERS);
            double remaining = stopLine - s;
            double safeSpeed = Math.sqrt(2.0 * acceleration * Math.max(0, remaining));
            targetV = Math.min(safeSpeed, road.speedLimit());
        } else
        {
            targetV = Math.min(maxSpeed, road.speedLimit());
        }
    }

    private void enterNextRoad()
    {
        Intersection node = road.to();
        Intersection prevNode = road.from();
        List<Road> allOutgoing = net.outgoing(node);

        if (allOutgoing.isEmpty())
        {
            road = null;
            return;
        }

        List<Road> validChoices;
        if (node instanceof SignalisedIntersection)
        {
            validChoices = allOutgoing.stream().filter(r -> r.to() != prevNode).collect(Collectors.toList());
        } else
        {
            // roundabouts
            validChoices = allOutgoing;
        }

        if (validChoices.isEmpty())
        {
            // dead end road
            road = null;
            return;
        }

        road = validChoices.get(rng.nextInt(validChoices.size()));
        s = 0.0;
    }

    public Vec2 worldPos()
    {
        if (road == null)
        {
            return new Vec2(0, 0);
        }

        double t = s / road.length();
        Vec2 a = road.from().position(), b = road.to().position();
        return new Vec2(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t);
    }

    public double headingRad()
    {
        if (road == null)
        {
            return 0.0;
        }

        Vec2 a = road.from().position(), b = road.to().position();
        return Math.atan2(b.y - a.y, b.x - a.x);
    }

    public double getMaxSpeed()
    {
        return maxSpeed;
    }

    public double getAcceleration()
    {
        return acceleration;
    }

    public void setMaxSpeed(double v)
    {
        this.maxSpeed = Math.max(0, v);
    }

    public void setAcceleration(double a)
    {
        this.acceleration = Math.max(0, a);
    }
}