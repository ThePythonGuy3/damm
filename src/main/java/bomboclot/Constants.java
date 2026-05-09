package bomboclot;

/**
 * Centralised tunable defaults for the route optimiser. Anything that's "a number we
 * picked because it seemed reasonable" lives here so that a single line change
 * propagates everywhere. Mentor corrections to assumptions land in this file alone.
 */
public final class Constants
{
    /**
     * Latitude of the DDI Mollet del Vallès depot. Within roughly 200 m of the
     * warehouse entrance — exact coordinates pending mentor confirmation.
     */
    public static final double DEPOT_LATITUDE = 41.5424;

    /**
     * Longitude of the DDI Mollet del Vallès depot.
     */
    public static final double DEPOT_LONGITUDE = 2.2134;

    /**
     * Human-readable depot name used in reports and the JSON output.
     */
    public static final String DEPOT_NAME = "DDI Mollet del Vallès";

    /**
     * When the driver leaves the depot, expressed as minutes since midnight.
     * 8 * 60 = 08:00. Pending confirmation with the mentor.
     */
    public static final int DAY_START_MINUTES = 8 * 60;

    /**
     * Default unloading time in minutes when no per-stop information is available.
     * Used by ConstantServiceTime; a future provider could vary it per stop.
     */
    public static final int DEFAULT_SERVICE_MINUTES = 10;

    /**
     * Multiplier applied to straight-line distance to approximate actual road
     * distance. 1.4 is a well-established empirical value for European driving.
     */
    public static final double ROAD_DETOUR_FACTOR = 1.4;

    /**
     * Mean cruising speed across mixed urban and secondary roads in Catalonia.
     */
    public static final double AVERAGE_SPEED_KMH = 40.0;

    /**
     * Kilometres per degree of latitude. Constant on a flat-earth model.
     */
    public static final double KM_PER_DEGREE_LATITUDE = 111.0;

    /**
     * Kilometres per degree of longitude at Catalonia's latitude (~41.5°N),
     * computed as 111 km × cos(41.5°). Errors over 50 km tours are well under 1%.
     */
    public static final double KM_PER_DEGREE_LONGITUDE = 83.0;

    /**
     * Default date used by Main when no CLI argument is provided. Picked because
     * we have a real Hoja Carga PDF to validate against.
     */
    public static final String DEMO_DATE = "08/05/2026";

    /**
     * Default route used by Main when no CLI argument is provided. Spans three
     * towns (Sant Julià de Vilatorta, Calldetenes, Folgueroles), good for a demo.
     */
    public static final String DEMO_ROUTE = "DR0027";

    /**
     * Disk path of the geocoder cache file.
     */
    public static final String GEOCODE_CACHE_PATH = "geocode-cache.json";

    /**
     * Disk path of the route JSON output file consumed by Team B.
     */
    public static final String OUTPUT_JSON_PATH = "route.json";

    private Constants() {}
}
