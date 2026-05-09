package bomboclot.algorithm.model;

import bomboclot.algorithm.model.Layer;
import bomboclot.input.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class Pallet
{
    private final Dimensions dimensions;

    private final List<Column> columns =
            new ArrayList<>();

    public Pallet(Dimensions dimensions)
    {
        this.dimensions = dimensions;
    }

    public boolean place(Prism prism)
    {
        // 1. try existing columns
        for (Column column : columns)
        {
            if (column.place(prism))
            {
                return true;
            }
        }

        // 2. create new column
        Column new_column =
                new Column(
                        prism.get_product_id(),
                        find_new_position(prism),
                        new Dimensions(
                                prism.get_dimensions().length(),
                                prism.get_dimensions().width(),
                                dimensions.height()
                        )
                );

        boolean placed =
                new_column.place(prism);

        if (!placed)
        {
            return false;
        }

        columns.add(new_column);

        return true;
    }

    private Position find_new_position(Prism prism)
    {
        Dimensions d = prism.get_dimensions();

        double step = 1.0;

        for (double y = 0;
             y <= dimensions.width() - d.width();
             y += step)
        {
            for (double x = 0;
                 x <= dimensions.length() - d.length();
                 x += step)
            {
                Position candidate =
                        new Position(x, y, 0);

                if (can_place_column(candidate, d))
                {
                    return candidate;
                }
            }
        }

        return new Position(0, 0, 0);
    }

    private boolean can_place_column(Position pos,
                                     Dimensions d)
    {
        double x1 = pos.x();
        double y1 = pos.y();

        double x2 = x1 + d.length();
        double y2 = y1 + d.width();

        for (Column c : columns)
        {
            double cx1 = c.get_base_position().x();
            double cy1 = c.get_base_position().y();

            double cx2 = cx1 + c.get_base_dimensions().length();
            double cy2 = cy1 + c.get_base_dimensions().width();

            boolean overlap =
                    x1 < cx2
                            && x2 > cx1
                            && y1 < cy2
                            && y2 > cy1;

            if (overlap)
            {
                return false;
            }
        }

        return true;
    }


    public List<PrismPosition> get_prism_positions_world()
    {
        List<PrismPosition> result =
                new ArrayList<>();

        double pallet_half_x =
                get_dimensions().length() / 2.0;

        double pallet_half_y =
                get_dimensions().width() / 2.0;

        for (Column column : get_columns())
        {
            double base_x =
                    column.get_base_position().x()
                            - pallet_half_x;

            double base_y =
                    column.get_base_position().y()
                            - pallet_half_y;

            double current_z = 0;

            for (Layer layer : column.get_layers())
            {
                double layer_height =
                        compute_layer_height(layer);

                for (Layer.PlacedPrism placed :
                        layer.get_items())
                {
                    Prism prism =
                            placed.prism();

                    Dimensions d =
                            prism.get_dimensions();

                    double world_x =
                            base_x
                                    + placed.x()
                                    + (d.length() / 2.0);

                    double world_y =
                            base_y
                                    + placed.y()
                                    + (d.width() / 2.0);

                    double world_z =
                            current_z
                                    + (d.height() / 2.0);

                    result.add(
                            new PrismPosition(
                                    prism,
                                    world_x,
                                    world_y,
                                    world_z
                            )
                    );
                }

                current_z += layer_height;
            }
        }

        return result;
    }

    private double compute_layer_height(Layer layer)
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

    public record PrismPosition(
            Prism prism,
            double x,
            double y,
            double z
    ) {}
    
    public Dimensions get_dimensions()
    {
        return dimensions;
    }

    public List<Column> get_columns()
    {
        return columns;
    }
}