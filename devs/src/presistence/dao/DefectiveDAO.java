package presistence.dao;

import logic.models.DefectiveProduct;
import logic.models.StoreBranch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DefectiveDAO implements DAO<DefectiveProduct, Integer, Integer, Integer> {

    private Connection conn = null; //Connection instance

    @Override
    public Result update(DefectiveProduct defectiveProduct, Integer key1, Integer key2, Integer key3) {
        return null;
    }

    @Override
    public Result delete(DefectiveProduct defect) {
        String sql = "DELETE FROM defects WHERE barcode = ?  AND locationID = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, defect.getBarcode());
            pstmt.setInt(2, defect.getLocationID());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Result insert(DefectiveProduct defect) {
        String sql = "INSERT INTO defects" +
                    "(barcode, locationID, quantity, reason, date_reported)" +
                    " VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, defect.getBarcode());
            pstmt.setInt(2, defect.getLocationID());
            pstmt.setInt(3, defect.getQuantity());
            pstmt.setString(4, defect.getReason());
            pstmt.setString(5, defect.getDate_reported().toString());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public DefectiveProduct findByKey(DefectiveProduct defectiveProduct) {
        return null;
    }

    public List<DefectiveProduct> findAll(StoreBranch activeBranch, Integer barcode) {
        List<DefectiveProduct> defects = new ArrayList<>();
        String sql =(barcode == null) ?
                    "SELECT defects.barcode, products.name, locations.physical_place,\n" +
                    "locations.place_identifier, defects.date_reported, defects.locationID, SUM(defects.quantity) AS amount, defects.reason\n" +
                    "FROM defects JOIN products ON defects.barcode = products.barcode\n" +
                    "JOIN locations ON locations.locationID = defects.locationID\n" +
                    "WHERE locations.branchID = ?\n" +
                    "GROUP BY defects.barcode, locations.physical_place, locations.place_identifier, defects.reason\n" +
                    "HAVING amount > 0"
                    :
                    "SELECT defects.barcode, products.name, locations.physical_place,\n" +
                    "locations.place_identifier, defects.date_reported, defects.locationID, SUM(defects.quantity) AS amount, defects.reason\n" +
                    "FROM defects JOIN products ON defects.barcode = products.barcode\n" +
                    "JOIN locations ON locations.locationID = defects.locationID\n" +
                    "WHERE locations.branchID = ? AND products.barcode = ?\n" +
                    "GROUP BY defects.barcode, locations.physical_place, locations.place_identifier, defects.reason\n" +
                    "HAVING amount > 0";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activeBranch.getBranchID());
            if(barcode != null)
                pstmt.setInt(2, barcode);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                defects.add(new DefectiveProduct(rs.getString("name"),
                                                rs.getInt("barcode"),
                                                rs.getInt("amount"),
                                                null,
                                                rs.getString("physical_place")+", Shelf "+rs.getString("place_identifier"),
                                                rs.getString("reason"),
                                                rs.getString("date_reported"),
                                                rs.getInt("locationID")));
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return defects;
    }

    public List<DefectiveProduct> findAllTotal(StoreBranch activeBranch) {
        List<DefectiveProduct> defects = new ArrayList<>();
        String sql = "SELECT defects.barcode, products.name, SUM(defects.quantity) AS amount, stock_amount\n" +
                    "FROM defects JOIN products ON defects.barcode = products.barcode\n" +
                    "JOIN locations ON locations.locationID = defects.locationID\n" +
                    "JOIN (SELECT SUM(stock.quantity) AS stock_amount FROM\n" +
                    "stock JOIN products ON stock.barcode = products.barcode\n" +
                    "JOIN locations ON locations.locationID = stock.locationID\n" +
                    "GROUP BY stock.barcode\n" +
                    "HAVING stock_amount > 0)\n" +
                    "WHERE locations.branchID = ?\n" +
                    "GROUP BY defects.barcode\n" +
                    "HAVING amount > 0";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activeBranch.getBranchID());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                defects.add(new DefectiveProduct(rs.getString("name"),
                                                rs.getInt("barcode"),
                                                rs.getInt("amount"),
                                                rs.getInt("stock_amount")));
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return defects;
    }

    @Override
    public void setConnection(Connection conn)
    {
        this.conn = conn;
    }
}
