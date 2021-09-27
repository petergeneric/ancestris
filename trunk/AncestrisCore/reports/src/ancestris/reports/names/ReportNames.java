package ancestris.reports.names;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.TagPath;
import genj.report.Report;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 * @author   = Ekran, based on work of Carsten Muessig <carsten.muessig@gmx.net>
 * @version $Revision: 1.5 $
 * @modified by $Author: paul59 $, Ekran
 * updated   = $Date: 2010-01-16 11:06:32 $
 * Modified by Paul Robinson 2010/01/16
 */
@ServiceProvider(service=Report.class)
public class ReportNames extends Report {

    public boolean reportOutputBirth = true;
    public boolean reportOutputDeath = true;
    public boolean reportOutputMarriage = true;
    public boolean reportFilterName = false;
    public boolean reportFilterLine = false;
    public String StrFilter = "";
    public boolean reportDatesOnlyYears = false;
    public String reportDetailSeparator = ";";
    public boolean reportAlwaysDetailSeparator = false;

    /**
     * Main for argument Gedcom
     */
    public void start(Gedcom gedcom) {
        Entity[] indis = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
        for (int i = 0; i < indis.length; i++) {
            analyzeIndi((Indi) indis[i]);
        }
    }

    private String getDate(String str) {
        if (reportDatesOnlyYears == true & str.length() > 4) {
            str = str.substring(str.length() - 4);
        }
        return str;
    }

    /**
     * Main for argument Family
     */
    public void start(Indi indi) {
        analyzeIndi(indi);
    }

    private String trim(Object o) {
        if (o == null) {
            return "";
        }
        return o.toString();
    }

    private void analyzeIndi(Indi indi) {
        if (indi == null) {
            return;
        }
        String str = "";

        // ID
        str += "("+indi.getId() + ")\t";

        // Name
        str += indi.getName();

        // Birth
        if (reportOutputBirth) {
            // any data for birth available? IF yes, print birthsymbol
            if ((trim(indi.getBirthAsString()).length() > 0) || (trim(indi.getProperty(new TagPath("INDI:BIRT:PLAC"))).length() > 0)) {
                str += reportDetailSeparator + OPTIONS.getBirthSymbol();
            } else if (reportAlwaysDetailSeparator) {
                str += reportDetailSeparator;
            }

            // get date of birth
            if (trim(indi.getBirthAsString()).length() > 0) {
                str += trim(getDate(indi.getBirthAsString()));
            }

            // get place of birth
            if (trim(indi.getProperty(new TagPath("INDI:BIRT:PLAC"))).length() > 0) {
                str += reportDetailSeparator + trim(indi.getProperty(new TagPath("INDI:BIRT:PLAC")).getDisplayValue());
            }
        } else if (reportAlwaysDetailSeparator) {
            str += reportDetailSeparator;
        }


        // Death
        if (reportOutputDeath) {
            if (indi.getProperty("DEAT") != null && ((trim(indi.getDeathAsString()).length() > 0) || (trim(indi.getProperty(new TagPath("INDI:DEAT:PLAC"))).length() > 0))) {
                str += reportDetailSeparator + OPTIONS.getDeathSymbol();
            } else if (reportAlwaysDetailSeparator) {
                str += reportDetailSeparator;
            }

            if (trim(indi.getDeathAsString()).length() > 0) {
                str += trim(getDate(indi.getDeathAsString()));
            }

            if (trim(indi.getProperty(new TagPath("INDI:DEAT:PLAC"))).length() > 0) {
                str += reportDetailSeparator + trim(indi.getProperty(new TagPath("INDI:DEAT:PLAC")).getDisplayValue());
            }

        } else if (reportAlwaysDetailSeparator) {
            str += reportDetailSeparator;
        }


        // Marriage
        if (reportOutputMarriage) {
            Fam[] families = indi.getFamiliesWhereSpouse();
            for (int i = 0; i < families.length; i++) {
                if (((trim(families[i].getMarriageDate()).length() > 0) || (trim(families[i].getProperty(new TagPath("FAM:MARR:PLAC"))).length() > 0)) || ((indi != families[i].getHusband()) && (families[i].getHusband() != null)) || ((indi != families[i].getWife()) && (families[i].getWife() != null))) {
                    str += reportDetailSeparator + OPTIONS.getMarriageSymbol();
                }

                if ((trim(families[i].getMarriageDate()).length() > 0) || (trim(families[i].getProperty(new TagPath("FAM:MARR:PLAC"))).length() > 0)) {
                    if (trim(families[i].getMarriageDate()).length() > 0) {
                        str += getDate(trim(families[i].getMarriageDate().getDisplayValue()));
                    }
                    if (trim(families[i].getProperty(new TagPath("FAM:MARR:PLAC"))).length() > 0) {
                        str += reportDetailSeparator + trim(families[i].getProperty(new TagPath("FAM:MARR:PLAC")).getDisplayValue());
                    }
                } else if (reportAlwaysDetailSeparator) {
                    str += reportDetailSeparator;
                }

                if ((indi != families[i].getHusband()) && (families[i].getHusband() != null)) {
                    if (reportAlwaysDetailSeparator || (trim(families[i].getMarriageDate()).length() > 0) || (trim(families[i].getProperty(new TagPath("FAM:MARR:PLAC"))).length() > 0)) {
                        str += reportDetailSeparator;
                    }
                    str += families[i].getHusband().getName();
                }

                if ((indi != families[i].getWife()) && (families[i].getWife() != null)) {
                    if (reportAlwaysDetailSeparator || (trim(families[i].getMarriageDate()).length() > 0) || (trim(families[i].getProperty(new TagPath("FAM:MARR:PLAC"))).length() > 0)) {
                        str += reportDetailSeparator;
                    }
                    str += families[i].getWife().getName();
                }

                if (((trim(families[i].getMarriageDate()).length() > 0) || (trim(families[i].getProperty(new TagPath("FAM:MARR:PLAC"))).length() > 0)) || ((indi != families[i].getHusband()) && ( families[i].getHusband() != null)) || ((indi !=  families[i].getWife()) && (families[i].getWife() != null))) {
                    str += (reportAlwaysDetailSeparator ? reportDetailSeparator : "");
                }
            }
        }

        // filter for name
        if (reportFilterName) {
            if (indi.getName().toLowerCase().matches(".*" + StrFilter.toLowerCase() + ".*")) {
                println(str);
            }
        } else // filter in output line
        if (reportFilterLine) {
            if (str.toLowerCase().matches(".*" + StrFilter.toLowerCase() + ".*")) {
                println(str);
            }
        }

        // without filtering
        if ((reportFilterName == false) & (reportFilterLine == false)) {
            println(str);
        }
    }
} //ReportNames

