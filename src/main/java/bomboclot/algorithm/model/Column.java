package bomboclot.algorithm.model;

import bomboclot.input.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class Column
{
    private static final double EPSILON = 0.0001;

    private final String product_id;

    private final Position base_position;

    private final Dimensions base_dimensions;

    private double current_height;

    private final List<Prism> stacked_prisms;

    public Column(String product_id,
                  Position base_position,
                  Dimensions base_dimensions)
    {
        this.product_id = product_id;
        this.base_position = base_position;
        this.base_dimensions = base_dimensions;

        this.current_height = 0;

        this.stacked_prisms = new ArrayList<>();
    }

    public boolean can_stack(Prism prism, double max_height)
    {
        Dimensions dimensions = prism.get_dimensions();

        return prism.get_product_id()
                .equals(product_id)

                && same_base(dimensions, base_dimensions)

                && current_height
                + dimensions.height()
                <= max_height;
    }

    public void stack(Prism prism)
    {
        stacked_prisms.add(prism);

        current_height +=
                prism.get_dimensions().height();
    }

    private boolean same_base(Dimensions a, Dimensions b)
    {
        return nearly_equal(a.length(), b.length()) && nearly_equal(a.width(), b.width());
    }

    private boolean nearly_equal(double a, double b) { return Math.abs(a - b) < EPSILON; }

    public String get_product_id() { return product_id; }

    public Position get_base_position() { return base_position; }

    public Dimensions get_base_dimensions() { return base_dimensions; }

    public double get_current_height() { return current_height; }

    public List<Prism> get_stacked_prisms() { return stacked_prisms; }
}