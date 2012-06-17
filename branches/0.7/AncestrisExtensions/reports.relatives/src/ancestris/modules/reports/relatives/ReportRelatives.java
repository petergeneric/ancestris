/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Dominique Baron (lemovice-at-ancestris-dot-org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * from genjreports.ReportRelatives by Nils Meier
 *
 */
package ancestris.modules.reports.relatives;

import genj.fo.Document;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class ReportRelatives {

    /**
     *
     */
    static class Relative {

        /** how to get to it */
        String key;
        String expression;
        int sex;

        /** constructor */
        Relative(String key, String expression) {
            this(key, expression, PropertySex.UNKNOWN);
        }

        /** constructor */
        Relative(String key, String expression, int sex) {
            this.key = key;
            this.expression = expression.trim();
            this.sex = sex;
        }
    }
    private final static Relative[] RELATIVES = {
        new Relative("father", "INDI:FAMC:*:..:HUSB:*"),
        new Relative("mother", "INDI:FAMC:*:..:WIFE:*"),
        new Relative("farfar", "father+father"),
        new Relative("farmor", "father+mother"),
        new Relative("morfar", "mother+father"),
        new Relative("mormor", "mother+mother"),
        new Relative("brother", "INDI:FAMC:*:..:CHIL:*", PropertySex.MALE),
        new Relative("sister", "INDI:FAMC:*:..:CHIL:*", PropertySex.FEMALE),
        new Relative("husband", "INDI:FAMS:*:..:HUSB:*"),
        new Relative("wife", "INDI:FAMS:*:..:WIFE:*"),
        new Relative("daughter", "INDI:FAMS:*:..:CHIL:*", PropertySex.FEMALE),
        new Relative("son", "INDI:FAMS:*:..:CHIL:*", PropertySex.MALE),
        new Relative("grandson", "son+son|daughter+son", PropertySex.MALE),
        new Relative("granddaughter", "son+daughter|daughter+daughter", PropertySex.FEMALE),
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
     * the report's entry point Our main logic
     */
    public Document start(Indi indiDeCujus) {
        Document document = new Document(NbBundle.getMessage(this.getClass(), "title", indiDeCujus.getName()));

        // prepare map of relationships
        Map<String, Relative> key2relative = new HashMap<String, Relative>();
        for (int i = 0; i < RELATIVES.length; i++) {
            Relative relative = RELATIVES[i];
            key2relative.put(relative.key, relative);
        }

        document.startSection(document.getTitle(), 5);

        for (int i = 0; i < RELATIVES.length; i++) {
            Relative relative = RELATIVES[i];
            List<Indi> find = find(indiDeCujus, relative.expression, relative.sex, key2relative);

            if (!find.isEmpty()) {
                document.startSection(NbBundle.getMessage(this.getClass(), relative.key), 3);
                document.startTable("border=1, width=100%, cellpadding=5, cellspacing=2, frame=below, rules=rows");
                document.addTableColumn("column-width=10%");
                document.addTableColumn("column-width=90%");
                document.nextTableRow("font-weight=bold");
                document.addText(NbBundle.getMessage(this.getClass(), "indi.ID"));
                document.nextTableCell();
                document.addText(NbBundle.getMessage(this.getClass(), "indi.name"));
                document.nextTableRow("font-weight=normal");

                for (Indi found : find) {
                    document.addLink(found.getId(), found);
                    document.nextTableCell();
                    document.addText(found.getName());
                    document.nextTableRow("font-weight=normal");
                }

                document.endTable();
            }
        }

        return document;
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
                if (sex == PropertySex.UNKNOWN || indi.getSex() == sex) {
                    result.add(indi);
                }
            }
        }

        // done
        return result;
    }
}
