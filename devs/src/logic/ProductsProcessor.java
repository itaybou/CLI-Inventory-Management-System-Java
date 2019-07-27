package logic;

import logic.models.Discount;
import logic.models.Product;
import logic.models.ProductCategory;
import presentation.Prompter;
import presentation.printers.CategoryPrinter;
import presentation.printers.ProductsPrinter;
import presistence.Repository;
import presistence.dao.CategoryDAO;
import presistence.dao.DiscountsDAO;
import presistence.dao.ProductsDAO;

import java.util.*;

public class ProductsProcessor implements Processor{

    private Repository repo; //A repository instance used to communicate with database
    private Prompter prompter;

    public ProductsProcessor(Prompter prompter, Repository repo) {
        this.prompter = prompter;
        this.repo = repo;
    }

    @Override
    public void process() {
        boolean menu = false;
        while (!menu) {
            switch (prompter.getPrinter(Modules.PRODUCTS).printMenu()) {
                case 1:
                    prompter.printMessage(false, null);
                    while (!processAddNewProduct());
                    break;
                case 2:
                    prompter.printMessage(false, null);
                    while (!processUpdateProduct());
                    break;
                case 3:
                    while (!processDeleteProduct());
                    break;
                case 4:
                    prompter.printMessage(false, null);
                    processDisplayDetails();
                    break;
                case 5:
                    processPrintAllProducts();
                    break;
                case 6:
                    prompter.printMessage(false, null);
                    processGetProductDetails(true,
                            "Viewing product category details,\n" +
                                    "Press ENTER to return.");
                    prompter.printMessage(false, null);
                    break;
                case 7:
                    prompter.printMessage(false, null);
                    processViewProductByCategory();
                    break;
                case 8: menu = true;
                    break;
            }
        }
    }

    private void processViewProductByCategory() {
        List<Product> products = new ArrayList<>();
        switch(((CategoryPrinter)prompter.getPrinter(Modules.CATEGORIES)).promptViewByCategory())
        {
            case 1:
                prompter.printMessage(false, null);
                int catID = ((CategoryPrinter)prompter.getPrinter(Modules.CATEGORIES)).categoryIDPrompt();
                if(((CategoryDAO)repo.getDAO(Modules.CATEGORIES)).findCategoryByKey(catID) != null)
                {
                    products = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findAllByCategory(null, catID);
                    ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(products, true, "Found the following products for category ID "+catID+
                                                                        "\nPress ENTER to return.", false, true);
                    prompter.printMessage(false, null);
                } else prompter.printMessage(true, "Category ID does not exist in system.");
                break;
            case 2:
                prompter.printMessage(false, null);
                String name = prompter.nameSearchPrompt();
                products = ((ProductsDAO) repo.getDAO(Modules.PRODUCTS)).findAllByCategory(name, 0);
                if(products.size() != 0) {
                    ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(products, true, "Found the following products for category name '" + name + "'." +
                                                                        "\nPress ENTER to return.", false, true);
                    prompter.printMessage(false, null);
                }
                else prompter.printMessage(true, "No matching categories found for category name '"+name+"'.");
                break;
            case 3:
                prompter.printMessage(false, null);
                break;
            default:
                prompter.printMessage(false, null);
                break;
        }
    }

    private void processDisplayDetails() {
        int barcode;
        switch(prompter.byBarcodeNamePrompt("Products", "Find a product to display by:"))
        {
            case 1:
                barcode = prompter.barcodePrompt();
                if (barcode == prompter.RETURN)
                    break;
                Product product = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findByKey(new Product(barcode));
                if (product == null) //No discounts for current barcode
                    prompter.printMessage(true, "No matching products for barcode number " + barcode);
                else ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(new ArrayList<>(Arrays.asList(product)), true, "Press ENTER to return.", false, true);
                break;

            case 2:
                String name = prompter.nameSearchPrompt();
                if (name == null)
                    break;
                List<Product> products = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findAllLike(name);
                if (products.size() == 0) //No discounts for current barcode
                    prompter.printMessage(true, "No matching products for your searched name string '" +name+ "'");
                else ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(products, true, "Press ENTER to return.", false, true);
                break;
            case 3:
                break;
        }
        prompter.printMessage(false, null);
    }


