package trafficsim.core.events;

import trafficsim.core.model.Intersection;
import java.util.Objects;

public final class AddIntersectionEvent implements ModelCommandEvent
{
    private final Intersection intersection;

    public AddIntersectionEvent(Intersection intersection)
    {
        this.intersection = intersection;
    }

    public Intersection getIntersection()
    {
        return intersection;
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

        AddIntersectionEvent that = (AddIntersectionEvent) o;
        return Objects.equals(intersection, that.intersection);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(intersection);
    }
}
