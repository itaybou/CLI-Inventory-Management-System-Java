package presistence.dao;

import logic.models.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LocationsDAO implements DAO<Location, Integer, Integer,Integer> {

    private Connection conn = null; //Connection instance

    @Override
    public Result update(Location location, Integer key1, Integer key2, Integer key3) {
        return null;
    }

    @Override
    public Result delete(Location location) {
        return null;
    }

    @Override
    public Result insert(Location location) {
        String sql = "INSERT INTO locations" +
                    "(branchID, physical_place, place_identifier)" +
                    " VALUES (?, ?, ?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, location.getBranchID());
            pstmt.setString(2, location.getPhysical_place());
            pstmt.setString(3, location.getPlace_identifier().toUpperCase());
            pstmt.executeUpdate();
            conn.commit();
            return Result.SUCCESS;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Result.FAIL;
    }

    @Override
    public Location findByKey(Location location) {
        String sql = "SELECT * FROM locations WHERE locationID = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, location.getLocationID());
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return new Location(rs.getInt("locationID"),
                                    rs.getInt("branchID"),
                                    rs.getString("physical_place"),
                                    rs.getString("place_identifier"));
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Location findLocation(Location location) {
        String sql = "SELECT * FROM locations WHERE branchID = ? AND physical_place = ? AND place_identifier = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, location.getBranchID());
            pstmt.setString(2, location.getPhysical_place());
            pstmt.setString(3, location.getPlace_identifier());
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return new Location(rs.getInt("locationID"),
                                    rs.getInt("branchID"),
                                    rs.getString("physical_place"),
                                    rs.getString("place_identifier"));
            rs.close();
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
