package logic.models;

import logic.datatypes.Date;

public class StockProducts {

    private String name;
    private String location;
    private int barcode;
    private int minimal_amount;
    private Date expiration_date;
    private int locationID;
    private int quantity;
    private int store_quantity;
    private int warehouse_quantity;

    public StockProducts(String name, int barcode, Date expiration_date, int quantity, int minimal_amount, int locationID, String location) {
        this.name = name;
        this.barcode = barcode;
        this.expiration_date = expiration_date;
        this.quantity = quantity;
        this.minimal_amount = minimal_amount;
        this.location = location;
        this.locationID = locationID;
    }

    public StockProducts(String name, int barcode, int quantity, int minimal_amount, int store_quantity, int warehouse_quantity) {
        this.name = name;
        this.barcode = barcode;
        this.quantity = quantity;
        this.minimal_amount = minimal_amount;
        this.store_quantity = store_quantity;
        this.warehouse_quantity = warehouse_quantity;
    }

    public StockProducts(String name, int barcode, int quantity, Date expiration, String location) {
        this.name = name;
        this.barcode = barcode;
        this.quantity = quantity;
        this.location = location;
        this.expiration_date = expiration;
    }

    public StockProducts(int barcode, Date expiration_date, int locationID, int quantity) {
        this.barcode = barcode;
        this.expiration_date = expiration_date;
        this.locationID = locationID;
        this.quantity = quantity;
    }

    public StockProducts(String name, int barcode, int quantity) {
        this.name = name;
        this.barcode = barcode;
        this.quantity = quantity;
    }

    public StockProducts(int quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getMinimal_amount() {
        return minimal_amount;
    }

    public void setMinimal_amount(int minimal_amount) {
        this.minimal_amount = minimal_amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBarcode() {
        return barcode;
    }

    public void setBarcode(int barcode) {
        this.barcode = barcode;
    }

    public Date getExpiration_date() {
        return expiration_date;
    }

    public void setExpiration_date(Date expiration_date) {
        this.expiration_date = expiration_date;
    }

    public int getLocationID() {
        return locationID;
    }

    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getStore_quantity() {
        return store_quantity;
    }

    public void setStore_quantity(int store_quantity) {
        this.store_quantity = store_quantity;
    }

    public int getWarehouse_quantity() {
        return warehouse_quantity;
    }

    public void setWarehouse_quantity(int warehouse_quantity) {
        this.warehouse_quantity = warehouse_quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
