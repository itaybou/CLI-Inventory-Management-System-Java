package presentation.printers;

import logic.models.Product;
import presentation.Prompter;
import presentation.TableList;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import static org.fusesource.jansi.Ansi.ansi;

public class ProductsPrinter extends Prompter implements Printer {

    public int printMenu()
    {
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                    "@|green == Product management menu ==|@\n" +
                            "@|yellow 1|@ - Insert new product\n" +
                            "@|yellow 2|@ - Update product details\n" +
                            "@|yellow 3|@ - Delete product\n" +
                            "@|yellow 4|@ - Display product details\n" +
                            "@|yellow 5|@ - Display all products\n" +
                            "@|yellow 6|@ - Display product categories\n" +
                            "@|yellow 7|@ - Display products by category\n" +
                            "@|yellow 8|@ - RETURN TO MAIN MENU\n"));
            return menuSelection(1, 8);
        }
        return ILLEGAL;
    }

    public String[] addProductDetailsPrompt() {
        int min_amount;
        double cost, sell;
        String curr_param = "";
        String[] details = new String[5];
        try {
            System.out.print(ansi().render("@|yellow Enter product name: |@\n"));
            details[0] = reader.readLine();
            System.out.print(ansi().render("@|yellow Enter product manufacturer: |@\n"));
            details[1] = reader.readLine();
            System.out.print(ansi().render("@|yellow Enter product cost price: |@\n"));
            curr_param = "Cost price";
            cost = Double.parseDouble(reader.readLine());
            details[2] = cost+"";
            System.out.print(ansi().render("@|yellow Enter product selling price: |@\n"));
            curr_param = "Selling price";
            sell = Double.parseDouble(reader.readLine());
            details[3] = sell+"";
            System.out.print(ansi().render("@|yellow Enter product minimal amount: |@\n"));
            curr_param = "Minimum amount";
            min_amount = Integer.parseInt(reader.readLine());
            details[4] = min_amount+"";
        } catch (Exception e) {
            return new String[]{curr_param};
        }
        return details;
    }

    public String addProductMainCategory() {
        String catName = null;
        boolean valid = false;
        try {
            while(!valid) {
                System.out.print(ansi().render("@|green \nDo you wish to assign categories to product? Y\\N : |@"));
                String ans = reader.readLine();
                if ((ans.equals("Y") || ans.equals("y"))) {
                    System.out.print(ansi().render("@|cyan Enter product Main Category name|@ @|yellow (hierarchy 1): |@\n"));
                    catName = reader.readLine();
                    if (catName.length() == 0)
                        System.out.print(ansi().render("@|red Category name cannot be empty. |@\n"));
                    else valid = true;
                } else if ((ans.equals("N") || ans.equals("n"))) {
                    valid = true;
                    return "";
                } else System.out.println(ansi().render("@|red Illegal choice, please choose Y\\N |@\n"));
            }
        } catch (Exception e) {
            return null;
        }
        return catName;
    }

    public boolean checkAddProductsSubCategory()
    {
        String ans = null;
        boolean valid = false;
        System.out.print(ansi().render("@|green \nNo you wish to assign sub-categories to product? Y\\N : |@"));
        try {
            while(!valid) {
                ans = reader.readLine();
                if ((ans.equals("Y") || ans.equals("y")))
                    return true;
                else if ((ans.equals("N") || ans.equals("n")))
                    return false;
                else System.out.println(ansi().render("@|red Illegal choice, please choose Y\\N |@\n"));
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public String addProductSubCategories(int hierarchy) {
        String catName = null;
        boolean valid = false;
        try {
            while(!valid) {
                System.out.print(ansi().render("@|yellow \nEnter q\\Q to stop assigning categories. |@\n"));
                System.out.print(ansi().render("@|cyan Enter product Sub-Category name |@ @|yellow (hierarchy "+hierarchy+"): |@\n"));
                catName = reader.readLine();
                if (catName.length() == 0)
                    System.out.println(ansi().render("@|red Category name cannot be empty. |@\n"));
                else valid = true;
            }
        } catch (Exception e) {
            return null;
        }
        return catName;
    }

    public boolean promptCategoryNotExist(String catName) {
        boolean valid = false;
        try {
            while(!valid) {
                System.out.print(ansi().render("@|red \nCategory |@ @|yellow '"+catName +"' |@ @|red does not exist. \n"+
                        "Do you wish to create it? |@ @|white Y\\N : |@"));
                String ans = reader.readLine();
                if((ans.equals("Y") || ans.equals("y")))
                    return true;
                else if ((ans.equals("N") || ans.equals("n")))
                    return false;
                else System.out.println(ansi().render("@|red Illegal choice, please choose Y\\N |@"));
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public Map.Entry<Integer, String> printUpdateSelectProduct()
    {
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                    "@|yellow Choose a field to edit:|@\n" +
                            "@|yellow 1|@ - Product Barcode\n" +
                            "@|yellow 2|@ - Product Name\n" +
                            "@|yellow 3|@ - Product Manufacturer\n" +
                            "@|yellow 4|@ - Product Cost Price\n" +
                            "@|yellow 5|@ - Product Selling Price\n" +
                            "@|yellow 6|@ - Product Minimal Amount\n" +
                            "@|yellow 7|@ - Edit Product Categories\n" +
                            "@|yellow 8|@ - RETURN TO MAIN MENU\n"));
            int choice = menuSelection(1, 8);
            if (choice != 0) {
                String error = "";
                System.out.print(ansi().render("@|yellow Enter A new value: |@\n"));
                try {
                    String value = (choice != 8) ? (choice != 7) ? reader.readLine() : null : "";
                    switch (choice) {
                        case 1:
                            error = "Barcode must be an integer value.";
                            Integer.parseInt(value);
                            break;
                        case 4:
                            error = "Cost price must be a real number value.";
                            Double.parseDouble(value);
                            break;
                        case 5:
                            error = "Selling price must be a real number value.";
                            Double.parseDouble(value);
                            break;
                        case 6:
                            error = "Minimal amount must be an integer value.";
                            Integer.parseInt(value);
                            break;
                    }
                    return new AbstractMap.SimpleImmutableEntry<>(choice, value);
                } catch (Exception e) {
                    printMessage(true, error);
                }
            }
            return null;
        }
        return null;
    }

    public void printProducts(List<Product> products, boolean wait, String message, boolean error, boolean newScreen) {
        if(newScreen)
            printMessage(error, message);
        if(products.size() == 0) {
            System.out.println(ansi().render("@|red No products available to show. |@"));
        } else {
            System.out.println(ansi().render("Showing @|cyan " + products.size() + "|@ existing products.\n"));
            TableList productTable = new TableList(6,
                    "Barcode", "Product Name", "Manufacturer", "Current Cost Price", "Current Selling Price", "Minimal Amount")
                    .sortBy(1).withUnicode(false);
            products.forEach(product -> productTable.addRow(
                    product.getBarcode() + "", product.getName(), product.getManufacturer(),
                    product.getCost_price() + " $", product.getSelling_price() + " $", product.getMinimal_amount() + " Units"));

            productTable.print();
        }
        if(wait)
            scan.nextLine();
    }
}
