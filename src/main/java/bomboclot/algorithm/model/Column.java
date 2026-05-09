package bomboclot.algorithm.model;

import bomboclot.algorithm.model.Layer;
import bomboclot.input.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class Column
{
    private final String product_id;

    private final Position base_position;

    private final Dimensions base_dimensions;

    private final List<Layer> layers = new ArrayList<>();

    public Column(String product_id,
                  Position base_position,
                  Dimensions base_dimensions)
    {
        this.product_id = product_id;
        this.base_position = base_position;
        this.base_dimensions = base_dimensions;
    }

    /*
     * Try placing prism into existing layers,
     * otherwise create a new layer
     */
    public boolean place(Prism prism)
    {
        // 1. try existing layers
        for (Layer layer : layers)
        {
            boolean placed = try_place_in_layer(layer, prism);

            if (placed)
            {
                return true;
            }
        }

        // 2. create new layer
        Layer new_layer =
                new Layer(base_dimensions);

        boolean placed =
                try_place_in_layer(new_layer, prism);

        if (!placed)
        {
            return false;
        }

        layers.add(new_layer);

        return true;
    }

    /*
     * Finds a valid (x,y) inside a layer
     * using ONLY Layer.can_fit(...)
     */
    private boolean try_place_in_layer(Layer layer,
                                       Prism prism)
    {
        Dimensions d = prism.get_dimensions();

        double step = 1.0;

        for (double y = 0;
             y <= base_dimensions.width() - d.width();
             y += step)
        {
            for (double x = 0;
                 x <= base_dimensions.length() - d.length();
                 x += step)
            {
                if (layer.can_fit(prism, x, y))
                {
                    layer.add(prism, x, y);
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * Total height = sum of layer heights
     */
    public double get_current_height()
    {
        double total = 0;

        for (Layer layer : layers)
        {
            total += layer_height(layer);
        }

        return total;
    }

    /*
     * Layer height = max height of items in layer
     * (we don't modify Layer, so we compute externally)
     */
    private double layer_height(Layer layer)
    {
        double max = 0;

        for (Layer.PlacedPrism p : layer.get_items())
        {
            max = Math.max(
                    max,
                    p.prism()
                            .get_dimensions()
                            .height()
            );
        }

        return max;
    }

    public String get_product_id()
    {
        return product_id;
    }

    public Position get_base_position()
    {
        return base_position;
    }

    public Dimensions get_base_dimensions()
    {
        return base_dimensions;
    }

    public List<Layer> get_layers()
    {
        return layers;
    }
}