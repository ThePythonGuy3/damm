package bomboclot.routing;

import java.util.ArrayList;
import java.util.List;

/**
 * Solves Travelling Salesman with Time Windows for a single truck-day.
 *
 * Two phases are applied in sequence. The first builds a feasible tour by
 * nearest-neighbour from the depot, filtering out any candidate whose window has
 * already closed by the time the truck would arrive and tie-breaking on
 * drive_time + waiting_time so the solver does not get stuck idling. The second
 * phase runs 2-opt local search: every pair of edges is considered for reversal,
 * and a swap is accepted only if it lowers total drive time AND keeps every
 * arrival within its window.
 *
 * The tour returned by both solve() and improve() always has the depot at index 0
 * and at the last index.
 */
public final class RouteSolver
{
    private final DistanceProvider distance;
    private final ServiceTimeProvider serviceTime;
    private final int dayStartMinutes;

    public RouteSolver(DistanceProvider distance, ServiceTimeProvider serviceTime, int dayStartMinutes)
    {
        this.distance = distance;
        this.serviceTime = serviceTime;
        this.dayStartMinutes = dayStartMinutes;
    }

    /**
     * Builds a feasible tour from the depot through all clients and back to the
     * depot, using nearest-neighbour with hard window filtering. If no candidate
     * fits remaining windows, the leftovers are appended so the validator can flag
     * them rather than dropping work silently.
     */
    public List<Stop> solve(Stop depot, List<Stop> clients)
    {
        List<Stop> remaining = new ArrayList<>(clients);
        List<Stop> ordered = new ArrayList<>(clients.size() + 2);
        ordered.add(depot);

        Stop current = depot;
        int clock = dayStartMinutes;

        while (!remaining.isEmpty())
        {
            Stop next = pickNearestFeasible(current, clock, remaining);
            if (next == null)
            {
                ordered.addAll(remaining);
                break;
            }
            int driveMin = distance.driveMinutes(current, next);
            int arrival = clock + driveMin;
            int waited = next.window().wait_minutes(arrival);
            ordered.add(next);
            clock = arrival + waited + serviceTime.minutesFor(next);
            current = next;
            remaining.remove(next);
        }
        ordered.add(depot);
        return ordered;
    }

    /**
     * From the candidates whose window is still open, returns the one with the
     * smallest drive_time + waiting_time. Returns null when no candidate fits.
     */
    private Stop pickNearestFeasible(Stop from, int clockNow, List<Stop> candidates)
    {
        Stop best = null;
        int bestCost = Integer.MAX_VALUE;
        for (Stop c : candidates)
        {
            int driveMin = distance.driveMinutes(from, c);
            int arrival = clockNow + driveMin;
            if (!c.window().accepts_arrival(arrival)) continue;
            int waitMin = c.window().wait_minutes(arrival);
            int cost = driveMin + waitMin;
            if (cost < bestCost)
            {
                bestCost = cost;
                best = c;
            }
        }
        return best;
    }

    /**
     * Iteratively reverses pairs of edges in the tour. Accepts a swap when it
     * reduces total drive time and keeps every arrival within its window. Iterates
     * until a full pass produces no improvement.
     */
    public List<Stop> improve(List<Stop> tour)
    {
        List<Stop> best = new ArrayList<>(tour);
        int bestCost = totalDriveMinutes(best);
        boolean improved = true;

        while (improved)
        {
            improved = false;
            for (int i = 1; i < best.size() - 2; i++)
            {
                for (int j = i + 1; j < best.size() - 1; j++)
                {
                    List<Stop> candidate = twoOptSwap(best, i, j);
                    int candidateCost = totalDriveMinutes(candidate);
                    if (candidateCost < bestCost && allWindowsRespected(candidate))
                    {
                        best = candidate;
                        bestCost = candidateCost;
                        improved = true;
                    }
                }
            }
        }
        return best;
    }

    /**
     * Returns a copy of the tour with the segment [i..j] reversed.
     */
    private List<Stop> twoOptSwap(List<Stop> tour, int i, int j)
    {
        List<Stop> out = new ArrayList<>(tour.size());
        out.addAll(tour.subList(0, i));
        for (int k = j; k >= i; k--) out.add(tour.get(k));
        out.addAll(tour.subList(j + 1, tour.size()));
        return out;
    }

    /**
     * Sum of drive time over consecutive pairs in the tour. Service and waiting
     * times are excluded — they are simulated separately when needed.
     */
    private int totalDriveMinutes(List<Stop> tour)
    {
        int sum = 0;
        for (int i = 0; i < tour.size() - 1; i++)
            sum += distance.driveMinutes(tour.get(i), tour.get(i + 1));
        return sum;
    }

    /**
     * Walks the tour with the clock and confirms every arrival fits its window.
     */
    private boolean allWindowsRespected(List<Stop> tour)
    {
        int clock = dayStartMinutes;
        for (int i = 1; i < tour.size(); i++)
        {
            Stop prev = tour.get(i - 1);
            Stop curr = tour.get(i);
            clock += distance.driveMinutes(prev, curr);
            if (!curr.window().accepts_arrival(clock)) return false;
            clock += curr.window().wait_minutes(clock) + serviceTime.minutesFor(curr);
        }
        return true;
    }
}
