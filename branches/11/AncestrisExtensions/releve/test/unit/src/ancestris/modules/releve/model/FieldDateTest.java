package ancestris.modules.releve.model;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class FieldDateTest {

    /**
     * Test of setValue method, of class FieldDate.
     */
    @Test
    public void test_SetValue_Object() {
        FieldDate fieldDate = new FieldDate();

        fieldDate.setValue("01/02/1999");
        assertEquals("set jj/mm/aaaa", "1 fév 1999", fieldDate.getPropertyDate().getDisplayValue());
        assertEquals("set jj/mm/aaaa", "1 FEB 1999", fieldDate.getPropertyDate().getValue());
        assertEquals("set jj/mm/aaaa", "Date 1 fév 1999", fieldDate.getPropertyDate().toString());
        assertEquals("set jj/mm/aaaa", "01/02/1999", fieldDate.getValue());

        fieldDate.setValue("03/1999");
        assertEquals("set mm/aaaa", "MAR 1999", fieldDate.getPropertyDate().getValue());
        assertEquals("valid mm/aaaa", true, fieldDate.getPropertyDate().isValid());
        assertEquals("comparable mm/aaaa", true, fieldDate.getPropertyDate().isComparable());

        fieldDate.setValue("1999");
        assertEquals("set aaaa", "1999", fieldDate.getPropertyDate().getValue());
        assertEquals("valid aaaa", true, fieldDate.getPropertyDate().isValid());
        assertEquals("comparable aaaa", true, fieldDate.getPropertyDate().isComparable());

        fieldDate.setValue("1 FEB 2000");
        assertEquals("set jj mmm aaaa", "1 fév 2000", fieldDate.getPropertyDate().getDisplayValue());
        fieldDate.setValue("2 fév 2002");
        assertEquals("set jj mmm aaaa", false, fieldDate.getPropertyDate().isValid());
        assertEquals("set jj mmm aaaa", "2 fév 2002", fieldDate.getPropertyDate().getValue());

        fieldDate.setValue("FEB 2000");
        assertEquals("set mmm aaaa", "fév 2000", fieldDate.getPropertyDate().getDisplayValue());

    }

    /**
     * Test of setValue method, of class FieldDate.
     */
    public void test_GetFranchValue() {
        FieldDate fieldDate = new FieldDate();

        fieldDate.setValue("01/01/1800");
        assertEquals("set jj/mm/aaaa", "11 Nivô An VIII", fieldDate.getFrenchCalendarValue());
    }
}
