package bomboclot;

import bomboclot.algorithm.model.Dimensions;
import bomboclot.algorithm.model.Item;
import bomboclot.algorithm.model.Position;
import bomboclot.algorithm.packer.Categorizer;
import bomboclot.algorithm.packer.Packer3D;
import bomboclot.algorithm.packer.PlacedItem;
import bomboclot.input.*;
import bomboclot.view.CargoViewer;

import java.util.*;

public class Main
{
    private static IReader reader;

    public static void main(String[] args)
    {
        reader = new XLSXReader();

        reader.load();

        ArrayList<Item> items = new ArrayList();

        for (int i = 0; i < 300; i++)
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

        ArrayList<Categorizer.Group> categories = Categorizer.categorize(items);

        int size = categories.size();
        for (int i = 0; i < size; i++)
        {
            categories.add(categories.get(i));
        }

        var pack_result = Packer3D.ultra_pack(categories, new Dimensions(1600, 900, 1295));

        var placed_items   = pack_result.placed_items();
        var unplaced_items = pack_result.unplaced_items();
        ArrayList<CargoViewer.PlacedPrism> placed_prisms = new ArrayList<>();

        for (PlacedItem placed_item : placed_items)
        {
            placed_prisms.add(new CargoViewer.PlacedPrism(placed_item.item(), placed_item.position()));
        }

        placed_prisms.add(new CargoViewer.PlacedPrism(new Item("a", new Dimensions(1600, 900, 100)), new Position(0, -100, 0)));

        CargoViewer.set(placed_prisms);

        CargoViewer.main(args);
    }
}