    private boolean processDeleteProduct() {
        Boolean delete = null;
        boolean cancel = false, valid = false;
        int barcode = -1, discountID = -1;

        prompter.printMessage(false, null);
        switch(prompter.byBarcodeNamePrompt("Products", "Find product to remove by:"))
        {
            case 1: // Cancel by barcode
                while(!valid) {
                    barcode = prompter.barcodePrompt();
                    if (barcode == prompter.RETURN) {
                        break;
                    }
                    Product product = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findByKey(new Product(barcode));
                    if (product == null) //No discounts for current barcode
                        prompter.printMessage(true, "No matching products for barcode number " + barcode);
                    else {
                        ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(new ArrayList<>(Arrays.asList(product)), false, "Found a product to remove.", false, true);
                        delete = prompter.deletePrompt();
                        while(delete == null) {
                            ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(new ArrayList<>(Arrays.asList(product)), false, "Illegal choice, please select Y\\N to proceed", true, true);
                            delete = prompter.deletePrompt();
                        }

                        valid = true;
                    }
                }
                break;

            case 2:
                while(!valid) {
                    String name = prompter.nameSearchPrompt();
                    if (name == null)
                        break;
                    List<Product> products = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findAllLike(name);
                    if (products.size() == 0) //No discounts for current barcode
                        prompter.printMessage(true, "No matching products for your searched name string '" +name+ "'");
                    else {
                        ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(products, false, "Found "+products.size()+" products that can be removed.", false, true);
                        barcode = prompter.barcodePrompt();
                        if(barcode == prompter.RETURN)
                            cancel = true;
                        else {
                            int code = barcode;
                            boolean found = products.stream().anyMatch(p -> p.getBarcode() == code);
                            while(!found) {
                                ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(products, false, "Barcode entered did not match any of the following products.", true, true);
                                barcode = prompter.barcodePrompt();
                                int newCode = barcode;
                                if(barcode == prompter.RETURN) {
                                    cancel = true;
                                    break;
                                }
                                found = products.stream().anyMatch(p -> p.getBarcode() == newCode);
                            }
                            if (!cancel) {
                            delete = prompter.deletePrompt();
                                while (delete == null) {
                                    ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(products, false, "Illegal choice, please select Y\\N to proceed", true, true);
                                    delete = prompter.deletePrompt();
                                }
                            }
                        }

                        valid = true;
                    }
                }
                break;
            case 3:
                prompter.printMessage(false, null);
                return true;
        }
        if(delete != null && !cancel && delete) {
            ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).delete(new Product(barcode));
            prompter.printMessage(false, "Product with barcode "+barcode+" deleted successfully!");
        }
        else return false;

