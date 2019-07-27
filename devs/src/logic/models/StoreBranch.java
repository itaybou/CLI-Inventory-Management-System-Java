package logic.models;

public class StoreBranch {

    private int branchID;
    private String name;

    public StoreBranch(int branchID, String name) {
        this.branchID = branchID;
        this.name = name;
    }

    public StoreBranch(int branchID) {
        this.branchID = branchID;
    }

    public StoreBranch(String name) {
        this.name = name;
    }

    public int getBranchID() {
        return branchID;
    }

    public void setBranchID(int branchID) {
        this.branchID = branchID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
