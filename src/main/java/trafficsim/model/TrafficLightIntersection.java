package trafficsim.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class TrafficLightIntersection implements IIntersection
{
    // Position Properties
    private final DoubleProperty positionX = new SimpleDoubleProperty();
    private final DoubleProperty positionY = new SimpleDoubleProperty();

    private final DoubleProperty totalCycleTime;
    private final DoubleProperty yellowDuration;

    private double calculatedGreenDuration;

    private final List<SignalGroup> signalGroups = new ArrayList<>();
    private int currentPhaseIndex = 0;
    private double phaseTimer = 0;

    public TrafficLightIntersection(double x, double y, double totalCycleTime, double yellowDuration)
    {
        this.positionX.set(x);
        this.positionY.set(y);
        this.totalCycleTime = new SimpleDoubleProperty(totalCycleTime);
        this.yellowDuration = new SimpleDoubleProperty(yellowDuration);

        this.totalCycleTime.addListener((obs, oldVal, newVal) -> recalculateDurations());
        this.yellowDuration.addListener((obs, oldVal, newVal) -> recalculateDurations());
    }

    public void addSignalGroup()
    {
        this.signalGroups.add(new SignalGroup());
        recalculateDurations();
    }

    private void recalculateDurations()
    {
        int numPhases = signalGroups.size();
        if (numPhases == 0)
        {
            this.calculatedGreenDuration = 0;
            return;
        }

        double greenTime = (this.totalCycleTime.get() / numPhases) - this.yellowDuration.get();

        this.calculatedGreenDuration = Math.max(0, greenTime);

        if (greenTime <= 0)
        {
            System.err.println("Warning: Calculated green duration is zero or negative. "
                    + "Total cycle time may be too short or yellow duration too long.");
        }
    }

    @Override
    public void update(double deltaTime)
    {
        if (signalGroups.isEmpty())
        {
            return; // no roads connected
        }

        phaseTimer += deltaTime;

        double currentPhaseYellowStartTime = calculatedGreenDuration;
        double currentPhaseTotalDuration = calculatedGreenDuration;

        SignalGroup activeGroup = signalGroups.get(currentPhaseIndex);

        if (phaseTimer < currentPhaseYellowStartTime)
        {
            activeGroup.setState(TrafficLightState.GREEN);
        } else if (phaseTimer < currentPhaseTotalDuration)
        {
            activeGroup.setState(TrafficLightState.YELLOW);
        } else
        {
            activeGroup.setState(TrafficLightState.RED);
            currentPhaseIndex = (currentPhaseIndex + 1) % signalGroups.size();
            signalGroups.get(currentPhaseIndex).setState(TrafficLightState.GREEN);
            phaseTimer = 0;
        }
    }

    public List<SignalGroup> getSignalGroups()
    {
        return Collections.unmodifiableList(signalGroups);
    }

    @Override
    public double getPositionX()
    {
        return positionX.get();
    }

    @Override
    public DoubleProperty positionXProperty()
    {
        return positionX;
    }

    @Override
    public double getPositionY()
    {
        return positionY.get();
    }

    @Override
    public DoubleProperty positionYProperty()
    {
        return positionY;
    }

    public double getTotalCycleTime()
    {
        return totalCycleTime.get();
    }

    public void setTotalCycleTime(double time)
    {
        this.totalCycleTime.set(time);
    }

    public DoubleProperty totalCycleTimeProperty()
    {
        return totalCycleTime;
    }

    public double getYellowDuration()
    {
        return yellowDuration.get();
    }

    public void setYellowDuration(double duration)
    {
        this.yellowDuration.set(duration);
    }

    public DoubleProperty yellowDurationProperty()
    {
        return yellowDuration;
    }
}