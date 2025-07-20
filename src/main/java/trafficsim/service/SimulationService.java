package trafficsim.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

public class SimulationService
{
    private ScheduledExecutorService executor;
    private final long tickRateMillis = 16; // ~60fps

    private final LongProperty simulationTime = new SimpleLongProperty(0);
    private final AtomicLong internalTimeMillis = new AtomicLong(0);

    public void start()
    {
        if (executor == null || executor.isShutdown())
        {
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(this::simulationStep, 0L, tickRateMillis, TimeUnit.MILLISECONDS);
        }
    }

    public void pause()
    {
        if (executor != null && !executor.isShutdown())
        {
            executor.shutdown();
        }
    }

    public void stop()
    {
        pause();
        internalTimeMillis.set(0);
        Platform.runLater(() -> simulationTime.set(0));
        // further reset will be needed
    }

    private void simulationStep()
    {
        long currentTime = internalTimeMillis.addAndGet(tickRateMillis);
        double deltaTime = tickRateMillis / 1000.0; // deltaT in seconds

        if (currentTime / 1000 != simulationTime.get())
        {
            Platform.runLater(() -> simulationTime.set(currentTime / 1000));
        }

        // Update all model objects (cars, intersections)
    }

    public LongProperty simulationTimeProperty()
    {
        return simulationTime;
    }
}
