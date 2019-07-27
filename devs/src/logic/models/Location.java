package logic.models;

public class Location {

    private int locationID;
    private int branchID;
    private String physical_place;
    private String place_identifier;

    public Location(int locationID, int branchID, String physical_place, String place_identifier) {
        this.locationID = locationID;
        this.branchID = branchID;
        this.physical_place = physical_place;
        this.place_identifier = place_identifier;
    }

    public Location(int branchID, String physical_place, String place_identifier) {
        this.branchID = branchID;
        this.physical_place = physical_place;
        this.place_identifier = place_identifier;
    }

    public Location(String physical_place) {
        this.physical_place = physical_place;
    }

    public int getLocationID() {
        return locationID;
    }

    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    public int getBranchID() {
        return branchID;
    }

    public void setBranchID(int branchID) {
        this.branchID = branchID;
    }

    public String getPhysical_place() {
        return physical_place;
    }

    public void setPhysical_place(String physical_place) {
        this.physical_place = physical_place;
    }

    public String getPlace_identifier() {
        return place_identifier;
    }

    public void setPlace_identifier(String place_identifier) {
        this.place_identifier = place_identifier;
    }

    @Override
    public String toString()
    {
        return "Location: "+physical_place+", Shelf: "+place_identifier;
    }

    public void switchPlace()
    {
        this.physical_place = (this.physical_place.equals("Warehouse")) ? "Store" : "Warehouse";
    }
}
