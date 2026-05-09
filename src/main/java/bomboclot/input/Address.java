package bomboclot.input;

public record Address(String street, String postal, String city)
{
    @Override
    public String toString()
    {
        return "<s:" + street + ",p:" + postal + ",c:" + city + ">";
    }
}
