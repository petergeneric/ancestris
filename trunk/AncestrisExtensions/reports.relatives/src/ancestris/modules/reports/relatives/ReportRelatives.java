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

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.actions.SubMenuAction;
import genj.fo.Document;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.report.Report;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import static ancestris.modules.reports.relatives.Bundle.*;
import ancestris.view.SelectionSink;

/**
 *
 * @author dominique
 */
@ServiceProvider(service = Report.class)
public class ReportRelatives extends Report {

    /**
     *
     */
    //FIXME: expand Relative API and put this class in core
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
    // prepare map of relationships
    static private final Map<String, Relative> KEY2RELATIVE = new HashMap<String, Relative>();

    static {
        for (Relative relative : RELATIVES) {
            KEY2RELATIVE.put(relative.key, relative);
        }
    }

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
                    document.addLink(found.getId(), found.getAnchor());
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
    private static List<Indi> find(List<Indi> roots, String expression, int sex, Map<String, Relative> key2relative) {

        List<Indi> result = new ArrayList<Indi>();
        for (int i = 0; i < roots.size(); i++) {
            result.addAll(find(roots.get(i), expression, sex, key2relative));
        }

        return result;

    }

    private static List<Indi> find(Property root, String relative) {
        return find(root, KEY2RELATIVE.get(relative).expression, KEY2RELATIVE.get(relative).sex, KEY2RELATIVE);
    }

    /**
     * Find all relatives of given root and expression
     */
    private static List<Indi> find(Property root, String expression, int sex, Map<String, Relative> key2relative) {

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
    static private final int POSITION = 1500;

    @ActionID(category = "Navigate", id = "ancestris.modules.reports.relatives.SiblingNavigateAction")
    @ActionRegistration(displayName = "Sibling")
    @ActionReferences({
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 30)})
    @NbBundle.Messages({
            "sibling.menu={0} siblings"
            ,"father.menu=Father"
            ,"mother.menu=Mother"
    })
    public static class SiblingNavigateAction extends NavigateAction{
        public SiblingNavigateAction() {
            super(new Relative("sibling", "brother|sister"));
        }
    }
    public static Action getSiblingNavigateAction(){
        Action navAction = new NavigateAction(new Relative("sibling", "brother|sister"));
        return navAction;
    }

    @ActionID(category = "Navigate", id = "ancestris.modules.reports.relatives.FatherNavigateAction")
    @ActionRegistration(displayName = "Father")
    @ActionReferences({
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 10)})
    public static class FatherNavigateAction extends NavigateAction{
        public FatherNavigateAction() {
            super(KEY2RELATIVE.get("father"));
        }
    }

    @ActionID(category = "Navigate", id = "ancestris.modules.reports.relatives.MotherNavigateAction")
    @ActionRegistration(displayName = "Mother")
    @ActionReferences({
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 10)})
    public static class MotherNavigateAction extends NavigateAction{
        public MotherNavigateAction() {
            super(KEY2RELATIVE.get("mother"));
        }
    }

    
    private static class NavigateAction extends SubMenuAction {

        Relative relative;
        public NavigateAction(Relative relative) {
            super();
            this.relative=relative;
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, false);
        }

        public @Override
        void actionPerformed(ActionEvent e) {
            assert false;
        }

        public @Override
        Action createContextAwareInstance(org.openide.util.Lookup context) {
            Entity entity = context.lookup(Entity.class);
            if (entity != null) {
                List<Indi> find = find(entity, relative.expression, relative.sex, KEY2RELATIVE);

                clearActions();
                setText(NbBundle.getMessage(ReportRelatives.class, relative.key+".menu",find.size()));
                for (Entity e : find) {
                    addAction(new Jump(e));
                }
            }
            return super.createContextAwareInstance(context);
        }
    }

    private static class Jump extends AbstractAncestrisAction {

        private Entity entity;

        public Jump(Entity entity) {
            this.entity = entity;
            setImage(entity.getImage());
            setText(entity.toString());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SelectionSink.Dispatcher.fireSelection(null, new Context(entity), true);
        }
    }
}
