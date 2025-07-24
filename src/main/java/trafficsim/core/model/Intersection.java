package trafficsim.core.model;

public interface Intersection extends Updatable
{
    Vec2 position();

    default TrafficLightState getSignalStateFor(Road incoming)
    {
        return TrafficLightState.GREEN;
    }
}
