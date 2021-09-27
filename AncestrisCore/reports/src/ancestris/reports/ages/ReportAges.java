package ancestris.reports.ages;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.report.Report;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 * ReportAges (based on ReportDescendants and ReportGedcomStatistics)
 */
@ServiceProvider(service = Report.class)
public class ReportAges extends Report {

    public boolean reportBaptismAge = true;
    public boolean reportConfirmationAge = true;
    public boolean reportMarriageAge = true;
    public boolean reportAgeAtDivorce = true;
    public boolean reportAgeAtChildBirth = true;
    public boolean reportAgeAtEmigration = true;
    public boolean reportAgeAtImmigration = true;
    public boolean reportAgeAtNaturalization = true;
    public boolean reportAgeAtDeath = true;
    public boolean reportAgeSinceBirth = true;
    public boolean reportAgeSinceDeath = true;

    /**
     * localized strings
     */
    private final static String AGE = Gedcom.getName("AGE");

    /**
     * Main for argument Indi
     */
    public void start(Indi indi) {

        // Display the ages
        analyzeIndi(indi);

        // Done
    }

    /**
     * Analyze an event and report its information, date and age of indi
     */
    private boolean analyzeEvent(boolean header, Indi indi, String tag) {

        // check for date under tag
        PropertyDate prop = (PropertyDate) indi.getProperty(new TagPath("INDI:" + tag + ":DATE"));
        if (prop == null || !prop.isValid()) {
            return false;
        }

        // do the header
        if (header) {
            println(getIndent(2) + Gedcom.getName(tag) + ':');
        }

        // format and ouput
        println(getIndent(3) + prop.getDisplayValue());
        Delta age = indi.getAge(prop.getStart());
        printAge(age, 4);
        println();

        // done
        return true;
    }

    /**
     * Analyze and report ages for given individual
     */
    private void analyzeIndi(Indi indi) {

        Delta age = null;

        println(indi);

        // print birth date (give up if none)
        PropertyDate birth = indi.getBirthDate();
        if (birth == null || !birth.isValid()) {
            println(OPTIONS.getBirthSymbol() + translate("noData"));
            return;
        }
        println(OPTIONS.getBirthSymbol() + birth);
        println();

        if (reportBaptismAge) {
            analyzeEvent(true, indi, "BAPM");
            analyzeEvent(true, indi, "BAPL");
            analyzeEvent(true, indi, "CHR");
            analyzeEvent(true, indi, "CHRA");
        }

        if (reportConfirmationAge) {
            analyzeEvent(true, indi, "CONF");
        }

        if (reportMarriageAge) {
            Fam[] fams = indi.getFamiliesWhereSpouse();
            if (fams.length > 0) {
                println(getIndent(2) + Gedcom.getName("MARR") + ":");
                for (int i = 0; i < fams.length; i++) {
                    Fam fam = fams[i];
                    String text = getIndent(3) + OPTIONS.getMarriageSymbol()
                            + " " + fam + ": ";
                    if (fam.getMarriageDate() == null) {
                        println(text + translate("noData"));
                    } else {
                        println(text + fam.getMarriageDate());
                        age = indi.getAge(fam.getMarriageDate().getStart());
                        printAge(age, 4);
                    }
                }
                println();
            }
        }

        if (reportAgeAtDivorce) {
            Fam[] fams = indi.getFamiliesWhereSpouse();
            if (fams.length > 0) {
                boolean found = false;
                for (int i = 0; i < fams.length; i++) {
                    Fam fam = fams[i];
                    if (fam.getDivorceDate() != null) {
                        if (!found) {
                            println(getIndent(2) + Gedcom.getName("DIV") + ":");
                            found = true;
                        }
                        println(getIndent(3) + OPTIONS.getDivorceSymbol()
                                + " " + fam + ": " + fam.getDivorceDate());
                        age = indi.getAge(fam.getDivorceDate().getStart());
                        printAge(age, 4);
                    }
                }
                if (found) {
                    println();
                }
            }
        }

        if (reportAgeAtChildBirth) {
            Indi[] children = indi.getChildren();
            if (children.length > 0) {
                println(getIndent(2) + translate("childBirths") + ":");
                for (int i = 0; i < children.length; i++) {
                    Indi child = children[i];
                    String text = getIndent(3) + OPTIONS.getBirthSymbol() + child + ": ";
                    PropertyDate cbirth = child.getBirthDate();
                    if (cbirth == null) {
                        println(text + translate("noData"));
                    } else {
                        println(text + cbirth);
                        age = indi.getAge(cbirth.getStart());
                        printAge(age, 4);
                    }
                }
                println();
            }
        }

        if (reportAgeAtEmigration) {
            analyzeEvent(true, indi, "EMIG");
        }

        if (reportAgeAtImmigration) {
            analyzeEvent(true, indi, "IMMI");
        }

        if (reportAgeAtNaturalization) {
            analyzeEvent(true, indi, "NATU");
        }

        if (reportAgeAtDeath) {
            PropertyDate death = indi.getDeathDate();
            if (death != null) {
                println(getIndent(2) + Gedcom.getName("DEAT") + ":");
                println(getIndent(3) + OPTIONS.getDeathSymbol() + death);
                age = indi.getAge(indi.getDeathDate().getStart());
                printAge(age, 4);
                println();
            }
        }

        if (reportAgeSinceBirth) {
            PointInTime now = PointInTime.getNow();
            age = indi.getAge(now);
            if (age != null) {
                println(getIndent(2) + translate("sinceBirth", now) + ":");
                printAge(age, 4);
                println();
            }
        }
        if (reportAgeSinceDeath) {
            PointInTime now = PointInTime.getNow();
            PropertyDate death = indi.getDeathDate();
            if (death != null) {
                age = death.getAnniversary();;
                if (age != null) {
                    println(getIndent(2) + translate("sinceDeath", now) + ":");
                    printAge(age, 4);
                    println();
                }
            }

        }
    }

    /**
     * Print a computed age with given indent
     */
    private void printAge(Delta age, int indent) {
        if (age == null) {
            println(getIndent(indent) + translate("noData"));
        } else {
            println(getIndent(indent) + AGE + ": " + age);
        }
    }

} //ReportAges
