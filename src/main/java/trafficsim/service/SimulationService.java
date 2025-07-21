package trafficsim.service;

import trafficsim.model.IIntersection;
import trafficsim.model.Car;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class SimulationService
{
    private ScheduledExecutorService executor;
    private final long tickRateMillis = 16; // ~60fps

    private final LongProperty simulationTime = new SimpleLongProperty(0);
    private final AtomicLong internalTimeMillis = new AtomicLong(0);

    private final List<Car> cars = new ArrayList<>();
    private final ObservableList<IIntersection> intersections = FXCollections.observableArrayList();

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

    public void addCar(Car car)
    {
        // needs updating to be thread safe, for now just assume before simulation starts
        cars.add(car);
    }

    public void addIntersection(IIntersection intersection)
    {
        intersections.add(intersection);
    }

    public ObservableList<IIntersection> getIntersections()
    {
        return intersections;
    }

    private void simulationStep()
    {
        long currentTime = internalTimeMillis.addAndGet(tickRateMillis);
        double deltaTime = tickRateMillis / 1000.0; // deltaT in seconds

        if (currentTime / 1000 != simulationTime.get())
        {
            Platform.runLater(() -> simulationTime.set(currentTime / 1000));
        }

        for (Car car : cars)
        {
            // update logic isn't thread safe, all needs to be updated to be.
            car.update(deltaTime);
        }

        for (IIntersection intersection : new ArrayList<>(intersections))
        {
            intersection.update(deltaTime);
        }

        // Update all model objects (cars, intersections)
    }

    public LongProperty simulationTimeProperty()
    {
        return simulationTime;
    }
}
