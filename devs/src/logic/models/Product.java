package logic.models;

public class Product {

    private int barcode;
    private String name;
    private String manufacturer;
    private double cost_price;
    private double selling_price;
    private double orig_selling_price;
    private double orig_cost_price;
    private int minimal_amount;

    public Product(int barcode, String name, String manufacturer, double cost_price, double selling_price, double orig_selling_price, double orig_cost_price, int minimal_amount) {
        this.barcode = barcode;
        this.name = name;
        this.manufacturer = manufacturer;
        this.cost_price = cost_price;
        this.selling_price = selling_price;
        this.orig_selling_price = orig_selling_price;
        this.orig_cost_price = orig_cost_price;
        this.minimal_amount = minimal_amount;
    }

    public Product(int barcode) {
        this.barcode = barcode;
    }

    public int getBarcode() {
        return barcode;
    }

    public void setBarcode(int barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public double getCost_price() {
        return cost_price;
    }

    public void setCost_price(double cost_price) {
        this.cost_price = cost_price;
    }

    public double getSelling_price() {
        return selling_price;
    }

    public void setSelling_price(double selling_price) {
        this.selling_price = selling_price;
    }

    public double getOrig_selling_price() {
        return orig_selling_price;
    }

    public void setOrig_selling_price(double orig_selling_price) {
        this.orig_selling_price = orig_selling_price;
    }

    public double getOrig_cost_price() {
        return orig_cost_price;
    }

    public void setOrig_cost_price(double orig_cost_price) {
        this.orig_cost_price = orig_cost_price;
    }

    public int getMinimal_amount() {
        return minimal_amount;
    }

    public void setMinimal_amount(int minimal_amount) {
        this.minimal_amount = minimal_amount;
    }
}
