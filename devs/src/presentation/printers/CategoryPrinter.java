package presentation.printers;

import logic.models.ProductCategory;
import presentation.Prompter;
import presentation.TableList;

import java.util.ArrayList;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class CategoryPrinter extends Prompter implements Printer {

    public int printMenu()
    {
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                    "@|green == Category management menu ==|@\n" +
                            "@|yellow 1|@ - Add A new category\n" +
                            "@|yellow 2|@ - Rename A category\n" +
                            "@|yellow 3|@ - Delete category\n" +
                            "@|yellow 4|@ - Display category details\n" +
                            "@|yellow 5|@ - Display all categories\n" +
                            "@|yellow 6|@ - RETURN TO MAIN MENU\n"));
            return menuSelection(1, 8);
        }
        return ILLEGAL;
    }

    public int promptViewByCategory() {
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                    "@|green == Product management menu ==|@\n" +
                            "@|yellow View products by category\n" +
                            "Choose how to display product category information: |@\n" +
                            "@|yellow 1|@ - Category ID\n" +
                            "@|yellow 2|@ - Searching by name\n" +
                            "@|yellow 3|@ - CANCEL\n"));
            return menuSelection(1, 3);
        }
        return ILLEGAL;
    }

    public int categoryIDPrompt() {
        boolean valid = false;
        int id;
        while(!valid) {
            System.out.print(ansi().render("@|red Enter (q\\Q) to cancel |@\n"));
            System.out.print(ansi().render("@|yellow Enter category ID number: |@\n"));
            try {
                String code = reader.readLine();
                if(code.trim().equals("q") || code.trim().equals("Q"))
                    return RETURN;
                else if((id = Integer.parseInt(code))> 0)
                    return id;
                else throw new IllegalArgumentException();
            } catch (Exception e) {
                printMessage(true, "Illegal category ID entered!\n" +
                        "Category ID must be a numeric value greater than 0.");
            }
        }
        return ILLEGAL;
    }

    public List<Integer> multipleCategoryIDPrompt() {
        boolean valid = false;
        List<Integer> categories = new ArrayList<>();
        int id;
        while(!valid) {
            if(categories.size() != 0) {
                System.out.print(ansi().render("@|yellow \nCategories ID inserted:\n|@"));
                for(Integer cat : categories)
                    System.out.print(ansi().render("@|white |ID: "+cat+"| |@"));
                System.out.print(ansi().render("\n"));
            }
            System.out.print(ansi().render("@|red Enter (q\\Q) to cancel.|@" +
                                            "\n@|cyan Enter (s\\S) to stop inserting categories. |@\n"));
            System.out.print(ansi().render("@|yellow Enter category ID number: |@\n"));
            try {
                String code = reader.readLine();
                if(code.trim().equals("q") || code.trim().equals("Q"))
                    return null;
                else if (code.trim().equals("s") || code.trim().equals("S"))
                    return categories;
                else if((id = Integer.parseInt(code))> 0)
                    categories.add(id);
                else throw new IllegalArgumentException();
            } catch (Exception e) {
                printMessage(true, "Illegal category ID entered!\n" +
                        "Category ID must be a numeric value greater than 0.");
            }
        }
        return null;
    }

    public String categoryPrompt(String additional) {
        String catName = null;
        boolean valid = false;
        try {
            while(!valid) {
                System.out.print(ansi().render("@|green Enter Q\\q to cancel operation.\n|@"));
                System.out.print(ansi().render("@|cyan Enter Category name"+additional+": |@\n"));
                catName = reader.readLine();
                if(!catName.equals("Q") && !catName.equals("q")) {
                    if (catName.length() == 0)
                        System.out.print(ansi().render("@|red Category name cannot be empty. |@\n"));
                    else return catName;
                } else {
                    catName = null;
                    valid = true;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return catName;
    }

    public void printCategoryDetails(String cat_name, int cat_id, String additional)
    {
        System.out.print(ansi().render("@|green \nShowing Category ID: "+cat_id+", name: '"+cat_name+"' "+additional+" |@"));
    }

    public void promptCategoryAdded(String catName) {
        System.out.print(ansi().render("@|green \nCategory |@ @|yellow '"+catName+"' |@ @|green added successfully! |@\n"));
    }

    public void printProductCategories(List<ProductCategory> categories, int barcode, boolean wait, String message, boolean error, boolean newScreen) {
        if(newScreen)
            printMessage(error, message);
        System.out.print(ansi().render("@|yellow \nProduct barcode "+barcode+" Categories:\n|@"));
        if(categories.size() == 0) {
            System.out.println(ansi().render("@|red No categories are assigned to product. |@"));
        } else {
            System.out.println(ansi().render("Showing @|cyan " + categories.size() + "|@ existing assigned categories."));
            TableList categoryTable = new TableList(3,
                    "Category ID", "Category Name", "Category Hierarchy")
                    .sortBy(2).withUnicode(false);
            categories.forEach(category -> categoryTable.addRow(category.getCategoryID()+"", category.getName(),
                    (category.getHierarchy() == 1) ? category.getHierarchy()+" (MAIN)" : category.getHierarchy()+""));

            categoryTable.print();
        }
        if(wait)
            scan.nextLine();
    }

    public void printCategories(List<ProductCategory> categories, boolean wait, String message, boolean error, boolean newScreen) {
        if(newScreen)
            printMessage(error, message);
        if(categories.size() == 0) {
            System.out.println(ansi().render("@|red No categories currently available |@"));
        } else {
            System.out.println(ansi().render("Showing @|cyan " + categories.size() + "|@ existing categories."));
            TableList categoryTable = new TableList(2,
                    "Category ID", "Category Name")
                    .sortBy(1).withUnicode(false);
            categories.forEach(category -> categoryTable.addRow(category.getCategoryID()+"", category.getName()));

            categoryTable.print();
        }
        if(wait)
            scan.nextLine();
    }
}
