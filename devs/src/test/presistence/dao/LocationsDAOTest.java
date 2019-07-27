package presistence.dao;

import logic.Modules;
import logic.models.Location;
import logic.models.StoreBranch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import presistence.Repository;

import java.util.Random;

import static org.junit.Assert.*;

public class LocationsDAOTest {

    private Location l;
    private StoreBranch branch;
    private Repository repo;
    private Random random = new Random();

    @Before
    public void setUp() throws Exception {
        repo = new Repository();
        repo.connect();
    }

    @After
    public void tearDown() throws Exception {
        repo.closeConnection();
    }

    @Test
    public void insert() {
        int n = random.nextInt(10000);
        branch = new StoreBranch("test"+n);
        l = new Location(999,1, "test"+n, "test"+n);
        ((BranchesDAO) repo.getDAO(Modules.BRANCHES)).insert(branch);
        branch = ((BranchesDAO) repo.getDAO(Modules.BRANCHES)).findByName(new StoreBranch("test"+n));
        l.setBranchID(branch.getBranchID());
        ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).insert(l);
        assertNotNull(l);

        ((BranchesDAO) repo.getDAO(Modules.BRANCHES)).delete(branch);
        ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).delete(l);

    }

    @Test
    public void findByKey() {
        int n = random.nextInt(10000);
        l = new Location(999,1, "test"+n, "test"+n);
        branch = new StoreBranch("test"+n);
        ((BranchesDAO) repo.getDAO(Modules.BRANCHES)).insert(branch);
        branch = ((BranchesDAO) repo.getDAO(Modules.BRANCHES)).findByName(new StoreBranch("test"+n));
        l.setBranchID(branch.getBranchID());
        ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).insert(l);
        l = ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).findByKey(l);
        assertNull(l);
    }

    @Test
    public void update() {
        int n = random.nextInt(10000);
        l = new Location(999,1, "test"+n, "test"+n);
        branch = new StoreBranch("test"+n);
        ((BranchesDAO) repo.getDAO(Modules.BRANCHES)).insert(branch);
        branch = ((BranchesDAO) repo.getDAO(Modules.BRANCHES)).findByName(new StoreBranch("test"+n));
        l.setBranchID(branch.getBranchID());
        ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).insert(l);
        l.setPhysical_place("test2");
        ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).update(l, 999, 999, 0);
        ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).delete(l);
        assertEquals(l.getLocationID(), 999);
    }

    @Test
    public void delete() {
        int n = random.nextInt(10000);
        l = new Location(999,1, "test"+n, "test"+n);
        branch = new StoreBranch("test"+n);
        ((BranchesDAO) repo.getDAO(Modules.BRANCHES)).insert(branch);
        branch = ((BranchesDAO) repo.getDAO(Modules.BRANCHES)).findByName(new StoreBranch("test"+n));
        l.setBranchID(branch.getBranchID());
        ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).insert(l);
        ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).delete(l);
        l = ((LocationsDAO) repo.getDAO(Modules.LOCATIONS)).findByKey(l);
        assertNull(l);
    }
}