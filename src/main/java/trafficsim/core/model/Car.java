package trafficsim.core.model;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import trafficsim.ui.adapter.IntersectionUtil;

public class Car implements Updatable
{
    private static final double STOP_LINE_OFFSET_METERS = 7.0;
    private static final double CAR_LENGTH_METERS = 5.0;
    private static final double SAFE_FOLLOWING_GAP_METERS = 3.0;

    private volatile double maxSpeed;
    private volatile double acceleration;
    private final RoadNetwork net;
    private final Random rng = new Random();
    private volatile boolean showDataBubble = true;

    private final Object stateLock = new Object();

    // Dynamic state - guarded by stateLock
    private Road road;
    private double s;
    private double v;
    private Road initialRoad;
    private double initialS;

    // internal state
    private double targetV;
    private List<Updatable> allSimObjects;

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

    public double getVelocity()
    {
        synchronized (stateLock)
        {
            return this.v;
        }
    }

    public void attachTo(Road road, double offsetMeters)
    {
        this.road = road;
        this.s = Math.max(0.0, Math.min(offsetMeters, road.length()));

        this.initialRoad = this.road;
        this.initialS = this.s;
    }

    public void resetToInitialState()
    {
        this.road = this.initialRoad;
        this.s = this.initialS;
        this.v = 0.0;
        this.targetV = 0.0;
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

        if (acceleration < 1e-6)
        {
            return true;
        }

        double stopLine = Math.max(0, road.length() - STOP_LINE_OFFSET_METERS);
        double remainingDistanceToStopLine = stopLine - s;
        double requiredStoppingDistance = (v * v) / (2.0 * acceleration);

        return remainingDistanceToStopLine <= requiredStoppingDistance;
    }

    @Override
    public void update(double deltaTime)
    {
        synchronized (stateLock)
        {
            if (road == null)
            {
                return;
            }
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
                Optional<Road> nextRoadOpt = findNextRoad();
                if (nextRoadOpt.isPresent())
                {
                    double overflowDistance = potentialNewS - road.length();
                    this.road = nextRoadOpt.get();
                    this.s = overflowDistance;
                    this.v = Math.min(v, this.road.speedLimit());
                } else
                {
                    // dead end, no uturns on signalised intersections
                    s = road.length();
                    v = 0;
                }
            } else
            {
                s = potentialNewS;
            }
        }

    }

    public void setSimulationObjects(List<Updatable> objects)
    {
        this.allSimObjects = objects;
    }

    private Optional<Car> findLeader()
    {
        if (allSimObjects == null || road == null)
        {
            return Optional.empty();
        }

        Car leader = null;
        double minDistance = Double.POSITIVE_INFINITY;

        for (Updatable obj : allSimObjects)
        {
            if (obj instanceof Car && obj != this)
            {
                Car otherCar = (Car) obj;

                synchronized (otherCar.stateLock)
                {
                    if (otherCar.road == this.road && otherCar.s > this.s)
                    {
                        double distance = otherCar.s - this.s;
                        if (distance < minDistance)
                        {
                            minDistance = distance;
                            leader = otherCar;
                        }
                    }
                }
            }
        }
        return Optional.ofNullable(leader);
    }

    private void decideTargetSpeed()
    {
        double effectiveSpeedLimit = Math.min(maxSpeed, road.speedLimit());
        double closestObstacleDistance = Double.POSITIVE_INFINITY;

        if (shouldStopForLight())
        {
            double stopLine = Math.max(0, road.length() - STOP_LINE_OFFSET_METERS);
            closestObstacleDistance = Math.max(0, stopLine - s);
        }

        Optional<Car> leaderOpt = findLeader();
        if (leaderOpt.isPresent())
        {
            Car leader = leaderOpt.get();
            double leaderS;

            synchronized (leader.stateLock)
            {
                leaderS = leader.s;
            }

            double distanceToLeader = (leaderS - this.s) - CAR_LENGTH_METERS - SAFE_FOLLOWING_GAP_METERS;

            closestObstacleDistance = Math.min(closestObstacleDistance, Math.max(0, distanceToLeader));
        }

        if (closestObstacleDistance != Double.POSITIVE_INFINITY)
        {
            double safeSpeed = Math.sqrt(2.0 * acceleration * closestObstacleDistance);
            targetV = Math.min(effectiveSpeedLimit, safeSpeed);
        } else
        {
            targetV = effectiveSpeedLimit;
        }
    }

    private Optional<Road> findNextRoad()
    {
        Intersection node = road.to();
        Intersection prevNode = road.from();
        List<Road> allOutgoing = net.outgoing(node);

        if (allOutgoing.isEmpty())
        {
            return Optional.empty();
        }

        List<Road> validChoices;
        if (node instanceof SignalisedIntersection)
        {
            validChoices = allOutgoing.stream().filter(r -> r.to() != prevNode).collect(Collectors.toList());
        } else
        {
            validChoices = allOutgoing;
        }

        if (validChoices.isEmpty())
        {
            return Optional.empty();
        }

        return Optional.of(validChoices.get(rng.nextInt(validChoices.size())));

    }

    public Vec2 worldPos()
    {
        Road localRoad;
        double localS;

        synchronized (stateLock)
        {
            localRoad = this.road;
            localS = this.s;
        }

        if (localRoad == null)
        {
            return new Vec2(0, 0);
        }

        Vec2 a = road.from().position();
        Vec2 b = road.to().position();

        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double lengthMeters = road.length();

        double t = lengthMeters > 0 ? Math.min(1.0, s / lengthMeters) : 0;

        double centerX = a.x + dx * t;
        double centerY = a.y + dy * t;

        Vec2 offsetPx = IntersectionUtil.getLaneOffsetVector(road);

        double offsetX_meters = offsetPx.x / IntersectionUtil.PX_PER_M;
        double offsetY_meters = offsetPx.y / IntersectionUtil.PX_PER_M;

        return new Vec2(centerX + offsetX_meters, centerY + offsetY_meters);
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

    public boolean getShowDataBubble()
    {
        return showDataBubble;
    }

    public void setShowDataBubble(boolean show)
    {
        this.showDataBubble = show;
    }
}