package bomboclot.algorithm.model;

import bomboclot.algorithm.packer.Rect;

public record Dimensions(int length, int width, int height)
{
    public Rect get_base()
    {
        return new Rect(0, 0, width, length);
    }
    @Override
    public String toString()
    {
        return "<l:" + length + ",w: " + width + ",h: " + height + ">mm";
    }
}
