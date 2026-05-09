package bomboclot.input;

/**
 * One product line within a delivery. The unit is one of CAJ (case), BRL (barrel),
 * UN (unit), BOT (bottle), and similar codes from the source data. Used by Team B
 * to compute load weight; the routing team does not look inside these.
 */
public record DeliveryLine(String product_name, String unit, double quantity) {}
