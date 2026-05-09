package bomboclot.routing;

import bomboclot.geocoding.Coordinates;
import bomboclot.geocoding.Geocoder;
import bomboclot.input.Address;
import bomboclot.input.Costumer;
import bomboclot.input.Delivery;
import bomboclot.input.IReader;
import bomboclot.input.TimeWindow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Bridges the input layer (Costumer, Delivery, TimeWindow, Address) with the
 * routing layer (Stop). Performs the customer and time-window lookups and the
 * geocoding call required to materialise routing-ready Stops. This is the only
 * place in the codebase that depends on both the IReader and the Geocoder.
 *
 * Failure modes — missing customer record, missing window, address that fails
 * geocoding — are logged and the affected client is skipped rather than aborting
 * the run. Better to demo a partial route than crash on one bad address.
 */
public final class StopAssembler
{
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final IReader reader;
    private final Geocoder geocoder;

    public StopAssembler(IReader reader, Geocoder geocoder)
    {
        this.reader = reader;
        this.geocoder = geocoder;
    }

    /**
     * Builds the list of Stops for a given (date, route) pair. Clients whose window
     * indicates "closed that weekday" are skipped, as are clients whose customer
     * record or address cannot be resolved — both situations are logged.
     */
    public List<Stop> assembleForRoute(String date, String routeCode)
    {
        int weekday = LocalDate.parse(date, DATE_FORMAT).getDayOfWeek().getValue();
        List<Delivery> deliveries = reader.get_deliveries(date, routeCode);
        List<Stop> stops = new ArrayList<>(deliveries.size());

        for (Delivery delivery : deliveries)
        {
            Costumer customer = reader.get_costumer(delivery.customer_identifier());
            if (customer == null)
            {
                warn("no Costumer for " + delivery.customer_identifier());
                continue;
            }

            TimeWindow window = reader.get_window(delivery.customer_identifier(), weekday);
            if (window == null)
            {
                // null window means the schedule says closed on that weekday — skip.
                continue;
            }

            String addressLine = formatAddress(customer.address());
            try
            {
                Coordinates coords = geocoder.geocode(addressLine);
                stops.add(new Stop(
                        delivery.entrega(),
                        customer.identifier(),
                        customer.name(),
                        addressLine,
                        coords.latitude(),
                        coords.longitude(),
                        window));
            }
            catch (Exception ex)
            {
                warn("geocoding failed for " + customer.identifier() + ": " + ex.getMessage());
            }
        }
        return stops;
    }

    /**
     * Formats an Address into a Nominatim-friendly single-line query string with
     * the country suffix that improves match accuracy for Spanish addresses.
     */
    private static String formatAddress(Address a)
    {
        return a.street() + ", " + a.postal() + " " + a.city() + ", Spain";
    }

    private static void warn(String message)
    {
        System.err.println("WARN " + message);
    }
}
