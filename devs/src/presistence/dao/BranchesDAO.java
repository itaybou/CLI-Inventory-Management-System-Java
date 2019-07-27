package presistence.dao;

import logic.models.Product;
import logic.models.StoreBranch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranchesDAO implements DAO<StoreBranch, Integer, Integer, Integer> {

    private Connection conn = null; //Connection instance

    @Override
    public Result update(StoreBranch branch, Integer key, Integer dummy, Integer dummy2) {
        String sql = "UPDATE discounts SET name = ? WHERE branchID = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, branch.getName());
            pstmt.setInt(2, key);
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Result delete(StoreBranch branch) {
        if(findByKey(branch) == null)
            return Result.FAIL;
        String sql = "DELETE FROM branches WHERE branchID = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, branch.getBranchID());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Result insert(StoreBranch branch) {
        if(findByKey(branch) != null)
            return Result.FAIL;
        String sql = "INSERT INTO branches(name) VALUES (?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, branch.getName());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public StoreBranch findByKey(StoreBranch branch) {
        String sql = "SELECT * FROM branches WHERE branchID = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, branch.getBranchID());
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return new StoreBranch(rs.getInt("branchID"),
                                        rs.getString("name"));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public StoreBranch findByName(StoreBranch branch) {
        String sql = "SELECT * FROM branches WHERE name = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, branch.getName());
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return new StoreBranch(rs.getInt("branchID"),
                                        rs.getString("name"));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<StoreBranch> findAll()
    {
        List<StoreBranch> products = new ArrayList<>();;
        String sql = "SELECT * FROM branches";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next())
                products.add(new StoreBranch(rs.getInt("branchID"),
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
