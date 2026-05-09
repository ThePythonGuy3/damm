package bomboclot.input;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class XLSXReader implements IReader
{
    private static final String PRODUCT_FILE_LOCATION = "/products.xlsx";
    private static final String GENERAL_FILE_LOCATION = "/general.xlsx";

    private final HashMap<String, Product>  products  = new HashMap<>();
    private final HashMap<String, Costumer> costumers = new HashMap<>();
    private HashMap<String, List<Delivery>> deliveries_by_route;
    private HashMap<String, TimeWindow>     windows;
    private HashSet<String>                 closed_days;

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

    private void load_costumers()
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
    public Costumer get_costumer(String identifier)
    {
        return costumers.get(identifier);
    }

    /**
     * Returns the deliveries for a given (date, route_code) pair, grouping rows of
     * the Detalle entrega sheet by Entrega so that callers iterate over client
     * stops rather than product lines.
     */
    @Override
    public List<Delivery> get_deliveries(String date_dd_mm_yyyy, String route_code)
    {
        if (deliveries_by_route == null)
        {
            deliveries_by_route = new HashMap<>();
            try (InputStream general_file_stream = getClass().getResourceAsStream(GENERAL_FILE_LOCATION))
            {
                assert general_file_stream != null;

                Workbook workbook = new XSSFWorkbook(general_file_stream);

                Sheet sheet = workbook.getSheet("Detalle entrega");

                int date_column = -1, route_column = -1, entrega_column = -1, customer_column = -1, material_column = -1, quantity_column = -1, unit_column = -1;
                int i = 0;
                for (Cell cell : sheet.getRow(0))
                {
                    String name = cell.getStringCellValue().toLowerCase().strip();

                    switch (name)
                    {
                        case "fecha"              -> date_column     = i;
                        case "ruta"               -> route_column    = i;
                        case "entrega"            -> entrega_column  = i;
                        case "destinatario mcía." -> customer_column = i;
                        case "material"           -> material_column = i;
                        case "cantidad entrega"   -> quantity_column = i;
                        case "un.medida venta"    -> unit_column     = i;
                    }

                    i++;
                }

                HashMap<String, HashMap<String, ArrayList<DeliveryLine>>> staged_lines     = new HashMap<>();
                HashMap<String, HashMap<String, String>>                  staged_customers = new HashMap<>();

                for (i = 1; i < sheet.getPhysicalNumberOfRows(); i++)
                {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    Cell date_cell = row.getCell(date_column);
                    String date = "";
                    if (date_cell != null)
                    {
                        if (date_cell.getCellType() == CellType.STRING) date = date_cell.getStringCellValue().strip();
                        else if (date_cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(date_cell))
                            date = new SimpleDateFormat("dd/MM/yyyy").format(date_cell.getDateCellValue());
                    }

                    Cell route_cell = row.getCell(route_column);
                    String route = (route_cell == null) ? "" : route_cell.getStringCellValue().strip();

                    Cell entrega_cell = row.getCell(entrega_column);
                    String entrega = "";
                    if (entrega_cell != null)
                    {
                        if (entrega_cell.getCellType() == CellType.STRING) entrega = entrega_cell.getStringCellValue().strip();
                        else if (entrega_cell.getCellType() == CellType.NUMERIC) entrega = String.valueOf((long) entrega_cell.getNumericCellValue());
                    }

                    Cell customer_cell = row.getCell(customer_column);
                    String customer = "";
                    if (customer_cell != null)
                    {
                        if (customer_cell.getCellType() == CellType.STRING) customer = customer_cell.getStringCellValue().strip();
                        else if (customer_cell.getCellType() == CellType.NUMERIC) customer = String.valueOf((long) customer_cell.getNumericCellValue());
                    }

                    String material = row.getCell(material_column).getStringCellValue();
                    String unit     = row.getCell(unit_column).getStringCellValue();
                    double quantity = row.getCell(quantity_column).getNumericCellValue();

                    String route_key = date + "|" + route;
                    staged_lines.computeIfAbsent(route_key, k -> new HashMap<>())
                                .computeIfAbsent(entrega,   k -> new ArrayList<>())
                                .add(new DeliveryLine(material, unit, quantity));
                    staged_customers.computeIfAbsent(route_key, k -> new HashMap<>())
                                    .put(entrega, customer);
                }

                for (var route_entry : staged_lines.entrySet())
                {
                    String route_key = route_entry.getKey();
                    ArrayList<Delivery> list = new ArrayList<>();
                    for (var entrega_entry : route_entry.getValue().entrySet())
                    {
                        String entrega_id  = entrega_entry.getKey();
                        String customer_id = staged_customers.get(route_key).get(entrega_id);
                        list.add(new Delivery(entrega_id, customer_id, entrega_entry.getValue()));
                    }
                    deliveries_by_route.put(route_key, list);
                }
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
            }
        }
        return deliveries_by_route.getOrDefault(date_dd_mm_yyyy + "|" + route_code, List.of());
    }

    /**
     * Returns the time window for a customer on a given weekday. Returns null when
     * the schedule says closed that day; returns TimeWindow.anyTime() when no
     * schedule row exists at all.
     */
    @Override
    public TimeWindow get_window(String customer_identifier, int weekday)
    {
        if (windows == null)
        {
            windows = new HashMap<>();
            closed_days = new HashSet<>();
            try (InputStream schedule_file_stream = getClass().getResourceAsStream("/horarios.xlsx"))
            {
                if (schedule_file_stream != null)
                {
                    Workbook workbook = new XSSFWorkbook(schedule_file_stream);

                    Sheet sheet = workbook.getSheetAt(0);

                    int customer_column = -1, weekday_column = -1, open_column = -1, close_column = -1, closed_column = -1;
                    int i = 0;
                    for (Cell cell : sheet.getRow(0))
                    {
                        String name = cell.getStringCellValue().toLowerCase().strip();

                        switch (name)
                        {
                            case "deudor"            -> customer_column = i;
                            case "día semana"        -> weekday_column  = i;
                            case "horario inicia a"  -> open_column     = i;
                            case "horario termina a" -> close_column    = i;
                            case "cierre si/no"      -> closed_column   = i;
                        }

                        i++;
                    }

                    for (i = 1; i < sheet.getPhysicalNumberOfRows(); i++)
                    {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        Cell customer_cell = row.getCell(customer_column);
                        String customer = "";
                        if (customer_cell != null)
                        {
                            if (customer_cell.getCellType() == CellType.STRING) customer = customer_cell.getStringCellValue().strip();
                            else if (customer_cell.getCellType() == CellType.NUMERIC) customer = String.valueOf((long) customer_cell.getNumericCellValue());
                        }

                        Cell weekday_cell = row.getCell(weekday_column);
                        if (weekday_cell == null) continue;
                        int wd;
                        try
                        {
                            if (weekday_cell.getCellType() == CellType.NUMERIC) wd = (int) weekday_cell.getNumericCellValue();
                            else wd = Integer.parseInt(weekday_cell.getStringCellValue().strip());
                        }
                        catch (NumberFormatException e) { continue; }

                        Cell open_cell = row.getCell(open_column);
                        int open_min = 0;
                        if (open_cell != null && open_cell.getCellType() == CellType.NUMERIC)
                        {
                            double frac = open_cell.getNumericCellValue();
                            frac = frac - Math.floor(frac);
                            open_min = (int) Math.round(frac * 1440);
                        }

                        Cell close_cell = row.getCell(close_column);
                        int close_min = 0;
                        if (close_cell != null && close_cell.getCellType() == CellType.NUMERIC)
                        {
                            double frac = close_cell.getNumericCellValue();
                            frac = frac - Math.floor(frac);
                            close_min = (int) Math.round(frac * 1440);
                        }

                        String closed_flag = "";
                        Cell closed_cell = row.getCell(closed_column);
                        if (closed_cell != null && closed_cell.getCellType() == CellType.STRING)
                            closed_flag = closed_cell.getStringCellValue().strip();

                        String key = customer + "|" + wd;
                        if ("X".equalsIgnoreCase(closed_flag) || (open_min == 0 && close_min == 0))
                            closed_days.add(key);
                        else
                            windows.put(key, new TimeWindow(open_min, close_min));
                    }
                }
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
            }
        }
        String key = customer_identifier + "|" + weekday;
        if (closed_days.contains(key)) return null;
        return windows.getOrDefault(key, TimeWindow.anyTime());
    }
}
