package bomboclot.input;

/**
 * Customer master record. Used by routing for the name and address, and by Team B
 * for delivery-side calculations. Promoted from a plain class to a record because
 * it is a four-field immutable value with no behaviour beyond access.
 */
public record Costumer(String identifier, String name, String name2, Address address) {}
