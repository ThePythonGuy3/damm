package bomboclot.input;

import bomboclot.algorithm.model.Dimensions;

import java.util.HashMap;

public class Product
{
    private final String name;
    private final HashMap<String, Double>     unit_conversions = new HashMap<>();
    private final HashMap<String, Dimensions> unit_dimensions  = new HashMap<>();

    public Product(String name)
    {
        this.name = name;
    }

    public String get_name()
    {
        return name;
    }

    public void add_unit(String unit, double CE_multiplier, Dimensions dimensions)
    {
        unit_conversions.put(unit, CE_multiplier);
        unit_dimensions.put(unit, dimensions);
    }

    public double get_CE(String unit, double amount)
    {
        String unit_lower = unit.toLowerCase();

        if (unit_lower.equals("zce"))
            return amount * unit_conversions.get("zce");
        else
            return amount * unit_conversions.get(unit) * unit_conversions.get("zce");
    }

    public Dimensions get_dimensions(String unit)
    {
        return unit_dimensions.get(unit);
    }

    @Override
    public String toString()
    {
        return "Product[name = " + name + ", unit_conv = " + unit_conversions + ", unit_dim = " + unit_dimensions + "]";
    }
}
