package logic.models;

import logic.datatypes.Date;

public class DefectiveProduct extends StockProducts {

    private String reason;
    private Date date_reported;
    private int remaining_quantity;


    public DefectiveProduct(int barcode, Date expiration_date, int locationID, int quantity, String reason, Date date_reported) {
        super(barcode, expiration_date, locationID, quantity);
        this.reason = reason;
        this.date_reported = date_reported;
    }

    public DefectiveProduct(String name, int barcode, int quantity, Date expiration, String location, String reason, String date_reported, int locationID) {
        super(name, barcode, quantity, expiration, location);
        this.reason = reason;
        this.date_reported = Date.parseDate(date_reported);
        this.setLocationID(locationID);
    }

    public DefectiveProduct(String name, int barcode, int quantity, int remaining_quantity) {
        super(name, barcode, quantity);
        this.remaining_quantity = remaining_quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getDate_reported() {
        return date_reported;
    }

    public void setDate_reported(Date date_reported) {
        this.date_reported = date_reported;
    }

    public int getRemaining_quantity() {
        return remaining_quantity;
    }

    public void setRemaining_quantity(int remaining_quantity) {
        this.remaining_quantity = remaining_quantity;
    }
}
