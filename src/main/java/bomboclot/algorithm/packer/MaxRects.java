package bomboclot.algorithm.packer;

import bomboclot.algorithm.model.Item;
import bomboclot.algorithm.model.Rectangle;
import bomboclot.input.Dimensions;
import javafx.util.Pair;

import java.util.*;

/**
 * MaxRects-based rectangle packing with:
 * - 90 degree rotation
 * - priority rectangles biased toward outer walls
 * - free-area minimization
 *
 * This is a practical heuristic approach.
 */
public class MaxRects
{
    public record PlacedCategory(double x, double y, double z, boolean flipped, Categorizer.Category category)
    {
        public Rectangle rectangle()
        {
            Rectangle base = category.bounding_box().get_base();

            if (flipped)
                return new Rectangle(base.h(), base.w());
            else
                return base;
        }

        public double right()
        {
            return x + rectangle().w();
        }

        public double bottom()
        {
            return y + rectangle().h();
        }
    }

    private static boolean eq(double a, double b)
    {
        return Math.abs(a - b) <= 1e-6;
    }

    // =========================
    // FREE SPACE
    // =========================

    private record FreeRect(double x, double y, double w, double h)
    {

    }

    // =========================
    // SCORE
    // =========================

    private record Score(double score, PlacedCategory category)
    {

    }

    // =========================
    // PACKER
    // =========================

    /*private final double binWidth;
    private final double binHeight;
    private final double binStack;

    private final List<FreeRect> freeRects = new ArrayList<>();
    private final List<PlacedCategory> placedCategories = new ArrayList<>();

    public MaxRects(double binWidth, double binHeight, double binStack)
    {
        this.binWidth = binWidth;
        this.binHeight = binHeight;
        this.binStack = binStack;

        freeRects.add(new FreeRect(0, 0, binWidth, binHeight));
    }*/

    // =========================
    // MAIN PACK
    // =========================

    private record PackTarget(double x, double y, double z, double w, double h, double s)
    {

    }

    private static List<Categorizer.Category> ultra_pack_(PriorityQueue<PackTarget> targets, List<Categorizer.Category> categories, List<PlacedCategory> placed_categories)
    {
        while (!categories.isEmpty() && !targets.isEmpty())
        {
            PackTarget packTarget = targets.poll();

            var packResult = pack(categories, packTarget.w, packTarget.h, packTarget.s);

            var placed_categories_ = packResult.getKey();
            var unplaced_categories = packResult.getValue();

            categories = unplaced_categories;

            for (PlacedCategory category : placed_categories_)
            {
                placed_categories.add(new PlacedCategory(category.x + packTarget.x, category.y + packTarget.y, packTarget.z, category.flipped, category.category));

                double stack_height = category.category().items().size() * category.category().bounding_box().height();
                double remaining = packTarget.s - stack_height;

                boolean usable = false;

                for (Categorizer.Category c : unplaced_categories)
                {
                    Rectangle r = c.bounding_box().get_base();

                    if (r.w() <= category.rectangle().w()
                            && r.h() <= category.rectangle().h()
                            && c.bounding_box().height() <= remaining)
                    {
                        usable = true;
                        break;
                    }

                    // rotated
                    if (r.h() <= category.rectangle().w()
                            && r.w() <= category.rectangle().h()
                            && c.bounding_box().height() <= remaining)
                    {
                        usable = true;
                        break;
                    }
                }

                if (!usable)
                    continue;

                boolean duplicate = targets.stream().anyMatch(t ->
                        eq(t.x, packTarget.x + category.x)
                                && eq(t.y, packTarget.y + category.y)
                                && eq(t.z, packTarget.z + stack_height)
                                && eq(t.w, category.rectangle().w())
                                && eq(t.h, category.rectangle().h())
                );

                if (duplicate)
                    continue;

                targets.add(new PackTarget(packTarget.x + category.x, packTarget.y + category.y, packTarget.z + stack_height, category.rectangle().w(), category.rectangle().h(), remaining));
            }
        }

        return categories;
    }

    public static Pair<List<PlacedCategory>, Boolean> ultra_pack(List<Categorizer.Category> categories, double binWidth, double binHeight, double binStack)
    {
        PriorityQueue<PackTarget> targets =
            new PriorityQueue<>(
                (a, b) ->
                    Double.compare(
                        b.w * b.h,
                        a.w * a.h
                    )
            );

        targets.add(new PackTarget(0, 0, 0, binWidth, binHeight, binStack));

        ArrayList<PlacedCategory> placed_categories = new ArrayList<>();

        boolean done = ultra_pack_(targets, categories, placed_categories).isEmpty();

        return new Pair<>(placed_categories, done);
    }

