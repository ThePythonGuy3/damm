package bomboclot;

import bomboclot.input.IReader;
import bomboclot.input.XLSXReader;
import bomboclot.view.CargoViewer;

public class Main
{
    private static IReader reader;

    public static void main(String[] args)
    {
        reader = new XLSXReader();

        //reader.load();

        CargoViewer.main(args);
    }
}