        return true;
    }

    private boolean processAddNewProduct() {
        int barcode = prompter.barcodePrompt();
        if(barcode == 0) {
            prompter.printMessage(false, null);
            return true;
        }
        Product p;
        if((p = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findByKey(new Product(barcode))) != null) {
            prompter.printMessage(true, "Product "+p.getName()+" already contains the BARCODE number "+p.getBarcode());
            return false;
        }
        String[] productDetails = ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).addProductDetailsPrompt();
        if(productDetails.length == 1) {
            prompter.printMessage(true, "Illegal "+productDetails[0] + " entered.\n" + productDetails[0] +
                    (productDetails[0].equals("Cost price") || productDetails[0].equals("Selling price") ? " must be a real number." : " must be an integer."));
            return false;
        }
        else {
            String mainCategory = ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).addProductMainCategory();
            List<Map.Entry<Integer,String>> subCategories = new ArrayList<>();
            processEditProductCategories(mainCategory, subCategories);
            ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).insert(new Product(
                    barcode, productDetails[0], productDetails[1], Double.parseDouble(productDetails[2]), Double.parseDouble(productDetails[3]),
                    Double.parseDouble(productDetails[3]), Double.parseDouble(productDetails[2]), Integer.parseInt(productDetails[4])));
            if(!mainCategory.equals(""))
                ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).insert(new ProductCategory(barcode, mainCategory, 1));
            for(Map.Entry<Integer,String> subCat : subCategories)
                ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).insert(new ProductCategory(barcode, subCat.getValue(), subCat.getKey()));
            prompter.printMessage(false, "Product " + productDetails[0] + " with barcode " + barcode + " successfully added.");
        }
        return true;
    }

    private boolean processUpdateProduct() {
       Product p = processGetProductDetails(false, "Found product to edit.");
       if(p == null)
           return true;
       else if(p.getBarcode() == 0)
           return false;
        Map.Entry<Integer, String> value = ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printUpdateSelectProduct();
        if(value != null) {
            switch (value.getKey()) {
                case 1:
                    int newBarcode = Integer.parseInt(value.getValue());
                    if (((ProductsDAO) repo.getDAO(Modules.PRODUCTS)).findByKey(new Product(newBarcode)) == null)
                        p.setBarcode(newBarcode);
                    else prompter.printMessage(true, "Barcode number chosen already exists in system.");
                    break;
                case 2:
                    p.setName(value.getValue());
                    break;
                case 3:
                    p.setManufacturer(value.getValue());
                    break;
                case 4:
                    p.setCost_price(Double.parseDouble(value.getValue()));
                    p.setOrig_cost_price(Double.parseDouble(value.getValue()));
                    break;
                case 5:
                    p.setSelling_price(Double.parseDouble(value.getValue()));
                    p.setOrig_selling_price(Double.parseDouble(value.getValue()));
                    break;
                case 6:
                    p.setMinimal_amount(Integer.parseInt(value.getValue()));
                    break;
                case 7:
                    prompter.printMessage(null, "This operation will ask you to re-enter all product category data."
                            + "\nall previous product category data will be reset if you choose to proceed."
                            + "\nEnter N\\n to cancel operation.");
                    String mainCategory = ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).addProductMainCategory();
                    if (!mainCategory.equals("")) {
                        ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).deleteAllByProduct(new ProductCategory(p.getBarcode(), mainCategory, 1));
                        List<Map.Entry<Integer, String>> subCategories = new ArrayList<>();
                        processEditProductCategories(mainCategory, subCategories);
                        ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).insert(new ProductCategory(
                                ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).findByName(new ProductCategory(mainCategory)).getCategoryID(), p.getBarcode(), mainCategory, 1));
                        for (Map.Entry<Integer, String> subCat : subCategories)
                            ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).insert(new ProductCategory(
                                    ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).findByName(new ProductCategory(subCat.getValue())).getCategoryID(), p.getBarcode(), subCat.getValue(), subCat.getKey()));
                        prompter.printMessage(false, "Successfully updated product with the new categories");
                    } else prompter.printMessage(false, null);
                    break;
                case 8:
                    prompter.printMessage(false, null);
                    return true;
            }
            if (value.getKey() != 7) {
                ((ProductsDAO) repo.getDAO(Modules.PRODUCTS)).update(p, p.getBarcode(), 0, 0);
                if (value.getKey() == 4 || value.getKey() == 5)
                    reDiscount(p);
                prompter.printMessage(false, "Successfully updated product with the new value " + value.getValue());
            }
            return true;
        }
        return false;
    }

    private void processEditProductCategories(String mainCategory, List<Map.Entry<Integer,String>> subCategories) {
        if(!mainCategory.equals("")) {
            if((((CategoryDAO)repo.getDAO(Modules.CATEGORIES)).findByName(new ProductCategory(mainCategory))) == null) {
                while(!mainCategory.equals("") && !((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).promptCategoryNotExist(mainCategory))
                    mainCategory = ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).addProductMainCategory();
                if(!mainCategory.equals("")) {
                    ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).insertCategory(new ProductCategory(mainCategory));
                    ((CategoryPrinter)prompter.getPrinter(Modules.CATEGORIES)).promptCategoryAdded(mainCategory);
                }
            }
            if (((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).checkAddProductsSubCategory()) {
                int hierarchy = 2;
                String subCategory = ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).addProductSubCategories(hierarchy);
                while (!subCategory.equals("q") && !subCategory.equals("Q")) {
                    if((((CategoryDAO)repo.getDAO(Modules.CATEGORIES)).findByName(new ProductCategory(subCategory))) == null) {
                        while(!subCategory.equals("q") && !subCategory.equals("Q") && !((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).promptCategoryNotExist(subCategory))
                            subCategory = ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).addProductSubCategories(hierarchy);
                        if(!subCategory.equals("q") && !subCategory.equals("Q")) {
                            ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).insertCategory(new ProductCategory(subCategory));
                            ((CategoryPrinter)prompter.getPrinter(Modules.CATEGORIES)).promptCategoryAdded(subCategory);
                        }
                    }
                    subCategories.add(new AbstractMap.SimpleEntry<>(hierarchy, subCategory));
                    hierarchy++;
                    subCategory = ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).addProductSubCategories(hierarchy);
                }
            }
        }
    }

    private Product processGetProductDetails(boolean wait, String message)
    {
        int barcode = prompter.barcodePrompt();
        if(barcode == 0) {
            prompter.printMessage(false, null);
            return null;
        }
        Product p;
        if((p = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findByKey(new Product(barcode))) == null) {
            prompter.printMessage(true, "Product with barcode " + barcode + " does not exist in system.");
            return new Product(0);
        }
        List<ProductCategory> categories = ((CategoryDAO)repo.getDAO(Modules.CATEGORIES)).findAllByProduct(p);
        ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(new ArrayList<>(Arrays.asList(p)),false, message, false, true);
        ((CategoryPrinter)prompter.getPrinter(Modules.CATEGORIES)).printProductCategories(categories, p.getBarcode(), wait, null, false, false);
        return p;
    }

    private void reDiscount(Product p) {
        List<Discount> discounts = ((DiscountsDAO) repo.getDAO(Modules.DISCOUNTS)).findByBarcode(
                new Discount(p.getBarcode()), true, false);
        for(Discount d : discounts) {
            if(d.getDate_ended() == null) {
                double newPrice;
                if (d.getDiscounter().equals("Store")) {
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

    private void processPrintAllProducts() {
        ((ProductsPrinter)prompter.getPrinter(Modules.PRODUCTS)).printProducts(((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findAll(), true, "Press ENTER to return to menu", false, true);
        prompter.printMessage(false, null);
    }
}
