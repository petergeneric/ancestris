package ancestris.modules.releve.model;

import genj.gedcom.GedcomException;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.PointInTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author Michel
 */
public class FieldDate extends Field implements Cloneable {
    static private Pattern jjmmaaaa = Pattern.compile("([0-9]{1,2})/([0-9]{1,2})/([0-9]{4,4})");
    static private Pattern mmaaaa = Pattern.compile("([0-9]{1,2})/([0-9]{4,4})");
    static private Pattern aaaa = Pattern.compile("([0-9]{4,4})");

    PropertyDate eventDate = new PropertyDate();

    @Override
    public String toString() {
         return getValueDDMMYYYY();
    }

    @Override
    public String getValue() {
//        if (eventDate.isValid()) {
//            try {
//                // je recupere la date dans le calendrier gregorien
//                PointInTime pit = eventDate.getStart().getPointInTime(PointInTime.GREGORIAN);
//                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//                Calendar cal = Calendar.getInstance();
//                cal.set(pit.getYear(), pit.getMonth(), pit.getDay() + 1);
//                //cal.roll(Calendar.DATE, true); // soustrait un jour
//                return dateFormat.format(cal.getTime()).trim();
//            } catch (GedcomException ex) {
//                return "";
//            }
//        } else {
//            return "";
//        }
        return getValueDDMMYYYY();
    }

    /**
     * retounr le date au format JJ/MM/AAAA
     * @return
     */
    public String getValueDDMMYYYY() {
        try {
            String result;
            // je recupere la date dans le calendrier gregorien
            PointInTime pit = eventDate.getStart().getPointInTime(PointInTime.GREGORIAN);
            if (pit.getYear() == PointInTime.UNKNOWN )  {
                result = "";
            } else if ( pit.getMonth() == PointInTime.UNKNOWN) {
                result = String.format("%04d", pit.getYear());
            } else if ( pit.getDay() == PointInTime.UNKNOWN) {
                result = String.format("%02d/%04d", pit.getMonth()+1, pit.getYear());
            } else {
                result = String.format("%02d/%02d/%04d", pit.getDay()+1, pit.getMonth()+1, pit.getYear());
            }
            return result;
        } catch (GedcomException ex) {
            return "";
        }
    }

    /**
     * enregistre une date
     * les formats acceptés sont :
     *   dd/mm/yyyy ,
     *   mm/yyyy ,
     *   yyyy ,
     *   ou les formats acceptés par PropertyDate.setValue(...)
     * @param dateString
     */
    @Override
    public void setValue(Object dateString) {
            String inputDate = dateString.toString().trim();
            Matcher matcher =  jjmmaaaa.matcher(inputDate);
            if ( matcher.matches() ) {
                PointInTime pit = new PointInTime(
                        Integer.parseInt(matcher.group(1)) -1,
                        Integer.parseInt(matcher.group(2)) -1,
                        Integer.parseInt(matcher.group(3)));
                eventDate.setValue(eventDate.getFormat(), pit, null, null);
            } else {
                matcher =  mmaaaa.matcher(inputDate);
                if ( matcher.matches()) {
                PointInTime pit = new PointInTime(
                        PointInTime.UNKNOWN,
                        Integer.parseInt(matcher.group(1)) -1,
                        Integer.parseInt(matcher.group(2)));
                eventDate.setValue(eventDate.getFormat(), pit, null, null);
                } else  {
                    matcher =  aaaa.matcher(inputDate);
                    if ( matcher.matches()) {
                        PointInTime pit = new PointInTime(
                                PointInTime.UNKNOWN,
                                PointInTime.UNKNOWN,
                                Integer.parseInt(matcher.group(1)));
                        eventDate.setValue(eventDate.getFormat(), pit, null, null);
                    } else {
                        eventDate.setValue(inputDate);
                    }
                }
            }
       
    }

    /**
     * enregistre une date , le jour, mois et années etant fournis sous forme de chaines de caractere
     * @param strDay
     * @param strMonth
     * @param strYear
     * @throws NumberFormatException
     */public void setValue(String strDay, String strMonth, String strYear) throws NumberFormatException {
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
        return toString().isEmpty();
    }

    @Override
    public FieldDate clone() {
	    FieldDate object = null;
        object = new FieldDate();
        object.getPropertyDate().setValue(getPropertyDate().getValue());
		// je renvoie le clone
		return object;
  	}
}
