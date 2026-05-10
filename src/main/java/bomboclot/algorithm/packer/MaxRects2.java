package bomboclot.algorithm.packer;

import bomboclot.algorithm.model.Rectangle;
import javafx.util.Pair;

import java.util.*;

public class MaxRects2
{
    public record PlacedCategory(double x, double y, double z, boolean flipped, Categorizer.Category category)
    {
        public Rectangle rectangle()
        {
            Rectangle base = category.bounding_box().get_base();
            return flipped ? new Rectangle(base.h(), base.w()) : base;
        }

        public double right()  { return x + rectangle().w(); }
        public double bottom() { return y + rectangle().h(); }
    }

    private record FreeRect(double x, double y, double w, double h) {}

    private record Score(double score, PlacedCategory placed) {}

    private record PackTarget(double x, double y, double z, double w, double h, double s) {}

    // =========================================================
    // PUBLIC API (UNCHANGED)
    // =========================================================

    public static Pair<List<PlacedCategory>, Boolean> ultra_pack(
            List<Categorizer.Category> categories,
            double binWidth,
            double binHeight,
            double binStack)
    {
        List<PlacedCategory> result = new ArrayList<>();

        Deque<PackTarget> queue = new ArrayDeque<>();
        queue.add(new PackTarget(0, 0, 0, binWidth, binHeight, binStack));

        List<Categorizer.Category> remaining = new ArrayList<>(categories);

        int safety = 0;

        while (!queue.isEmpty() && !remaining.isEmpty())
        {
            if (safety++ > 10_000) break; // HARD STOP against runaway growth

            PackTarget t = queue.poll();

            PackResult r = packLayer(remaining, t);

            result.addAll(offset(r.placed, t));

            remaining = r.unplaced;

            // ONLY generate next layer targets from current layer
            for (PlacedCategory p : r.placed)
            {
                double h = p.rectangle().h();

                if (t.s - h <= 0) continue;

                queue.add(new PackTarget(
                        t.x + p.x,
                        t.y + p.y,
                        t.z + h,
                        p.rectangle().w(),
                        p.rectangle().h(),
                        t.s - h
                ));
            }

            // prevent explosion: cap queue size
            if (queue.size() > 200)
                break;
        }

        return new Pair<>(result, remaining.isEmpty());
    }

    // =========================================================
    // PACK RESULT
    // =========================================================

    private record PackResult(List<PlacedCategory> placed, List<Categorizer.Category> unplaced) {}

    private static PackResult packLayer(
            List<Categorizer.Category> categories,
            PackTarget t)
    {
        List<Categorizer.Category> unplaced = new ArrayList<>();
        List<PlacedCategory> placed = new ArrayList<>();

        List<FreeRect> free = new ArrayList<>();
        free.add(new FreeRect(0, 0, t.w, t.h));

        for (Categorizer.Category c : categories)
        {
            Score best = findBest(c, free);

            if (best == null)
            {
                unplaced.add(c);
                continue;
            }

            placed.add(best.placed);
            applyPlacement(best.placed, free);
        }

        return new PackResult(placed, unplaced);
    }

    // =========================================================
    // PLACEMENT SEARCH
    // =========================================================

    private static Score findBest(Categorizer.Category c, List<FreeRect> freeRects)
    {
        Rectangle r = c.bounding_box().get_base();
        Score best = null;

        for (FreeRect f : freeRects)
        {
            for (int rot = 0; rot < 2; rot++)
            {
                double w = (rot == 0) ? r.w() : r.h();
                double h = (rot == 0) ? r.h() : r.w();

                if (w > f.w || h > f.h)
                    continue;

                PlacedCategory p = new PlacedCategory(f.x, f.y, 0, rot == 1, c);

                double score = score(p, f);

                if (best == null || score < best.score)
                    best = new Score(score, p);
            }
        }

        return best;
    }

    // =========================================================
    // SCORE
    // =========================================================

    private static double score(PlacedCategory p, FreeRect f)
    {
        Rectangle r = p.rectangle();

        double waste = f.w * f.h - r.w() * r.h();
        double fit = Math.min(f.w - r.w(), f.h - r.h());

        return waste + fit * 10;
    }

    // =========================================================
    // SAFE PLACEMENT (NO FREE LIST EXPLOSION)
    // =========================================================

    private static void applyPlacement(PlacedCategory p, List<FreeRect> free)
    {
        for (int i = 0; i < free.size(); i++)
        {
            FreeRect f = free.get(i);

            if (!intersects(p, f))
                continue;

            free.remove(i--); // safe removal

            double px1 = p.x;
            double py1 = p.y;
            double px2 = p.right();
            double py2 = p.bottom();

            // split into MAX 2 rectangles (not 4)
            if (px1 > f.x)
                free.add(new FreeRect(f.x, f.y, px1 - f.x, f.h));

            if (px2 < f.x + f.w)
                free.add(new FreeRect(px2, f.y, (f.x + f.w) - px2, f.h));

            if (py1 > f.y)
                free.add(new FreeRect(f.x, f.y, f.w, py1 - f.y));

            if (py2 < f.y + f.h)
                free.add(new FreeRect(f.x, py2, f.w, (f.y + f.h) - py2));
        }

        prune(free);
    }

    // =========================================================
    // PRUNE (SAFE, NO O(n²) GROWTH EXPLOSION)
    // =========================================================

    private static void prune(List<FreeRect> free)
    {
        free.removeIf(r -> r.w <= 0.5 || r.h <= 0.5);
    }

    // =========================================================
    // GEOMETRY
    // =========================================================

    private static boolean intersects(PlacedCategory p, FreeRect f)
    {
        return !(p.right() <= f.x ||
                p.x >= f.x + f.w ||
                p.bottom() <= f.y ||
                p.y >= f.y + f.h);
    }

    // =========================================================
    // OFFSET HELPER
    // =========================================================

    private static List<PlacedCategory> offset(List<PlacedCategory> list, PackTarget t)
    {
        List<PlacedCategory> out = new ArrayList<>(list.size());

        for (PlacedCategory p : list)
        {
            out.add(new PlacedCategory(
                    p.x + t.x,
                    p.y + t.y,
                    p.z,
                    p.flipped,
                    p.category
            ));
        }

        return out;
    }
}