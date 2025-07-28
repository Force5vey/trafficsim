package trafficsim.core.sim;

import trafficsim.core.model.*;
import trafficsim.core.events.*;

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
    private final BlockingQueue<SimulationEvent> eventQueue = new LinkedBlockingQueue<>();
    private volatile boolean isRunning = false;

    // world
    private final List<Updatable> updatables = new CopyOnWriteArrayList<>();
    private final RoadNetwork roadNet = new RoadNetwork();

    public SimulationEngine()
    {
        this.exec = Executors.newSingleThreadScheduledExecutor();
        this.exec.scheduleAtFixedRate(this::step, 0L, TICK_MS, TimeUnit.MILLISECONDS);
    }

    /**
     *   method for threads to post to engines queue
    **/
    public void postEvent(SimulationEvent event)
    {
        eventQueue.add(event);
    }

    private void start()
    {
        isRunning = true;
    }

    private void pause()
    {
        isRunning = false;
    }

    private void stop()
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

    private void clearAll()
    {
        pause();
        updatables.clear();
        roadNet.clear();
        simTimeMillis.set(0);
    }

    private void addVehicle(Car car, Road spawnRoad, double spawnOffsetMeters)
    {
        car.attachTo(spawnRoad, spawnOffsetMeters);
        updatables.add(car);
    }

    private void step()
    {
        processEventQueue();
        if (!isRunning)
        {
            return; // no update on pause
        }

        double deltaTime = TICK_MS / 1000.0;
        simTimeMillis.addAndGet(TICK_MS);
        for (Updatable u : updatables)
        {
            u.update(deltaTime);
        }
    }

    private void processEventQueue()
    {
        SimulationEvent event;
        while ((event = eventQueue.poll()) != null)
        {
            if (event instanceof EngineControlEvent)
            {
                handleEngineControl((EngineControlEvent) event);
            } else if (event instanceof ModelCommandEvent)
            {
                handleModelCommand((ModelCommandEvent) event);
            }
        }
    }

    private void handleEngineControl(EngineControlEvent event)
    {
        switch (event.getType()) {
        case START:
            start();
            break;
        case PAUSE:
            pause();
            break;
        case STOP:
            stop();
            break;
        }
    }

    private void handleModelCommand(ModelCommandEvent event)
    {
        if (event instanceof AddIntersectionEvent)
        {
            updatables.add(((AddIntersectionEvent) event).getIntersection());
        } else if (event instanceof AddRoadEvent)
        {
            Road road = ((AddRoadEvent) event).getRoad();
            updatables.add(road);
            roadNet.add(road);

        } else if (event instanceof AddCarEvent)
        {
            AddCarEvent cmd = (AddCarEvent) event;
            List<Road> outs = roadNet.outgoing(cmd.getSpawnPoint());
            if (!outs.isEmpty())
            {
                Road spawnRoad = outs.get(0);
                addVehicle(cmd.getCar(), spawnRoad, 0.0);
            }
        } else if (event instanceof DeleteItemEvent)
        {
            Object item = ((DeleteItemEvent) event).getItemToDelete();
            if (item instanceof Intersection)
            {
                Intersection i = (Intersection) item;
                List<Road> roadsToRemove = roadNet.findAllConnectedRoads(i);
                for (Road road : roadsToRemove)
                {
                    updatables.remove(road);
                    roadNet.removeRoad(road);
                }
                updatables.remove(i);
                roadNet.removeIntersection(i);
            } else if (item instanceof Road)
            {
                updatables.remove(item);
                roadNet.removeRoad((Road) item);
            } else if (item instanceof Car)
            {
                updatables.remove(item);
            }
        } else if (event instanceof ClearAllEvent)
        {
            clearAll();
        }
    }

    public void shutdown()
    {
        if (exec != null)
        {
            exec.shutdownNow();
        }
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

    public void removeVehicle(Car car)
    {
        updatables.remove(car);
    }

    public double simulationTimeSeconds()
    {
        return simTimeMillis.get() / 1000.0;
    }
}
