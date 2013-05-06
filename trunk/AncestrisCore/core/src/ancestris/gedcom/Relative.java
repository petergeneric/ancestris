/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.gedcom;

import ancestris.core.actions.JumpToEntityAction;
import ancestris.core.actions.SubMenuAction;
import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.NbBundle;
import static ancestris.gedcom.Bundle.*;
/**
 *
 * @author daniel
 */
/**
 *
 */
@NbBundle.Messages(value = {"sibling.menu={0} siblings", "parent.menu=Parents", "greatparent.menu={0} GreatParents", "spouse.menu={0} Spouse"})
public class Relative {
    // Relative cache

    private static final Map<String, Relative> KEY2RELATIVE = new HashMap<String, Relative>(20);
    //FIXME: Do we need all these shortcuts?
    /** Father */
    public static final String FATHER = "father";
    /** Mother */
    public static final String MOTHER = "mother";
    /** Parent (ie Father or Mother) */
    public static final String PARENT = "parent";
    /** GreatParent (parents of parents) */
    public static final String GREATPARENT = "greatparent";
    /** Sibling (Brother or sister) */
    public static final String SIBLING = "sibling";
    /** Brother */
    public static final String BROTHER = "brother";
    /** Sister */
    public static final String SISTER = "sister";
    /** Husband */
    public static final String HUSBAND = "husband";
    /** Wife */
    public static final String WIFE = "wife";
    /** Spouse (generic spouse access for an Indi).
     * For a Male Indi, returned values will be all wives,
     * for a Female Ind, returned values will be all husbands
     */
    public static final String SPOUSE = "spouse";
    /** Daughter */
    public static final String DAUGHTER = "daughter";
    /** Son */
    public static final String SON = "son";
    /** Child */
    public static final String CHILD = "child";
    /** GrandSon: son of children */
    public static final String GRANDSON = "grandson";
    /** GrandDaughter: daughter of children */
    public static final String GRANDDAUGHTER = "granddaughter";
    /** Child of children */
    public static final String GRANDCHILD = "grandchild";
    /** */
    public static final String UNCLE = "uncle";
    /** */
    public static final String PUNCLE = "uncle.paternal";
    /** */
    public static final String MUNCLE = "uncle.maternal";
    /** */
    public static final String AUNT = "aunt";
    /** */
    public static final String PAUNT = "aunt.paternal";
    /** */
    public static final String MAUNT = "aunt.maternal";
    public static final String UNCLEAUNT = "uncle.aunt";
    public static final String FNEPHEW = "nephew.fraternal";
    public static final String FNIECE = "niece.fraternal";
    public static final String SNEPHEW = "nephew.sororal";
    public static final String SNIECE = "niece.sororal";
    //        public static final String PCOUSIN = "cousin.paternal";
    //        public static final String MCOUSIN = "cousin.maternal";
    public static final String FIRSTCOUSIN = "first.cousin";
    //FIXME: We could probably use TagPath class insteadof String representing a Path

    static //FIXME: We could probably use TagPath class insteadof String representing a Path
    {
        // Parents
        create(Relative.FATHER, "INDI:FAMC:*:..:HUSB:*");
        create(Relative.MOTHER, "INDI:FAMC:*:..:WIFE:*");
        create(PARENT, FATHER + "|" + MOTHER);
        // great parents
        create(GREATPARENT, PARENT + "+" + PARENT);
        // Sibling
        create(SIBLING, "INDI:FAMC:*:..:CHIL:*");
        create(BROTHER, SIBLING, PropertySex.MALE);
        create(SISTER, SIBLING, PropertySex.FEMALE);
        //spouses
        create(Relative.HUSBAND, "INDI:FAMS:*:..:HUSB:*");
        create(Relative.WIFE, "INDI:FAMS:*:..:WIFE:*");
        create(SPOUSE, HUSBAND + "|" + WIFE);
        // children
        create(CHILD, "INDI:FAMS:*:..:CHIL:*");
        create(Relative.DAUGHTER, "INDI:FAMS:*:..:CHIL:*", PropertySex.FEMALE);
        create(Relative.SON, "INDI:FAMS:*:..:CHIL:*", PropertySex.MALE);
        //GrandChildren
        create(GRANDCHILD, "child+child");
        create(GRANDSON, "child+son", PropertySex.MALE);
        create(GRANDDAUGHTER, "child+daughter", PropertySex.FEMALE);
        //uncle & aunt
        create(PUNCLE, "father+brother|father+sister +husband");
        create(MUNCLE, "mother+brother|mother+sister +husband");
        create(UNCLE, "parent+brother|parent+sister+husband");
        create(PAUNT, "father+sister |father+brother+wife");
        create(MAUNT, "mother+sister |mother+brother+wife");
        create(AUNT, "parent+sister |parent+brother+wife");
//        create(UNCLEAUNT, "parent+sibling|parent+sister+husband");
        //nephew & niece
        create(FNEPHEW, "brother+son");
        create(FNIECE, "brother+daughter");
        create(SNEPHEW, "sister+son");
        create(SNIECE, "sister+daughter");
        // cousin
        create(FIRSTCOUSIN, "parent+sibling+child");
    }
    /** how to get to it */
    private String key;
    private String expression;
    private int sex;
    private String description = "";

