package bomboclot.algorithm.model;

import bomboclot.input.Dimensions;

public class Prism
{
    private final String product_id;

    private final Dimensions dimensions;

    public Prism(String product_id,
                 Dimensions dimensions)
    {
        this.product_id = product_id;
        this.dimensions = dimensions;
    }

    public String get_product_id()
    {
        return product_id;
    }

    public Dimensions get_dimensions()
    {
        return dimensions;
    }

    public double get_volume()
    {
        return dimensions.get_volume();
    }

    @Override
    public String toString()
    {
        return product_id
                + " "
                + dimensions;
    }
}