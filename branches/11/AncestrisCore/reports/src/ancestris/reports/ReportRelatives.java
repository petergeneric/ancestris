package ancestris.reports;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.report.Report;
import genj.view.ViewContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 * A report for displaying relatives of a person
 */
//@ServiceProvider(service = Report.class) => this report is launched from quick report menu
public class ReportRelatives extends Report {

    private final static int UNKNOWN = PropertySex.UNKNOWN,
            MALE = PropertySex.MALE,
            FEMALE = PropertySex.FEMALE;

    private List<Indi> relatives = null; // memorize indis list

    /**
     * A relative
     */
    static class Relative {

        /**
         * how to get to it
         */
        String key;
        String expression;
        int sex;

        /**
         * constructor
         */
        Relative(String key, String expression) {
            this(key, expression, UNKNOWN);
        }

        /**
         * constructor
         */
        Relative(String key, String expression, int sex) {
            this.key = key;
            this.expression = expression.trim();
            this.sex = sex;
        }

    } //Relative

    private final static Relative[] RELATIVES = {
        new Relative("farfar", "father+father"),
        new Relative("farmor", "father+mother"),
        new Relative("morfar", "mother+father"),
        new Relative("mormor", "mother+mother"),
        new Relative("father", "INDI:FAMC:*:..:HUSB:*"),
        new Relative("mother", "INDI:FAMC:*:..:WIFE:*"),
        new Relative("husband", "INDI:FAMS:*:..:HUSB:*"),
        new Relative("wife", "INDI:FAMS:*:..:WIFE:*"),
        new Relative("daughter", "INDI:FAMS:*:..:CHIL:*", FEMALE),
        new Relative("son", "INDI:FAMS:*:..:CHIL:*", MALE),
        new Relative("brother", "INDI:FAMC:*:..:CHIL:*", MALE),
        new Relative("sister", "INDI:FAMC:*:..:CHIL:*", FEMALE),
        new Relative("grandson", "son+son|daughter+son", MALE),
        new Relative("granddaughter", "son+daughter|daughter+daughter", FEMALE),
        new Relative("uncle.paternal", "father+brother|father+sister +husband"),
        new Relative("uncle.maternal", "mother+brother|mother+sister +husband"),
        new Relative("aunt.paternal", "father+sister |father+brother+wife"),
        new Relative("aunt.maternal", "mother+sister |mother+brother+wife"),
        new Relative("nephew.fraternal", "brother+son"),
        new Relative("niece.fraternal", "brother+daughter"),
        new Relative("nephew.sororal", "sister+son"),
        new Relative("niece.sororal", "sister+daughter"),
        new Relative("cousin.paternal", "uncle.paternal+son"),
        new Relative("cousin.maternal", "uncle.maternal+son"),
        new Relative("cousine.paternal", "uncle.paternal+daughter"),
        new Relative("cousine.maternal", "uncle.maternal+daughter")
    };

    /**
     * Reports main
     */
    public List<ViewContext> start(Indi indi) {

        relatives = new ArrayList<>();

        // prepare map of relationships
        Map<String, Relative> key2relative = new HashMap<String, Relative>();
        for (int i = 0; i < RELATIVES.length; i++) {
            Relative relative = RELATIVES[i];
            key2relative.put(relative.key, relative);
        }

        // Loop over relative descriptions
        List<ViewContext> result = new ArrayList<ViewContext>();
        result.add(new ViewContext(indi));
        relatives.add(indi);

        for (int i = 0; i < RELATIVES.length; i++) {
            Relative relative = RELATIVES[i];
            for (Indi found : find(indi, relative.expression, relative.sex, key2relative)) {
                result.add(new ViewContext(found).setText(translate(relative.key) + ": " + found));
                relatives.add(found);
            }
        }
        return result;

        // done
    }
    
    public List<Indi> getRelatives(Indi indi) {
        start(indi);
        return relatives;
    }

    /**
     * Find all relatives of given roots and expression
     */
    private List<Indi> find(List<Indi> roots, String expression, int sex, Map<String, Relative> key2relative) {

        List<Indi> result = new ArrayList<Indi>();
        for (int i = 0; i < roots.size(); i++) {
            result.addAll(find(roots.get(i), expression, sex, key2relative));
        }

        return result;

    }

    /**
     * Find all relatives of given root and expression
     */
    private List<Indi> find(Property root, String expression, int sex, Map<String, Relative> key2relative) {

        // any 'OR's?
        int or = expression.indexOf('|');
        if (or > 0) {
            List<Indi> result = new ArrayList<Indi>();
            StringTokenizer ors = new StringTokenizer(expression, "|");
            while (ors.hasMoreTokens()) {
                result.addAll(find(root, ors.nextToken().trim(), sex, key2relative));
            }
            return result;
        }

        // is relationship recursive?
        int dot = expression.indexOf('+');
        if (dot > 0) {
            List<Indi> roots = new ArrayList<Indi>();
            roots.add((Indi) root.getEntity());
            StringTokenizer cont = new StringTokenizer(expression, "+");
            while (cont.hasMoreTokens()) {
                roots = find(roots, cont.nextToken(), sex, key2relative);
            }
            return roots;
        }

        // a recursive path?
        int colon = expression.indexOf(':');
        if (colon < 0) {
            Relative relative = key2relative.get(expression.trim());
            return find(root, relative.expression, relative.sex, key2relative);
        }

        // assuming expression consists of tagpath from here
        List<Indi> result = new ArrayList<Indi>();
        Property[] found = root.getProperties(new TagPath(expression));
        for (int i = 0; i < found.length; i++) {
            Indi indi = (Indi) found[i].getEntity();
            if (indi != root) {
                if (sex == UNKNOWN || indi.getSex() == sex) {
                    result.add(indi);
                }
            }
        }

        // done
        return result;
    }

} //ReportRelatives
