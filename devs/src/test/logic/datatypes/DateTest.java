package logic.datatypes;

import org.junit.Test;

import static org.junit.Assert.*;

public class DateTest {

    private Date d;

    @Test
    public void checkLegalDate() {
        d = new Date(32, 13, 2019);
        boolean test1 = Date.checkLegalDate(d);
        assertFalse(test1);

        d = new Date(04, 04, 2018);
        boolean test2 = Date.checkLegalDate(d);
        assertFalse(test2);
    }

    @Test
    public void checkParseDate() {

        Date d = Date.parseDate("2019-04-04");
        assertEquals(d.getDay(), 4);
        assertEquals(d.getMonth(), 4);
        assertEquals(d.getYear(), 2019);
    }
}