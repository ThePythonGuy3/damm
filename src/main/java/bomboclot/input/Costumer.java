package bomboclot.input;

public class Costumer
{
    private final String identifier;
    private final String name, name2;
    private final Address address;

    public Costumer(String identifier, String name, String name2, Address address)
    {
        this.identifier = identifier;
        this.name = name;
        this.name2 = name2;
        this.address = address;
    }

    public String get_identifier()
    {
        return identifier;
    }

    public String get_name()
    {
        return name;
    }

    public String get_name2()
    {
        return name2;
    }

    public Address get_address()
    {
        return address;
    }

    @Override
    public String toString()
    {
        return "Costumer[id = " + identifier + ", name = " + name + ", name2 = " + name2 + ", address = " + address + "]";
    }
}
