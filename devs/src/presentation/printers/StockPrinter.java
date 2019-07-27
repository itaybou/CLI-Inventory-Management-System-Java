package presentation.printers;

import logic.datatypes.Date;
import logic.models.Location;
import logic.models.StockProducts;
import logic.models.StoreBranch;
import presentation.Prompter;
import presentation.TableList;

import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class StockPrinter extends Prompter implements Printer {

    public int printMenu()
    {
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                    "@|yellow PAY ATTENTION:|@ Expired products will not be displayed in stock.\n" +
                            "All expired products can be found in defects menu section.\n"));
            System.out.println(ansi().bold().render(
                            "@|green == Branch Stock management menu ==|@\n" +
                            "@|yellow 1|@ - Add products to stock\n" +
                            "@|yellow 2|@ - Remove products from stock\n" +
                            "@|yellow 3|@ - Move product stock location\n" +
                            "@|yellow 4|@ - Display specific product stock\n" +
                            "@|yellow 5|@ - Display all products in stock\n" +
                            "@|yellow 6|@ - Display products by category\n" +
                            "@|yellow 7|@ - RETURN TO MAIN MENU\n"));
            return menuSelection(1, 7);
        }
        return ILLEGAL;
    }

    public boolean promptStockDispalyByLocation() {
        printMessage(false, null);
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                    "@|yellow Choose how to display stock:\n|@" +
                            "@|yellow 1|@ - Display separated by locations \n" +
                            "@|yellow 2|@ - Display in all locations\n"));
            switch (menuSelection(1, 2)) {
                case 1:
                    return true;
                case 2:
                    return false;
            }
        }
        return false;
    }

    public Date promptExpirationDate() {
        boolean valid = false;
        while(!valid) {
            System.out.print(ansi().render("@|green \nEnter (q\\Q) to cancel |@\n"));
            System.out.print(ansi().render("@|cyan Expiration date format:|@ @|yellow yyyy-mm-dd (year-month-day) |@\n"));
            System.out.print(ansi().render("Enter products @|yellow Expiration date: |@\n"));
            try {
                String expiration_date = reader.readLine();
                if(!expiration_date.equals("q") && !expiration_date.equals("Q")) {
                    Date d = Date.parseDate(expiration_date);
                    if (!Date.checkLegalDate(d)) {
                        System.out.println(ansi().render("@|red Illegal date entered, please enter a legal date.\nNOTE: Year must be at least current year.|@\n"));
                    } else return d;
                } else valid = true;
            } catch (Exception e) {
                System.out.println(ansi().render("@|red Illegal date entered, please enter a legal date.\nNOTE: Year must be at least current year.|@"));
            }
        }
        printMessage(false, null);
        return null;
    }

    public int promptQuantity(String adj) {
        boolean valid = false;
        while(!valid) {
            System.out.print(ansi().render("@|green Enter (q\\Q) to cancel |@\n"));
            System.out.print(ansi().render("Enter products @|yellow Quantity to "+adj+": |@\n"));
            try {
                String quantity = reader.readLine();
                if(!quantity.equals("q") && !quantity.equals("Q")) {
                    int amount = Integer.parseInt(quantity);
                    if (amount > 0)
                        return amount;
                    else System.out.println(ansi().render("@|red Quantity must be an integer value larger than 0.|@\n"));
                }
                else valid = true;
            } catch (Exception e) {
                System.out.println(ansi().render("@|red Quantity must be an integer value larger than 0.|@\n"));
            }
        }
        printMessage(false, null);
        return ILLEGAL;
    }

    public boolean promptDeletion() {
        boolean valid = false;
        while(!valid) {
            System.out.print(ansi().bold().render("@|red The above products wiil be removed.\n" +
                                                "Are you sure you want to proceed? Y\\N  |@\n"));
            try {
                String choice = reader.readLine();
                if(choice.trim().equals("Y") || choice.trim().equals("y"))
                    return true;
                else if(choice.trim().equals("N") || choice.trim().equals("n"))
                    return false;
                else throw new IllegalArgumentException();
            } catch (Exception e) {
                System.out.println(ansi().render("@|red Illegal choice, please choose Y\\N.|@\n"));
            }
        }
        return false;
    }


    public Location promptLocation(StoreBranch activeBranch) {
        boolean valid = false;
        while(!valid) {
            System.out.print(ansi().render("@|green \nEnter (q\\Q) to cancel |@\n"));
            System.out.print(ansi().bold().render("@|cyan Enter location to add products to: |@\n"));
            try {
                System.out.print(ansi().render("@|yellow Enter location in store.|@ @|white \nW\\w for warehouse shelves, S\\s for store shelves: |@\n"));
                String physical_place = reader.readLine();
                if(!physical_place.equals("q") && !physical_place.equals("Q")) {
                    if(physical_place.equals("s") || physical_place.equals("S") || physical_place.equals("W") || physical_place.equals("w")) {
                        String place = (physical_place.equals("s") || physical_place.equals("S")) ? "Store" : "Warehouse";
                        System.out.print(ansi().render("@|yellow Enter location shelf description: (Ex. A5) |@\n"));
                        String description = reader.readLine();
                        if(!description.equals("q") && !description.equals("Q")) {
                            if(description.equals(""))
                                System.out.println(ansi().render("@|red ERROR: Shelf description cannot be empty.|@"));
                            else return new Location(activeBranch.getBranchID(), place, description);
                        } else valid = true;
                    } else System.out.println(ansi().render("@|red ERROR: Invalid location entered.\nPlease enter W\\w for warehouse shelves OR S\\s for store shelves.|@"));
                } else valid = true;
            } catch (Exception e) {
                printMessage(true, "Illegal location description entered!\n");
            }
        }
        printMessage(false, null);
        return null;
    }

    public Location movePrompt(StoreBranch activeBranch, String adj)
    {
        boolean valid = false;
        while(!valid) {
            System.out.print(ansi().render("@|green \nEnter (q\\Q) to cancel |@\n"));
            System.out.print(ansi().bold().render("@|cyan Enter location to "+adj+": |@\n"));
            try {
                System.out.print(ansi().render("@|yellow Enter location in store.|@ @|white \nW\\w for warehouse shelves, S\\s for store shelves: |@\n"));
                String physical_place = reader.readLine();
                if (!physical_place.equals("q") && !physical_place.equals("Q")) {
                    if (physical_place.equals("s") || physical_place.equals("S") || physical_place.equals("W") || physical_place.equals("w")) {
                        String place = (physical_place.equals("s") || physical_place.equals("S")) ? "Store" : "Warehouse";
                        System.out.print(ansi().render("@|yellow Enter location shelf description: (Ex. A5) |@\n"));
                        String description = reader.readLine();
                        if(!description.equals("q") && !description.equals("Q")) {
                            if (description.equals(""))
                                System.out.println(ansi().render("@|red ERROR: Shelf description cannot be empty.|@"));
                            else return new Location(activeBranch.getBranchID(), place, description);
                        } else valid = true;
                    } else System.out.println(ansi().render("@|red ERROR: Invalid location entered.\nPlease enter W\\w for warehouse shelves OR S\\s for store shelves.|@"));
                } else valid = true;
            } catch (Exception e) {
                printMessage(true, "Illegal location description entered!\n");
            }
        }
        return null;
    }

    public void printStockAmounts(List<StockProducts> products) {
        int amount = 0;
        for(StockProducts prod : products)
            amount += prod.getQuantity();
        System.out.print(ansi().render("@|yellow \nFound |@" + amount + " @|yellow product units in stock of product|@ barcode: "+products.get(0).getBarcode()+", name: "+products.get(0).getName()));
        System.out.print(ansi().render("@|cyan \nNOTE: Expired products are not included.|@\n"));
        TableList stockTable = new TableList(6,
                "Product Barcode", "Product Name", "Expiration Date", "Location", "Shelf", "Total Quantity")
                .sortBy(0).withUnicode(false);
        products.forEach(p -> {
            String[] loc_parts = p.getLocation().split("-");
            stockTable.addRow(p.getBarcode() + "", p.getName(), p.getExpiration_date().toString(), loc_parts[0], loc_parts[1], p.getQuantity() + " Units");
        });
        stockTable.print();
    }

    public void printAllStock(List<StockProducts> inv, boolean wait) {
        int total_amount = 0;
        for(StockProducts p : inv)
            total_amount += p.getQuantity();
        if(inv.size() != 0) {
            System.out.print(ansi().render("@|yellow \nNOTE: Expired products are not included.|@\n"));
            System.out.print(ansi().render("@|cyan Currently found |@" + total_amount + " @|cyan products available in stock.|@ \n" +
                    "@|cyan The Current inventory includes |@" + inv.size() + " @|cyan different product barcodes.\n|@"));
            TableList stockTable = new TableList(6,
                    "Product Barcode", "Product Name", "Store Quantity", "Warehouse Quantity", "Total Quantity", "Minimal Amount")
                    .sortBy(0).withUnicode(false);
            inv.forEach(i -> {
                if (i.getQuantity() > 0)
                    stockTable.addRow(i.getBarcode() + "", i.getName(),
                            i.getStore_quantity()+" Units", i.getWarehouse_quantity()+" Units", i.getQuantity() + " Units", i.getMinimal_amount() + " Units"); });
            stockTable.print();
        } else  System.out.print(ansi().render("@|red No inventory available at the moment.|@\n"));

        if(wait) {
            System.out.print(ansi().bold().render("@|yellow \nPress ENTER to continue to menu. |@\n"));
            scan.nextLine();
        } else System.out.print(ansi().bold().render("\n@|cyan BY SPECIFIC LOCATIONS:. |@"));
    }

    public void printAllStockByLocation(List<StockProducts> inv, boolean wait) {
        int total_amount = 0;
        for(StockProducts p : inv)
            total_amount += p.getQuantity();
        if(inv.size() != 0) {
            System.out.print(ansi().render("@|yellow \nNOTE: Expired products are not included.|@\n"));
            System.out.print(ansi().render("@|cyan Currently found |@" + total_amount + " @|cyan products available in stock.|@ \n" +
                        "@|cyan The Current inventory includes |@" + inv.size() + " @|cyan different product barcodes.\n|@"));
            TableList stockTable = new TableList(5,
                    "Product Barcode", "Product Name", "Location", "Quantity", "Expiration Date")
                    .sortBy(0).withUnicode(false);
            inv.forEach(l -> {
                if (l.getQuantity() > 0)
                    stockTable.addRow(l.getBarcode() + "", l.getName(),
                            l.getLocation(), l.getQuantity()+" Units", l.getExpiration_date().toString()); });
            stockTable.print();
            }
        if(wait) {
            System.out.print(ansi().bold().render("@|yellow \nPress ENTER to continue to menu. |@\n"));
            scan.nextLine();
        }
    }
}
