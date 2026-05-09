package bomboclot.input;

public interface IReader
{
    void load();

    Product get_product(String name);

    Costumer get_costumer(int identifier);
}
