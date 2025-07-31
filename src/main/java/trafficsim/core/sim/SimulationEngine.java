/***************************************************************

- File:        SimulationEngine.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Core simulation engine for TrafficSim.

- Description:
- Manages the simulation loop, event queue, and all simulation state.
- Handles time progression, model updates, and thread-safe event processing.
- Provides methods for posting events, controlling simulation state, and
- accessing the road network and simulation time.

***************************************************************/

package trafficsim.core.sim;

import trafficsim.core.model.*;
import trafficsim.core.events.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import javafx.application.Platform;

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

    /**
    * Constructs a SimulationEngine and starts the simulation loop.
    * Initializes the event queue, updatables list, and road network.
    */
    public SimulationEngine()
    {
        this.exec = Executors.newSingleThreadScheduledExecutor();
        this.exec.scheduleAtFixedRate(this::step, 0L, TICK_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Posts a simulation event to the engine's event queue for processing.
     * Thread-safe; can be called from any thread.
     *
     * @param event The SimulationEvent to post.
     */
    public void postEvent(SimulationEvent event)
    {
        eventQueue.add(event);
    }

    /**
    * Starts the simulation, allowing updates to proceed.
    */
    private void start()
    {
        isRunning = true;
    }

    /**
    * Pauses the simulation, halting updates.
    */
    private void pause()
    {
        isRunning = false;
    }

    /**
    * Stops the simulation and resets all cars to their initial state.
    * Pauses the simulation and resets the simulation time.
    */
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

    /**
    * Clears all simulation state, removing all updatables and roads.
    * Pauses the simulation and resets the simulation time.
    */
    private void clearAll()
    {
        pause();
        updatables.clear();
        roadNet.clear();
        simTimeMillis.set(0);
    }

    /**
    * Adds a vehicle to the simulation at the specified road and offset.
    *
    * @param car              The Car to add.
    * @param spawnRoad        The Road to attach the car to.
    * @param spawnOffsetMeters The offset along the road in meters.
    */
    private void addVehicle(Car car, Road spawnRoad, double spawnOffsetMeters)
    {
        car.attachTo(spawnRoad, spawnOffsetMeters);
        updatables.add(car);
    }

    /**
    * Advances the simulation by one tick, updating all updatable objects.
    * Called periodically by the simulation loop.
    */
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

    /**
    * Processes all events in the event queue, dispatching them to the appropriate handlers.
    * Handles engine control, model commands, and other simulation events.
    */
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

    /**
    * Handles engine control events (start, pause, stop).
    *
    * @param event The EngineControlEvent to process.
    */
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

    /**
    * Handles model command events, such as adding or removing items from the simulation.
    *
    * @param event The ModelCommandEvent to process.
    */
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

            Car car = cmd.getCar();
            car.setSimulationObjects(this.updatables);

            List<Road> outs = roadNet.outgoing(cmd.getSpawnPoint());
            if (!outs.isEmpty())
            {
                Road spawnRoad = outs.get(0);
                addVehicle(cmd.getCar(), spawnRoad, 0.0);
            }
        } else if (event instanceof DeleteItemEvent)
        {
            DeleteItemEvent cmd = (DeleteItemEvent) event;
            Object item = cmd.getItemToDelete();

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
            Platform.runLater(cmd.getPostDeletionCallback());

        } else if (event instanceof AppliableCommand)
        {
            ((AppliableCommand) event).apply();

        } else if (event instanceof ClearAllEvent)
        {
            clearAll();
        }
    }

    /**
    * Shuts down the simulation engine and stops the simulation loop.
    * Waits for the executor to terminate and interrupts if necessary.
    */
    public void shutdown()
    {
        if (exec != null && !exec.isShutdown())
        {
            exec.shutdown();
            try
            {
                if (!exec.awaitTermination(1, TimeUnit.SECONDS))
                {
                    exec.shutdownNow();
                }
            } catch (InterruptedException e)
            {
                exec.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
    * Returns the simulation's road network.
    *
    * @return The RoadNetwork object managed by the engine.
    */
    public RoadNetwork roadNetwork()
    {
        return roadNet;
    }

    public double simulationTimeSeconds()
    {
        return simTimeMillis.get() / 1000.0;
    }
}