    /** constructor */
    private Relative(String key, String expression, int sex) {
        this.key = key;
        this.expression = expression.trim();
        this.sex = sex;
        // set displayed description for this relative from BundleValue. 
        // Fallback to key if not found
        try {
            description = NbBundle.getMessage(Relative.class, key);
        } catch (MissingResourceException e){
            description = key;            
        }
    }
    
    /**
     * Return human description for this relative (ie "Father").
     * @return 
     */
    public String getDescription(){
        return description;
    }

    /**
     * Set human description for this relative (ie "Father").
     */
    public void setDescription(final String description){
        this.description = description;
    }

    public static Relative create(String key, String expression) {
        return create(key, expression, PropertySex.UNKNOWN);
    }

    public static Relative create(String key, String expression, int sex) {
        return create(key, expression, sex,null);
    }

    public static Relative create(String key, String expression, int sex, String description) {
        Relative result = KEY2RELATIVE.get(key);
        if (result == null) {
            result = new Relative(key, expression, sex);
            if (description != null){
                result.setDescription(description);
            }
            KEY2RELATIVE.put(key, result);
        }
        return result;
    }

    public static Relative get(final String key) {
        return KEY2RELATIVE.get(key);
    }

    /**
     * Find all relatives of given roots and expression
     */
    public Collection<Indi> find(Collection<Indi> roots) {
        List<Indi> result = new ArrayList<Indi>();
        for (Indi indi : roots) {
            result.addAll(find(indi));
        }
        return result;
    }

    /**
     * Find all relatives of given root and expression
     */
    public Collection<Indi> find(Property root) {
        // any 'OR's?
        int or = expression.indexOf('|');
        if (or > 0) {
            List<Indi> result = new ArrayList<Indi>();
            StringTokenizer ors = new StringTokenizer(expression, "|");
            while (ors.hasMoreTokens()) {
                result.addAll(Relative.get(ors.nextToken().trim()).find(root));
            }
            return result;
        }
        // is relationship recursive?
        int dot = expression.indexOf('+');
        if (dot > 0) {
            Collection<Indi> roots = new ArrayList<Indi>();
            roots.add((Indi) root.getEntity());
            StringTokenizer cont = new StringTokenizer(expression, "+");
            while (cont.hasMoreTokens()) {
                roots = Relative.get(cont.nextToken().trim()).find(roots);
            }
            return roots;
        }
        // a recursive path?
        int colon = expression.indexOf(':');
        if (colon < 0) {
            return Relative.get(expression.trim()).find(root);
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
    private static final int POSITION = 1500;

    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.ParentNavigateAction")
    @ActionRegistration(displayName = "Parents")
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 10)})
    public static class ParentNavigateAction extends NavigateAction {

        public ParentNavigateAction() {
            super(Relative.get(Relative.PARENT));
        }
    }

    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.SiblingNavigateAction")
    @ActionRegistration(displayName = "Sibling")
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 20)})
    public static class SiblingNavigateAction extends NavigateAction {

        public SiblingNavigateAction() {
            super(Relative.get(Relative.SIBLING));
        }
    }

    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.SpouseNavigateAction")
    @ActionRegistration(displayName = "Spouse")
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 30)})
    public static class SpouseNavigateAction extends NavigateAction {

        public SpouseNavigateAction() {
            super(Relative.get(Relative.SPOUSE));
        }
    }

    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.ChildNavigateAction")
    @ActionRegistration(displayName = "Children")
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 40)})
    public static class ChildNavigateAction extends NavigateAction {

        public ChildNavigateAction() {
            super(Relative.get(Relative.CHILD));
        }
    }

    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.UncleAuntNavigateAction")
    @ActionRegistration(displayName = "Uncles")
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 50)})
    @NbBundle.Messages({"uncle.aunt=Uncle and Aunt"})
    public static class UncleAuntNavigateAction extends NavigateAction {

        public UncleAuntNavigateAction() {
            super(Relative.create("uncle.aunt",Relative.PARENT+"+"+Relative.SIBLING,PropertySex.UNKNOWN,uncle_aunt()));
        }
    }

    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.CousinNavigateAction")
    @ActionRegistration(displayName = "Cousin")
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 60)})
    public static class CousinNavigateAction extends NavigateAction {

        public CousinNavigateAction() {
            super(Relative.get(Relative.FIRSTCOUSIN));
        }
    }

