package ancestris.reports.missinginformation;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.report.Report;
import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 */
@ServiceProvider(service = Report.class)
public class ReportMissingInformation extends Report {

    // check all relevant tags by default	
    public boolean checkBirthDate = true;
    public boolean checkBirthPlace = true;
    public boolean checkBirthSource = true;
    public boolean checkBaptismDate = true;
    public boolean checkBaptismPlace = true;
    public boolean checkBaptismSource = true;
    public boolean checkDeathDate = true;
    public boolean checkDeathPlace = true;
    public boolean checkDeathSource = true;
    public boolean checkSex = true;
    public boolean checkGiven = true;
    public boolean checkSurname = true;
    public boolean checkUseSosa = false;
    public boolean checkSortOnSosa = false;

    //translate strings for output  
    private final String textTitle = translate("title");
    private final String textSubject = translate("subject");
    private final String textBirth = translate("birth");
    private final String textBaptism = translate("baptism");
    private final String textDeath = translate("death");
    private final String textDate = translate("date");
    private final String textPlace = translate("place");
    private final String textSex = translate("sex");
    private final String textGiven = translate("given");
    private final String textSurname = translate("surname");
    private final String textKey = translate("key");
    private final String textSource = translate("source");

    //column widths etc
    private final int colName = 60;
    private final int colData = 6;
    private final int numDataCols = 12;

    public void start(Indi indi) {

        //show column headers
        displayHeader(indi.getName());
        //do report
        checkIndi(indi);

    }

    public void checkIndi(Indi indi) {
        //vars
        final StringBuilder strDataRow = new StringBuilder();
        PropertyDate tempDate;
        PropertyPlace tempPlace;
        String strNameID;
        Boolean flagOk1, flagOk2;

        //clear any previous data and align
        strNameID = String.format("%-10s", indi.getId()) + " " + indi.getName();
        strDataRow.append(align(strNameID, colName, 3));

        //NOTE: the order of the following tests corresponds with the display column order
        //check birth date if required
        if (checkBirthDate) {
            //read date of birth for validity checking
            tempDate = indi.getBirthDate();
            addSymbol(strDataRow, tempDate != null && tempDate.isValid());
        } else {
            strDataRow.append(align("-", colData, 1));	// not checked
        }

        //check place of birth if required
        if (checkBirthPlace) {
            tempPlace = (PropertyPlace) indi.getProperty(new TagPath("INDI:BIRT:PLAC"));
            addSymbol(strDataRow, tempPlace != null);
        } else {
            strDataRow.append(align("-", colData, 1));	// not checked
        }

        //check source of birth if required
        if (checkBirthSource) {
            final Property tempP = indi.getProperty(new TagPath("INDI:BIRT:SOUR"));
            addSymbol(strDataRow, tempP != null);
        } else {
            strDataRow.append(align("-", colData, 1));	// not checked
        }

        //check baptism and christening date if required
        if (checkBaptismDate) {
            //reset flags
            flagOk1 = true;
            flagOk2 = true;

            // bapm date...
            tempDate = (PropertyDate) indi.getProperty(new TagPath("INDI:BAPM:DATE"));
            if ((tempDate == null) || (!tempDate.isValid())) {
                flagOk1 = false;
            }
            //now do chr tag
            tempDate = (PropertyDate) indi.getProperty(new TagPath("INDI:CHR:DATE"));
            if ((tempDate == null) || (!tempDate.isValid())) {
                flagOk2 = false;
            }

            //if date found on either tag, flag is true
            addSymbol(strDataRow, flagOk1 || flagOk2);

        } else {
            strDataRow.append(align("-", colData, 1));
        }

        //baptism place
        if (checkBaptismPlace) {

            flagOk1 = true;
            flagOk2 = true;

            //check <bapt> 
            tempPlace = (PropertyPlace) indi.getProperty(new TagPath("INDI:BAPM:PLAC"));
            //tempPlace2 = (PropertyPlace)indi.getProperty(new TagPath("INDI:CHR:PLAC"));
            if ((tempPlace == null) || ("".equals(tempPlace.getValue()))) {
                flagOk1 = false;
            }

            //check <chr> 
            tempPlace = (PropertyPlace) indi.getProperty(new TagPath("INDI:CHR:PLAC"));
            if ((tempPlace == null) || (tempPlace.getValue().length() == 0)) {
                flagOk2 = false;
            }

            addSymbol(strDataRow, flagOk1 || flagOk2);

        } else {
            strDataRow.append(align("-", colData, 1));
        }

        //baptism place
        if (checkBaptismSource) {

            flagOk1 = true;
            flagOk2 = true;

            //check <bapt> 
            Property tempP = indi.getProperty(new TagPath("INDI:BAPM:SOUR"));
            if (tempP == null) {
                flagOk1 = false;
            }

            //check <chr> 
            tempP = indi.getProperty(new TagPath("INDI:CHR:SOUR"));
            if (tempP == null) {
                flagOk2 = false;
            }

            addSymbol(strDataRow, flagOk1 || flagOk2);

        } else {
            strDataRow.append(align("-", colData, 1));
        }

        //check death date if required
        if (checkDeathDate) {

            //reset flags
            tempDate = indi.getDeathDate();
            addSymbol(strDataRow, indi.getDeathDate() != null && tempDate.isValid());

        } else {
            strDataRow.append(align("-", colData, 1));
        }

        //check place of death if required
        if (checkDeathPlace) {
            tempPlace = (PropertyPlace) indi.getProperty(new TagPath("INDI:DEAT:PLAC"));
            addSymbol(strDataRow, tempPlace != null);
        } else {
            strDataRow.append(align("-", colData, 1));	// not checked
        }

        //check source of death if required
        if (checkDeathSource) {
            Property tempP = indi.getProperty(new TagPath("INDI:DEAT:SOUR"));
            addSymbol(strDataRow, tempP != null);
        } else {
            strDataRow.append(align("-", colData, 1));	// not checked
        }

        //check gender if required
        if (checkSex) {
            addSymbol(strDataRow, indi.getSex() == PropertySex.MALE || indi.getSex() == PropertySex.FEMALE);
        } else {
            strDataRow.append(align("-", colData, 1));
        }

        //check given/firstname
        // uses extraction from <name> rather than checking <GIVN>
        if (checkGiven) {
            addSymbol(strDataRow, !"".equals(indi.getFirstName()));
        } else {
            strDataRow.append(align("-", colData, 1));
        }

        //check surname/family name
        // uses extraction from <name> rather than checking <SURN>
        if (checkSurname) {
            addSymbol(strDataRow, !"".equals(indi.getLastName()));
        } else {
            strDataRow.append(align("-", colData, 1));
        }

        //display results
        println(strDataRow);
    }

