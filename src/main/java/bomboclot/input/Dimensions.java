package bomboclot.input;

public record Dimensions(double length, double width, double height)
{
    @Override
    public String toString()
    {
        return "<l:" + length + ",w: " + width + ",h: " + height + ">m";
    }
}
