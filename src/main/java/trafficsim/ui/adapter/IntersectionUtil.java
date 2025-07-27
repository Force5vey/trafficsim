package trafficsim.ui.adapter;

public final class IntersectionUtil
{
    public static final double PX_PER_M = 10;

    public static final double LANE_OFFSET_PX = 8.0;

    public static double toPx(double meters)
    {
        return meters * PX_PER_M;
    }

    private IntersectionUtil()
    {
    }

}
