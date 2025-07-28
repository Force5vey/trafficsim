package trafficsim.core.events;

import trafficsim.core.model.Car;
import trafficsim.core.model.Intersection;
import java.util.Objects;

public final class AddCarEvent implements ModelCommandEvent
{
    private final Car car;
    private final Intersection spawnPoint;

    public AddCarEvent(Car car, Intersection spawnPoint)
    {
        this.car = car;
        this.spawnPoint = spawnPoint;
    }

    public Car getCar()
    {
        return car;
    }

    public Intersection getSpawnPoint()
    {
        return spawnPoint;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        AddCarEvent that = (AddCarEvent) o;
        return Objects.equals(car, that.car) && Objects.equals(spawnPoint, that.spawnPoint);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(car, spawnPoint);
    }
}