    public static Pair<List<PlacedCategory>, ArrayList<Categorizer.Category>> pack(List<Categorizer.Category> categories, double binWidth, double binHeight, double binStack)
    {
        categories = new ArrayList<>(categories);
        categories.sort((a, b) -> Double.compare(b.base().area(), a.base().area()));

        ArrayList<Categorizer.Category> unplaced_categories = new ArrayList<>();
        ArrayList<PlacedCategory>       placed_categories   = new ArrayList<>();
        ArrayList<FreeRect>             free_rects          = new ArrayList<>();

        free_rects.add(new FreeRect(0, 0, binWidth, binHeight));

        for (Categorizer.Category category : categories)
        {
            int slice = 0;
            int max_stack = category.max_stack(binStack);
            for (int i = 0; i < category.stack_amount(binStack); i++)
            {
                Categorizer.Category stack_category = new Categorizer.Category(category.base(), category.items().subList(slice, Math.min(category.items().size(), slice + max_stack)));
                Score best = find_best_placement(stack_category, free_rects, binWidth, binHeight);

                if (best == null)
                {
                    unplaced_categories.add(stack_category);
                    break;
                }

                place(best.category, free_rects, placed_categories);

                slice += max_stack;
            }
        }

        return new Pair<>(placed_categories, unplaced_categories);
    }

    // =========================
    // FIND BEST POSITION
    // =========================

    private static Score find_best_placement(Categorizer.Category category, List<FreeRect> freeRects, double binWidth, double binHeight)
    {
        Score best = null;

        Rectangle rect = category.bounding_box().get_base();

        for (FreeRect free : freeRects)
        {
            // Try both orientations
            for (int rot = 0; rot < 2; rot++)
            {
                double rw = (rot == 0) ? rect.w() : rect.h();
                double rh = (rot == 0) ? rect.h() : rect.w();

                if (rw > free.w || rh > free.h)
                    continue;

                PlacedCategory candidate = new PlacedCategory(
                        free.x,
                        free.y,
                        0,
                        rot == 1,
                        category
                );

                double score = evaluate(candidate, free, binWidth, binHeight);

                if (best == null || score < best.score)
                    best = new Score(score, candidate);
            }
        }

        return best;
    }

    // =========================
    // SCORING FUNCTION
    // =========================

    private static double evaluate(PlacedCategory category, FreeRect free, double binWidth, double binHeight)
    {
        Rectangle rect = category.rectangle();

        double waste =
                (free.w * free.h)
                        - (rect.w() * rect.h());

        double shortSide =
                Math.min(
                        free.w - rect.w(),
                        free.h - rect.h()
                );

        // Boundary bonus
        int boundaryTouches = 0;

        if (eq(category.x, 0)) boundaryTouches++;
        if (eq(category.y, 0)) boundaryTouches++;
        if (eq(category.right(), binWidth)) boundaryTouches++;
        if (eq(category.bottom(), binHeight)) boundaryTouches++;

        double boundaryBonus = boundaryTouches * 1000.0;

        boundaryBonus *= 4.0;

        // Lower score = better
        return waste
                + shortSide * 10.0
                - boundaryBonus;
    }

    // =========================
    // PLACE RECTANGLE
    // =========================

    private static void place(PlacedCategory placed, List<FreeRect> freeRects, List<PlacedCategory> placedCategories)
    {
        List<FreeRect> newFreeRects = new ArrayList<>();

        for (FreeRect free : freeRects)
        {
            if (!intersects(placed, free))
            {
                newFreeRects.add(free);
                continue;
            }

            // Split free rectangle

            // Top
            if (placed.y > free.y)
            {
                newFreeRects.add(new FreeRect(
                        free.x,
                        free.y,
                        free.w,
                        placed.y - free.y
                ));
            }

            // Bottom
            if (placed.bottom() < free.y + free.h) {
                newFreeRects.add(new FreeRect(
                        free.x,
                        placed.bottom(),
                        free.w,
                        (free.y + free.h) - placed.bottom()
                ));
            }

            // Left
            if (placed.x > free.x) {
                newFreeRects.add(new FreeRect(
                        free.x,
                        free.y,
                        placed.x - free.x,
                        free.h
                ));
            }

            // Right
            if (placed.right() < free.x + free.w) {
                newFreeRects.add(new FreeRect(
                        placed.right(),
                        free.y,
                        (free.x + free.w) - placed.right(),
                        free.h
                ));
            }
        }

        freeRects.clear();
        freeRects.addAll(prune(newFreeRects));

        placedCategories.add(placed);
    }

    // =========================
    // INTERSECTION
    // =========================

    private static boolean intersects(PlacedCategory r, FreeRect f) {

        return !(r.right() <= f.x
                || r.x >= f.x + f.w
                || r.bottom() <= f.y
                || r.y >= f.y + f.h);
    }

    // =========================
    // REMOVE REDUNDANT FREE RECTS
    // =========================

    private static List<FreeRect> prune(List<FreeRect> rects)
    {
        List<FreeRect> result = new ArrayList<>();

        for (int i = 0; i < rects.size(); i++)
        {
            FreeRect a = rects.get(i);

            boolean contained = false;

            for (int j = 0; j < rects.size(); j++)
            {
                if (i == j) continue;

                FreeRect b = rects.get(j);

                if (contains(b, a))
                {
                    contained = true;
                    break;
                }
            }

            double area = a.w * a.h;

            if (area < 1e-3)
                continue;

            if (a.w < 1e-3 || a.h < 1e-3)
                continue;

            if (!contained && a.w > 0 && a.h > 0)
                result.add(a);
        }

        return result;
    }

    private static boolean contains(FreeRect outer, FreeRect inner)
    {
        return inner.x >= outer.x
                && inner.y >= outer.y
                && inner.x + inner.w <= outer.x + outer.w
                && inner.y + inner.h <= outer.y + outer.h;
    }
}