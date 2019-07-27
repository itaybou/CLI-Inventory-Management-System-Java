package presistence.dao;

import logic.models.Product;
import logic.models.ProductCategory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO implements DAO<ProductCategory, Integer, String, Integer> {

    private Connection conn = null; //Connection instance

    @Override
    public Result update(ProductCategory category, Integer key, String newName, Integer key3) {

        String sql = "UPDATE category SET name=?, discounted = ?, discounter = ? WHERE name = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category.getName());
            pstmt.setObject(2, category.getDiscounted(), Types.DOUBLE);
            pstmt.setString(3, category.getDiscounter());
            pstmt.setString(4, newName);
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Result delete(ProductCategory productCategory) {
        String sql = "DELETE FROM category WHERE name = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productCategory.getName());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    public Result deleteAllByProduct(ProductCategory productCategory) {
        String sql = "DELETE FROM product_category WHERE barcode = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productCategory.getBarcode());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Result insert(ProductCategory productCategory) {
        String sql = "INSERT INTO product_category(barcode, categoryID, hierarchy)" +
                    " VALUES (?, (SELECT categoryID FROM category WHERE name = ?), ?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productCategory.getBarcode());
            pstmt.setString(2, productCategory.getName());
            pstmt.setInt(3, productCategory.getHierarchy());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public ProductCategory findByKey(ProductCategory productCategory) {
        String sql = "SELECT * FROM product_category WHERE barcode = ? AND categoryID = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productCategory.getBarcode());
            pstmt.setInt(2, productCategory.getCategoryID());
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return new ProductCategory(rs.getInt("categoryID"),
                                            rs.getInt("barcode"),
                                            rs.getInt("hierarchy"));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public ProductCategory findCategoryByKey(Integer key) {
        String sql = "SELECT * FROM category WHERE categoryID = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, key);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return new ProductCategory(rs.getInt("categoryID"),
                                            rs.getString("name"), 0);
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public ProductCategory findByName(ProductCategory productCategory) {
        String sql = "SELECT * FROM category WHERE name = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productCategory.getName());
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return new ProductCategory(rs.getInt("categoryID"),
                                             rs.getString("name"));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Result insertCategory(ProductCategory category) {
        if(findByName(category) != null)
            return Result.FAIL;
        String sql = "INSERT INTO category(name) VALUES (?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category.getName());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    public List<ProductCategory> findAllByProduct(Product prod)
    {
        List<ProductCategory> products = new ArrayList<>();;
        String sql = "SELECT category.categoryID, category.name, hierarchy FROM " +
                    "product_category JOIN category WHERE barcode = ?" +
                    "AND product_category.categoryID = category.categoryID";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, prod.getBarcode());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
                products.add(new ProductCategory(rs.getInt("categoryID"),
                                                    rs.getString("name"),
                                                    rs.getInt("hierarchy")));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return products;
    }

    public List<ProductCategory> findAllDiscounted(String discounter)
    {
        List<ProductCategory> products = new ArrayList<>();
        String sql = "SELECT * FROM category WHERE discounted IS NOT NULL AND discounter IS NOT NULL";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next())
                products.add(new ProductCategory(rs.getInt("categoryID"),
                                                rs.getString("name"),
                                                rs.getDouble("discounted"),
                                                rs.getString("discounter")));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return products;
    }

    public List<ProductCategory> findAll()
    {
        List<ProductCategory> products = new ArrayList<>();;
        String sql = "SELECT * FROM category";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next())
                products.add(new ProductCategory(rs.getInt("categoryID"),
                                                rs.getString("name")));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return products;
    }

    @Override
    public void setConnection(Connection conn)
    {
        this.conn = conn;
    }
}
