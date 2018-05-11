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
import static ancestris.gedcom.Bundle.*;
import static ancestris.gedcom.PropertyFinder.Constants.*;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.NbBundle;

/**
 * @author daniel
 */
/**
 * This class is use to find all short relative for an Indi using an expression.
 * This expression is a String representing either
 * <li/>a TagPath for the originating Indi
 * <li/>a compound statement such as key1+key2+key3+...|key10+key11
 * wich means key3 applied to key2 applied to key1 epplied to Indi Or key11...
 * where key1... are key value for that Relative.
 */
//XXX: update javadoc
@NbBundle.Messages({"child=Children","grandchild=Grandchildren"})
public class Relative {
    // Relative cache

    private static final Map<PropertyFinder, Relative> RELATIVES = new HashMap<PropertyFinder, Relative>(20);
//    /** */
//    public static final String UNCLE = "uncle";
//    /** */
//    public static final String PUNCLE = "uncle.paternal";
//    /** */
//    public static final String MUNCLE = "uncle.maternal";
//    /** */
//    public static final String AUNT = "aunt";
//    /** */
//    public static final String PAUNT = "aunt.paternal";
//    /** */
//    public static final String MAUNT = "aunt.maternal";
//    public static final String UNCLEAUNT = "uncle.aunt";
//    public static final String FNEPHEW = "nephew.fraternal";
//    public static final String FNIECE = "niece.fraternal";
//    public static final String SNEPHEW = "nephew.sororal";
//    public static final String SNIECE = "niece.sororal";
//    //        public static final String PCOUSIN = "cousin.paternal";
//    //        public static final String MCOUSIN = "cousin.maternal";

    //FIXME: We could probably put description in PropertyFinder and 
    // cleanup things here
    static {
        // Parents
        create(NbBundle.getMessage(Relative.class, "father"), FATHER);
        create(NbBundle.getMessage(Relative.class, "mother"), MOTHER);
        create(NbBundle.getMessage(Relative.class, "parent"), PARENT);
        // grand parents
        create(NbBundle.getMessage(Relative.class, "grandparent"), GRANDPARENT);
        // Sibling
        create(NbBundle.getMessage(Relative.class, "sibling"), SIBLING);
        create(NbBundle.getMessage(Relative.class, "brother"), BROTHER);
        create(NbBundle.getMessage(Relative.class, "sister"), SISTER);
        //spouses
        create(NbBundle.getMessage(Relative.class, "husband"), HUSBAND);
        create(NbBundle.getMessage(Relative.class, "wife"), WIFE);
        create(NbBundle.getMessage(Relative.class, "spouse"), SPOUSE);
        // children
        create(child(), CHILD);
        create(NbBundle.getMessage(Relative.class, "daughter"), DAUGHTER);
        create(NbBundle.getMessage(Relative.class, "son"), SON);
        //GrandChildren
        create(NbBundle.getMessage(Relative.class, "grandchild"), GRANDCHILD);
        create(NbBundle.getMessage(Relative.class, "grandson"), GRANDSON);
        create(NbBundle.getMessage(Relative.class, "granddaughter"), GRANDDAUGHTER);
        //uncle & aunt
//        create(PUNCLE, "father+brother|father+sister +husband");
//        create(MUNCLE, "mother+brother|mother+sister +husband");
//        create(UNCLE, "parent+brother|parent+sister+husband");
//        create(PAUNT, "father+sister |father+brother+wife");
//        create(MAUNT, "mother+sister |mother+brother+wife");
//        create(AUNT, "parent+sister |parent+brother+wife");
        create(uncle_aunt(), UNCLE_AUNT);
        //nephew & niece
//        create(FNEPHEW, "brother+son");
//        create(FNIECE, "brother+daughter");
//        create(SNEPHEW, "sister+son");
//        create(SNIECE, "sister+daughter");
        // cousin
        create(NbBundle.getMessage(Relative.class, "first.cousin"), FIRSTCOUSIN);
    }
    /** how to get to it */
    private String description = "";
    private PropertyFinder finder = null;

    private Relative(String desc, PropertyFinder finder) {
        this.description = desc;
        this.finder = finder;
    }

    /**
     * Return human description for this relative (ie "Father").
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set human description for this relative (ie "Father").
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    public static Relative create(String Desc, PropertyFinder finder) {
        Relative result = RELATIVES.get(finder);
        if (result == null) {
            result = new Relative(Desc, finder);
            RELATIVES.put(finder, result);
        }
        return result;
    }

    public static Relative get(final PropertyFinder key) {
        return RELATIVES.get(key);
    }

    /**
     * Find all relatives of given roots and expression
     */
    public Collection<Entity> find(Collection<Entity> roots) {
        Collection<Entity> result = new ArrayList<Entity>();
        if (finder != null) {
            result = finder.find(roots);
        }
        return result;
    }

