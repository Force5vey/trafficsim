package trafficsim.ui.adapter;

import trafficsim.core.model.Vec2;

public final class IntersectionUtil
{
    public static final double PX_PER_M = 10;

    public static double toPx(double meters)
    {
        return meters * PX_PER_M;
    }

    private IntersectionUtil()
    {
    }

}
