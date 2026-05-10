package bomboclot.input;

import bomboclot.algorithm.model.Rectangle;

public record Dimensions(double length, double width, double height)
{
    public Rectangle get_base()
    {
        return new Rectangle(width, length);
    }

    @Override
    public String toString()
    {
        return "<l:" + length + ",w: " + width + ",h: " + height + ">m";
    }
}
