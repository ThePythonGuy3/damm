package bomboclot.routing;

import bomboclot.Constants;

/**
 * Flat-earth Euclidean distance approximation. For tours under roughly 50 km in
 * Catalonia the curvature error is well below 0.1%, far smaller than the
 * uncertainty in the road detour factor and the assumed average speed. This keeps
 * the math at grade-school level while remaining accurate enough for the use case.
 */
public final class FlatDistance implements DistanceProvider
{
    @Override
    public int driveMinutes(Stop from, Stop to)
    {
        double dxKm = (to.longitude() - from.longitude()) * Constants.KM_PER_DEGREE_LONGITUDE;
        double dyKm = (to.latitude()  - from.latitude())  * Constants.KM_PER_DEGREE_LATITUDE;
        double straightKm = Math.sqrt(dxKm * dxKm + dyKm * dyKm);
        double roadKm = straightKm * Constants.ROAD_DETOUR_FACTOR;
        double minutes = roadKm / Constants.AVERAGE_SPEED_KMH * 60.0;
        return (int) Math.round(minutes);
    }
}
