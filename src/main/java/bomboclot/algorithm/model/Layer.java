package bomboclot.algorithm.model;

import bomboclot.input.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class Layer
{
    private static final double EPSILON = 0.0001;

    private final double layer_height;

    private final Dimensions pallet_dimensions;

    private final List<PlacedPrism> items =
            new ArrayList<>();

    public Layer(Dimensions pallet_dimensions)
    {
        this.pallet_dimensions = pallet_dimensions;
        this.layer_height = 0;
    }

    public boolean can_fit(Prism prism, double x, double y)
    {
        Dimensions d = prism.get_dimensions();

        // 1. bounds check
        if (x + d.length() > pallet_dimensions.length()
                || y + d.width() > pallet_dimensions.width())
        {
            return false;
        }

        // 2. collision check
        for (PlacedPrism p : items)
        {
            if (overlaps(
                    x, y, d.length(), d.width(),
                    p.x, p.y,
                    p.dimensions().length(),
                    p.dimensions().width()))
            {
                return false;
            }
        }

        // 3. support check (must be fully supported underneath)
        return is_supported(x, y, d);
    }

    public void add(Prism prism, double x, double y)
    {
        items.add(new PlacedPrism(prism, x, y));
    }

    private boolean is_supported(double x,
                                 double y,
                                 Dimensions d)
    {
        /*
         * A simple but REAL rule:
         *
         * - if it's on the floor -> OK
         * - otherwise must sit fully on existing boxes
         */

        if (items.isEmpty())
        {
            return true;
        }

        double x2 = x + d.length();
        double y2 = y + d.width();

        // check if at least one supporting surface exists
        boolean supported = false;

        for (PlacedPrism p : items)
        {
            double px2 = p.x + p.dimensions().length();
            double py2 = p.y + p.dimensions().width();

            boolean covers_underneath =
                    x >= p.x
                            && x2 <= px2
                            && y >= p.y
                            && y2 <= py2;

            if (covers_underneath)
            {
                supported = true;
                break;
            }
        }

        return supported;
    }

    private boolean overlaps(
            double x1, double y1, double w1, double h1,
            double x2, double y2, double w2, double h2)
    {
        return !(x1 + w1 <= x2
                || x2 + w2 <= x1
                || y1 + h1 <= y2
                || y2 + h2 <= y1);
    }

    public List<PlacedPrism> get_items()
    {
        return items;
    }

    public record PlacedPrism(
            Prism prism,
            double x,
            double y
    )
    {
        public Dimensions dimensions()
        {
            return prism.get_dimensions();
        }
    }
}