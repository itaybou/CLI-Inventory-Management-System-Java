package presistence;

import logic.Modules;
import org.sqlite.SQLiteConfig;
import presistence.dao.*;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A class used to communicate with the database
 */
public class Repository {

    //Fields
    private Map<Modules, DAO> daoMap; //Maps all DAO type to corresponding DAO objects

    private final String DB_NAME = "inventory.db"; //Database file name
    private final String CONN_URL = "jdbc:sqlite:"; //Database connection string
    private final String DRIVER = "org.sqlite.JDBC";
    private static Connection conn = null; //Conenction to database

    //Constructor
    public Repository () {
        //Map Entity type to corresponding DAO object
        daoMap = new HashMap<>() {{
            put(Modules.BRANCHES, new BranchesDAO());
            put(Modules.PRODUCTS, new ProductsDAO());
            put(Modules.DISCOUNTS, new DiscountsDAO());
            put(Modules.CATEGORIES, new CategoryDAO());
            put(Modules.LOCATIONS, new LocationsDAO());
            put(Modules.DEFECTS, new DefectiveDAO());
            put(Modules.STOCK, new StockDAO());
        }};
    }

    /**
     * Set the connection instance to connect to database using the connection string and DB filname
     * @throws SQLException
     */
    private void setConnection() throws SQLException
    {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        conn = DriverManager.getConnection(CONN_URL+DB_NAME, config.toProperties());
        conn.setAutoCommit(false);
    }

