package bomboclot.input;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.HashMap;
import java.util.Locale;

public class XLSXReader implements IReader
{
    private static final String PRODUCT_FILE_LOCATION  = "/products.xlsx";
    private static final String GENERAL_FILE_LOCATION  = "/general.xlsx";

    private final HashMap<String, Product>  products  = new HashMap<>();
    private final HashMap<String, Costumer> costumers = new HashMap<>();

    private static double convert(double size, String multiplier)
    {
        switch (multiplier)
        {
            case "cm" -> { return size / 100.0;  }
            case "mm" -> { return size / 1000.0; }
            default   -> { return size;          }
        }
    }

    private void load_products()
    {
        try (InputStream product_file_stream = getClass().getResourceAsStream(PRODUCT_FILE_LOCATION))
        {
            assert product_file_stream != null;

            Workbook workbook = new XSSFWorkbook(product_file_stream);

            Sheet sheet = workbook.getSheet("Sheet1");

            int material_column = -1, unit_column = -1, count_column = -1, denom_column = -1, length_column = -1, width_column = -1, height_column = -1;
            int i = 0;
            for (Cell cell : sheet.getRow(0))
            {
                String name = cell.getStringCellValue().toLowerCase().strip();

                switch (name)
                {
                    case "material" -> material_column = i;
                    case "uma"      -> unit_column     = i;
                    case "contador" -> count_column    = i;
                    case "denom."   -> denom_column    = i;
                    case "longitud" -> length_column   = i;
                    case "ancho"    -> width_column    = i;
                    case "altura"   -> height_column   = i;
                }

                i++;
            }

            for (i = 1; i < sheet.getPhysicalNumberOfRows(); i++)
            {
                Row row = sheet.getRow(i);

                String name   = row.getCell(material_column).getStringCellValue();
                String unit   = row.getCell(unit_column).getStringCellValue();
                double count  = row.getCell(count_column).getNumericCellValue();
                double denom  = row.getCell(denom_column).getNumericCellValue();
                double length = row.getCell(length_column).getNumericCellValue();
                double width  = row.getCell(width_column).getNumericCellValue();
                double height = row.getCell(height_column).getNumericCellValue();

                String length_multiplier = row.getCell(length_column + 1).getStringCellValue().toLowerCase().strip();
                String width_multiplier  = row.getCell(width_column + 1).getStringCellValue().toLowerCase().strip();
                String height_multiplier = row.getCell(height_column + 1).getStringCellValue().toLowerCase().strip();

                if (!products.containsKey(name))
                    products.put(name, new Product(name));

                products.get(name).add_unit(unit, denom / count,
                    new Dimensions(
                        convert(length, length_multiplier),
                        convert(width, width_multiplier),
                        convert(height, height_multiplier)
                    )
                );
            }
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public void load_costumers()
    {
        try (InputStream general_file_stream = getClass().getResourceAsStream(GENERAL_FILE_LOCATION))
        {
            assert general_file_stream != null;

            Workbook workbook = new XSSFWorkbook(general_file_stream);

            Sheet sheet = workbook.getSheet("Direcciones");

            int identifier_column = -1, name_column = -1, name2_column = -1, street_column = -1, postal_column = -1, city_column = -1;
            int i = 0;
            for (Cell cell : sheet.getRow(0))
            {
                String name = cell.getStringCellValue().toLowerCase().strip();

                switch (name)
                {
                    case "cliente"   -> identifier_column = i;
                    case "nombre 1"  -> name_column       = i;
                    case "nombre 2"  -> name2_column      = i;
                    case "calle"     -> street_column     = i;
                    case "cp"        -> postal_column     = i;
                    case "población" -> city_column       = i;
                }

                i++;
            }

            for (i = 1; i < sheet.getPhysicalNumberOfRows(); i++)
            {
                Row row = sheet.getRow(i);

                String identifier = row.getCell(identifier_column).getStringCellValue().strip();
                String name   = row.getCell(name_column).getStringCellValue().strip();
                String name2  = row.getCell(name2_column).getStringCellValue().strip();
                String street = row.getCell(street_column).getStringCellValue().toLowerCase().strip();
                String postal = row.getCell(postal_column).getStringCellValue().toLowerCase().strip();
                String city   = row.getCell(city_column).getStringCellValue().toLowerCase().strip();

                costumers.put(identifier, new Costumer(identifier, name, name2, new Address(street, postal, city)));
            }
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public void load()
    {
        load_costumers();
        load_products();
    }

    @Override
    public Product get_product(String name)
    {
        return products.get(name);
    }

    @Override
    public Costumer get_costumer(int identifier)
    {
        return costumers.get(identifier);
    }
}
