package trafficsim.core.model;

import java.util.*;

public final class RoadNetwork
{
    private final Map<Intersection, List<Road>> adj = new HashMap<>();

    public void add(Road r)
    {
        adj.computeIfAbsent(r.from(), k -> new ArrayList<>()).add(r);
    }

    public List<Road> outgoing(Intersection i)
    {
        return adj.getOrDefault(i, List.of());
    }
}
