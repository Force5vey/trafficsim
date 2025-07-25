package trafficsim.ui.adapter;

public class UnitConverter
{
    private static final double MPS_PER_MPH = 0.44704;

    public static final double MPH_60_IN_MPS = 60.0 * MPS_PER_MPH;

    private UnitConverter()
    {
        // prevent instantiation
    }

    public static double mphToMps(double mph)
    {
        return mph * MPS_PER_MPH;
    }

    public static double mpsToMph(double mps)
    {
        return mps / MPS_PER_MPH;
    }
}
