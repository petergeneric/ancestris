package ancestris.modules.releve.model;

import genj.gedcom.PropertyDate;
import genj.gedcom.time.PointInTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 *
 * @author Michel
 */
public class FieldDate extends Field {

    PropertyDate eventDate = new PropertyDate();

    @Override
    public String toString() {
        return eventDate.toString();
    }

    @Override
    public String getValue() {
        if (eventDate.isValid()) {
            PointInTime pit = eventDate.getStart();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Calendar cal = Calendar.getInstance();
            cal.set(pit.getYear(), pit.getMonth(), pit.getDay() + 1);
            //cal.roll(Calendar.DATE, true); // soustrait un jour
            return dateFormat.format(cal.getTime()).trim();
        } else {
            return "";
        }
    }

    @Override
    public void setValue(Object dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateFormat.parse(dateString.toString().trim()));
            PointInTime pit = new PointInTime(cal);
            eventDate.setValue(eventDate.getFormat(), pit, null, null);
        } catch (ParseException ex) {
            //je laisse la date nulle
        }
    }

    public void setValue(String strDay, String strMonth, String strYear) throws NumberFormatException {
        try {
            int day = PointInTime.UNKNOWN;
            int month = PointInTime.UNKNOWN;
            int year = PointInTime.UNKNOWN;
            if (!strDay.isEmpty()) {
                day = Integer.parseInt(strDay)-1;
            }
            if (!strMonth.isEmpty()) {
                month = Integer.parseInt(strMonth)-1;
            }
            if (!strYear.isEmpty()) {
                year = Integer.parseInt(strYear);
            }
            PointInTime pit = new PointInTime(day, month, year);
            eventDate.setValue(eventDate.getFormat(), pit, null, null);
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Error "+strDay+ " " +ex);
        }

    }

    public PropertyDate getPropertyDate() {
        return eventDate;
    }

    /**
     * Compares this field to another field
     * @return  a negative integer, zero, or a positive integer as this object
     *      is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Field that) {
        if ( that instanceof FieldDate) {
            return eventDate.compareTo(((FieldDate)that).getPropertyDate());
        } else {
            return -1; 
        }
        
    }

    @Override
    public boolean isEmpty() {
        return eventDate.isValid();
    }

}
