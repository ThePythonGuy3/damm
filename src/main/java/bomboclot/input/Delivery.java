package bomboclot.input;

import java.util.List;

/**
 * One delivery document — the unique work for one client stop on one truck-trip.
 * Aggregates all product lines for the same Entrega so that the routing team
 * iterates over client stops rather than individual product lines, while Team B
 * walks the lines to compute total weight and volume.
 */
public record Delivery(String entrega, String customer_identifier, List<DeliveryLine> lines) {}
