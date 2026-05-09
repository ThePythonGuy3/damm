package bomboclot.output;

import bomboclot.input.TimeWindow;
import bomboclot.routing.CapacityReport;
import bomboclot.routing.DistanceProvider;
import bomboclot.routing.ServiceTimeProvider;
import bomboclot.routing.Stop;
import bomboclot.routing.TripPlanner.Trip;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.List;

/**
 * Writes the route plan to disk as JSON for downstream consumption by Team B and
 * any future visualisation. The walker simulates the clock to compute arrival and
 * departure timestamps that already include service and waiting time, so consumers
 * read times directly without re-simulating the schedule.
 */
public final class RouteJsonWriter
{
    private static final ObjectMapper JSON = new ObjectMapper();

    private final DistanceProvider distance;
    private final ServiceTimeProvider serviceTime;
    private final int dayStartMinutes;

    public RouteJsonWriter(DistanceProvider distance, ServiceTimeProvider serviceTime, int dayStartMinutes)
    {
        this.distance = distance;
        this.serviceTime = serviceTime;
        this.dayStartMinutes = dayStartMinutes;
    }

    /**
     * Writes the multi-trip route plan to the given path as pretty-printed JSON.
     */
    public void write(List<Trip> trips, String date, String routeCode, String outputPath) throws Exception
    {
        ObjectNode root = JSON.createObjectNode();
        root.put("date", date);
        root.put("route", routeCode);
        ArrayNode tripsArr = root.putArray("trips");

        int idx = 1;
        for (Trip trip : trips)
            tripsArr.add(serializeTrip(idx++, trip));

        JSON.writerWithDefaultPrettyPrinter().writeValue(new File(outputPath), root);
    }

    /**
     * Serialises one trip including the full per-stop schedule with arrival and
     * departure timestamps computed by walking the tour.
     */
    private ObjectNode serializeTrip(int tripIndex, Trip trip)
    {
        ObjectNode tripNode = JSON.createObjectNode();
        tripNode.put("tripIndex", tripIndex);

        List<Stop> tour = trip.orderedStops();
        int clock = dayStartMinutes;
        int totalDrive = 0;
        ArrayNode stopsArr = tripNode.putArray("stops");

        tripNode.put("depotDeparture", formatTime(clock));
        int order = 1;

        for (int i = 1; i < tour.size(); i++)
        {
            Stop prev = tour.get(i - 1);
            Stop curr = tour.get(i);
            int driveMin = distance.driveMinutes(prev, curr);
            totalDrive += driveMin;
            int arrival = clock + driveMin;
            int wait = curr.window().wait_minutes(arrival);
            int departure = arrival + wait + serviceTime.minutesFor(curr);

            if (!curr.isDepot())
                stopsArr.add(serializeStop(order++, curr, arrival, departure));

            clock = departure;
        }

        tripNode.put("depotReturn", formatTime(clock));
        tripNode.put("totalDriveMinutes", totalDrive);
        tripNode.set("capacity", serializeCapacity(trip.report()));
        return tripNode;
    }

    /**
     * Serialises one stop with its arrival, departure, and time window expressed
     * as HH:mm strings for human readability.
     */
    private ObjectNode serializeStop(int order, Stop stop, int arrival, int departure)
    {
        ObjectNode node = JSON.createObjectNode();
        node.put("order", order);
        node.put("entrega", stop.entrega());
        node.put("clientId", stop.clientId());
        node.put("clientName", stop.clientName());
        node.put("address", stop.address());
        node.put("latitude", stop.latitude());
        node.put("longitude", stop.longitude());
        TimeWindow w = stop.window();
        node.put("windowStart", formatTime(w.open_minutes()));
        node.put("windowEnd", formatTime(w.close_minutes()));
        node.put("arrival", formatTime(arrival));
        node.put("departure", formatTime(departure));
        return node;
    }

    /**
     * Serialises the capacity utilisation block reported by Team B.
     */
    private ObjectNode serializeCapacity(CapacityReport r)
    {
        ObjectNode node = JSON.createObjectNode();
        node.put("usedKilograms", r.usedKilograms());
        node.put("maxKilograms", r.maxKilograms());
        node.put("usedLitres", r.usedLitres());
        node.put("maxLitres", r.maxLitres());
        return node;
    }

    /**
     * Renders minutes-since-midnight as HH:mm.
     */
    private static String formatTime(int minutes)
    {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
