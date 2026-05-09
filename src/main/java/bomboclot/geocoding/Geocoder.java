package bomboclot.geocoding;

/**
 * Strategy for converting a human-readable address into geographic coordinates.
 * Implementations may call out to an external service or read from a cache; callers
 * depend only on this interface so the underlying provider can change without
 * touching the routing pipeline.
 */
public interface Geocoder
{
    /**
     * Returns the coordinates of the given address. Throws if the address cannot be
     * resolved — callers typically log and skip rather than abort the run.
     */
    Coordinates geocode(String address);
}