    public void start(Gedcom gedcom) {
        //show report header
        displayHeader(gedcom.getName());

        if (checkUseSosa) {
            useSosa(gedcom);
        } else {
            //grab all
            for (Entity e : gedcom.getEntities(Gedcom.INDI)) {
                checkIndi((Indi) e);
            }
        }
    }
    
    private void useSosa(Gedcom gedcom){
        final Map<BigInteger, Indi> sosaList = new TreeMap();
        final Map<String, Indi> sosaListById = new TreeMap();
        for (Entity e : gedcom.getEntities(Gedcom.INDI)) {
            Indi indi = (Indi) e;
            Property[] props = indi.getProperties(Indi.TAG_SOSA);
            if (props.length == 0) {
                props = indi.getProperties(Indi.TAG_SOSADABOVILLE);
            }
            for (Property prop : props) {
                // extract big integer from sosa number, grabing siblings of sosa, thus extracting the next number after '-', and stripping out generation number
                String sosaStr = prop.getValue();
                int index = sosaStr.indexOf(".");
                if (index != -1 || sosaStr.matches(".*[a-z].*")) {
                    continue;
                }
                index = sosaStr.indexOf(" ");
                if (index != -1) {
                    sosaStr = sosaStr.substring(0, index); // stripping end
                }
                index = sosaStr.indexOf("-"); // in case there are siblings
                if (index != -1) {
                    continue;
                }
                BigInteger divider = BigInteger.ONE;
               
                BigInteger bi = (new BigInteger(sosaStr+"00")).divide(divider);
                sosaList.put(bi, indi);
                sosaListById.put(indi.getId(), indi);
            }
        }
        
        if (checkSortOnSosa){
            for (BigInteger bi : sosaList.keySet()){
                checkIndi(sosaList.get(bi));
            }
        } else {
            for (String id : sosaListById.keySet()){
                checkIndi(sosaListById.get(id));
            }
        }

    }

    public void displayHeader(String strSubject) {

        String strColHeader1, strColHeader2;
        String strUnderLine;
        int loop;

        //print report title
        println(align(textTitle, (colName + numDataCols * colData), 1));
        println();

        println(textSubject + ": " + strSubject);
        println(textDate + ": " + PointInTime.getNow().toString());
        println(textKey);
        println();

        strUnderLine = "-";
        for (loop = 1; loop < (colName + numDataCols * colData) - 1; loop++) {
            strUnderLine += "-";
        }

        //create column header labels
        strColHeader1 = align(" ", colName, 1)
                + align(textBirth, colData, 1)
                + align(textBirth, colData, 1)
                + align(textBirth, colData, 1)
                + align(textBaptism, colData, 1)
                + align(textBaptism, colData, 1)
                + align(textBaptism, colData, 1)
                + align(textDeath, colData, 1)
                + align(textDeath, colData, 1)
                + align(textDeath, colData, 1);

        strColHeader2 = align(" ", colName, 1)
                + align(textDate, colData, 1)
                + align(textPlace, colData, 1)
                + align(textSource, colData, 1)
                + align(textDate, colData, 1)
                + align(textPlace, colData, 1)
                + align(textSource, colData, 1)
                + align(textDate, colData, 1)
                + align(textPlace, colData, 1)
                + align(textSource, colData, 1)
                + align(textSex, colData, 1)
                + align(textGiven, colData, 1)
                + align(textSurname, colData, 1);

        //display
        println(strColHeader1);
        println(strColHeader2);
        println(strUnderLine);

    }

    private void addSymbol(StringBuilder sb, boolean test) {
        if (test) {
            sb.append(align("ok", colData, 1));
        } else {
            sb.append(align("X", colData, 1));
        }
    }
}
