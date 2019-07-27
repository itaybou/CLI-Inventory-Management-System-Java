package presistence.dao;

import logic.models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductsDAO implements DAO<Product, Integer, Integer, Integer> {

    private Connection conn = null; //Connection instance

    @Override
    public Result update(Product prod, Integer key, Integer dummy, Integer dummy2) {
        String sql = "UPDATE products SET name=?, manufacturer=?, cost_price=?, selling_price=?,  " +
                     "orig_cost_price=?, orig_selling_price=?, minimal_amount=? WHERE barcode = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, prod.getName());
            pstmt.setString(2, prod.getManufacturer());
            pstmt.setDouble(3, prod.getCost_price());
            pstmt.setDouble(4, prod.getSelling_price());
            pstmt.setDouble(5, prod.getOrig_cost_price());
            pstmt.setDouble(6, prod.getOrig_selling_price());
            pstmt.setDouble(7, prod.getMinimal_amount());
            pstmt.setInt(8, key);
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Result delete(Product prod) {
        if(findByKey(prod) == null)
            return Result.FAIL;
        String sql = "DELETE FROM products WHERE barcode = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, prod.getBarcode());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Result insert(Product prod) {
        if(findByKey(prod) != null)
            return Result.FAIL;
        String sql = "INSERT INTO products" +
                    "(barcode, name, manufacturer, cost_price, selling_price, orig_cost_price, orig_selling_price, minimal_amount)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, prod.getBarcode());
            pstmt.setString(2, prod.getName());
            pstmt.setString(3, prod.getManufacturer());
            pstmt.setDouble(4, prod.getCost_price());
            pstmt.setDouble(5, prod.getSelling_price());
            pstmt.setDouble(6, prod.getOrig_cost_price());
            pstmt.setDouble(7, prod.getOrig_selling_price());
            pstmt.setInt(8, prod.getMinimal_amount());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Product findByKey(Product prod) {
        String sql = "SELECT * FROM products WHERE barcode = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, prod.getBarcode());
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return createProductFromResult(rs);
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<Product> findAll()
    {
        List<Product> products = new ArrayList<>();;
        String sql = "SELECT * FROM products";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next())
                 products.add(createProductFromResult(rs));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return products;
    }

    public List<Product> findAllLike(String like)
    {
        List<Product> products = new ArrayList<>();;
        String sql = "SELECT * FROM products WHERE products.name LIKE ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%"+like+"%");
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                products.add(createProductFromResult(rs));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return products;
    }

    public List<Product> findAllByCategory(String name, int categoryID)
    {
        List<Product> products = new ArrayList<>();;
        String sql = (name == null) ? "SELECT products.barcode, products.name, products.manufacturer," +
                                        "products.cost_price, products.selling_price, products.orig_cost_price, "+
                                        "products.orig_selling_price, products.minimal_amount " +
                                        "FROM products JOIN product_category " +
                                        "WHERE products.barcode = product_category.barcode " +
                                        "AND product_category.categoryID = ?" :
                                   "SELECT products.barcode, products.name, products.manufacturer," +
                                        "products.cost_price, products.selling_price, products.orig_cost_price, "+
                                        "products.orig_selling_price, products.minimal_amount " +
                                        "FROM products JOIN product_category JOIN category " +
                                        "WHERE products.barcode = product_category.barcode " +
                                        "AND category.categoryID = product_category.categoryID AND category.name = ?" ;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            if(name == null)
                pstmt.setInt(1, categoryID);
            else pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                products.add(createProductFromResult(rs));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return products;
    }

    private Product createProductFromResult(ResultSet rs)
    {
        try {
            return new Product(rs.getInt("barcode"),
                                rs.getString("name"),
                                rs.getString("manufacturer"),
                                rs.getDouble("cost_price"),
                                rs.getDouble("selling_price"),
                                rs.getDouble("orig_cost_price"),
                                rs.getDouble("orig_selling_price"),
                                rs.getInt("minimal_amount"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void setConnection(Connection conn)
    {
        this.conn = conn;
    }
}
