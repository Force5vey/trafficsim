/***************************************************************

- File:        UnitConverter.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Utility class for converting between MPH and MPS units.

- Description:
- Provides static methods for converting speeds between miles per hour
- and meters per second, as well as constants for common conversions.

***************************************************************/

package trafficsim.ui.adapter;

public class UnitConverter
{
    /**
    * Conversion factor from miles per hour to meters per second.
    * 1 mph = 0.44704 m/s
    */
    private static final double MPS_PER_MPH = 0.44704;

    /**
    * The speed of 60 mph expressed in meters per second.
    * Used for acceleration/time-to-speed calculations.
    * MPH_60_IN_MPS = 60 * MPS_PER_MPH
    */
    public static final double MPH_60_IN_MPS = 60.0 * MPS_PER_MPH;

    private UnitConverter()
    {
        // prevent instantiation
    }

    /**
    * Converts a speed from miles per hour to meters per second.
    *
    * @param mph Speed in miles per hour.
    * @return    Speed in meters per second.
    */
    public static double mphToMps(double mph)
    {
        return mph * MPS_PER_MPH;
    }

    /**
    * Converts a speed from meters per second to miles per hour.
    *
    * @param mps Speed in meters per second.
    * @return    Speed in miles per hour.
    */
    public static double mpsToMph(double mps)
    {
        return mps / MPS_PER_MPH;
    }
}
