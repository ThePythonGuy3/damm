package bomboclot.algorithm.model;

public record Rectangle(double w, double h)
{
    @Override
    public String toString()
    {
        return "<" + w + "," + h + ">";
    }

    public double area()
    {
        return w * h;
    }
}
