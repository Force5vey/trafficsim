package trafficsim.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public final class SignalisedIntersection implements Intersection
{
    private final Vec2 position;

    private double totalCycleTime;
    private double yellowDuration;
    private double greenDuration;

    private final Map<Road, SignalGroup> signalMap = new HashMap<>();
    private final List<SignalGroup> signalCycle = new ArrayList<>();
    private int currentPhase = 0;
    private double phaseTimer = 0;

    public SignalisedIntersection(double x, double y, double totalCycleTime, double yellowDuration)
    {
        this.position = new Vec2(x, y);
        this.totalCycleTime = totalCycleTime;
        this.yellowDuration = yellowDuration;

        recalculateDurations();
    }

    public void registerIncomingRoad(Road road)
    {
        if (signalMap.containsKey(road))
        {
            return;
        }
        SignalGroup newGroup = new SignalGroup();
        signalMap.put(road, newGroup);
        signalCycle.add(newGroup);
        recalculateDurations();

        if (signalCycle.size() == 1)
        {
            newGroup.setState(TrafficLightState.GREEN);
        }
    }

    public void unregisterIncomingRoad(Road road)
    {
        SignalGroup groupToRemove = signalMap.remove(road);
        if (groupToRemove != null)
        {
            signalCycle.remove(groupToRemove);
            recalculateDurations();
        }
    }

    public Map<Road, SignalGroup> getSignalMap()
    {
        return Collections.unmodifiableMap(signalMap);
    }

    @Override
    public Vec2 position()
    {
        return position;
    }

    @Override
    public void update(double deltaTime)
    {
        if (signalCycle.isEmpty())
        {
            return;
        }

        phaseTimer += deltaTime;

        double yellowStart = greenDuration;
        double phaseLength = greenDuration + yellowDuration;

        if (currentPhase >= signalCycle.size())
        {
            currentPhase = 0;
            if (signalCycle.isEmpty())
            {
                return;
            }
        }

        SignalGroup active = signalCycle.get(currentPhase);

        if (phaseTimer < yellowStart)
        {
            active.setState(TrafficLightState.GREEN);
        } else if (phaseTimer < phaseLength)
        {
            active.setState(TrafficLightState.YELLOW);
        } else
        {
            active.setState(TrafficLightState.RED);

            currentPhase = (currentPhase + 1) % signalCycle.size();
            if (!signalCycle.isEmpty())
            {
                signalCycle.get(currentPhase).setState(TrafficLightState.GREEN);
            }
            phaseTimer = 0;
        }
    }

    private void recalculateDurations()
    {
        if (signalCycle.isEmpty())
        {
            greenDuration = 0;
            return;
        }
        greenDuration = Math.max(0, (totalCycleTime / signalCycle.size()) - yellowDuration);
    }

    @Override
    public TrafficLightState getSignalStateFor(Road incoming)
    {
        SignalGroup group = signalMap.get(incoming);
        if (group == null)
        {
            return TrafficLightState.RED;
        }
        return group.state();
    }

}