package bomboclot.algorithm.packer;

import bomboclot.input.Dimensions;
import bomboclot.algorithm.model.Pallet;
import bomboclot.algorithm.model.Prism;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DFFPacker
{
    private final Dimensions pallet_dimensions;

    public DFFPacker(
            Dimensions pallet_dimensions)
    {
        this.pallet_dimensions =
                pallet_dimensions;
    }

    public List<Pallet> pack(
            List<Prism> prisms)
    {
        /*
         * Copy list
         */
        List<Prism> sorted_prisms =
                new ArrayList<>(prisms);

        /*
         * Sort descending by volume
         */
        sorted_prisms.sort(
                Comparator.comparingDouble(
                        Prism::get_volume
                ).reversed()
        );

        List<Pallet> pallets =
                new ArrayList<>();

        /*
         * First-fit
         */
        for (Prism prism : sorted_prisms)
        {
            boolean placed = false;

            for (Pallet pallet : pallets)
            {
                if (pallet.place(prism))
                {
                    placed = true;
                    break;
                }
            }

            /*
             * No pallet fits:
             * create new pallet
             */
            if (!placed)
            {
                Pallet pallet =
                        new Pallet(
                                pallet_dimensions
                        );

                boolean success =
                        pallet.place(prism);

                if (!success)
                {
                    throw new IllegalArgumentException(
                            "Prism too large for pallet: "
                                    + prism
                    );
                }

                pallets.add(pallet);
            }
        }

        return pallets;
    }
}