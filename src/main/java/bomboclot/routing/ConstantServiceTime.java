package bomboclot.routing;

import bomboclot.Constants;

/**
 * Returns a fixed service time for every non-depot stop, drawn from
 * Constants.DEFAULT_SERVICE_MINUTES. The depot contributes zero so the truck does
 * not "unload" anything at the start or end of the day.
 */
public final class ConstantServiceTime implements ServiceTimeProvider
{
    @Override
    public int minutesFor(Stop stop)
    {
        if (stop.isDepot()) return 0;
        return Constants.DEFAULT_SERVICE_MINUTES;
    }
}
