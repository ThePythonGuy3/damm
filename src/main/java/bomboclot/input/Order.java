package bomboclot.input;

public record Order(String material, int amount, String unit)
{
    @Override
    public String toString()
    {
        return "Order[material = " + material + ", amount = " + amount + ", unit = " + unit + "]";
    }
}
