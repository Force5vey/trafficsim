package trafficsim.core.model;

import java.util.*;

public final class RoadNetwork
{
    private final Map<Intersection, List<Road>> adj = new HashMap<>();

    public void add(Road road)
    {
        adj.computeIfAbsent(road.from(), k -> new ArrayList<>()).add(road);

        if (road.to() instanceof SignalisedIntersection)
        {
            ((SignalisedIntersection) road.to()).registerIncomingRoad(road);
        }
    }

    public List<Road> outgoing(Intersection intersection)
    {
        return adj.getOrDefault(intersection, List.of());
    }
}