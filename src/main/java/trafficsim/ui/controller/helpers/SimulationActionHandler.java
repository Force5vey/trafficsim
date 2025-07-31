/***************************************************************

- File:        SimulationActionHandler.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Handles user-initiated actions and posts events to the simulation engine.

- Description:
- Provides methods for adding and removing intersections, roads, and cars,
- as well as clearing the simulation. Coordinates UI updates with the
- simulation model by posting events and invoking renderer callbacks.

***************************************************************/

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

    /**
    * Adds a new intersection to the simulation and updates the UI.
    *
    * @param intersection The intersection to add.
    */
    public void addIntersection(Intersection intersection)
    {
        engine.postEvent(new AddIntersectionEvent((intersection)));
        renderer.onIntersectionAdded(intersection);
    }

    /**
    * Adds a bidirectional road between two intersections with the specified speed.
    * Posts events for both directions and updates the UI.
    *
    * @param from      The starting intersection.
    * @param to        The ending intersection.
    * @param speedMps  The speed limit for the road in meters per second.
    */
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

    /**
    * Adds a car to the simulation at the specified intersection and updates the UI.
    *
    * @param car         The car to add.
    * @param spawnPoint  The intersection where the car will be spawned.
    */
    public void addCar(Car car, Intersection spawnPoint)
    {
        engine.postEvent(new AddCarEvent(car, spawnPoint));
        renderer.onCarAdded(car);
    }

    /**
    * Deletes the specified item (intersection, road, or car) from the simulation.
    * Posts a deletion event and updates the UI accordingly.
    *
    * @param item The model object to delete.
    */
    public void deleteItem(Object item)
    {
        final Runnable uiUpdateCallback;

        if (item instanceof Intersection)
        {
            Intersection i = (Intersection) item;
            List<Road> affectedRoads = engine.roadNetwork().findAllConnectedRoads(i);
            uiUpdateCallback = () ->
            {
                renderer.removeIntersection(i);
                for (Road road : affectedRoads)
                {
                    renderer.removeRoad(road);
                }
            };
        } else if (item instanceof Road)
        {
            Road roadToDelete = (Road) item;
            Optional<Road> oppositeRoadOpt = engine.roadNetwork().findOppositeRoad(roadToDelete);
            uiUpdateCallback = () ->
            {
                renderer.removeRoad(roadToDelete);
                oppositeRoadOpt.ifPresent(renderer::removeRoad);
            };
        } else if (item instanceof Car)
        {
            Car carToDelete = (Car) item;
            uiUpdateCallback = () -> renderer.removeCar(carToDelete);
        } else
        {
            uiUpdateCallback = () ->
            {
            };
        }

        engine.postEvent(new DeleteItemEvent(item, uiUpdateCallback));
    }

    /**
    * Clears all items from the simulation and UI.
    */
    public void clearAll()
    {
        engine.postEvent(ClearAllEvent.INSTANCE);
        renderer.clearAll();
    }
}