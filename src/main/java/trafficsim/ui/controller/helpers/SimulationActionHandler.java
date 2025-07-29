package trafficsim.ui.controller.helpers;

import java.util.List;
import java.util.Optional;

import trafficsim.core.events.*;
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
        engine.postEvent(new AddIntersectionEvent((intersection)));
        renderer.onIntersectionAdded(intersection);
    }

    public void addRoad(Intersection from, Intersection to, double speedMps)
    {
        double length = from.position().distanceTo(to.position());
        Road road1 = new Road(from, to, length, speedMps);
        engine.postEvent(new AddRoadEvent(road1));
        renderer.onRoadAdded(road1);

        Road road2 = new Road(to, from, length, speedMps);
        engine.postEvent(new AddRoadEvent(road2));
        renderer.onRoadAdded(road2);
    }

    public void addCar(Car car, Intersection spawnPoint)
    {
        engine.postEvent(new AddCarEvent(car, spawnPoint));
        renderer.onCarAdded(car);
    }

    public void deleteItem(Object item)
    {
        engine.postEvent(new DeleteItemEvent(item));

        if (item instanceof Intersection)
        {
            Intersection i = (Intersection) item;
            List<Road> affectedRoads = engine.roadNetwork().findAllConnectedRoads(i);
            renderer.removeIntersection(i);
            for (Road road : affectedRoads)
            {
                renderer.removeRoad(road);
            }
        } else if (item instanceof Road)
        {
            Road roadToDelete = (Road) item;
            Optional<Road> oppositeRoadOpt = engine.roadNetwork().findOppositeRoad(roadToDelete);
            renderer.removeRoad(roadToDelete);
            oppositeRoadOpt.ifPresent(renderer::removeRoad);
        } else if (item instanceof Car)
        {
            renderer.removeCar((Car) item);
        }
    }

    public void clearAll()
    {
        engine.postEvent(ClearAllEvent.INSTANCE);
        renderer.clearAll();
    }
}