package logic.models;

public class ProductCategory {

    private int categoryID;
    private int barcode;
    private String name;
    private int hierarchy;
    private Double discounted;
    private String discounter;

    public ProductCategory(int categoryID, int barcode, String name, int hierarchy) {
        this.categoryID = categoryID;
        this.barcode = barcode;
        this.name = name;
        this.hierarchy = hierarchy;
    }

    public ProductCategory(int categoryID, int barcode, int hierarchy) {
        this.categoryID = categoryID;
        this.barcode = barcode;
        this.hierarchy = hierarchy;
    }

    public ProductCategory(int categoryID, String name, int hierarchy) {
        this.categoryID = categoryID;
        this.barcode = categoryID;
        this.name = name;
        this.hierarchy = hierarchy;
    }

    public ProductCategory(int categoryID, String name, Double discounted, String discounter) {
        this.categoryID = categoryID;
        this.name = name;
        this.discounted = discounted;
        this.discounter = discounter;
    }

    public ProductCategory(int categoryID, String name) {
        this.categoryID = categoryID;
        this.name = name;
    }

    public ProductCategory(String name) {
        this.name = name;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
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

    public int getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(int hierarchy) {
        this.hierarchy = hierarchy;
    }

    public Double getDiscounted() {
        return discounted;
    }

    public void setDiscounted(Double discounted) {
        this.discounted = discounted;
    }

    public String getDiscounter() {
        return discounter;
    }

    public void setDiscounter(String discounter) {
        this.discounter = discounter;
    }
}