    /**
     * Find all relatives of given root and expression
     */
    public Collection<Entity> find(Entity root) {
        Collection<Entity> result = new ArrayList<Entity>();
        if (finder != null) {
            result = finder.find(root);
        }
        return result;

    }
    private static final int POSITION = 1500;

    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.ParentNavigateAction")
    @ActionRegistration(displayName = "Parents",lazy = false)
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 10)})
    public static class ParentNavigateAction extends NavigateAction {

        public ParentNavigateAction() {
            super(Relative.get(PARENT));
        }
    }

    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.SiblingNavigateAction")
    @ActionRegistration(displayName = "Sibling",lazy = false)
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 20)})
    public static class SiblingNavigateAction extends NavigateAction {

        public SiblingNavigateAction() {
            super(Relative.get(SIBLING));
        }
    }

    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.SpouseNavigateAction")
    @ActionRegistration(displayName = "Spouse",lazy = false)
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 30)})
    public static class SpouseNavigateAction extends NavigateAction {

        public SpouseNavigateAction() {
            super(Relative.create(NbBundle.getMessage(Relative.class, "spouse"),
                    new AbstractPropertyFinder.TagPathFinder(Indi.class, "INDI:FAMS:*")));
        }

        @Override
        @NbBundle.Messages({
            "parents.law=Parents in Law",
            "sibling.law=Brothers/Sisters in Law",
            "child.law=Son/Daughter in Law"
        })
        protected List<Action> buildActions(Entity entity, Collection<Entity> entities) {
            if (!(entity instanceof Indi))
                return super.buildActions(entity,entities);
            List<Action> result = new ArrayList<Action>();
            for (Entity e:entities){
                Fam fams = (Fam)e;
                Indi spouse = fams.getOtherSpouse((Indi)entity);
                if (spouse != null){
                    result.add(new JumpToEntityAction(spouse));
                    result.add(createSubmenuAction(parents_law(), null, PARENT.find(spouse)));
                    result.add(createSubmenuAction(sibling_law(), null, SIBLING.find(spouse)));
                }
                List<Entity> children = new ArrayList<Entity>(Arrays.asList(fams.getChildren()));
                result.add(createSubmenuAction(child(), null, children));
                result.add(createSubmenuAction(child_law(), null, SPOUSE.find(children)));
                result.add(createSubmenuAction(grandchild(), null, new ArrayList<Entity>(CHILD.find(children))));
                result.add(null);
            }
            return result;
        }
        
    }

//    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.ChildNavigateAction")
//    @ActionRegistration(displayName = "Children")
//    @ActionReferences(value = {
//        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 40)})
//    public static class ChildNavigateAction extends NavigateAction {
//
//        public ChildNavigateAction() {
//            super(Relative.get(CHILD));
//        }
//    }
//
    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.UncleAuntNavigateAction")
    @ActionRegistration(displayName = "Uncles",lazy = false)
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 50)})
    @NbBundle.Messages({"uncle.aunt=Uncles and Aunts"})
    public static class UncleAuntNavigateAction extends NavigateAction {

        public UncleAuntNavigateAction() {
            super(Relative.create(uncle_aunt(), UNCLE_AUNT));
        }
    }

    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.CousinNavigateAction")
    @ActionRegistration(displayName = "Cousin",lazy = false)
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 60)})
    public static class CousinNavigateAction extends NavigateAction {

        public CousinNavigateAction() {
            super(Relative.get(FIRSTCOUSIN));
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
    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.GrandParentNavigateAction")
    @ActionRegistration(displayName = "Mother",lazy = false)
    @ActionReferences(value = {
        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 5)})
    public static class GrandParentNavigateAction extends NavigateAction {

        public GrandParentNavigateAction() {
            super(Relative.get(GRANDPARENT));
        }
    }

//    @ActionID(category = "Navigate", id = "ancestris.gedcom.Relative.GrandChildNavigateAction")
//    @ActionRegistration(displayName = "Grand Children")
//    @ActionReferences(value = {
//        @ActionReference(path = "Ancestris/Actions/GedcomProperty/Navigate", position = POSITION + 80)})
//    public static class GrandChildNavigateAction extends NavigateAction {
//
//        public GrandChildNavigateAction() {
//            super(Relative.get(GRANDCHILD));
//        }
//    }

    /**
     * This class builds a submenu action populated with Indi relatives.
     * <p/>
     * The main usage for this class is subclassing this class by another class
     * with proper Action annotations to construc a menu or toolbar. Generally
     * only one call is necessary:
     * <pre> new NavigateAction(Relative)</pre>
     * The parameter is used to find relatives
     * (see {@link Relative#find(genj.gedcom.Property) })
     * as well as this submenu entry
     * descriptions as using {@link Relative#getDescription() }.
     * <p/>
     * If the submenu needs to be logically divided into smaller parts with
     * a separator between them, subsequent calls to {@link #addGroup(ancestris.gedcom.Relative) }
     * may be done and {@link #setDescription(java.lang.String) } can by used
     * to set this menu text properly
     */
    private static class NavigateAction extends SubMenuAction {

        private List<Relative> relatives;
        private String description = "";

        /**
         * C
         *
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
         *
         * @param relative
         */
        public void addGroup(Relative relative) {
            relatives.add(relative);
        }

        /**
         * Overrides this menu displayed test description.
         *
         * @param description
         */
        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        protected List<Action> buildActions(Entity entity, Collection<Entity> entities){
            List<Action> result  = new ArrayList<Action>();
                    for (Entity e : entities) {
                        result.add(new JumpToEntityAction(e));
                    }
                    return result;
        }
        @Override
        public Action createContextAwareInstance(org.openide.util.Lookup context) {
            Entity entity = context.lookup(Entity.class);
            if (entity != null && entity instanceof Indi) {
                int count = 0;
                clearActions();
                putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, false);
                for (Relative relative : relatives) {
                    Collection<Entity> find = relative.find(entity);
                    count += find.size();
                    addActions(buildActions(entity, find));
                    addAction(null);
                }
                setText(description + " (" + count + ")");
            } else {
                clearActions();
                setText("");
                putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            }
            return super.createContextAwareInstance(context);
        }
    }
    private static SubMenuAction createSubmenuAction(String description, Icon icon, Collection<Entity> entities){
        SubMenuAction result = new SubMenuAction();
        if (entities != null){
            for (Entity entity:entities){
                result.addAction(new JumpToEntityAction(entity));
            }
            result.setText(description+ " ("+entities.size()+")");
            result.setImage(icon);
        } else {
            result.clearActions();
            result.setText("");
            result.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }
        return result;
    }
}
