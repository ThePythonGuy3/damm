package bomboclot.input;

public record Dimensions(double length, double width, double height)
{
    public double get_volume()
    {
        return length * width * height;
    }

    public double get_base_area()
    {
        return length * width;
    }
    @Override
    public String toString()
    {
        return "<l:" + length + ",w: " + width + ",h: " + height + ">m";
    }
}
