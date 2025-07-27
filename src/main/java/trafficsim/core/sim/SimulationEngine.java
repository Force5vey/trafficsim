package trafficsim.core.sim;

import trafficsim.core.model.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public final class SimulationEngine
{
    // timing
    private static final long TICK_MS = 16; // ~ 60 fps 
    private final AtomicLong simTimeMillis = new AtomicLong(0);

    // threads
    private ScheduledExecutorService exec;

    // world
    private final List<Updatable> updatables = new CopyOnWriteArrayList<>();
    private final RoadNetwork roadNet = new RoadNetwork();

    public void start()
    {
        if (exec == null || exec.isShutdown())
        {
            exec = Executors.newSingleThreadScheduledExecutor();
            exec.scheduleAtFixedRate(this::step, 0L, TICK_MS, TimeUnit.MILLISECONDS);
        }
    }

    public void pause()
    {
        if (exec != null && !exec.isShutdown())
            exec.shutdown();
    }

    public void stop()
    {
        pause();
        simTimeMillis.set(0);

        for (Updatable u : updatables)
        {
            if (u instanceof Car)
            {
                ((Car) u).resetToInitialState();
            }
        }
    }

    public void addVehicle(Car car)
    {
        updatables.add(car);
    }

    public void addIntersection(Intersection intersection)
    {
        updatables.add(intersection);
    }

    public List<Road> removeIntersection(Intersection intersection)
    {
        List<Road> roadsToRemove = roadNet.findAllConnectedRoads(intersection);
        for (Road road : roadsToRemove)
        {
            removeRoad(road);
        }
        updatables.remove(intersection);
        roadNet.removeIntersection(intersection);

        return roadsToRemove;
    }

    public void clearAll()
    {
        pause();
        updatables.clear();
        roadNet.clear();
        simTimeMillis.set(0);
    }

    public RoadNetwork roadNetwork()
    {
        return roadNet;
    }

    public void addRoad(Road road)
    {
        updatables.add(road);
        roadNet.add(road);
    }

    public void removeRoad(Road road)
    {
        updatables.remove(road);
        roadNet.removeRoad(road);
    }

    public void addVehicle(Car car, Road spawnRoad, double spawnOffsetMeters)
    {
        car.attachTo(spawnRoad, spawnOffsetMeters);
        updatables.add(car);
    }

    public void removeVehicle(Car car)
    {
        updatables.remove(car);
    }

    private void step()
    {
        double deltaTime = TICK_MS / 1000.0;
        simTimeMillis.addAndGet(TICK_MS);
        for (Updatable u : updatables)
        {
            u.update(deltaTime);
        }
    }

    public double simulationTimeSeconds()
    {
        return simTimeMillis.get() / 1000.0;
    }
}
