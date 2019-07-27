package presistence.dao;

import logic.datatypes.Date;
import logic.models.Discount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DiscountsDAO implements DAO<Discount, Integer, Integer, Integer> {

    private Connection conn = null; //Connection instance

    @Override
    public Result update(Discount d, Integer key, Integer dummy, Integer dummy2) {
        String sql = "UPDATE discounts SET barcode=?, discounter=?, percentage=?," +
                    " date_given=?, date_ended=? WHERE discountID = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, d.getBarcode());
            pstmt.setString(2, d.getDiscounter());
            pstmt.setDouble(3, d.getPercentage());
            pstmt.setString(4, d.getDate_given().toString());
            pstmt.setString(5, d.getDate_ended().toString());
            pstmt.setInt(6, key);
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Result delete(Discount d) {
        if(findByKey(d) == null)
            return Result.FAIL;
        String sql = "DELETE FROM discounts WHERE discountID = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, d.getDiscountID());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Result insert(Discount d) {
        if(findByKey(d) != null)
            return Result.FAIL;
        String sql = "INSERT INTO discounts(barcode, discounter, percentage, date_given)" +
                    " VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, d.getBarcode());
            pstmt.setString(2, d.getDiscounter());
            pstmt.setDouble(3, d.getPercentage());
            pstmt.setString(4, d.getDate_given().toString());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Discount findByKey(Discount d) {
        String sql = "SELECT * FROM discounts WHERE discountID = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, d.getDiscountID());
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return createDiscountFromResult(rs);
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<Discount> findByBarcode(Discount d, boolean checkEnded, boolean checkDiscounter) {
        List<Discount> discounts = new ArrayList<>();
        String sql;
        if(checkDiscounter)
            sql = checkEnded ? "SELECT * FROM discounts WHERE barcode = ? AND discounter = ? AND date_ended IS NULL"
                                    : "SELECT * FROM discounts WHERE barcode = ? AND discounter = ?";
        else sql = checkEnded ? "SELECT * FROM discounts WHERE barcode = ? AND date_ended IS NULL"
                                    : "SELECT * FROM discounts WHERE barcode = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            if(checkDiscounter) {
                pstmt.setInt(1, d.getBarcode());
                pstmt.setString(2, d.getDiscounter());
            } else  pstmt.setInt(1, d.getBarcode());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                discounts.add(createDiscountFromResult(rs));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return discounts;
    }

    public List<Discount> findAllByDiscounter(String discounter, boolean onlyActive)
    {
        List<Discount> discounts = new ArrayList<>();;
        String sql = (onlyActive) ? "SELECT * FROM discounts WHERE discounter = ? AND date_ended IS NULL" :
                                    "SELECT * FROM discounts WHERE discounter = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, discounter);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                discounts.add(createDiscountFromResult(rs));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return discounts;
    }

    public List<Map.Entry<String, Discount>> findAllLike(String like, String discounter, boolean checkEnded)
    {
        List<Map.Entry<String, Discount>> discounts = new ArrayList<>();;
        String sql = checkEnded ? "SELECT products.name, discountID, discounts.barcode, discounter, percentage, date_given, date_ended" +
                                " FROM discounts JOIN products WHERE products.name LIKE ? AND" +
                                " discounts.discounter = ? AND discounts.date_ended IS NULL AND discounts.barcode = products.barcode" :
                                 "SELECT products.name, discountID, discounts.barcode, discounter, percentage, date_given, date_ended" +
                                " FROM discounts JOIN products WHERE products.name LIKE ? AND" +
                                " discounts.discounter = ? AND discounts.barcode = products.barcode";

        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%"+like+"%");
            pstmt.setString(2, discounter);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                discounts.add(new AbstractMap.SimpleImmutableEntry(rs.getString("name"), createDiscountFromResult(rs)));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return discounts;
    }

    private Discount createDiscountFromResult(ResultSet rs)
    {
        try {
            return new Discount(rs.getInt("discountID"),
                                rs.getInt("barcode"),
                                rs.getString("discounter"),
                                rs.getDouble("percentage"),
                                Date.parseDate(rs.getString("date_given")),
                                Date.parseDate(rs.getString("date_ended")));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }


    public void setConnection(Connection conn)
    {
        this.conn = conn;
    }

}
