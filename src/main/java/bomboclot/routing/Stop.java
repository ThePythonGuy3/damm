package bomboclot.routing;

import bomboclot.Constants;
import bomboclot.input.TimeWindow;

/**
 * One stop on the planned route — either a real client visit or the depot at the
 * start and end of the day. Records are immutable, which makes the solver easier
 * to reason about: no class can mutate a Stop while another class reads it.
 */
public record Stop(
        String entrega,
        String clientId,
        String clientName,
        String address,
        double latitude,
        double longitude,
        TimeWindow window
)
{
    /**
     * Sentinel client identifier used for the depot stop. Real customer ids are
     * 10-digit numbers, so this string can never collide.
     */
    public static final String DEPOT_ID = "DEPOT";

    /**
     * Builds the depot stop with coordinates and name from Constants. The depot
     * has no time-window restriction.
     */
    public static Stop depot()
    {
        return new Stop(DEPOT_ID, DEPOT_ID, Constants.DEPOT_NAME, Constants.DEPOT_NAME,
                        Constants.DEPOT_LATITUDE, Constants.DEPOT_LONGITUDE,
                        TimeWindow.anyTime());
    }

    /**
     * Whether this stop is the depot rather than a real client.
     */
    public boolean isDepot()
    {
        return DEPOT_ID.equals(clientId);
    }
}
