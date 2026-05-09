package bomboclot.geocoding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Geocoder backed by Nominatim, the OpenStreetMap free geocoding API. The public
 * service requires a unique User-Agent header and a one-request-per-second rate
 * limit; both are honoured by this implementation. For production use, this class
 * should be wrapped in a CachingGeocoder so each address is only resolved once.
 */
public final class NominatimGeocoder implements Geocoder
{
    private static final String ENDPOINT = "https://nominatim.openstreetmap.org/search";
    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpClient http = HttpClient.newHttpClient();
    private long lastRequestMs = 0;

    @Override
    public Coordinates geocode(String address)
    {
        respectRateLimit();
        try
        {
            String url = ENDPOINT + "?format=json&limit=1&q="
                    + URLEncoder.encode(address, StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "damm-hackathon/1.0 (route optimiser)")
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode arr = JSON.readTree(res.body());
            if (arr.isEmpty())
                throw new IllegalStateException("Address not found: " + address);
            JsonNode first = arr.get(0);
            return new Coordinates(
                    Double.parseDouble(first.get("lat").asText()),
                    Double.parseDouble(first.get("lon").asText()));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Geocoding failed for: " + address, e);
        }
    }

    /**
     * Sleeps if needed to keep the request rate at most one per ~1.1 seconds, the
     * published Nominatim usage policy. Hammering the public service can get the
     * client IP blocked.
     */
    private void respectRateLimit()
    {
        long elapsed = System.currentTimeMillis() - lastRequestMs;
        if (elapsed < 1100)
        {
            try { Thread.sleep(1100 - elapsed); }
            catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
        lastRequestMs = System.currentTimeMillis();
    }
}
