package ancestris.reports;

import genj.gedcom.GedcomException;
import java.util.ArrayList;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.PointInTime;
import genj.report.Report;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 * author = Paul Robinson version = 1.0 - 23 Jan 2010
 */
@ServiceProvider(service = Report.class)
public class ReportPedigree extends Report {

    //translate strings for output  
    private String textTitle = translate("title");
    private String textSelect = translate("select");
    private String textUnknown = translate("unknown");

    public void start(Indi indi) {

        //do report
        doReport(indi);
    }

    //action!
    public void doReport(Indi indi) {

        //variables
        ArrayList<Indi> parseList = new ArrayList<>();
        ArrayList<Indi> tempList = new ArrayList<>();
        int iGen, iLoop, iTemp;
        Indi tempIndi;
        Indi dummyIndi;
        int iGenerations;
        Object aGens[] = {2, 3, 4, 5, 6};
        int iPosition, iRow, iCol;
        int iDOBYear, iDODYear;
        String strDates;
        Indi iDiagIndi[] = new Indi[31];
        int iDiag[][] = {
            {-1, -1, -1, -2, 15},
            {-1, -1, -2, 7, -4},
            {-1, -1, -3, -2, 16},
            {-1, -2, 3, -4, -4},
            {-1, -3, -3, -2, 17},
            {-1, -3, -2, 8, -4},
            {-1, -3, -1, -2, 18},
            {-2, 1, -4, -4, -4},
            {-3, -3, -1, -2, 19},
            {-3, -3, -2, 9, -4},
            {-3, -3, -3, -2, 20},
            {-3, -2, 4, -4, -4},
            {-3, -1, -3, -2, 21},
            {-3, -1, -2, 10, -4},
            {-3, -1, -1, -2, 22},
            {0, -4, -4, -4, -4},
            {-3, -1, -1, -2, 23},
            {-3, -1, -2, 11, -4},
            {-3, -1, -3, -2, 24},
            {-3, -2, 5, -4, -4},
            {-3, -3, -3, -2, 25},
            {-3, -3, -2, 12, -4},
            {-3, -3, -1, -2, 26},
            {-2, 2, -4, -4, -4},
            {-1, -3, -1, -2, 27},
            {-1, -3, -2, 13, -4},
            {-1, -3, -3, -2, 28},
            {-1, -2, 6, -4, -4},
            {-1, -1, -3, -2, 29},
            {-1, -1, -2, 14, -4},
            {-1, -1, -1, -2, 30}
        };
        String strRow;

        //huge text field length (for names+dates) to be sure to accomodate all sorts of name lengths - will be trimmed eventually
        int iTextFieldLength = 80;

        //initialise parse list with root person
        parseList.add(indi);

        //create dummy person as a place-holder in list for
        //missing people in tree
        dummyIndi = new Indi();

        //start at generation 1 - doh!
        iGen = 1;
        //number of gens
        iGenerations = 5;

        //position in final diagram
        iPosition = 1;
        iDiagIndi[0] = indi;

        do {

            for (iLoop = 0; iLoop < parseList.size(); iLoop++) {

                //add pappy to temp list and name in correct diagram position
                if ((parseList.get(iLoop).getBiologicalFather()) == null) {
                    tempIndi = dummyIndi;
                    //println("Unknown");
                } else {
                    tempIndi = parseList.get(iLoop).getBiologicalFather();
                    //println(tempIndi);
                }
                //add to list
                tempList.add(tempIndi);

                //add mammy to temp list
                if ((parseList.get(iLoop).getBiologicalMother()) == null) {
                    tempIndi = dummyIndi;
                    //println("Unknown");				 
                } else {
                    tempIndi = parseList.get(iLoop).getBiologicalMother();
                    //println(tempIndi);
                }
                //add to list
                tempList.add(tempIndi);

            }
            //switch templist into parselist
            parseList.clear();
            for (iLoop = 0; iLoop < tempList.size(); iLoop++) {
                parseList.add(tempList.get(iLoop));
                iDiagIndi[iPosition++] = tempList.get(iLoop);
            }
            tempList.clear();

        } while (++iGen < iGenerations);

        //at this point all people are in the iDiagIndi array 
        //display according to transformation diagram
        //print report title
        println(align(textTitle + ": " + indi, 125, 1));
        println();

        for (iRow = 0; iRow < 31; iRow++) {
            strRow = "";
            for (iCol = 0; iCol < 5; iCol++) {
                //get item to print
                iTemp = iDiag[iRow][iCol];
                if (iTemp < 0) {
                    if (iTemp == -1) {
                        strRow = strRow + align(" ", 24, 0);
                    }
                    if (iTemp == -2) {
                        strRow = strRow + align("|-------------", 24, 2);
                    }
                    if (iTemp == -3) {
                        strRow = strRow + align("|             ", 24, 2);
                    }
                } else {
                    if (iDiagIndi[iTemp].getName().isEmpty()) {
                        strRow = strRow + align("(" + textUnknown + ")", iTextFieldLength, 0).trim();
                    } else {
                        strDates = "";
                        iDOBYear = getYear(iDiagIndi[iTemp].getBirthDate());
                        if (iDOBYear == -1) {
                            strDates = " (-";
                        } else {
                            strDates = " (" + iDOBYear + "-";
                        }
                        iDODYear = getYear(iDiagIndi[iTemp].getDeathDate());
                        if (iDODYear == -1) {
                            strDates = strDates + ")";
                        } else {
                            strDates = strDates + iDODYear + ")";
                        }
                        strRow = strRow + align(iDiagIndi[iTemp].getName() + strDates, iTextFieldLength, 0).trim();

                    }
                }
            }
            println(strRow);
        }

    }

  public int getYear(PropertyDate someDate) {
	  
	  //check for null, invalid or range-type birth date
	  if ((someDate==null) || (!someDate.isValid()) || (someDate.isRange())) 
		  return -1;
	  
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