    /**
     * Initiates the connection to the database
     * If database file does not exists, creates it.
     * Sets the connection instance and sets all DAOs connection.
     */
    public void connect()
    {
        try{
            Class.forName(DRIVER);
            if(!(new File(DB_NAME).isFile()))
                createDB();
            setConnection();
            daoMap.values().forEach(dao -> dao.setConnection(conn));
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Closes the connection to the database
     */
    public void closeConnection()
    {
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Sets the connection and creates the database tables.
     * The master database user is inserted on creation.
     * @throws SQLException
     */
    private void createDB() throws SQLException
    {
        setConnection();
        Statement stmt = conn.createStatement();
        //Create DB Tables

        String sql = "CREATE TABLE IF NOT EXISTS products (\n"
                +   "   barcode INTEGER PRIMARY KEY,\n"
                +   "	name TEXT NOT NULL,\n"
                +   "   manufacturer TEXT DEFAULT 'Unknown',\n"
                +   "   cost_price REAL NOT NULL,\n"
                +   "   selling_price REAL NOT NULL,\n"
                +   "   orig_cost_price REAL NOT NULL,\n"
                +   "   orig_selling_price REAL NOT NULL,\n"
                +   "   minimal_amount INTEGER NOT NULL);"; //TODO maybe add constraint selling >= cost
        stmt.execute(sql);

            sql =   " CREATE TABLE IF NOT EXISTS discounts (\n"
                +   "   discountID INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                +   "   barcode INTEGER NOT NULL, \n"
                +   "   discounter TEXT NOT NULL,\n"
                +   "	percentage REAL NOT NULL,\n"
                +   "   date_given TEXT NOT NULL,\n"
                +   "   date_ended TEXT DEFAULT NULL,\n"
                +   "   FOREIGN KEY(barcode) REFERENCES products(barcode) ON DELETE CASCADE ON UPDATE CASCADE);";
        stmt.execute(sql);

            sql =   " CREATE TABLE IF NOT EXISTS stock (\n"
                +   "   barcode INTEGER NOT NULL,\n"
                +   "	expiration_date TEXT NOT NULL,\n"
                +   "   locationID INTEGER NOT NULL,\n"
                +   "   quantity INTEGER NOT NULL,\n"
                +   "   FOREIGN KEY(locationID) REFERENCES locations(locationID) ON DELETE CASCADE ON UPDATE CASCADE,\n"
                +   "   PRIMARY KEY (barcode, expiration_date, locationID)," +
                    "   CONSTRAINT quantity_amount CHECK (quantity >= 0));";
        stmt.execute(sql);

            sql =   " CREATE TABLE IF NOT EXISTS defects (\n"
                +   "   barcode INTEGER NOT NULL,\n"
                +   "   locationID INTEGER NOT NULL,\n"
                +   "	quantity INTEGER NOT NULL,\n"
                +   "   reason TEXT NOT NULL,\n"
                +   "   date_reported TEXT NOT NULL,\n"
                +   "   FOREIGN KEY(locationID) REFERENCES locations(locationID) ON DELETE CASCADE ON UPDATE CASCADE,\n"
                +   "   FOREIGN KEY(barcode) REFERENCES products(barcode) ON DELETE NO ACTION ON UPDATE CASCADE,\n"
                +   "   PRIMARY KEY (barcode, locationID));";
        stmt.execute(sql);

            sql =   " CREATE TABLE IF NOT EXISTS category (\n"
                +   "   categoryID INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                +   "   name TEXT NOT NULL UNIQUE," +
                    "   discounted INTEGER DEFAULT NULL," +
                    "   discounter TEXT DEFAULT NULL);";
        stmt.execute(sql);

            sql =   " CREATE TABLE IF NOT EXISTS product_category (\n"
                +   "   barcode INTEGER NOT NULL,\n"
                +   "	categoryID INTEGER NOT NULL,\n"
                +   "   hierarchy INTEGER NOT NULL,\n"
                +   "   FOREIGN KEY(barcode) REFERENCES products(barcode) ON DELETE CASCADE ON UPDATE CASCADE,\n"
                +   "   FOREIGN KEY(categoryID) REFERENCES category(categoryID) ON DELETE CASCADE ON UPDATE CASCADE,\n"
                +   "   PRIMARY KEY(barcode, categoryID));";
        stmt.execute(sql);

            sql =   " CREATE TABLE IF NOT EXISTS branches (\n"
                +   "   branchID INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                +   "	name TEXT NOT NULL UNIQUE);";
            stmt.execute(sql);

            sql =   " CREATE TABLE IF NOT EXISTS locations (\n"
                +   "   locationID INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                +   "	branchID INTEGER NOT NULL,\n"
                +   "   physical_place TEXT NOT NULL,\n"
                +   "   place_identifier TEXT NOT NULL,\n"
                +   "   UNIQUE (physical_place, place_identifier),\n"
                +   "   FOREIGN KEY(branchID) REFERENCES branches(branchID) ON DELETE CASCADE ON UPDATE CASCADE);";
        stmt.execute(sql);
        conn.commit();
        createTriggers();
    }


    /**
     * Sets the connection and creates the database tables.
     * The master database user is inserted on creation.
     * @throws SQLException
     */
    private void createTriggers() throws SQLException
    {
        Statement stmt = conn.createStatement();
        String sql =" CREATE TRIGGER start_discount_store AFTER INSERT ON discounts " +
                    " BEGIN " +
                    "       UPDATE products SET selling_price = (selling_price -(selling_price * NEW.percentage/100)) " +
                    "       WHERE products.barcode = NEW.barcode AND NEW.discounter='Store';" +
                    " END;";
        stmt.execute(sql);

        sql = " CREATE TRIGGER reset_discount_store AFTER UPDATE ON discounts " +
                " FOR EACH ROW WHEN NEW.date_ended IS NOT NULL AND NEW.discounter='Store'"+
                " BEGIN " +
                "       UPDATE products SET selling_price = (SELECT orig_selling_price FROM products WHERE barcode = OLD.barcode) " +
                "       WHERE products.barcode = OLD.barcode AND OLD.discounter='Store';"+
                " END;";
        stmt.execute(sql);

        sql =" CREATE TRIGGER start_discount_supplier AFTER INSERT ON discounts " +
                " BEGIN " +
                "       UPDATE products SET cost_price = (cost_price -(cost_price * NEW.percentage/100)) " +
                "       WHERE products.barcode = NEW.barcode AND NEW.discounter='Supplier';" +
                " END;";
        stmt.execute(sql);

        sql = " CREATE TRIGGER reset_discount_supplier AFTER UPDATE ON discounts " +
                " FOR EACH ROW WHEN NEW.date_ended IS NOT NULL AND NEW.discounter='Supplier'"+
                " BEGIN " +
                "       UPDATE products SET cost_price = (SELECT orig_cost_price FROM products WHERE barcode = OLD.barcode) " +
                "       WHERE products.barcode = OLD.barcode AND OLD.discounter='Supplier';"+
                " END;";
        stmt.execute(sql);

        sql = " CREATE TRIGGER remove_finished_stock AFTER UPDATE ON stock " +
                " FOR EACH ROW WHEN NEW.quantity = 0"+
                " BEGIN " +
                "       DELETE FROM stock WHERE barcode = NEW.barcode AND" +
                "       expiration_date = NEW.expiration_date AND locationID = NEW.expiration_date;" +
                " END;";
        stmt.execute(sql);

        conn.commit();
    }

    //Getters
    public DAO getDAO(Modules daoType) {
        return daoMap.get(daoType);
    }

}
