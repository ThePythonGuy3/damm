package bomboclot;

import bomboclot.input.IReader;
import bomboclot.input.XLSXReader;

public class Main
{
    private static IReader reader;

    public static void main()
    {
        reader = new XLSXReader();

        reader.load();
    }
}
