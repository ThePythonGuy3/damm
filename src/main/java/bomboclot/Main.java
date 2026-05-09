package bomboclot;

import bomboclot.algorithm.model.Position;
import bomboclot.algorithm.model.Prism;
import bomboclot.input.Dimensions;
import bomboclot.input.IReader;
import bomboclot.input.XLSXReader;
import bomboclot.view.CargoViewer;

import java.util.ArrayList;
import java.util.List;

public class Main
{
    private static IReader reader;

    public static void main(String[] args)
    {
        reader = new XLSXReader();

        //reader.load();

        ArrayList<CargoViewer.PlacedPrism> prisms = new ArrayList<>(List.of(
            new CargoViewer.PlacedPrism(
                new Prism(
                    "A",
                    new Dimensions(100, 200, 50)
                ),
                new Position(0, 0, 0)
            ),

            new CargoViewer.PlacedPrism(
                new Prism(
                    "B",
                    new Dimensions(80, 80, 80)
                ),
                new Position(150, 0, 0)
            ),

            new CargoViewer.PlacedPrism(
                new Prism(
                    "C",
                    new Dimensions(120, 60, 140)
                ),
                new Position(-150, 0, -100)
            )
        ));

        CargoViewer.set(prisms);

        CargoViewer.main(args);
    }
}
