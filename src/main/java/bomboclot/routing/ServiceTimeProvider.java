package bomboclot.routing;

/**
 * Strategy for how long unloading takes at a given stop. Today's only implementation
 * returns a constant; a future one could read the number of cases or barrels at the
 * stop and return a longer time for heavy stops, without any solver changes.
 */
public interface ServiceTimeProvider
{
    /**
     * Returns the unloading time at this stop in minutes. Returns zero for the depot —
     * we do not unload anything there mid-route.
     */
    int minutesFor(Stop stop);
}
