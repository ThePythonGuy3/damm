package bomboclot.routing;

import java.util.List;

/**
 * Strategy for validating whether a list of stops fits a single truck-trip.
 * Implemented by Team B; the routing team only consumes the report.
 */
public interface CapacityChecker
{
    /**
     * Validates the proposed stops against the truck assigned to (date, route_code).
     * Returns a CapacityReport with the verdict and utilisation; if the load does not
     * fit, the report identifies which stops should move to another trip.
     */
    CapacityReport check(String date, String route_code, List<String> stop_order);
}
