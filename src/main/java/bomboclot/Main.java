package bomboclot;

import bomboclot.geocoding.CachingGeocoder;
import bomboclot.geocoding.Geocoder;
import bomboclot.geocoding.NominatimGeocoder;
import bomboclot.input.IReader;
import bomboclot.input.XLSXReader;
import bomboclot.output.RouteJsonWriter;
import bomboclot.routing.CapacityChecker;
import bomboclot.routing.CapacityReport;
import bomboclot.routing.ConstantServiceTime;
import bomboclot.routing.DistanceProvider;
import bomboclot.routing.FlatDistance;
import bomboclot.routing.RouteSolver;
import bomboclot.routing.ServiceTimeProvider;
import bomboclot.routing.Stop;
import bomboclot.routing.StopAssembler;
import bomboclot.routing.TripPlanner;

import java.io.File;
import java.util.List;

/**
 * Pipeline orchestrator. Reads the day's deliveries from the data team's IReader,
 * geocodes the customer addresses, plans one or more trips that together cover all
 * stops while respecting time windows and capacity, and writes the result to disk
 * as JSON for Team B and any downstream visualisation tool.
 *
 * Run with no arguments to use the demo defaults from Constants, or pass
 * "&lt;date&gt; &lt;route&gt;" (e.g. "08/05/2026 DR0027") to plan a specific trip.
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {
        String date = args.length > 0 ? args[0] : Constants.DEMO_DATE;
        String route = args.length > 1 ? args[1] : Constants.DEMO_ROUTE;

        IReader reader = new XLSXReader();
        reader.load();

        Geocoder geocoder = new CachingGeocoder(
                new NominatimGeocoder(),
                new File(Constants.GEOCODE_CACHE_PATH));

        List<Stop> clients = new StopAssembler(reader, geocoder).assembleForRoute(date, route);
        if (clients.isEmpty())
        {
            System.out.println("No deliveries for " + date + " on route " + route);
            return;
        }

        Stop depot = Stop.depot();
        DistanceProvider distance = new FlatDistance();
        ServiceTimeProvider serviceTime = new ConstantServiceTime();

        RouteSolver solver = new RouteSolver(distance, serviceTime, Constants.DAY_START_MINUTES);

        // TODO Team B: replace this stub with the real CapacityChecker once available.
        // The stub always reports 'fits', so the pipeline runs end-to-end with no splits.
        // Suggested defaults for a typical DDI distribution truck: 6000 kg, 30000 L.
        CapacityChecker capacity = (d, r, ids) ->
                new CapacityReport(true, 0, 6000, 0, 30000, List.of(), "stub");

        List<TripPlanner.Trip> trips = new TripPlanner(solver, capacity).plan(depot, clients, date, route);

        new RouteJsonWriter(distance, serviceTime, Constants.DAY_START_MINUTES)
                .write(trips, date, route, Constants.OUTPUT_JSON_PATH);

        System.out.println("Wrote " + Constants.OUTPUT_JSON_PATH +
                " with " + trips.size() + " trip(s).");
    }
}
