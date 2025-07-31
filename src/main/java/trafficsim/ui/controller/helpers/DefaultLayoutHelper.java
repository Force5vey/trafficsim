/***************************************************************

- File:        DefaultLayoutHelper.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Provides a default road network and car layout for the simulation.

- Description:
- Sets up a basic grid of intersections, roads, and cars to initialize
- the simulation with a working example. Intended for demonstration and
- testing purposes.

***************************************************************/

package trafficsim.ui.controller.helpers;

import java.util.ArrayList;
import java.util.List;

import trafficsim.core.model.Car;
import trafficsim.core.model.Intersection;
import trafficsim.core.model.RoadNetwork;
import trafficsim.core.model.SignalisedIntersection;
import trafficsim.ui.adapter.UnitConverter;

public final class DefaultLayoutHelper
{
    private DefaultLayoutHelper()
    {
        // guard cstr
    }

    /**
    * Sets up a default layout of intersections, roads, and cars in the simulation.
    * Adds a square of signalised intersections, connects them with roads, and
    * spawns a car at each intersection.
    *
    * @param actionHandler The SimulationActionHandler to use for adding items.
    * @param roadNetwork   The RoadNetwork to associate with new cars.
    */
    public static void setupDefaultLayout(SimulationActionHandler actionHandler, RoadNetwork roadNetwork)
    {
        List<Intersection> intersections = new ArrayList<>();
        Intersection i1 = new SignalisedIntersection(20, 20, 25, 3);
        Intersection i2 = new SignalisedIntersection(83, 20, 25, 3);
        Intersection i3 = new SignalisedIntersection(83, 52, 25, 3);
        Intersection i4 = new SignalisedIntersection(20, 52, 25, 3);

        intersections.add(i1);
        intersections.add(i2);
        intersections.add(i3);
        intersections.add(i4);

        for (Intersection i : intersections)
        {
            actionHandler.addIntersection(i);
        }

        double defaultSpeedMps = UnitConverter.mphToMps(35);
        actionHandler.addRoad(i1, i2, defaultSpeedMps);
        actionHandler.addRoad(i2, i3, defaultSpeedMps);
        actionHandler.addRoad(i3, i4, defaultSpeedMps);
        actionHandler.addRoad(i4, i1, defaultSpeedMps);

        for (Intersection i : intersections)
        {
            double maxSpeedMps = UnitConverter.mphToMps(30);
            double accelMps2 = UnitConverter.MPH_60_IN_MPS / 15.0;
            Car carToAdd = new Car(roadNetwork, maxSpeedMps, accelMps2);
            actionHandler.addCar(carToAdd, i);
        }
    }
}
