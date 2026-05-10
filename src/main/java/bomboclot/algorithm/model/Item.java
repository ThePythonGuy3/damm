package bomboclot.algorithm.model;

public class Item
{
    private final String product_id;

    private final Dimensions dimensions;

    public Item(String product_id,
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

    @Override
    public String toString()
    {
        return product_id
                + " "
                + dimensions;
    }
}