package bomboclot.algorithm.packer;

import bomboclot.algorithm.model.Item;
import bomboclot.algorithm.model.Rectangle;
import bomboclot.input.Dimensions;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Categorizer
{
    public static boolean is_similar(Item a, Item b)
    {
        Dimensions a_dim = a.get_dimensions();
        Dimensions b_dim = b.get_dimensions();

        return Math.abs(a_dim.width() - b_dim.width()) <= 0.01 &&
               Math.abs(a_dim.height() - b_dim.height()) <= 0.01 &&
               Math.abs(a_dim.length() - b_dim.length()) <= 0.01;
    }

    public static Category[] categorize(ArrayList<Item> items)
    {
        ArrayList<Category> output = new ArrayList<>();
        ArrayList<Pair<Item, ArrayList<Item>>> categories = new ArrayList<>();

        for (Item item : items)
        {
            boolean categorized = false;
            for (var pair : categories)
            {
                if (is_similar(pair.getKey(), item))
                {
                    pair.getValue().add(item);
                    categorized = true;
                    break;
                }
            }

            if (!categorized)
            {
                categories.add(new Pair<>(item, new ArrayList<>(List.of(item))));
            }
        }

        for (var category : categories)
        {
            output.add(new Category(category.getKey().get_dimensions().get_base(), category.getValue()));
        }

        return output.toArray(new Category[0]);
    }

    public static class Category
    {
        private final Rectangle base_;
        private final List<Item> items_;

        private Dimensions bounding_box_cache = null;

        public Category(Rectangle base, List<Item> items)
        {
            this.base_ = base;
            this.items_ = new ArrayList<>(items);
        }

        public Rectangle base()
        {
            return base_;
        }

        public List<Item> items()
        {
            return items_;
        }

        public Dimensions bounding_box()
        {
            if (bounding_box_cache == null)
            {
                double max_l = 0, max_w = 0, max_h = 0;
                for (Item item : items_)
                {
                    Dimensions item_dim = item.get_dimensions();

                    if (item_dim.length() > max_l)
                        max_l = item_dim.length();

                    if (item_dim.width() > max_w)
                        max_w = item_dim.width();

                    if (item_dim.height() > max_h)
                        max_h = item_dim.height();
                }

                bounding_box_cache = new Dimensions(max_l, max_w, max_h);
            }

            return bounding_box_cache;
        }

        public int max_stack(double max_height)
        {
            if (bounding_box().height() < 0.01) return 1;

            return Math.min((int) Math.floor(max_height / bounding_box().height()), (int) Math.floor((bounding_box().get_base().area() * 100) / bounding_box().height()));
        }

        public int stack_amount(double max_height)
        {
            return (int) Math.ceil(items_.size() / (double) max_stack(max_height));
        }
    }
}
