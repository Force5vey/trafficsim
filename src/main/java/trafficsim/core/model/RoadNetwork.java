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

    public List<Road> findAllConnectedRoads(Intersection intersection)
    {
        Set<Road> connected = new HashSet<>();

        connected.addAll(outgoing(intersection));

        for (List<Road> roads : adj.values())
        {
            for (Road road : roads)
            {
                if (road.to().equals(intersection))
                {
                    connected.add(road);
                }
            }
        }
        return new ArrayList<>(connected);
    }

    public void removeRoad(Road road)
    {
        if (road == null)
        {
            return;
        }
        List<Road> outgoingRoads = adj.get(road.from());
        if (outgoingRoads != null)
        {
            outgoingRoads.remove(road);
        }
    }

    public void removeIntersection(Intersection intersection)
    {
        adj.remove(intersection);
    }
}