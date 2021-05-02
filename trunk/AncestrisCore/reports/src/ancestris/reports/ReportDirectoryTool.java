package ancestris.reports;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.report.Report;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 */
@ServiceProvider(service = Report.class)
public class ReportDirectoryTool extends Report {

    // check all relevant tags by default	
    public int optionLifeSpan = 70;

    //translate strings for output  
    private final String textTitle = translate("title");
    private final String textDate = translate("date");
    private final String textAge = translate("age");
    private final String textSelect = translate("select");
    private final String total = translate("total");

    public void start(Gedcom gedcom) {

        int loop;
        int tempDOBYear, tempDODYear;
        int yearToCheck;
        int iAge;
        String strTemp;
        String strOccu;
        Entity[] individuals;
        Boolean flagHasOccu;

        //get the year of interest
        strTemp = getValueFromUser("", textSelect);

        if (strTemp == null) {
            return;
        }

        //catch silly input
        try {
            yearToCheck = Integer.parseInt(strTemp);
        } catch (java.lang.NumberFormatException e) {
            println("Invalid year");
            return;
        }

        //print report title
        println(textTitle + " " + yearToCheck);
        println();

        final List<String> indiToPrint = new ArrayList<>();

        //list anyone in the file who was alive in the specified year
        //use the life span option if a birth or death date is missing
        //include their age in the given year and any occupation info
        //get all
        individuals = gedcom.getEntities(Gedcom.INDI, "");

        for (loop = 0; loop < individuals.length; loop++) {

            //get birth
            tempDOBYear = getYear(((Indi) individuals[loop]).getBirthDate());
            //get death
            tempDODYear = getYear(((Indi) individuals[loop]).getDeathDate());

            //ignore anyone who has neither birth or death date
            if (!((tempDOBYear == -1) && (tempDODYear == -1))) {

                //missing birth year?
                if (tempDOBYear == -1) {
                    tempDOBYear = tempDODYear - optionLifeSpan;
                }

                //no death year?
                if (tempDODYear == -1) {
                    tempDODYear = tempDOBYear + optionLifeSpan;
                }

                //if target year in range print indi
                if ((yearToCheck >= tempDOBYear) && (yearToCheck <= tempDODYear)) {
                    //calc age
                    iAge = yearToCheck - tempDOBYear;

                    flagHasOccu = false;
                    //display
                    //if occu data add it to string for display
                    if (individuals[loop].getProperty(new TagPath("INDI:OCCU")) == null) {
                        strOccu = "";
                    } else {
                        strOccu = individuals[loop].getProperty(new TagPath("INDI:OCCU")).getDisplayValue();
                        flagHasOccu = true;
                    }

                    if ((flagHasOccu) && (individuals[loop].getProperty(new TagPath("INDI:OCCU:DATE")) != null)) {
                        strOccu += " " + individuals[loop].getProperty(new TagPath("INDI:OCCU:DATE")).getDisplayValue();
                    }

                    if ((flagHasOccu) && (individuals[loop].getProperty(new TagPath("INDI:OCCU:PLAC")) != null)) {
                        strOccu += " " + individuals[loop].getProperty(new TagPath("INDI:OCCU:PLAC")).getDisplayValue();
                    }

                    if (strOccu.length() > 0) {
                        strOccu = "[" + strOccu + "]";
                    }

                    indiToPrint.add(individuals[loop] + " - " + textAge + " " + iAge + " " + strOccu);
                }
            }
        }// end for loop
        indiToPrint.stream().sorted().forEach(this::println);

        println();

        println(total + " " + indiToPrint.size());
    }

    public int getYear(PropertyDate someDate) {

        //check for null, invalid or range-type birth date
        if ((someDate == null) || (!someDate.isValid()) || (someDate.isRange())) {
            return -1;
        }

        int year = -1;
        try {
            year = someDate.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
        } catch (GedcomException ex) {
            //Exceptions.printStackTrace(ex);
        }

        //get year 
        return year;
    }

}
