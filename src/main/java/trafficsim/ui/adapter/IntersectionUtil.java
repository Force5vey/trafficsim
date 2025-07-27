package trafficsim.ui.adapter;

import trafficsim.core.model.Intersection;
import trafficsim.core.model.Road;
import trafficsim.core.model.Vec2;

public final class IntersectionUtil
{
    public static final double PX_PER_M = 10;
    public static final double LANE_OFFSET_PX = 8.0;

    public static double toPx(double meters)
    {
        return meters * PX_PER_M;
    }

    // guard cstr
    private IntersectionUtil()
    {
    }

    public static Vec2 getLaneOffsetVector(Road road)
    {
        Intersection i1 = road.from();
        Intersection i2 = road.to();

        // establishing a canonical direction, then using that for +/- offset for different directions (angles) of roads
        Intersection canonicalStart;
        Intersection canonicalEnd;

        double x1 = i1.position().x;
        double y1 = i1.position().y;
        double x2 = i2.position().x;
        double y2 = i2.position().y;

        if (x1 < x2 || (Math.abs(x1 - x2) < 1e-6 && y1 < y2))
        {
            canonicalStart = i1;
            canonicalEnd = i2;
        } else
        {
            canonicalStart = i2;
            canonicalEnd = i1;
        }

        double cdx = canonicalEnd.position().x - canonicalStart.position().x;
        double cdy = canonicalEnd.position().y - canonicalStart.position().y;
        double length = Math.hypot(cdx, cdy);

        if (length < 1e-6)
        {
            return new Vec2(0, 0);
        }

        double cux = cdx / length;
        double cuy = cdy / length;

        double px = -cuy;
        double py = cux;

        double directionMultiplier = (road.from() == canonicalStart) ? 1.0 : -1.0;

        return new Vec2(px * LANE_OFFSET_PX * directionMultiplier, py * LANE_OFFSET_PX * directionMultiplier);
    }

}
