package bomboclot.routing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Combines RouteSolver with CapacityChecker to produce one or more Trips that
 * together cover the day's clients. The recursion is shallow in practice — one or
 * two splits at most — and is guarded against infinite loops by detecting overflow
 * lists that don't shrink.
 */
public final class TripPlanner
{
    /**
     * The planning unit returned to callers: an ordered tour with the capacity
     * report that validated it.
     */
    public record Trip(List<Stop> orderedStops, CapacityReport report) {}

    private final RouteSolver solver;
    private final CapacityChecker capacityChecker;

    public TripPlanner(RouteSolver solver, CapacityChecker capacityChecker)
    {
        this.solver = solver;
        this.capacityChecker = capacityChecker;
    }

    /**
     * Plans one or more trips that together cover all the given clients. If the
     * full client list fits a single trip, returns one Trip; otherwise splits the
     * overflow returned by CapacityChecker into a separate trip and recurses until
     * every client is assigned. Throws when the checker reports overflow but
     * cannot identify which stops to drop.
     */
    public List<Trip> plan(Stop depot, List<Stop> clients, String date, String routeCode)
    {
        if (clients.isEmpty()) return List.of();

        List<Stop> tour = solver.improve(solver.solve(depot, clients));

        List<String> stopIds = entregaIdsExcludingDepot(tour);
        CapacityReport report = capacityChecker.check(date, routeCode, stopIds);

        if (report.fits())
            return List.of(new Trip(tour, report));

        Set<String> overflow = new HashSet<>(report.overflowStopIds());
        if (overflow.isEmpty() || overflow.size() == clients.size())
            throw new IllegalStateException(
                    "CapacityChecker reported overflow but couldn't shrink the trip: " + report);

        List<Stop> primary = clients.stream()
                .filter(s -> !overflow.contains(s.entrega()))
                .toList();
        List<Stop> overflowStops = clients.stream()
                .filter(s -> overflow.contains(s.entrega()))
                .toList();

        List<Trip> trips = new ArrayList<>();
        trips.addAll(plan(depot, primary, date, routeCode));
        trips.addAll(plan(depot, overflowStops, date, routeCode));
        return trips;
    }

    /**
     * Extracts Entrega ids from the tour, omitting the depot endpoints.
     */
    private List<String> entregaIdsExcludingDepot(List<Stop> tour)
    {
        List<String> ids = new ArrayList<>(tour.size());
        for (Stop s : tour)
            if (!s.isDepot()) ids.add(s.entrega());
        return ids;
    }
}
