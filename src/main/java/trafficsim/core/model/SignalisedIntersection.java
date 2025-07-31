/***************************************************************

- File:        SignalisedIntersection.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Represents an intersection with traffic signals in the simulation.

- Description:
- Manages signal groups for incoming roads, handles signal timing and
- phase transitions, and provides signal state information for each road.
- Supports dynamic updates to signal timing parameters.

***************************************************************/

package trafficsim.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public final class SignalisedIntersection implements Intersection
{
    private final Vec2 position;

    private volatile double totalCycleTime;
    private volatile double yellowDuration;
    private double greenDuration;

    private final Map<Road, SignalGroup> signalMap = new HashMap<>();
    private final List<SignalGroup> signalCycle = new ArrayList<>();
    private int currentPhase = 0;
    private double phaseTimer = 0;

    /**
    * Constructs a SignalisedIntersection at the given position with specified
    * total cycle time and yellow duration.
    *
    * @param x             The x-coordinate of the intersection.
    * @param y             The y-coordinate of the intersection.
    * @param totalCycleTime The total duration of a full signal cycle (seconds).
    * @param yellowDuration The duration of the yellow phase (seconds).
    */
    public SignalisedIntersection(double x, double y, double totalCycleTime, double yellowDuration)
    {
        this.position = new Vec2(x, y);
        this.totalCycleTime = totalCycleTime;
        this.yellowDuration = yellowDuration;

        recalculateDurations();
    }

    /**
    * Registers a new incoming road with this intersection and creates a signal group for it.
    * Recalculates signal timings and sets the first group to green if this is the first road.
    *
    * @param road The incoming Road to register.
    */
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

    /**
    * Unregisters an incoming road and removes its signal group.
    * Recalculates signal timings.
    *
    * @param road The Road to unregister.
    */
    public void unregisterIncomingRoad(Road road)
    {
        SignalGroup groupToRemove = signalMap.remove(road);
        if (groupToRemove != null)
        {
            signalCycle.remove(groupToRemove);
            recalculateDurations();
        }
    }

    /**
    * Returns an unmodifiable map of roads to their signal groups.
    *
    * @return Map of Road to SignalGroup.
    */
    public Map<Road, SignalGroup> getSignalMap()
    {
        return Collections.unmodifiableMap(signalMap);
    }

    /**
    * Returns the position of the intersection as a Vec2.
    *
    * @return The position vector.
    */
    @Override
    public Vec2 position()
    {
        return position;
    }

    /**
    * Updates the signal phase and state based on elapsed time.
    * Advances the signal cycle and updates signal group states.
    *
    * @param deltaTime The time step in seconds.
    */
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

    /**
    * Returns the current signal state for a given incoming road.
    *
    * @param incoming The incoming Road.
    * @return         The TrafficLightState for the road.
    */
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

    /**
    * Returns the total signal cycle time in seconds.
    *
    * @return The total cycle time.
    */
    public double getTotalCycleTime()
    {
        return totalCycleTime;
    }

    /**
    * Sets the total signal cycle time and recalculates green durations.
    *
    * @param totalCycleTime The new total cycle time in seconds.
    */
    public void setTotalCycleTime(double totalCycleTime)
    {
        this.totalCycleTime = totalCycleTime;
    }

    /**
    * Returns the yellow phase duration in seconds.
    *
    * @return The yellow duration.
    */
    public double getYellowDuration()
    {
        return yellowDuration;
    }

    /**
    * Sets the yellow phase duration and recalculates green durations.
    *
    * @param yellowDuration The new yellow duration in seconds.
    */
    public void setYellowDuration(double yellowDuration)
    {
        this.yellowDuration = yellowDuration;
        recalculateDurations();
    }

}