package bomboclot.algorithm.packer;

import java.util.ArrayList;

// based on the "MAxRECTS" method developed by Jukka Jylänki: http://clb.demon.fi/files/RectangleBinPack.pdf
public class BinPacker
{
    ArrayList<Rect> freeList;

    public BinPacker(int width, int height)
    {
        freeList = new ArrayList<>(16);
        freeList.add(new Rect(0, 0, width, height));
    }

    public void Clear(int width, int height)
    {
        freeList.clear();
        freeList.add(new Rect(0, 0, width, height));
    }

    public Rect Insert(int width, int height)
    {
        Rect bestNode = new Rect(0, 0, 0, 0);
        int bestShortFit = Integer.MAX_VALUE;
        int bestLongFit = Integer.MIN_VALUE;

        int size = freeList.size();
        for (int i = 0; i < size; i++)
        {
            // try to place the rect
            Rect rect = freeList.get(i);
            if (rect.width < width || rect.height < height)
                continue;

            int leftoverx = Math.abs(rect.width - width);
            int leftovery = Math.abs(rect.height - height);
            int shortFit = Math.min(leftoverx, leftovery);
            int longFit = Math.max(leftoverx, leftovery);

            if (shortFit < bestShortFit || (shortFit == bestShortFit && longFit < bestLongFit)) {
                bestNode = new Rect(rect.x, rect.y, width, height);
                bestShortFit = shortFit;
                bestLongFit = longFit;
            }
        }

        if (bestNode.height == 0)
            return null;

        // split out free areas into smaller ones
        for (int i = 0; i < size; i++)
        {
            if (SplitFreeNode(freeList.get(i), bestNode)) {
                freeList.remove(i);
                i--;
                size--;
            }
        }

        // prune the freelist
        for (int i = 0; i < freeList.size(); i++)
        {
            for (int j = i + 1; j < freeList.size(); j++)
            {
                Rect idata = freeList.get(i);
                Rect jdata = freeList.get(j);

                if (jdata.contains(idata))
                {
                    freeList.remove(i);
                    i--;
                    break;
                }

                if (idata.contains(jdata))
                {
                    freeList.remove(j);
                    j--;
                }
            }
        }

        return bestNode;
    }

    boolean SplitFreeNode(Rect freeNode, Rect usedNode)
    {
        // test if the rects even intersect
        boolean insidex = usedNode.x < freeNode.right() && usedNode.right() > freeNode.x;
        boolean insidey = usedNode.y < freeNode.bottom() && usedNode.bottom() > freeNode.y;
        if (!insidex || !insidey)
            return false;

        // new node at the top side of the used node
        if (usedNode.y > freeNode.y && usedNode.y < freeNode.bottom())
        {
            Rect newNode = freeNode.copy();
            newNode.height = usedNode.y - newNode.y;
            freeList.add(newNode);
        }

        // new node at the bottom side of the used node
        if (usedNode.bottom() < freeNode.bottom())
        {
            Rect newNode = freeNode.copy();
            newNode.y = usedNode.bottom();
            newNode.height = freeNode.bottom() - usedNode.bottom();
            freeList.add(newNode);
        }

        // new node at the left side of the used node
        if (usedNode.x > freeNode.x && usedNode.x < freeNode.right())
        {
            Rect newNode = freeNode.copy();
            newNode.width = usedNode.x - newNode.x;
            freeList.add(newNode);
        }

        // new node at the right side of the used node
        if (usedNode.right() < freeNode.right()) {
            Rect newNode = freeNode.copy();
            newNode.x = usedNode.right();
            newNode.width = freeNode.right() - usedNode.right();
            freeList.add(newNode);
        }

        return true;
    }
}