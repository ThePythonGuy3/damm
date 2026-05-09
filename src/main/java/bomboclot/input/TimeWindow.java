package bomboclot.input;

/**
 * A delivery time window expressed as minutes since midnight. Comparing two ints is
 * trivial and avoids any LocalTime or timezone subtleties; conversion to clock format
 * happens only at the I/O boundary.
 */
public record TimeWindow(int open_minutes, int close_minutes)
{
    /**
     * A 24-hour "no restriction" window, used as a default when no schedule is
     * available for a client on a given weekday.
     */
    public static TimeWindow anyTime()
    {
        return new TimeWindow(0, 24 * 60 - 1);
    }

    /**
     * Whether the driver arrived in time. Arriving early is fine — the truck waits.
     */
    public boolean accepts_arrival(int arrival_minutes)
    {
        return arrival_minutes <= close_minutes;
    }

    /**
     * Minutes the truck must wait if it arrived before the window opened. Returns
     * zero when the truck is already inside the window or beyond.
     */
    public int wait_minutes(int arrival_minutes)
    {
        return Math.max(0, open_minutes - arrival_minutes);
    }
}
