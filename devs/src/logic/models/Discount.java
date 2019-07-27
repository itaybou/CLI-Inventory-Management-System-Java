package logic.models;

import logic.datatypes.Date;

public class Discount {

    private int discountID;
    private int barcode;
    private String discounter;
    private double percentage;
    private Date date_given;
    private Date date_ended;

    public Discount(int discountID, int barcode, String discounter, double percentage, Date date_given, Date date_ended) {
        this.discountID = discountID;
        this.barcode = barcode;
        this.discounter = discounter;
        this.percentage = percentage;
        this.date_given = date_given;
        this.date_ended = date_ended;
    }

    public Discount(int barcode, String discounter) {
        this.barcode = barcode;
        this.discountID = barcode;
        this.discounter = discounter;
    }

    public Discount(int barcode) {
        this.barcode = barcode;
        this.discountID = barcode;
    }

    public int getDiscountID() {
        return discountID;
    }

    public void setDiscountID(int discountID) {
        this.discountID = discountID;
    }

    public int getBarcode() {
        return barcode;
    }

    public void setBarcode(int barcode) {
        this.barcode = barcode;
    }

    public String getDiscounter() {
        return discounter;
    }

    public void setDiscounter(String discounter) {
        this.discounter = discounter;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Date getDate_given() {
        return date_given;
    }

    public void setDate_given(Date date_given) {
        this.date_given = date_given;
    }

    public Date getDate_ended() {
        return date_ended;
    }

    public void setDate_ended(Date date_ended) {
        this.date_ended = date_ended;
    }
}
