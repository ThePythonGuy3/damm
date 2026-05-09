package bomboclot.routing;

/**
 * Strategy for computing drive time between two stops. The solver depends only on
 * this interface, so a future implementation backed by a real routing API (OSRM,
 * Google Maps, etc.) can be plugged in without touching the algorithm.
 */
public interface DistanceProvider
{
    /**
     * Returns drive time in minutes from `from` to `to`. Always non-negative.
     */
    int driveMinutes(Stop from, Stop to);
}
