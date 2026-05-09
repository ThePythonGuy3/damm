package bomboclot.geocoding;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Decorator that wraps another Geocoder with a JSON-file cache. The first run pays
 * the network cost for each new address; subsequent runs read from disk in
 * milliseconds. Cache writes are best-effort — a save failure does not propagate,
 * since the next run will simply re-geocode missing entries.
 */
public final class CachingGeocoder implements Geocoder
{
    private static final ObjectMapper JSON = new ObjectMapper();

    private final Geocoder delegate;
    private final File cacheFile;
    private final Map<String, Coordinates> cache;

    public CachingGeocoder(Geocoder delegate, File cacheFile)
    {
        this.delegate = delegate;
        this.cacheFile = cacheFile;
        this.cache = loadOrEmpty();
    }

    @Override
    public Coordinates geocode(String address)
    {
        Coordinates hit = cache.get(address);
        if (hit != null) return hit;
        Coordinates fresh = delegate.geocode(address);
        cache.put(address, fresh);
        save();
        return fresh;
    }

    /**
     * Loads the on-disk cache into memory, or returns an empty map if the file does
     * not exist or cannot be parsed.
     */
    private Map<String, Coordinates> loadOrEmpty()
    {
        if (!cacheFile.exists()) return new HashMap<>();
        try
        {
            return JSON.readValue(cacheFile, new TypeReference<>() {});
        }
        catch (Exception e)
        {
            return new HashMap<>();
        }
    }

    /**
     * Persists the in-memory cache to disk. Best-effort; failures are ignored.
     */
    private void save()
    {
        try { JSON.writerWithDefaultPrettyPrinter().writeValue(cacheFile, cache); }
        catch (Exception ignored) {}
    }
}
