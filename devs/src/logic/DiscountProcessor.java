package logic;

import logic.datatypes.Date;
import logic.models.Discount;
import logic.models.Product;
import logic.models.ProductCategory;
import presentation.Prompter;
import presentation.printers.CategoryPrinter;
import presentation.printers.DiscountsPrinter;
import presentation.printers.ProductsPrinter;
import presistence.Repository;
import presistence.dao.CategoryDAO;
import presistence.dao.DiscountsDAO;
import presistence.dao.ProductsDAO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class DiscountProcessor implements Processor{

    private Repository repo; //A repository instance used to communicate with database
    private Prompter prompter;

    public DiscountProcessor(Prompter prompter, Repository repo) {
        this.prompter = prompter;
        this.repo = repo;
    }

    @Override
    public void process() {
        prompter.printMessage(false, null);
        boolean menu = false;
        String discounter;
        while (!menu) {
            int discounterVal = ((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).printManageDiscountsSelect();
            if(discounterVal == 3)
                menu = true;
            else {
                discounter = (discounterVal == 1) ? "Store" : "Supplier";
                prompter.printMessage(false, null);
                switch(((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).printMenu(discounter))
                {
                    case 1:
                        prompter.printMessage(false, null);
                        while (!processAddNewDiscount(discounter));
                        break;
                    case 2:
                        prompter.printMessage(false, null);
                        processCancelDiscount(discounter);
                        break;
                    case 3:
                        prompter.printMessage(false, null);
                        processViewDiscounts(discounter);
                        break;
                    case 4:
                        processPrintAllDiscounts(discounter);
                        break;
                    case 5:
                        processDisplayDiscountedCategories(discounter);
                        break;
                    case 6:
                        prompter.printMessage(false, null);
                        break;

                }
            }
        }
    }

    private void processDisplayDiscountedCategories(String discounter) {
        List<ProductCategory> categories = ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).findAllDiscounted(discounter);
        ((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).printDiscountedCategories(categories, true, "Press ENTER to return.", false, true);
        prompter.printMessage(false, null);
    }

    private void processViewDiscounts(String discounter) { //TODO add by category

        boolean onlyActive = ((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).activeDiscountPrompt();
        prompter.printMessage(false, null);
        switch(prompter.byBarcodeNamePrompt("Discounts", "Find discount to display by:"))
        {
            case 1:
                int barcode = prompter.barcodePrompt();
                if (barcode == prompter.RETURN)
                    break;
                List<Discount> discounts = ((DiscountsDAO) repo.getDAO(Modules.DISCOUNTS)).findByBarcode(new Discount(barcode, discounter), onlyActive, true);
                if (discounts.size() == 0) //No discounts for current barcode
                    prompter.printMessage(true, "No matching active discounts for barcode number " + barcode);
                else {
                    ((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).printDiscounts(discounts, true, discounter, "Press ENTER to return.", false);
                    prompter.printMessage(false, null);
                }
                break;
            case 2:
                String name = prompter.nameSearchPrompt();
                if (name == null)
                    break;
                List<Map.Entry<String, Discount>> discountsByName = ((DiscountsDAO) repo.getDAO(Modules.DISCOUNTS)).findAllLike(name,discounter, onlyActive);
                if (discountsByName.size() == 0) //No discounts for current barcode
                    prompter.printMessage(true, "No matching discounts for your searched name string '" +name+ "'");
                else {
                    ((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).printDiscountsWithNames(discountsByName, discounter,true, "Press ENTER to return.", false);
                    prompter.printMessage(false, null);
                }
                break;
            case 3:
                prompter.printMessage(false, null);
                break;

        }

    }

    private void processCancelDiscount(String discounter) {

        boolean cancel = false, valid = false;
        int discountID = -1;
        switch(((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).byProductCategoryDiscountPrompt("Product", "cancel discounted products")) {
            case 1:
                switch (prompter.byBarcodeNamePrompt("Discounts", "Find discount to remove by:")) {
                    case 1: // Cancel by barcode
                        while (!valid) {
                            int barcode = prompter.barcodePrompt();
                            if (barcode == prompter.RETURN)
                                break;
                            List<Discount> discounts = ((DiscountsDAO) repo.getDAO(Modules.DISCOUNTS)).findByBarcode(new Discount(barcode, discounter), true, true);
                            if (discounts.size() == 0) //No discounts for current barcode
                                prompter.printMessage(true, "No matching active discounts for barcode number " + barcode);
                            else {
                                boolean invalid = false;
                                while (discountID == -1) {
                                    ((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).printDiscounts(discounts, false, discounter, invalid ?
                                            "Illegal Discount ID entered!\nDiscount does not apply on chosen barcode or id does not exist." : null, invalid);
                                    discountID = ((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).promptDiscountToRemove(discounts);
                                    invalid = true;
                                }
                                valid = true;
                            }
                        }
                        break;

                    case 2: //Cancel by name
                        String name = prompter.nameSearchPrompt();
                        if (name == null)
                            break;
                        List<Map.Entry<String, Discount>> discounts = ((DiscountsDAO) repo.getDAO(Modules.DISCOUNTS)).findAllLike(name, discounter, true);
                        List<Discount> discount_list = new ArrayList<>();
                        discounts.forEach(entry -> discount_list.add(entry.getValue()));
                        if (discounts.size() == 0) //No discounts for current barcode
                            prompter.printMessage(true, "No matching active discounts for your searched name string '" + name + "'");
                        else {
                            boolean invalid = false;
                            while (discountID == -1) {
                                ((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).printDiscountsWithNames(discounts, discounter, false, invalid ?
                                        "Illegal Discount ID entered!\nDiscount does not apply on chosen barcode or id does not exist." : null, invalid);
                                discountID = ((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).promptDiscountToRemove(discount_list);
                                invalid = true;
                            }
                        }
                        break;
                    case 3:
                        cancel = true;
                        prompter.printMessage(false, null);
                        break;
                }
                if (discountID != prompter.ILLEGAL)
                    valid = true;
                if (discountID == prompter.RETURN)
                    cancel = true;
                if (!cancel) {
                    Discount d = ((DiscountsDAO) repo.getDAO(Modules.DISCOUNTS)).findByKey(new Discount(discountID, discounter));
                    if (discountID != -1) {
                        Date date;
                        ((DiscountsDAO) repo.getDAO(Modules.DISCOUNTS)).update(new Discount(d.getDiscountID(), d.getBarcode(),
                                        d.getDiscounter(), d.getPercentage(), d.getDate_given(), (date = Date.parseDate(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime())))),
                                discountID, 0, 0);
                        prompter.printMessage(false, "Discount to product with barcode " + d.getBarcode() + " successfully canceled.\n" +
                                "Discount end date is " + date);
                        reDiscount(d);
                    }
                } else prompter.printMessage(false, null);
                break;
            case 2:
                String name = ((CategoryPrinter)prompter.getPrinter(Modules.CATEGORIES)).categoryPrompt(" to cancel discount for");
                if(name != null) {
                    ProductCategory category;
                    if ((category = ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).findByName(new ProductCategory(name))) != null) {
                        boolean isDiscounted = false;
                        List<ProductCategory> categories = ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).findAllDiscounted(discounter);
                        for(ProductCategory cat : categories)
                            if(cat.getCategoryID() == category.getCategoryID()) {
                                isDiscounted = true;
                                break;
                            }
                        if(!isDiscounted)
                            prompter.printMessage(true, "Category name '" + name + "' does not currently have applied discount.");
                        else {
                            Date date = null;
                            List<Discount> d;
                            List<Product> products = ((ProductsDAO) repo.getDAO(Modules.PRODUCTS)).findAllByCategory(name, 0);
                            ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(products, true, "Found "+products.size()+" discounted products in category name '"+name+"'.\nPress ENTER to confirm canceling discount on category '"+name+"'.", false, true);
                            for(Product p : products) {
                                d = ((DiscountsDAO) repo.getDAO(Modules.DISCOUNTS)).findByBarcode(new Discount(p.getBarcode()), true, false);
                                for(Discount dis : d) {
                                    ((DiscountsDAO) repo.getDAO(Modules.DISCOUNTS)).update(new Discount(
                                            dis.getDiscountID(), dis.getBarcode(), dis.getDiscounter(), dis.getPercentage(), dis.getDate_given(),
                                            (date = Date.parseDate(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime())))), dis.getDiscountID(), 0,0);
                                    reDiscount(dis);
                                }
                            }
                            category.setDiscounted(null);
                            category.setDiscounter(null);
                            ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).update(category, 0, category.getName(), 0);
                            prompter.printMessage(false, "Discount to category name '" + name + "' successfully canceled.\n" +
                                                                        "Discount end date is " + date);
                        }
                    } else prompter.printMessage(true, "Category name does not exist in system");
                }
                break;
            case 3:
                prompter.printMessage(false, null);
                break;
        }
    }

    private void reDiscount(Discount discount) {
        List<Discount> discounts = ((DiscountsDAO) repo.getDAO(Modules.DISCOUNTS)).findByBarcode(
                new Discount(discount.getBarcode(), discount.getDiscounter()), true, true);
        Product p = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findByKey(new Product(discount.getBarcode()));
        for(Discount d : discounts) {
            if(d.getDate_ended() == null) {
                double newPrice;
                if (discount.getDiscounter().equals("Store")) {
                    newPrice = p.getSelling_price() - (p.getSelling_price() * (d.getPercentage() / 100));
                    p.setSelling_price(newPrice);
                }
                else {
                    newPrice = p.getCost_price() - (p.getCost_price() * (d.getPercentage() / 100));
                    p.setCost_price(newPrice);
                }
                ((ProductsDAO) repo.getDAO(Modules.PRODUCTS)).update(p, p.getBarcode(), 0, 0);
            }
        }
    }


    private boolean processAddNewDiscount(String discounter) { //TODO add by category

        switch(((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).byProductCategoryDiscountPrompt("Barcode", "discount products")) {
            case 1:
            int barcode = prompter.barcodePrompt();
            if (barcode != prompter.RETURN) {
                if (((ProductsDAO) repo.getDAO(Modules.PRODUCTS)).findByKey(new Product(barcode)) != null) {
                    List<Discount> discounts = ((DiscountsDAO) repo.getDAO(Modules.DISCOUNTS)).findByBarcode(new Discount(barcode, discounter), true, true);
                    prompter.printMessage(false, "Found " + discounts.size() + " Active discounts for Product barcode " + barcode);
                    Double percentage = ((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).addDiscountDetailsPrompt();
                    if (percentage == null) {
                        prompter.printMessage(true, "Illegal percentage value entered. must be a real number.");
                        return false;
                    } else {
                        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                        ((DiscountsDAO) repo.getDAO(Modules.DISCOUNTS)).insert(new Discount(
                                0, barcode, discounter, percentage, Date.parseDate(timeStamp), null));
                        prompter.printMessage(false, "Discount of " + percentage + "% by " + discounter + " to product with barcode " + barcode + " successfully added.\n" +
                                "Discount starting date is " + timeStamp);
                    }
                } else prompter.printMessage(true, "Product Barcode does not exist in system");
            } else prompter.printMessage(false, null);
            return true;
            case 2:
                String name = ((CategoryPrinter)prompter.getPrinter(Modules.CATEGORIES)).categoryPrompt(" to discount");
                if(name != null) {
                    ProductCategory category;
                    if ((category = ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).findByName(new ProductCategory(name))) != null) {
                        prompter.printMessage(false, "Found category name '"+name+"'." );
                        Double percentage = ((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).addDiscountDetailsPrompt();
                        if (percentage == null) {
                            prompter.printMessage(true, "Illegal percentage value entered. must be a real number.");
                            return false;
                        } else {
                            List<Product> products = ((ProductsDAO) repo.getDAO(Modules.PRODUCTS)).findAllByCategory(name, 0);
                            if(products.size() != 0)
                            {
                                String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                                 for(Product p : products) {
                                     ((DiscountsDAO) repo.getDAO(Modules.DISCOUNTS)).insert(new Discount(
                                             0, p.getBarcode(), discounter, percentage, Date.parseDate(timeStamp), null));
                                 }
                                 category.setDiscounted(percentage);
                                 category.setDiscounter(discounter);
                                ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).update(category, 0, category.getName(), 0);
                                prompter.printMessage(false, "Discount of " + percentage + "% by " + discounter + " to category name '" + name + "' successfully added.\n" +
                                                                         "Discount starting date is " + timeStamp);
                            } else prompter.printMessage(true, "No products are assigned to category name '"+name+"'.");
                        }
                    } else prompter.printMessage(true, "Category name does not exist in system");
                    return true;
                }
            case 3:
                prompter.printMessage(false, null);
                return true;
        }
        return false;
    }

    private void processPrintAllDiscounts(String discounter) {
        ((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).printDiscounts(((DiscountsDAO)repo.getDAO(Modules.DISCOUNTS)).findAllByDiscounter(discounter,
                ((DiscountsPrinter)prompter.getPrinter(Modules.DISCOUNTS)).activeDiscountPrompt()), true, discounter, "Press ENTER to return to menu", false);
        prompter.printMessage(false, null);
    }
}
