package presistence.dao;

import logic.Modules;
import logic.models.Product;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import presistence.Repository;

import static org.junit.Assert.*;

public class ProductsDAOTest {

    private  Product p;
    private Repository repo;
    private final int barcode = 1;

    @Before
    public void setUp() throws Exception {
        repo = new Repository();
        repo.connect();
        p = new Product(barcode, "test", "test", 0, 0, 0, 0, 0);
    }

    @After
    public void tearDown() throws Exception {
        repo.closeConnection();
    }

    @Test
    public void insert() {
        ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).insert(p);
        assertNotNull(p);
    }

    @Test
    public void findByKey() {
        p = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findByKey(p);
        assertNotNull(p);
    }

    @Test
    public void update() {
        p.setBarcode(2);
        ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).update(p, barcode, 0,0);
        p = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findByKey(p);
        assertEquals(p.getBarcode(), 2);
    }

    @Test
    public void delete() {
        ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).delete(p);
        p = ((ProductsDAO)repo.getDAO(Modules.PRODUCTS)).findByKey(p);
        assertNull(p);
    }

}