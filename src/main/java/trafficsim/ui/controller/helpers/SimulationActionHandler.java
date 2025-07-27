package trafficsim.ui.controller.helpers;

import java.util.List;
import java.util.Optional;

import trafficsim.core.model.*;
import trafficsim.core.sim.SimulationEngine;
import trafficsim.ui.view.SimulationRenderer;

public class SimulationActionHandler
{
    private final SimulationEngine engine;
    private final SimulationRenderer renderer;

    public SimulationActionHandler(SimulationEngine engine, SimulationRenderer renderer)
    {
        this.engine = engine;
        this.renderer = renderer;
    }

    public void addIntersection(Intersection intersection)
    {
        engine.addIntersection(intersection);
        renderer.onIntersectionAdded(intersection);
    }

    public void addRoad(Intersection from, Intersection to, double speedMps)
    {
        double length = from.position().distanceTo(to.position());
        Road road1 = new Road(from, to, length, speedMps);
        engine.addRoad(road1);
        renderer.onRoadAdded(road1);

        Road road2 = new Road(to, from, length, speedMps);
        engine.addRoad(road2);
        renderer.onRoadAdded(road2);
    }

    public void addCar(Car car, Intersection spawnPoint)
    {
        List<Road> outs = engine.roadNetwork().outgoing(spawnPoint);
        if (outs.isEmpty())
        {
            return;
        }
        engine.addVehicle(car, outs.get(0), 0.0);
        renderer.onCarAdded(car);
    }

    public void deleteItem(Object item)
    {
        if (item instanceof Intersection)
        {
            deleteIntersection((Intersection) item);
        } else if (item instanceof Road)
        {
            Road roadToDelete = (Road) item;
            Optional<Road> oppositeRoadOpt = engine.roadNetwork().findOppositeRoad(roadToDelete);

            removeRoadAndItsView(roadToDelete);
            oppositeRoadOpt.ifPresent(this::removeRoadAndItsView);

        } else if (item instanceof Car)
        {
            engine.removeVehicle((Car) item);
            renderer.removeCar((Car) item);
        }
    }

    private void removeRoadAndItsView(Road road)
    {
        if (road != null)
        {
            engine.removeRoad(road);
            renderer.removeRoad(road);
        }
    }

    private void deleteIntersection(Intersection intersection)
    {
        List<Road> affectedRoads = engine.removeIntersection(intersection);
        renderer.removeIntersection(intersection);
        for (Road road : affectedRoads)
        {
            renderer.removeRoad(road);
        }
    }

}
