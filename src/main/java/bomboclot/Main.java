package bomboclot;

import bomboclot.algorithm.model.Item;
import bomboclot.algorithm.model.Position;
import bomboclot.algorithm.model.Rectangle;
import bomboclot.algorithm.packer.Categorizer;
import bomboclot.algorithm.packer.MaxRects;
import bomboclot.algorithm.packer.MaxRects2;
import bomboclot.input.*;
import bomboclot.view.CargoViewer;

import java.util.*;

public class Main
{
    private static IReader reader;

    public static boolean is_similar(Rectangle a, Rectangle b)
    {
        return Math.abs(a.w() - b.w()) <= 0.05 &&
                Math.abs(a.h() - b.h()) <= 0.05;
    }

    public static void main(String[] args)
    {
        reader = new XLSXReader();

        reader.load();

        ArrayList<Item> items = new ArrayList();

        for (int i = 0; i < 30; i++)
        {
            Order order = reader.get_orders()[i];

            Product product = reader.get_product(order.material());
            if (product == null)
            {
                continue;
            }
            if (product.get_dimensions(order.unit()) == null)
            {
                System.out.println(order.material() + " " + order.unit());
                continue;
            }
            for (int j = 0; j < order.amount(); j++)
            {
                Dimensions dimensions = product.get_dimensions(order.unit());
                if (dimensions.height() <= 0.01) break;

                items.add(new Item(product.get_name(), dimensions));
            }
        }

        Categorizer.Category[] categories = Categorizer.categorize(items);

        var pack_result = MaxRects.pack(Arrays.asList(categories), 1.6, 0.9, 1.295);

        var placedCategories = pack_result.getKey();
        var unplacedCategories = pack_result.getValue();
        System.out.println(unplacedCategories);
        ArrayList<CargoViewer.PlacedPrism> placedItems = new ArrayList<>();

        for (MaxRects.PlacedCategory category : placedCategories)
        {
            Dimensions dimensions;
            if (category.flipped())
            {
                Dimensions current_dimensions = category.category().bounding_box();
                dimensions = new Dimensions(current_dimensions.width(), current_dimensions.length(), current_dimensions.height());
            }
            else
                dimensions = category.category().bounding_box();

            if (dimensions.height() <= 0.01) continue;

            for (int i = 0; i < category.category().items().size(); i++)
            {
                placedItems.add(new CargoViewer.PlacedPrism(new Item("a", dimensions), new Position(category.x(), category.z() + i * dimensions.height(), category.y())));
            }
        }

        placedItems.add(new CargoViewer.PlacedPrism(new Item("a", new Dimensions(0.9, 1.6, 0.1)), new Position(0, -0.1, 0)));

        CargoViewer.set(placedItems);

        CargoViewer.main(args);
    }
}
