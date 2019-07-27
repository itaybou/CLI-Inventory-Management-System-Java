package presentation.printers;

import logic.models.DefectiveProduct;
import logic.models.StockProducts;
import presentation.Prompter;
import presentation.TableList;

import java.util.List;
import java.util.Map;

import static org.fusesource.jansi.Ansi.ansi;

public class DefectsPrinter extends Prompter implements Printer {


    @Override
    public int printMenu() {
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                            "@|green == Defects management menu  ==|@\n" +
                            "@|yellow NOTE:|@ @|cyan Expired products will be moved to defective section automatically|@\n" +
                            "@|yellow 1|@ - Report Defective products \n" +
                            "@|yellow 2|@ - Remove product from defective section\n" +
                            "@|yellow 3|@ - Show All defective products\n" +
                            "@|yellow 4|@ - RETURN TO MAIN MENU\n"));
            return menuSelection(1, 4);
        }
        return ILLEGAL;
    }

    public void printAllStockByLocationAndID(List<Map.Entry<Integer,StockProducts>> inv, boolean wait) {
        int total_amount = 0;
        for(Map.Entry<Integer,StockProducts> p : inv)
            total_amount += p.getValue().getQuantity();
        if(inv.size() != 0) {
            System.out.print(ansi().render("@|yellow \nNOTE: Expired products are not included.|@\n"));
            System.out.print(ansi().render("@|cyan Currently found |@" + total_amount + " @|cyan products available in stock.|@ \n" +
                                            "@|cyan The Current inventory includes |@" + inv.size() + " @|cyan different product barcodes.\n|@"));
            TableList stockTable = new TableList(6,
                    "Identifier", "Product Barcode", "Product Name", "Location", "Quantity", "Expiration Date")
                    .sortBy(0).withUnicode(false);
            inv.forEach(l -> {
                if (l.getValue().getQuantity() > 0)
                    stockTable.addRow(l.getKey()+"", l.getValue().getBarcode() + "", l.getValue().getName(),
                            l.getValue().getLocation(), l.getValue().getQuantity()+" Units", l.getValue().getExpiration_date().toString()); });
            stockTable.print();
        }
        if(wait) {
            System.out.print(ansi().bold().render("@|yellow \nPress ENTER to continue to menu. |@\n"));
            scan.nextLine();
        }
    }

    public void printAllDefectiveByLocationAndID(List<Map.Entry<Integer, DefectiveProduct>> inv, boolean wait) {
        int total_amount = 0;
        for(Map.Entry<Integer,DefectiveProduct> p : inv)
            total_amount += p.getValue().getQuantity();
        if(inv.size() != 0) {
            System.out.print(ansi().render("@|cyan Currently found |@" + total_amount + " @|cyan defective products in defective section.|@ \n" +
                    "@|cyan The Current defective section includes |@" + inv.size() + " @|cyan different product barcodes.\n|@"));
            TableList stockTable = new TableList(7,
                    "Identifier", "Product Barcode", "Product Name", "Location", "Quantity", "Date Reported", "Reason")
                    .sortBy(0).withUnicode(false);
            inv.forEach(l -> {
                if (l.getValue().getQuantity() > 0)
                    stockTable.addRow(l.getKey()+"", l.getValue().getBarcode() + "", l.getValue().getName(),
                            l.getValue().getLocation(), l.getValue().getQuantity()+" Units", l.getValue().getDate_reported().toString(), l.getValue().getReason()); });
            stockTable.print();
        }
        if(wait) {
            System.out.print(ansi().bold().render("@|yellow \nPress ENTER to continue to menu. |@\n"));
            scan.nextLine();
        }
    }

    public int printAskIdentifier(int limit) {
        boolean valid = false;
        while(!valid) {
            System.out.print(ansi().render("@|green \nEnter (q\\Q) to cancel |@\n"));
            System.out.print(ansi().render("@|yellow Enter the Identifier of the products to report: |@\n"));
            try {
                String id = reader.readLine();
                if(!id.equals("q") && !id.equals("Q")) {
                    int ident = Integer.parseInt(id);
                    if(ident <= 0 || ident > limit)
                        System.out.print(ansi().render("@|yellow Invalid identifier entered.\nPlease check identifier exists in the above stock description. |@\n"));
                    else return ident;
                } else valid = true;
            } catch (Exception e) {
                System.out.println(ansi().render("@|red Illegal identifier entered.|@"));
            }
        }
        printMessage(false, null);
        return ILLEGAL;
    }


}
