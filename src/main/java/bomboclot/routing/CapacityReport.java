package bomboclot.routing;

import java.util.List;

/**
 * Outcome of CapacityChecker.check(). When `fits` is true, `overflowStopIds` is empty
 * and the trip is good to go. When `fits` is false, `overflowStopIds` names the
 * deliveries that should move to the next trip; the routing team then re-routes the
 * remainder and recurses on the overflow until every client is assigned.
 */
public record CapacityReport(
        boolean fits,
        double usedKilograms,
        double maxKilograms,
        double usedLitres,
        double maxLitres,
        List<String> overflowStopIds,
        String message
)
{
    /**
     * Free weight capacity left in the truck after the proposed load.
     */
    public double remainingKilograms()
    {
        return maxKilograms - usedKilograms;
    }

    /**
     * Free volume capacity left in the truck after the proposed load.
     */
    public double remainingLitres()
    {
        return maxLitres - usedLitres;
    }

    /**
     * Fraction of the truck's weight capacity used, between 0 and 1.
     */
    public double weightUtilisation()
    {
        return usedKilograms / maxKilograms;
    }
}
