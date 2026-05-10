package bomboclot.algorithm.packer;

import bomboclot.algorithm.model.Dimensions;
import bomboclot.algorithm.model.Item;
import bomboclot.algorithm.model.Position;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class Packer3D
{
    private static final BinPacker packer = new BinPacker(1, 1);

    public record Surface(int z, int z_free, Rect rect)
    {
    }

    public record Packer3DResult(ArrayList<PlacedItem> placed_items, ArrayList<Categorizer.Group> unplaced_items, ArrayList<Surface> surfaces)
    {
    }

    public static Packer3DResult pack(ArrayList<Categorizer.Group> groups, Dimensions pallet_size, Position offset)
    {
        ArrayList<PlacedItem>        placed_items   = new ArrayList<>();
        ArrayList<Categorizer.Group> unplaced_items = new ArrayList<>();
        ArrayList<Surface>           surfaces       = new ArrayList<>();

        PriorityQueue<Categorizer.Group> group_queue = new PriorityQueue<>((a, b) -> Double.compare(b.bounding_box().width() * b.bounding_box().height(), a.bounding_box().width() * a.bounding_box().height()));
        group_queue.addAll(groups);

        packer.Clear(pallet_size.width(), pallet_size.length());
        while (!group_queue.isEmpty())
        {
            Categorizer.Group group = group_queue.poll();
            Rect packed = packer.Insert(group.bounding_box().width(), group.bounding_box().length());

            Categorizer.Group surplus_group = null;
            ArrayList<Item> surplus_items = new ArrayList<>();

            if (packed != null)
            {
                int stack = 0;
                boolean placed = false;
                for (Item item : group.items())
                {
                    if (stack + item.get_dimensions().height() > pallet_size.height())
                        surplus_items.add(item);
                    else
                    {
                        placed_items.add(new PlacedItem(item, new Position(offset.x() + packed.x, offset.z() + stack, offset.y() + packed.y)));
                        placed = true;

                        stack += item.get_dimensions().height();
                    }
                }

                if (!surplus_items.isEmpty())
                    surplus_group = new Categorizer.Group(group.base(), surplus_items);

                if (placed)
                    surfaces.add(new Surface(offset.z() + stack, pallet_size.height() - stack, new Rect(packed.x + offset.x(), packed.y + offset.y(), packed.width, packed.height)));
            }
            else
            {
                unplaced_items.add(group);
            }

            while (surplus_group != null)
            {
                packed = packer.Insert(surplus_group.bounding_box().width(), surplus_group.bounding_box().length());

                if (packed == null) break;

                int stack = 0;
                surplus_items.clear();

                boolean placed = false;
                for (Item item : surplus_group.items())
                {
                    if (stack + item.get_dimensions().height() > pallet_size.height())
                        surplus_items.add(item);
                    else
                    {
                        placed_items.add(new PlacedItem(item, new Position(offset.x() + packed.x, offset.z() + stack, offset.y() + packed.y)));
                        placed = true;

                        stack += item.get_dimensions().height();
                    }
                }

                surplus_group = null;
                if (!surplus_items.isEmpty())
                    surplus_group = new Categorizer.Group(group.base(), surplus_items);

                if (placed)
                    surfaces.add(new Surface(offset.z() + stack, pallet_size.height() - stack, new Rect(packed.x + offset.x(), packed.y + offset.y(), packed.width, packed.height)));
            }
        }

        return new Packer3DResult(placed_items, unplaced_items, surfaces);
    }

    public static Packer3DResult ultra_pack(ArrayList<Categorizer.Group> groups, Dimensions pallet_size)
    {
        ArrayList<PlacedItem>        placed_items   = new ArrayList<>();
        ArrayList<Surface>           surfaces       = new ArrayList<>();

        surfaces.add(new Surface(0, pallet_size.height(), new Rect(0, 0, pallet_size.width(), pallet_size.length())));

        while (!groups.isEmpty() && !surfaces.isEmpty())
        {
            surfaces.sort((a, b) -> Integer.compare(b.rect.width * b.rect.height, a.rect.width * a.rect.height));

            Surface surface = surfaces.removeFirst();

            Packer3DResult result = pack(groups, new Dimensions(surface.rect.height, surface.rect.width, surface.z_free), new Position(surface.rect.x, surface.rect.y, surface.z));

            groups = result.unplaced_items;
            surfaces.addAll(result.surfaces);
            placed_items.addAll(result.placed_items);
        }

        return new Packer3DResult(placed_items, groups, surfaces);
    }
}
