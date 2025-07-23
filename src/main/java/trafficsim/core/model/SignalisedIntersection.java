package trafficsim.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SignalisedIntersection implements Intersection
{
    private final Vec2 position;

    private double totalCycleTime;
    private double yellowDuration;
    private double greenDuration;

    private final List<SignalGroup> groups = new ArrayList<>();
    private int currentPhase = 0;
    private double phaseTimer = 0;

    public SignalisedIntersection(double x, double y, double totalCycleTime, double yellowDuration)
    {
        this.position = new Vec2(x, y);
        this.totalCycleTime = totalCycleTime;
        this.yellowDuration = yellowDuration;

        recalculateDurations();
    }

    public void addSignalGroup()
    {
        groups.add(new SignalGroup());
        recalculateDurations();
    }

    public List<SignalGroup> groups()
    {
        return Collections.unmodifiableList(groups);
    }

    @Override
    public Vec2 position()
    {
        return position;
    }

    @Override
    public void update(double deltaTime)
    {
        if (groups.isEmpty())
        {
            return;
        }

        phaseTimer += deltaTime;

        double yellowStart = greenDuration;
        double phaseLength = greenDuration + yellowDuration;
        SignalGroup active = groups.get(currentPhase);

        if (phaseTimer < yellowStart)
        {
            active.setState(TrafficLightState.GREEN);
        } else if (phaseTimer < phaseLength)
        {
            active.setState(TrafficLightState.YELLOW);
        } else
        {
            active.setState(TrafficLightState.RED);

            currentPhase = (currentPhase + 1) % groups.size();
            groups.get(currentPhase).setState(TrafficLightState.GREEN);
            phaseTimer = 0;
        }
    }

    private void recalculateDurations()
    {
        if (groups.isEmpty())
        {
            greenDuration = 0;
            return;
        }

        greenDuration = Math.max(0, (totalCycleTime / groups.size()) - yellowDuration);
    }

    @Override
    public boolean mayEnter(Road incoming, double tToArrivalSecs)
    {
        return !groups.isEmpty() && groups.get(currentPhase).state() == TrafficLightState.GREEN;
    }
}