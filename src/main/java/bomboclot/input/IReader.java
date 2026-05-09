package bomboclot.input;

public interface IReader
{
    void load();

    Product get_product(String name);
}