//    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.NephewNavigateAction")
//    @ActionRegistration(displayName = "Nephew")
//    @ActionReferences(value = {
//        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 70)})
//    @NbBundle.Messages({"nephew.niece=Nephew and Niece"})
//    public static class NephewNavigateAction extends NavigateAction {
//
//        public NephewNavigateAction() {
//            super(Relative.create("nephew.niece",Relative.NPARENT+"+"+Relative.SIBLING,PropertySex.UNKNOWN,uncle_aunt()));
//        }
//    }

    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.GreatParentNavigateAction")
    @ActionRegistration(displayName = "Mother")
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 70)})
    public static class GreatParentNavigateAction extends NavigateAction {

        public GreatParentNavigateAction() {
            super(Relative.get(Relative.GREATPARENT));
        }
    }

    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.GrandChildNavigateAction")
    @ActionRegistration(displayName = "Grand Children")
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 80)})
    public static class GrandChildNavigateAction extends NavigateAction {

        public GrandChildNavigateAction() {
            super(Relative.get(Relative.GRANDCHILD));
        }
    }

    /**
     * This class builds a submenu action populated with Indi relatives.
     * <p/>The main usage for this class is subclassing this class by another class
     * with proper Action annotations to construc a menu or toolbar. Generally
     * only one call is necessary:
     * <pre> new NavigateAction(Relative)</pre>
     * The parameter is used to find relatives 
     * (see {@link Relative#find(genj.gedcom.Property) })
     * as well as  this submenu entry
     * descriptions as using {@link Relative#getDescription() }.
     * <p/>If the submenu needs to be logically divided into smaller parts with
     * a separator between them, subsequent calls to {@link #addGroup(ancestris.gedcom.Relative) }
     * may be done and {@link #setDescription(java.lang.String) } can by used 
     * to set this menu text properly
     */
    private static class NavigateAction extends SubMenuAction {

        private List<Relative> relatives;
        private String description = "";

        /**
         * C
         * @param relative 
         */
        public NavigateAction(Relative relative) {
            super();
            this.relatives = new ArrayList<Relative>(Collections.singletonList(relative));
            description = relative.getDescription();
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, false);
        }
        
        /**
         * Add a new group of submenu entries related to the Relative parameter
         * @param relative 
         */
        public void addGroup(Relative relative){
            relatives.add(relative);
        }
        
        /**
         * Overrides this menu displayed test description.
         * @param description 
         */
        public void setDescription(String description){
            this.description = description;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(org.openide.util.Lookup context) {
            Entity entity = context.lookup(Entity.class);
            if (entity != null) {
                int count = 0;
                clearActions();
                for (Relative relative: relatives){
//                for (;relatives.iterator().hasNext();addAction(null)){
//                    Relative relative = relatives.iterator().next();
                    Collection<Indi> find = relative.find(entity);
                    count += find.size();
                    for (Entity e : find) {
                        addAction(new JumpToEntityAction(e));
                    }
                    addAction(null);
                }
                setText(description+ " ("+count+")");
            }
            return super.createContextAwareInstance(context);
        }
    }
}
