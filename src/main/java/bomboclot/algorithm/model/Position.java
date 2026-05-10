package bomboclot.algorithm.model;

public record Position(int x, int y, int z)
{
    @Override
    public String toString()
    {
        return "<x:" + x + ",y: " + y + ",z: " + z + ">mm";
    }
}
