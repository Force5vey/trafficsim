package trafficsim.core.model;

public interface Intersection extends Updatable
{
    Vec2 position();

    default boolean mayEnter(Road incoming, double tToArrivalSecs)
    {
        // default for roundabouts / unsignalised 
        return true;
    }
}
