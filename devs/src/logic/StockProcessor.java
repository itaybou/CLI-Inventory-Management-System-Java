package logic;

import logic.datatypes.Date;
import logic.models.*;
import presentation.Prompter;
import presentation.printers.CategoryPrinter;
import presentation.printers.ProductsPrinter;
import presentation.printers.StockPrinter;
import presistence.Repository;
import presistence.dao.CategoryDAO;
import presistence.dao.LocationsDAO;
import presistence.dao.ProductsDAO;
import presistence.dao.StockDAO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockProcessor implements Processor {

    private StoreBranch activeBranch;
    private Repository repo; //A repository instance used to communicate with database
    private Prompter prompter;

    public StockProcessor(Prompter prompter, Repository repo) {
        this.prompter = prompter;
        this.repo = repo;
    }

    @Override
    public void process() {
        boolean menu = false;
        while (!menu) {
            switch (((StockPrinter)prompter.getPrinter(Modules.STOCK)).printMenu()) {
                case 1:
                    prompter.printMessage(false, null);
                    processAddToStock();
                    break;
                case 2:
                    prompter.printMessage(false, null);
                    processRemoveFromStock();
                    break;
                case 3:
                    prompter.printMessage(false, null);
                    processMoveStock();
                    break;
                case 4:
                    prompter.printMessage(false, null);
                    processDisplayStockDetails();
                    break;
                case 5:
                    processDisplayAllStock();
                    break;
                case 6:
                    processDisplayAllStockByCategory();
                    break;
                case 7: menu = true;
                    break;
            }
        }
    }

    private void processMoveStock() {
        switch(prompter.byBarcodeNamePrompt("Branch Stock", "Choose how to find a product to move:")) {
            case 1:
                int barcode = prompter.barcodePrompt();
                if(barcode != prompter.ILLEGAL)
                    moveStock(barcode);
                break;
            case 2:
                String name = prompter.nameSearchPrompt();
                List<Product> prod = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findAllLike(name);
                if(prod.size() != 0) {
                    ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(prod, false, "Found the following products that match the search string '"+name+"'.\n" +
                            "Enter the barcode of the product you wish to move stock of.",false, true);
                    barcode = prompter.barcodePrompt();
                    if(((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findByKey(new Product(barcode)) != null)
                        moveStock(barcode);
                    else prompter.printMessage(true, "Barcode entered did not match any of the resulted products.");
                } else prompter.printMessage(true, "Could not find any products that correspond with the search string '"+name+"'.");
                break;
            case 3:
                prompter.printMessage(false, null);
                break;
        }
    }

    private void processRemoveFromStock() {
        prompter.printMessage(false, "Enter the barcode of the product you wish to remove from stock:");
        int barcode = prompter.barcodePrompt();
        if(barcode != prompter.RETURN) {
            Product p;
            if ((p = (((ProductsDAO) repo.getDAO(Modules.PRODUCTS)).findByKey(new Product(barcode)))) != null) {
                List<StockProducts> availableToRemove = ((StockDAO) repo.getDAO(Modules.STOCK)).findByBarcodeAndLocation(activeBranch, barcode);
                String name = (availableToRemove.size() != 0) ? availableToRemove.get(0).getName() : "";
                if (availableToRemove.size() != 0) {
                    prompter.printMessage(false, "Found the following product stock for barcode: " + barcode);
                    ((StockPrinter) prompter.getPrinter(Modules.STOCK)).printAllStockByLocation(availableToRemove, false);
                    if (((StockPrinter) prompter.getPrinter(Modules.STOCK)).promptDeletion()) {
                        int stock_amount = 0;
                        for (StockProducts stock : availableToRemove) {
                            stock_amount += stock.getQuantity();
                            ((StockDAO) repo.getDAO(Modules.STOCK)).delete(stock);
                        }
                        prompter.printMessage(false, "Successfully removed " + stock_amount + " units of the product '" + name + "', barcode: " + barcode + ".");
                    }
                } else prompter.printMessage(true, "Product does not currently exist in stock.");
            } else prompter.printMessage(true, "Product does not exist in system");
        } else prompter.printMessage(false, null);
    }

    private void processAddToStock() {
        Product p;
        switch(prompter.byBarcodeNamePrompt("Branch Stock", "Choose how to add product:"))
        {
            case 1:
                int barcode = prompter.barcodePrompt();
                if(barcode != prompter.ILLEGAL) {
                    if ((p = (((ProductsDAO) repo.getDAO(Modules.PRODUCTS)).findByKey(new Product(barcode)))) != null) {
                        ((ProductsPrinter) prompter.getPrinter(Modules.PRODUCTS)).printProducts(new ArrayList<>(Arrays.asList(p)), false, null, false, false);
                        addToStock(p);
                    } else prompter.printMessage(true, "Product is not available for adding to stock in store chain.");
                }
                break;
            case 2:
                String name = prompter.nameSearchPrompt();
                List<Product> products = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findAllLike(name);
                if(products.size() != 0) {
                    ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(products, false, "Found the following products that match the search string '"+name+"'.\n" +
                                                                            "Enter the barcode of the product you wish to add to stock.",false, true);
                    barcode = prompter.barcodePrompt();
                    if((p = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findByKey(new Product(barcode))) != null)
                        addToStock(p);
                    else prompter.printMessage(true, "Barcode entered did not match any of the resulted products.");
                } else prompter.printMessage(true, "Could not find any products that correspond with the search string '"+name+"'.");
                break;
            case 3:
                prompter.printMessage(false, null);
                break;
        }
    }

    private void addToStock(Product p) {
        int quantity = ((StockPrinter) prompter.getPrinter(Modules.STOCK)).promptQuantity("add");
        if (quantity != prompter.ILLEGAL) {
            Date date = ((StockPrinter) prompter.getPrinter(Modules.STOCK)).promptExpirationDate();
            if (date != null) {
                Location location = ((StockPrinter) prompter.getPrinter(Modules.STOCK)).promptLocation(activeBranch);
                if (location != null) {
                    location.setPlace_identifier(location.getPlace_identifier().toUpperCase());
                    Location actual, switched = null;
                    if ((actual = ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).findLocation(location)) == null) {
                        ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).insert(location);
                        actual = ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).findLocation(location);
                    }
                    location.switchPlace();
                    if((switched = ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).findLocation(location)) == null) {
                        ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).insert(location);
                        switched = ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).findLocation(location);
                    }
                    ((StockDAO) repo.getDAO(Modules.STOCK)).insert(new StockProducts(p.getBarcode(), date, actual.getLocationID(), quantity));
                    ((StockDAO) repo.getDAO(Modules.STOCK)).insert(new StockProducts(p.getBarcode(), date, switched.getLocationID(), 0));
                    prompter.printMessage(false, "Added successfully " + quantity + " Units of the product '" + p.getName() +
                            "', Barcode: " + p.getBarcode() + "\nWith Expiration date: " + date.toString() + " to " + actual.toString());
                }
            }
        }
    }

    private void moveStock(int barcode) {

        prompter.printMessage(false, "Found the following products quantities.\nChoose A quantity and A location to move from:");
        List<StockProducts> products = ((StockDAO)repo.getDAO(Modules.STOCK)).findAmountByLocations(activeBranch, barcode);
        if(products.size() > 0)
        {
            StockProducts stock = null;
            ((StockPrinter)prompter.getPrinter(Modules.STOCK)).printStockAmounts(products);
            Location location = ((StockPrinter) prompter.getPrinter(Modules.STOCK)).movePrompt(activeBranch,"move products from");
            if(location != null) {
                location.setPlace_identifier(location.getPlace_identifier().toUpperCase());
                for (StockProducts prod : products) {
                    if (prod.getBarcode() == barcode && checkLegalLocationString(prod, location)) {
                        stock = prod;
                        break;
                    }
                }
                if (stock != null) {
                    Location current = ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).findLocation(location);
                    int quantity = ((StockPrinter) prompter.getPrinter(Modules.STOCK)).promptQuantity("to move");
                    if (quantity <= stock.getQuantity()) {
                        Location toMove = ((StockPrinter) prompter.getPrinter(Modules.STOCK)).promptLocation(activeBranch);
                        toMove.setPlace_identifier(toMove.getPlace_identifier().toUpperCase());
                        Location actual;
                        if ((actual = ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).findLocation(toMove)) == null) {
                            ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).insert(toMove);
                            actual = ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).findLocation(toMove);
                        }
                        stock.setLocationID(current.getLocationID());
                        stock.setQuantity(stock.getQuantity() - quantity);
                        if (stock.getQuantity() == 0)
                            ((StockDAO) repo.getDAO(Modules.STOCK)).delete(stock);
                        else
                            ((StockDAO) repo.getDAO(Modules.STOCK)).update(stock, stock.getBarcode(), stock.getExpiration_date(), actual.getLocationID());
                        stock.setQuantity(quantity);
                        stock.setLocationID(actual.getLocationID());
                        ((StockDAO) repo.getDAO(Modules.STOCK)).insert(stock);
                        prompter.printMessage(false, quantity + " Stock products named '" + stock.getName() + "' barcode: " + stock.getBarcode() + "" +
                                " Moved successfully.\nFrom location: " + current.getPhysical_place() + ", Shelf " + current.getPlace_identifier() +
                                "\nTo location: " + actual.getPhysical_place() + ", Shelf " + actual.getPlace_identifier());

                    } else
                        prompter.printMessage(true, "Insufficient quantity chosen fot product " + stock.getName() + ", barcode: " + stock.getBarcode() + "." +
                                "\nIn location: " + location.getPhysical_place() + ", Shelf " + location.getPlace_identifier());

                } else prompter.printMessage(true, "Illegal location chosen: " + location);
            } else prompter.printMessage(true, "Illegal location chosen");
        } else prompter.printMessage(true, "No Stock currently available of chosen barcode: "+barcode);
    }

    private void processDisplayStockDetails() {
        prompter.printMessage(false, "Enter the barcode of the product you want to view:");
        int barcode = prompter.barcodePrompt();
        if(barcode != prompter.RETURN) {
            Product p;
            if ((p = (((ProductsDAO) repo.getDAO(Modules.PRODUCTS)).findByKey(new Product(barcode)))) != null) {
                List<StockProducts> available_locations = ((StockDAO) repo.getDAO(Modules.STOCK)).findByBarcodeAndLocation(activeBranch, barcode);
                List<StockProducts> available = ((StockDAO) repo.getDAO(Modules.STOCK)).findAllInventoryByBarcode(activeBranch, barcode);
                if (available.size() != 0 && available_locations.size() != 0) {
                    prompter.printMessage(false, "Found the following product stock for barcode: " + barcode);
                    ((StockPrinter) prompter.getPrinter(Modules.STOCK)).printAllStock(available, false);
                    ((StockPrinter) prompter.getPrinter(Modules.STOCK)).printAllStockByLocation(available_locations, true);
                    }else prompter.printMessage(true, "Product does not currently exist in stock.");
            } else prompter.printMessage(true, "Product does not exist in system");
        } else prompter.printMessage(false, null);
        prompter.printMessage(false, null);
    }

    private void processDisplayAllStock() {
        boolean byLocations = ((StockPrinter)prompter.getPrinter(Modules.STOCK)).promptStockDispalyByLocation();
        int choice = (byLocations) ? 0 : 1;
        switch(choice)
        {
            case 0:
                prompter.printMessage(false, null);
                List<StockProducts> inventory = ((StockDAO)repo.getDAO(Modules.STOCK)).findAllByLocation(activeBranch);
                ((StockPrinter)prompter.getPrinter(Modules.STOCK)).printAllStockByLocation(inventory, true);
                prompter.printMessage(false, null);
                break;
            case 1:
                prompter.printMessage(false, null);
                inventory = ((StockDAO)repo.getDAO(Modules.STOCK)).findAllInventory(activeBranch, null);
                ((StockPrinter)prompter.getPrinter(Modules.STOCK)).printAllStock(inventory, true);
                prompter.printMessage(false, null);
                break;
        }
    }

    private void processDisplayAllStockByCategory() {
        prompter.printMessage(false, null);
        int catID = ((CategoryPrinter)prompter.getPrinter(Modules.CATEGORIES)).categoryIDPrompt();
        ProductCategory p;
        if ((p = ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).findCategoryByKey(catID)) != null) {
            List<StockProducts> cat_stock = ((StockDAO)repo.getDAO(Modules.STOCK)).findAllInventory(activeBranch, p);
            if(cat_stock.size() != 0) {
                ((CategoryPrinter)prompter.getPrinter(Modules.CATEGORIES)).printCategoryDetails(p.getName(), p.getCategoryID(), "stock products:");
                ((StockPrinter)prompter.getPrinter(Modules.STOCK)).printAllStock(cat_stock, true);
            } else prompter.printMessage(true, "No products from stock are currently assigned to Category ID: "+catID+", named: '"+p.getName()+"'.");
        } else prompter.printMessage(true, "Category ID: "+catID+" does not exist in system.");
        prompter.printMessage(false, null);
    }

    private boolean checkLegalLocationString(StockProducts p, Location location)
    {
        String[] loc_split = p.getLocation().split("-");
        return (loc_split[0].equals(location.getPhysical_place()) && loc_split[1].equals(location.getPlace_identifier()));
    }

    public void setActiveBranch(StoreBranch activeBranch) {
        this.activeBranch = activeBranch;
    }
}
