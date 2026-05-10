package bomboclot.algorithm.packer;

public class Rect
{
    public int x, y, width, height;

    public Rect(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int right()
    {
        return x + width;
    }
    public int bottom()
    {
        return y + height;
    }

    public boolean contains(Rect rect)
    {
        return rect.x >= x && rect.y >= y &&
            rect.right() <= right() && rect.bottom() <= bottom();
    }

    public int area()
    {
        return width * height;
    }

    public Rect copy()
    {
        return new Rect(x, y, width, height);
    }

    @Override
    public String toString()
    {
        return "<" + x + "," + y + "," + width + "," + height + ">";
    }
}