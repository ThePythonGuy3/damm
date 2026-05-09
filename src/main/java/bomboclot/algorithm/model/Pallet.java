package bomboclot.algorithm.model;

import bomboclot.input.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class Pallet
{
    private final Dimensions dimensions;

    private final List<Column> columns;

    public Pallet(Dimensions dimensions)
    {
        this.dimensions = dimensions;

        this.columns = new ArrayList<>();
    }

    public boolean place(Prism prism)
    {
        /*
         * RULE 1:
         * Stack on same-material column
         */
        for (Column column : columns)
        {
            if (column.can_stack(prism, dimensions.height()))
            {
                column.stack(prism);
                return true;
            }
        }

        /*
         * RULE 2:
         * Create new column near corner
         */
        Position position = find_corner_position(prism);

        if (position == null)
        {
            return false;
        }

        Column column = new Column(
                        prism.get_product_id(),
                        position,
                        prism.get_dimensions()
                );

        column.stack(prism);

        columns.add(column);

        return true;
    }

    private Position find_corner_position(Prism prism)
    {
        Dimensions prism_dimensions = prism.get_dimensions();

        double step = 1.0;

        for (double y = 0; y <= dimensions.width() - prism_dimensions.width(); y += step)
        {
            for (double x = 0; x <= dimensions.length() - prism_dimensions.length(); x += step)
            {
                Position candidate = new Position(x, y, 0);

                if (can_place_at(candidate, prism_dimensions)) { return candidate; }
            }
        }

        return null;
    }

    private boolean can_place_at(Position position, Dimensions prism_dimensions)
    {
        double x1 = position.x();
        double y1 = position.y();

        double x2 = x1 + prism_dimensions.length();

        double y2 = y1 + prism_dimensions.width();

        /*
         * Check pallet bounds
         */
        if (x2 > dimensions.length() || y2 > dimensions.width()) { return false; }

        /*
         * Collision check
         */
        for (Column column : columns)
        {
            double cx1 = column.get_base_position().x();

            double cy1 = column.get_base_position().y();

            double cx2 = cx1 + column.get_base_dimensions().length();

            double cy2 = cy1 + column.get_base_dimensions().width();

            boolean overlap = x1 < cx2
                            && x2 > cx1
                            && y1 < cy2
                            && y2 > cy1;

            if (overlap)
            { return false; }
        }

        return true;
    }

    public Dimensions get_dimensions()
    {
        return dimensions;
    }

    public List<Column> get_columns()
    {
        return columns;
    }
}