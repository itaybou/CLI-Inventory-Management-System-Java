package presentation.printers;

import logic.models.Discount;
import logic.models.ProductCategory;
import presentation.Prompter;
import presentation.TableList;

import java.util.List;
import java.util.Map;

import static org.fusesource.jansi.Ansi.ansi;

public class DiscountsPrinter extends Prompter implements Printer {

    public int printMenu()
    {
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                    "@|green == Discounts management menu ==|@\n" +
                            "@|yellow 1|@ - Add new discount\n" +
                            "@|yellow 2|@ - End active discount\n" +
                            "@|yellow 3|@ - Display discounts\n" +
                            "@|yellow 4|@ - Display all discounts\n" +
                            "@|yellow 5|@ - Display discounted categories.\n" +
                            "@|yellow 6|@ - RETURN TO MAIN MENU\n"));
            return menuSelection(1, 6);
        }
        return ILLEGAL;
    }

    public int printMenu(String discounter)
    {
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                    "@|green == Discounts management menu ==|@\n" +
                            "@|yellow 1|@ - Add new discount\n" +
                            "@|yellow 2|@ - End active discount\n" +
                            "@|yellow 3|@ - Display " + discounter + " discounts\n" +
                            "@|yellow 4|@ - Display all " + discounter + " discounts\n" +
                            "@|yellow 5|@ - Display discounted categories.\n" +
                            "@|yellow 6|@ - RETURN TO MAIN MENU\n"));
            return menuSelection(1, 6);
        }
        return ILLEGAL;
    }

    public int printManageDiscountsSelect()
    {
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                    "@|green == Discounts management menu ==|@\n" +
                            "@|yellow Choose the discounter: (Supplier\\Store)|@\n" +
                            "@|yellow Supplier discounts cost price, Store discounts selling price|@\n" +
                            "@|yellow 1|@ - Store\n" +
                            "@|yellow 2|@ - Supplier\n" +
                            "@|yellow 3|@ - RETURN TO MAIN MENU\n"));
            return menuSelection(1, 3);
        }
        return ILLEGAL;
    }

    public int byProductCategoryDiscountPrompt(String prod_descriptor, String title) {
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                    "@|green == Discounts management menu ==|@\n" +
                            "@|yellow Choose a method to "+title+": |@\n" +
                            "@|yellow 1|@ - "+prod_descriptor+"\n" +
                            "@|yellow 2|@ - Category name\n" +
                            "@|yellow 3|@ - CANCEL\n"));
            return menuSelection(1, 3);
        }
        return ILLEGAL;
    }

    public Double addDiscountDetailsPrompt() {
        double percentage;
        try {
            System.out.print(ansi().render("@|yellow Enter discount percentage: |@\n"));
            percentage = Double.parseDouble(reader.readLine());
            return percentage;
        } catch (Exception e) {
            return null;
        }
    }

    public int promptDiscountToRemove(List<Discount> discounts) {
        boolean valid = false;
        int id;
        while (!valid) {
            System.out.print(ansi().render("@|red Enter (q\\Q) to cancel |@\n"));
            System.out.print(ansi().render("@|yellow Choose a discount ID to cancel: |@\n"));
            try {
                String code = reader.readLine();
                if (code.trim().equals("q") || code.trim().equals("Q"))
                    return RETURN;
                else if ((id = Integer.parseInt(code)) > 0) {
                    for (Discount d : discounts)
                        if (d.getDiscountID() == id)
                            return id;
                    return ILLEGAL;
                } else throw new IllegalArgumentException();
            } catch (Exception e) {
                printMessage(true, "Illegal Discount ID entered!");
                return  ILLEGAL;
            }
        }
        return ILLEGAL;
    }

    public boolean activeDiscountPrompt() {
        printMessage(false, null);
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                    "@|yellow Choose discounts to display:\n|@" +
                            "@|yellow 1|@ - View all discounts \n" +
                            "@|yellow 2|@ - View only active discounts\n"));
            switch (menuSelection(1, 2)) {
                case 1:
                    return false;
                case 2:
                    return true;
            }
        }
        return false;
    }

    public void printDiscounts(List<Discount> discounts, boolean wait, String discounter, String message, boolean error) {
        printMessage(error, message);
        if(discounts.size() == 0) {
            System.out.println(ansi().render("@|red No discounts by " + discounter +" available to show. |@"));
        } else {
            System.out.println(ansi().render("Showing @|cyan " + discounts.size() + "|@ existing " + discounter + " discounts.\n"));
            TableList discountTable = new TableList(6,
                    "Discount ID", "Discounter", "Discounted Barcode", "Discount Percentage", "Date Started YYYY-MM-DD", "Date Ended YYYY-MM-DD")
                    .sortBy(1).withUnicode(false);
            discounts.forEach(discount -> discountTable.addRow(
                    discount.getDiscountID()+ "", discount.getDiscounter(), discount.getBarcode() + "",
                    discount.getPercentage() + "%", discount.getDate_given() + "",
                    discount.getDate_ended() == null ? "Still Active" : discount.getDate_ended() + ""));

            discountTable.print();
        }
        if(wait)
            scan.nextLine();
    }

    public void printDiscountsWithNames(List<Map.Entry<String, Discount>> discounts, String discounter, boolean wait, String message, boolean error) {
        printMessage(error, message);
        if(discounts.size() == 0) {
            System.out.println(ansi().render("@|red No discounts by " + discounter +" available to show. |@"));
        } else {
            System.out.println(ansi().render("Showing @|cyan " + discounts.size() + "|@ existing " + discounter + " discounts.\n"));
            TableList discountTable = new TableList(7,
                    "Discount ID", "Product Name", "Discounter", "Discounted Barcode", "Discount Percentage", "Date Started YYYY-MM-DD", "Date Ended YYYY-MM-DD")
                    .sortBy(1).withUnicode(false);
            discounts.forEach(discount -> discountTable.addRow(
                    discount.getValue().getDiscountID()+ "", discount.getKey(), discount.getValue().getDiscounter(), discount.getValue().getBarcode() + "",
                    discount.getValue().getPercentage() + "%", discount.getValue().getDate_given() + "",
                    discount.getValue().getDate_ended() == null ? "Still Active" : discount.getValue().getDate_ended() + ""));

            discountTable.print();
        }
        if(wait)
            scan.nextLine();
    }

    public void printDiscountedCategories(List<ProductCategory> categories, boolean wait, String message, boolean error, boolean newScreen) {
        if(newScreen)
            printMessage(error, message);
        if(categories.size() == 0) {
            System.out.println(ansi().render("@|red No category discounts currently available |@"));
        } else {
            System.out.println(ansi().render("Showing @|cyan " + categories.size() + "|@ existing discounted categories."));
            TableList categoryTable = new TableList(4,
                    "Category ID", "Category Name", "Discounter", "Discount Percentage")
                    .sortBy(2).withUnicode(false);
            categories.forEach(category -> categoryTable.addRow(category.getCategoryID()+"", category.getName(), category.getDiscounter(), category.getDiscounted()+"%"));

            categoryTable.print();
        }
        if(wait)
            scan.nextLine();
    }
}
