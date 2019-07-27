package presentation.printers;

import logic.models.DefectiveProduct;
import logic.models.ProductCategory;
import logic.models.StockProducts;
import presentation.Prompter;
import presentation.TableList;

import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class ReportsPrinter extends Prompter implements Printer  {

    public int printMenu()
    {
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                        "@|green == Reports And Notifications ==|@\n" +
                            "@|yellow 1|@ - Show recent Notifications\n" +
                            "@|yellow 2|@ - Inventory report\n" +
                            "@|yellow 3|@ - Defected items report\n" +
                            "@|yellow 4|@ - RETURN TO MAIN MENU\n"));
            return menuSelection(1, 4);
        }
        return ILLEGAL;
    }

    public int chooseCategoryAll()
    {
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                            "@|yellow Choose how to display the inventory report: |@\n" +
                            "@|yellow 1|@ - Show All Inventory\n" +
                            "@|yellow 2|@ - Display By Categories\n" +
                            "@|yellow 3|@ - RETURN TO MAIN MENU\n"));
            return menuSelection(1, 3);
        }
        return ILLEGAL;
    }

    public void reportDefects(List<DefectiveProduct> defects, List<DefectiveProduct> defectsLocations) {
        printMessage(false, null);
        System.out.print(ansi().bold().render("@|magenta Defected items reports: |@\n"));
        System.out.print(ansi().bold().render("@|magenta ----------------------- |@\n"));
        if(defects.size() != 0) {
            int defects_amount = 0;
            for(DefectiveProduct p : defects)
                defects_amount += p.getQuantity();
            System.out.print(ansi().render("@|cyan \nCurrently found |@" + defects_amount+ " @|cyan defected products.\n|@"));
            TableList defectsTotalTable = new TableList(5,
                    "Product Barcode", "Product Name", "Defects Quantity", "Left In Stock", "Total Quantity")
                    .sortBy(0).withUnicode(false);
            defects.forEach(p -> {
                if (p.getQuantity() > 0)
                    defectsTotalTable.addRow(p.getBarcode() + "", p.getName(),
                            p.getQuantity() + " Units", p.getRemaining_quantity()+" Units", (p.getQuantity() + p.getRemaining_quantity()+" Units")); });
            defectsTotalTable.print();
        } else printMessage(true, "Did not find any defected products in stock currently.");
        System.out.print(ansi().render("@|cyan \nShowing the defected products by locations, amounts and reasons defected.\n|@"));
        if(defectsLocations.size() != 0) {
            TableList locationTable = new TableList(6,
                    "Product Barcode", "Product Name", "Location", "Quantity", "Date Reported", "Reason Defected")
                    .sortBy(0).withUnicode(false);
            defectsLocations.forEach(l -> {
                if (l.getQuantity() > 0)
                    locationTable.addRow(l.getBarcode() + "", l.getName(),
                                        l.getLocation(), l.getQuantity()+" Units", l.getDate_reported().toString(), l.getReason()); });
            locationTable.print();
        }
        System.out.print(ansi().bold().render("@|yellow \nPress ENTER to continue to menu. |@\n"));
        scan.nextLine();
    }


    public void printInventory(ProductCategory category, List<StockProducts> inv, boolean wait)
    {
        String location = (category != null) ? "category" : "inventory";
        int total_amount = 0;
        for(StockProducts p : inv)
            total_amount += p.getQuantity();
        if(category != null)
            System.out.print(ansi().bold().render("@|magenta \nPresenting details for category: |@ @|yellow name: '"+category.getName()+"', Category ID: "+category.getCategoryID()+".|@\n"));
        if(inv.size() != 0) {
            System.out.print(ansi().render("@|yellow \nNOTE: Expired products are not included.|@\n"));
            System.out.print(ansi().render("@|cyan Currently found |@" + total_amount + " @|cyan products available in "+location+".|@ \n" +
                                            "@|cyan The Current "+location +" includes |@" + inv.size() + " @|cyan different product barcodes.\n|@"));
            TableList criticalTable = new TableList(6,
                    "Product Barcode", "Product Name", "Store Quantity", "Warehouse Quantity", "Total Quantity", "Minimal Amount")
                    .sortBy(0).withUnicode(false);
            inv.forEach(i -> {
                if (i.getQuantity() > 0)
                    criticalTable.addRow(i.getBarcode() + "", i.getName(),
                            i.getStore_quantity()+"", i.getWarehouse_quantity()+" Units", i.getQuantity() + " Units", i.getMinimal_amount() + " Units"); });
            criticalTable.print();
        } else  System.out.print(ansi().render("@|red No inventory available at the moment.|@\n"));

        if(wait){
            System.out.print(ansi().bold().render("@|yellow \nPress ENTER to continue to menu. |@\n"));
            scan.nextLine();
        }
    }

    public void printCategoryNotExist(int id, boolean wait) {

        System.out.print(ansi().bold().render("\nERROR: @|red Category with ID: "+id+" does not exist in system.|@\n"));
        if(wait) {
            System.out.print(ansi().bold().render("@|yellow \nPress ENTER to continue to menu. |@\n"));
            scan.nextLine();
        }
    }
}
