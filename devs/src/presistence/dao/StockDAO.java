package presistence.dao;

import logic.datatypes.Date;
import logic.models.ProductCategory;
import logic.models.StockProducts;
import logic.models.StoreBranch;

import java.sql.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StockDAO implements DAO<StockProducts, Integer, Date, Integer> {

    private Connection conn = null; //Connection instance

    @Override
    public Result update(StockProducts stock, Integer key1, Date key2, Integer key3) {
        String sql = "UPDATE stock SET quantity = ?  " +
                "WHERE barcode = ? AND expiration_date = ? AND locationID = ? ";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, stock.getQuantity());
            pstmt.setInt(2, key1);
            pstmt.setString(3, key2.toString());
            pstmt.setInt(4, stock.getLocationID());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Result delete(StockProducts stock) {
        String sql = "DELETE FROM stock WHERE barcode = ? AND expiration_date = ? AND locationID = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, stock.getBarcode());
            pstmt.setString(2, stock.getExpiration_date().toString());
            pstmt.setInt(3, stock.getLocationID());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Result insert(StockProducts stock) {
        String sql = "INSERT INTO stock" +
                    "(barcode, expiration_date, locationID, quantity)" +
                    " VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, stock.getBarcode());
            pstmt.setString(2, stock.getExpiration_date().toString());
            pstmt.setInt(3, stock.getLocationID());
            pstmt.setDouble(4, stock.getQuantity());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }


    @Override
    public StockProducts findByKey(StockProducts stockProducts) {
        return null;
    }

    public Result findTotalAmount(StockProducts stock, Boolean warehouse) {
        String sql = (warehouse == null) ? "SELECT SUM(quantity) FROM stock GROUP BY barcode " +
                                            "(barcode, expiration_date, locationID, quantity)" +
                                            " VALUES (?, ?, ?, ?)" : "";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, stock.getBarcode());
            pstmt.setString(2, stock.getExpiration_date().toString());
            pstmt.setInt(3, stock.getLocationID());
            pstmt.setDouble(4, stock.getQuantity());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    public List<StockProducts> findAmountByLocations(StoreBranch activeBranch, int barcode)
    {
        List<StockProducts> products = new ArrayList<>();
        String sql = " SELECT stock.barcode, products.name, stock.expiration_date, locations.physical_place, locations.place_identifier, SUM(stock.quantity) AS total_amount\n" +
                        "FROM stock JOIN products ON stock.barcode = products.barcode \n" +
                        "JOIN locations ON locations.locationID = stock.locationID\n" +
                        "WHERE stock.expiration_date >= date('now') AND locations.branchID = ? AND products.barcode = ?\n" +
                        "GROUP BY stock.barcode, locations.physical_place, locations.place_identifier, stock.expiration_date\n" +
                        "HAVING total_amount > 0";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activeBranch.getBranchID());
            pstmt.setInt(2, barcode);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                products.add(new StockProducts(rs.getString("name"),
                        rs.getInt("barcode"),
                        rs.getInt("total_amount"),
                        Date.parseDate(rs.getString("expiration_date")),
                        rs.getString("physical_place")+"-"+rs.getString("place_identifier")));
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return products;
    }

    public Map.Entry<List<StockProducts>, List<StockProducts>> findExpired(StoreBranch activeBranch) {
        List<StockProducts> expired = new ArrayList<>();
        List<StockProducts> expired_null = new ArrayList<>();
        String sql = "SELECT products.name, stock.barcode, locations.locationID, locations.physical_place,\n" +
                "locations.place_identifier, stock.expiration_date, SUM(stock.quantity) AS amount,\n" +
                "products.minimal_amount FROM stock JOIN products ON stock.barcode = products.barcode\n" +
                "JOIN locations ON locations.locationID = stock.locationID\n" +
                "WHERE stock.expiration_date < date('now') AND locations.branchID = ?\n" +
                "GROUP BY stock.barcode, locations.physical_place, locations.place_identifier, stock.expiration_date "+
                "HAVING amount > 0";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activeBranch.getBranchID());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                expired.add(createStockFromResult(rs, true));

            sql = "SELECT products.name, stock.barcode, locations.locationID, locations.physical_place,\n" +
                    "locations.place_identifier, stock.expiration_date, SUM(stock.quantity) AS amount,\n" +
                    "products.minimal_amount FROM stock JOIN products ON stock.barcode = products.barcode\n" +
                    "JOIN locations ON locations.locationID = stock.locationID\n" +
                    "WHERE stock.expiration_date < date('now') AND locations.branchID = ?\n" +
                    "GROUP BY stock.barcode, locations.physical_place, locations.place_identifier, stock.expiration_date "+
                    "HAVING amount = 0";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activeBranch.getBranchID());
            rs = pstmt.executeQuery();
            while(rs.next())
                expired_null.add(createStockFromResult(rs, true));
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return new AbstractMap.SimpleEntry<>(expired, expired_null);
    }

    public List<StockProducts> findCriticalAmount(StoreBranch activeBranch) {
        List<StockProducts> critical_amount = new ArrayList<>();
        String sql = "SELECT stock.barcode, products.name, IFNULL(store.s_amount, 0) AS s_amount, IFNULL(warehouse.w_amount, 0) AS w_amount, "+
                "SUM(stock.quantity) AS total_amount, products.minimal_amount "+
                "FROM stock JOIN products ON stock.barcode = products.barcode " +
                "JOIN locations ON locations.locationID = stock.locationID " +
                "JOIN (SELECT products.barcode, SUM(stock.quantity) AS s_amount " +
                "FROM stock JOIN products ON stock.barcode = products.barcode "+
                "JOIN locations ON locations.locationID = stock.locationID " +
                "WHERE stock.expiration_date >= date('now') AND locations.branchID = ? "+
                "AND locations.physical_place = 'Store' "+
                "GROUP BY stock.barcode) as store ON store.barcode = products.barcode "+
                "JOIN (SELECT products.barcode, SUM(stock.quantity) AS w_amount "+
                "FROM stock JOIN products ON stock.barcode = products.barcode "+
                "JOIN locations ON locations.locationID = stock.locationID "+
                "WHERE stock.expiration_date >= date('now') AND locations.branchID = ? "+
                "AND locations.physical_place = 'Warehouse'"+
                "GROUP BY stock.barcode) as warehouse ON warehouse.barcode = products.barcode "+
                "WHERE stock.expiration_date >= date('now') AND locations.branchID = ? "+
                "GROUP BY stock.barcode "+
                "HAVING products.minimal_amount >= total_amount AND total_amount > 0";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activeBranch.getBranchID());
            pstmt.setInt(2, activeBranch.getBranchID());
            pstmt.setInt(3, activeBranch.getBranchID());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                critical_amount.add(createStockFromResult(rs, false));
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return critical_amount;
    }

    public List<StockProducts> findAllInventory(StoreBranch activeBranch, ProductCategory category) {
        List<StockProducts> inv = new ArrayList<>();
        String sql =(category == null) ? "SELECT stock.barcode, products.name, IFNULL(store.s_amount, 0) AS s_amount, " +
                                        "IFNULL(warehouse.w_amount, 0) AS w_amount, "+
                                        "SUM(stock.quantity) AS total_amount, products.minimal_amount "+
                                        "FROM stock JOIN products ON stock.barcode = products.barcode " +
                                        "JOIN locations ON locations.locationID = stock.locationID " +
                                        "JOIN (SELECT products.barcode, SUM(stock.quantity) AS s_amount " +
                                        "FROM stock JOIN products ON stock.barcode = products.barcode "+
                                        "JOIN locations ON locations.locationID = stock.locationID " +
                                        "WHERE stock.expiration_date >= date('now') AND locations.branchID = ? "+
                                        "AND locations.physical_place = 'Store' "+
                                        "GROUP BY stock.barcode) as store ON store.barcode = products.barcode "+
                                        "JOIN (SELECT products.barcode, SUM(stock.quantity) AS w_amount "+
                                        "FROM stock JOIN products ON stock.barcode = products.barcode "+
                                        "JOIN locations ON locations.locationID = stock.locationID "+
                                        "WHERE stock.expiration_date >= date('now') AND locations.branchID = ? "+
                                        "AND locations.physical_place = 'Warehouse'"+
                                        "GROUP BY stock.barcode) as warehouse ON warehouse.barcode = products.barcode "+
                                        "WHERE stock.expiration_date >= date('now') AND locations.branchID = ? "+
                                        "GROUP BY stock.barcode "+
                                        "HAVING total_amount > 0"
                                        :
                                        "SELECT stock.barcode, products.name, IFNULL(store.s_amount, 0) AS s_amount, " +
                                        "IFNULL(warehouse.w_amount, 0) AS w_amount, "+
                                        "SUM(stock.quantity) AS total_amount, products.minimal_amount "+
                                        "FROM stock JOIN products ON stock.barcode = products.barcode " +
                                        "JOIN locations ON locations.locationID = stock.locationID " +
                                        "JOIN (SELECT products.barcode, SUM(stock.quantity) AS s_amount " +
                                        "FROM stock JOIN products ON stock.barcode = products.barcode "+
                                        "JOIN locations ON locations.locationID = stock.locationID " +
                                        "WHERE stock.expiration_date >= date('now') AND locations.branchID = ? "+
                                        "AND locations.physical_place = 'Store' "+
                                        "GROUP BY stock.barcode) as store ON store.barcode = products.barcode "+
                                        "JOIN (SELECT products.barcode, SUM(stock.quantity) AS w_amount "+
                                        "FROM stock JOIN products ON stock.barcode = products.barcode "+
                                        "JOIN locations ON locations.locationID = stock.locationID "+
                                        "WHERE stock.expiration_date >= date('now') AND locations.branchID = ? "+
                                        "AND locations.physical_place = 'Warehouse'"+
                                        "GROUP BY stock.barcode) as warehouse ON warehouse.barcode = products.barcode "+
                                        "JOIN product_category ON product_category.barcode = products.barcode\n" +
                                        "JOIN category ON category.categoryID = product_category.categoryID " +
                                        "WHERE stock.expiration_date >= date('now') AND locations.branchID = ? AND category.categoryID = ?"+
                                        "GROUP BY stock.barcode "+
                                        "HAVING total_amount > 0";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activeBranch.getBranchID());
            pstmt.setInt(2, activeBranch.getBranchID());
            pstmt.setInt(3, activeBranch.getBranchID());
            if(category != null)
                pstmt.setInt(4, category.getCategoryID());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                inv.add(createStockFromResult(rs, false));
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return inv;
    }

    public List<StockProducts> findAllInventoryByBarcode(StoreBranch activeBranch, int barcode) {
        List<StockProducts> inv = new ArrayList<>();
        String sql ="SELECT stock.barcode, products.name, IFNULL(store.s_amount, 0) AS s_amount, " +
                    "IFNULL(warehouse.w_amount, 0) AS w_amount, "+
                    "SUM(stock.quantity) AS total_amount, products.minimal_amount "+
                    "FROM stock JOIN products ON stock.barcode = products.barcode " +
                    "JOIN locations ON locations.locationID = stock.locationID " +
                    "JOIN (SELECT products.barcode, SUM(stock.quantity) AS s_amount " +
                    "FROM stock JOIN products ON stock.barcode = products.barcode "+
                    "JOIN locations ON locations.locationID = stock.locationID " +
                    "WHERE stock.expiration_date >= date('now') AND locations.branchID = ? "+
                    "AND locations.physical_place = 'Store' "+
                    "GROUP BY stock.barcode) as store ON store.barcode = products.barcode "+
                    "JOIN (SELECT products.barcode, SUM(stock.quantity) AS w_amount "+
                    "FROM stock JOIN products ON stock.barcode = products.barcode "+
                    "JOIN locations ON locations.locationID = stock.locationID "+
                    "WHERE stock.expiration_date >= date('now') AND locations.branchID = ? "+
                    "AND locations.physical_place = 'Warehouse'"+
                    "GROUP BY stock.barcode) as warehouse ON warehouse.barcode = products.barcode "+
                    "WHERE stock.expiration_date >= date('now') AND locations.branchID = ? AND products.barcode = ? "+
                    "GROUP BY stock.barcode "+
                    "HAVING total_amount > 0";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activeBranch.getBranchID());
            pstmt.setInt(2, activeBranch.getBranchID());
            pstmt.setInt(3, activeBranch.getBranchID());
            pstmt.setInt(4, barcode);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                inv.add(createStockFromResult(rs, false));
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return inv;
    }

    public List<StockProducts> findAllByLocation(StoreBranch activeBranch) {
        List<StockProducts> inv = new ArrayList<>();
        String sql = "SELECT products.name, stock.barcode, locations.locationID, locations.physical_place,\n" +
                "locations.place_identifier, stock.expiration_date, SUM(stock.quantity) AS amount,\n" +
                "products.minimal_amount FROM stock JOIN products ON stock.barcode = products.barcode\n" +
                "JOIN locations ON locations.locationID = stock.locationID\n" +
                "WHERE locations.branchID = ?\n" +
                "GROUP BY stock.barcode, locations.physical_place, locations.place_identifier, stock.expiration_date "+
                "HAVING amount > 0";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activeBranch.getBranchID());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                inv.add(createStockFromResult(rs, true));
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return inv;
    }

    public List<StockProducts> findByBarcodeAndLocation(StoreBranch activeBranch, int barcode) {
        List<StockProducts> inv = new ArrayList<>();
        String sql = "SELECT products.name, stock.barcode, locations.locationID, locations.physical_place,\n" +
                "locations.place_identifier, stock.expiration_date, SUM(stock.quantity) AS amount,\n" +
                "products.minimal_amount FROM stock JOIN products ON stock.barcode = products.barcode\n" +
                "JOIN locations ON locations.locationID = stock.locationID\n" +
                "WHERE locations.branchID = ? AND products.barcode = ?\n" +
                "GROUP BY stock.barcode, locations.physical_place, locations.place_identifier, stock.expiration_date "+
                "HAVING amount > 0";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activeBranch.getBranchID());
            pstmt.setInt(2, barcode);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                inv.add(createStockFromResult(rs, true));
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return inv;
    }

    private StockProducts createStockFromResult(ResultSet rs, boolean location) throws SQLException {
        return location ? new StockProducts(rs.getString("name"),
                                            rs.getInt("barcode"),
                                            Date.parseDate(rs.getString("expiration_date")),
                                            rs.getInt("amount"),
                                            rs.getInt("minimal_amount"),
                                            rs.getInt("locationID"),
                                            rs.getString("physical_place") +", Shelf "+rs.getString("place_identifier")) :
                        new StockProducts(rs.getString("name"),
                                        rs.getInt("barcode"),
                                        rs.getInt("total_amount"),
                                        rs.getInt("minimal_amount"),
                                        rs.getInt("s_amount"),
                                        rs.getInt("w_amount"));
    }

    @Override
    public void setConnection(Connection conn)
    {
        this.conn = conn;
    }
}
