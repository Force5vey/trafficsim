/***************************************************************

- File:        RoadNetwork.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Manages the network of roads and intersections in the simulation.

- Description:
- Stores and manages the connectivity between intersections and roads.
- Provides methods for adding, removing, and querying roads and intersections,
- as well as for finding connected and opposite roads.

***************************************************************/

package trafficsim.core.model;

import java.util.*;

public final class RoadNetwork
{
    private final Map<Intersection, List<Road>> adj = new HashMap<>();

    /**
    * Adds a road to the network and registers it with the destination intersection
    * if it is signalised.
    *
    * @param road The Road to add.
    */
    public void add(Road road)
    {
        adj.computeIfAbsent(road.from(), k -> new ArrayList<>()).add(road);

        if (road.to() instanceof SignalisedIntersection)
        {
            ((SignalisedIntersection) road.to()).registerIncomingRoad(road);
        }
    }

    /**
    * Returns a list of outgoing roads from the specified intersection.
    *
    * @param intersection The intersection to query.
    * @return             List of outgoing Road objects.
    */
    public List<Road> outgoing(Intersection intersection)
    {
        return adj.getOrDefault(intersection, List.of());
    }

    /**
    * Finds the road in the network that is the opposite direction of the given road.
    *
    * @param road The road to find the opposite for.
    * @return     Optional containing the opposite Road, or empty if not found.
    */
    public Optional<Road> findOppositeRoad(Road road)
    {
        if (road == null)
        {
            return Optional.empty();
        }

        Intersection from = road.from();
        Intersection to = road.to();

        List<Road> candidates = outgoing(to);
        return candidates.stream().filter(r -> r.to().equals(from)).findFirst();
    }

    /**
    * Finds all roads connected to the specified intersection, both incoming and outgoing.
    *
    * @param intersection The intersection to query.
    * @return             List of all connected Road objects.
    */
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

    /**
    * Removes the specified road from the network.
    *
    * @param road The Road to remove.
    */
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

    /**
    * Removes the specified intersection and its outgoing roads from the network.
    *
    * @param intersection The Intersection to remove.
    */
    public void removeIntersection(Intersection intersection)
    {
        adj.remove(intersection);
    }

    /**
    * Removes all roads and intersections from the network.
    */
    public void clear()
    {
        adj.clear();
    }
}