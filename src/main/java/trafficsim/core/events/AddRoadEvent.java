package trafficsim.core.events;

import trafficsim.core.model.Road;
import java.util.Objects;

public final class AddRoadEvent implements ModelCommandEvent
{
    private final Road road;

    public AddRoadEvent(Road road)
    {
        this.road = road;
    }

    public Road getRoad()
    {
        return road;
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

        AddRoadEvent that = (AddRoadEvent) o;
        return Objects.equals(road, that.road);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(road);
    }
}
