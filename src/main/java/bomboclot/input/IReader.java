package bomboclot.input;

import java.util.List;

/**
 * Source of all input data for the optimiser. Implementations may read from Excel,
 * CSV, a database, or in-memory test fixtures — the rest of the program depends only
 * on this interface, so a future swap of the data source touches no caller.
 */
public interface IReader
{
    /**
     * Loads all data from the underlying source into memory. Must be called once
     * before any of the get_* methods.
     */
    void load();

    /**
     * Returns the product master record by name, or null if the product is unknown.
     */
    Product get_product(String name);

    /**
     * Returns the customer master record by identifier, or null if unknown.
     * Identifiers are 10-digit strings (e.g. "9100627695") that exceed the range
     * of int — always pass them as String.
     */
    Costumer get_costumer(String identifier);

    /**
     * Returns all deliveries for a given (date, route_code) pair in the source data.
     * Each Delivery groups all the product lines of a single Entrega so that the
     * caller iterates over client stops rather than product lines. Returns an empty
     * list if no rows match.
     */
    List<Delivery> get_deliveries(String date_dd_mm_yyyy, String route_code);

    /**
     * Returns the time window for a customer on a given weekday (1 = Monday,
     * 5 = Friday). Returns null when the schedule says the client is closed that day,
     * and TimeWindow.anyTime() when no schedule is on file at all.
     */
    TimeWindow get_window(String customer_identifier, int weekday);
}
