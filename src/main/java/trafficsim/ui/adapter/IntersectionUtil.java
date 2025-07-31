/***************************************************************

- File:        IntersectionUtil.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Utility functions for intersection and road geometry in the UI.

- Description:
- Provides methods for converting between world and pixel units,
- and for calculating lane offset vectors for road rendering.
- Used to ensure consistent visual placement of roads and intersections.

***************************************************************/

package trafficsim.ui.adapter;

import trafficsim.core.model.Intersection;
import trafficsim.core.model.Road;
import trafficsim.core.model.Vec2;

public final class IntersectionUtil
{
    /**
    * Conversion factor: number of pixels per meter in the UI.
    */
    public static final double PX_PER_M = 10;

    /**
    * Lane offset in pixels for rendering parallel lanes.
    */
    public static final double LANE_OFFSET_PX = 14.0;

    /**
    * Converts a distance in meters to pixels using the UI scale.
    *
    * @param meters Distance in meters.
    * @return       Distance in pixels.
    */
    public static double toPx(double meters)
    {
        return meters * PX_PER_M;
    }

    // guard cstr
    private IntersectionUtil()
    {
    }

    /**
    * Calculates the lane offset vector for a given road, used to visually
    * separate parallel roads in the UI. The offset is perpendicular to the
    * road direction and depends on the canonical direction of the road.
    *
    * @param road The road for which to calculate the offset.
    * @return     A Vec2 representing the offset in pixels.
    */
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
