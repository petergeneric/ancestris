/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.nav;

import ancestris.api.editor.AncestrisEditor;
import ancestris.awt.FilteredMouseAdapter;
import ancestris.core.actions.AncestrisActionProvider;
import ancestris.gedcom.PropertyNode;
import ancestris.modules.beans.ABluePrintBeans;
import ancestris.modules.beans.AListBean;
import ancestris.view.ExplorerHelper;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintManager;
import genj.renderer.ChooseBlueprintAction;
import genj.util.Registry;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.awt.MouseUtils;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;


public final class FamilyPanel extends JPanel implements AncestrisActionProvider, GedcomListener {

    private final static Registry REGISTRY = Registry.get(FamilyPanel.class);
    
    private final static String BEG_EMPTY = "<p align=center>";
    private final static String END_EMPTY = "</p>";
    private final static String HUSBAND_EMPTY_BP = BEG_EMPTY + org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.husband.empty") + END_EMPTY;
    private final static String WIFE_EMPTY_BP = BEG_EMPTY + org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.wife.empty") + END_EMPTY;
    private final static String FATHER_EMPTY_BP = BEG_EMPTY + org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.father.empty") + END_EMPTY;
    private final static String MOTHER_EMPTY_BP = BEG_EMPTY + org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.mother.empty") + END_EMPTY;
    private final static String FAMS_EMPTY_BP = BEG_EMPTY + org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.fams.empty") + END_EMPTY;

    private final static String HUSBAND_BP = "navindi";
    private final static String WIFE_BP = "navspouse";
    private final static String PARENT_BP = "navparent";
    private final static String INDILINE_BP = "navindiline";
    private final static String EVENT_BP = "navevent";
    private final static String FAMI_BP = "navfamindi";
    private final static String FAMP_BP = "navfamparent";
    
    private final static String[] NAV_TAGS = { HUSBAND_BP, WIFE_BP, PARENT_BP, INDILINE_BP, EVENT_BP, FAMI_BP, FAMP_BP };

    
    private Context context;
    private boolean sticky = false;
    private Component selectedPanel = null;
    private Indi focusIndi;
    private Fam focusFam;
    private EntitiesPanel oFamsPanel = null;
    private int famIndex = 0;
    private EntitiesPanel childrenPanel = null;
    private EntitiesPanel siblingsPanel = null;
    private EntitiesPanel eventsPanel = null;

    /**
     * The blueprints we're using
     * 
     * - husband : indi (default Nav_Indi)
     * - wife : indi (default Nav_Spouse)
     * - husbFather : indi (default Nav_Parents)
     * - husbMother : indi (default Nav_Parents)
     * - oFamsPanel : indi (default Nav_Indi_Line)
     * - familySpouse : fam (default Default)
     * - childrenPanel : indi (default Nav_Indi_Line)
     * - familyParent : fam (default Default)
     * - siblingsPanel : indi (default Nav_Indi_Line)
     * - eventsPanel : indi (default Nav_Event)
     * 
     */
    private final Map<String, String> tag2blueprint = new HashMap<String, String>();

    
    
    
    
    /** Creates new form FamilyPanel */
    public FamilyPanel() {
        initComponents();
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane2.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane3.getVerticalScrollBar().setUnitIncrement(16);
    }

    public void init() {
        
        // Load saved blueprints NAMES in the view registry for each panel ; default to default panel tag
        for (String tag : NAV_TAGS) {
            tag2blueprint.put(tag, REGISTRY.get("blueprint." + tag, tag));
        }
        
        // Init main panels
        enableBlueprint(husband, new ABeanHandler(false), HUSBAND_EMPTY_BP);
        enableBlueprint(wife, new SpouseHandler(husband), WIFE_EMPTY_BP);
        enableBlueprint(husbFather, new ParentHandler(husband, PropertySex.MALE), FATHER_EMPTY_BP);
        enableBlueprint(husbMother, new ParentHandler(husband, PropertySex.FEMALE), MOTHER_EMPTY_BP);
        enableBlueprint(familySpouse, new ABeanHandler(false), FAMS_EMPTY_BP);
        enableBlueprint(familyParent, new ABeanHandler(false), FAMS_EMPTY_BP);

        // Init other panels
        // Childs
        childrenPanel = new EntitiesPanel(jScrollPane1) {
            @Override
            public Entity[] getEntities(Property rootProperty) {
                if (rootProperty != null && rootProperty instanceof Fam) {
                    return ((Fam) rootProperty).getChildren();
                }
                return null;
            }
        };

        // other families
        oFamsPanel = new EntitiesPanel(jScrollPane2) {
            @Override
            public Entity[] getEntities(Property rootProperty) {
                if (rootProperty != null && rootProperty instanceof Indi) {
                    return ((Indi) rootProperty).getPartners();
                }
                return null;
            }
        };
        

        // Siblings
        siblingsPanel = new EntitiesPanel(jScrollPane3) {
            @Override
            public Entity[] getEntities(Property rootProperty) {
                if (rootProperty != null && rootProperty instanceof Indi) {
                    return ((Indi) rootProperty).getSiblings(false);
                }
                return null;
            }
        };

        // Events
        eventsPanel = new EntitiesPanel(jsEvents) {
            @Override
            public Property[] getEntities(Property rootProperty) {
                if (rootProperty != null && rootProperty instanceof Indi) {
                    ArrayList<Property> result = new ArrayList<Property>(5);
                    for (Property p : rootProperty.getProperties()) {
                        if (p instanceof PropertyEvent) {
                            result.add(p);
                        }
                    }
                    return result.toArray(new Property[]{});
                }
                return null;
            }
        };

        // Set bueprint and display data
        resetBlueprints();
    }
    
    private void enableBlueprint(ABluePrintBeans bp, ABeanHandler bh, String defaultBP) {
        // Set mouse listener
        bp.addMouseListener(bh);
        
        // Init blueprint
        bp.setEmptyBluePrint(defaultBP);
        bp.setAntialiasing(true);
        
        // Set helper
        new ExplorerHelper(bp).setPopupAllowed(true);
        
    }

    private void setPanel(EntitiesPanel panel) {
        for (Component c : panel.getComponents()) {
            if (c instanceof ABluePrintBeans) {
                ABluePrintBeans bean = (ABluePrintBeans) c;
                bean.addMouseListener(new ABeanHandler(false));
                
                // Set tooltip
                bean.setToolTipText(NbBundle.getMessage(FamilyPanel.class, "GeneralTootlTipText"));

                // Set helper
                new ExplorerHelper(bean).setPopupAllowed(true);
            }
        }
    }

    
    
    public void resetBlueprints() {
        husband.setBlueprint(Gedcom.INDI, getBlueprint(Gedcom.INDI, HUSBAND_BP).getHTML());
        wife.setBlueprint(Gedcom.INDI, getBlueprint(Gedcom.INDI, WIFE_BP).getHTML());
        husbFather.setBlueprint(Gedcom.INDI, getBlueprint(Gedcom.INDI, PARENT_BP).getHTML());
        husbMother.setBlueprint(Gedcom.INDI, getBlueprint(Gedcom.INDI, PARENT_BP).getHTML());
        familySpouse.setBlueprint(Gedcom.FAM, getBlueprint(Gedcom.FAM, FAMI_BP).getHTML());
        familyParent.setBlueprint(Gedcom.FAM, getBlueprint(Gedcom.FAM, FAMP_BP).getHTML());
        
        oFamsPanel.setBlueprint(Gedcom.INDI, getBlueprint(Gedcom.INDI, INDILINE_BP).getHTML());
        childrenPanel.setBlueprint(Gedcom.INDI, getBlueprint(Gedcom.INDI, INDILINE_BP).getHTML());
        siblingsPanel.setBlueprint(Gedcom.INDI, getBlueprint(Gedcom.INDI, INDILINE_BP).getHTML());
        eventsPanel.setBlueprint("", getBlueprint(Gedcom.INDI, EVENT_BP).getHTML());  // empty tag will correspond to property tag
        refresh();
    }

    /**
     * Get blueprint from blueprint manager to be used for given type
     */
    private Blueprint getBlueprint(String entityTag, String BPtag) {
        return BlueprintManager.getInstance().getBlueprint(entityTag, tag2blueprint.get(BPtag));
    }
    
    
    public void setContext(Context context) {
        if (sticky) {
            //sticky = false;
            return;
        }
        
        if (context == null || context.getGedcom() == null) {
            return;
        }
        if (this.context != null && !context.getGedcom().equals(this.context.getGedcom())) {
            return;
        }
        Entity entity = context.getEntity();
        if (entity == null) {
            return;
        }

        famIndex = 0;
        this.context = context;
        if (entity instanceof Fam) {
            Fam family = (Fam) entity;
            if (family.getNoOfSpouses() == 0) {
                refresh();
                return;
            }
            // don't reset vue if focus fam is already this context's family:
            // spouses are not swapped if wife is focus indi
            if (family.equals(focusFam)) {
                refresh();
                return;
            }
            focusFam = ((Fam) entity);
            focusIndi = focusFam.getHusband();
            if (focusIndi == null) {
                focusIndi = focusFam.getWife();
            }
        } else if (entity instanceof Indi) {
            if (((Indi) entity).equals(focusIndi)) {
                refresh();
                return;
            }
            focusIndi = (Indi) entity;
            focusFam = null;
        } else {
            refresh();
            return;
        }
        refresh();

        
    }

    private void refresh() {

        if (focusIndi == null) {
            return;
        }
        
        if (focusIndi != null && focusIndi.getNoOfFams() > 0) {
            focusFam = focusIndi.getFamiliesWhereSpouse()[famIndex];
        }
        
        // Main indi, his/her father and his/her Mother
        husband.setContext(focusIndi);
        husbFather.setContext(focusIndi.getBiologicalFather());
        husbMother.setContext(focusIndi.getBiologicalMother());
        familySpouse.setContext(focusFam);
        
        // Spouse of main indi
        if (focusFam == null) {
            wife.setContext(null);
        } else {
            wife.setContext(focusFam.getOtherSpouse(focusIndi));
        }
        
        // OTHER SPOUSES
        oFamsPanel.update(husband.getProperty(), focusFam != null ? focusFam.getOtherSpouse(focusIndi) : null);
        oFamsPanel.setEnabled(((Indi) husband.getProperty()).getNoOfFams() > 0);
        
        // CHILDREN tab : Family entity of main indi and spouse and their children
        childrenPanel.update(familySpouse.getProperty() == null ? null : (Fam) (familySpouse.getProperty().getEntity()),null);

        // SIBLINGS tab : Siblings of indi based on family of father and mother
        siblingsPanel.update(husband.getProperty(), null);
        Fam famChild = ((Indi) husband.getProperty()).getFamilyWhereBiologicalChild();
        familyParent.setContext(famChild);
        
        // EVENTS tab
        eventsPanel.update(husband.getProperty(), null);

        // Enable panels' blueprints
        setPanel(oFamsPanel);
        setPanel(childrenPanel);
        setPanel(siblingsPanel);
        setPanel(eventsPanel);
    }


    /**
     * Action for blueprint modification depends on which panel has the focus
     * 
     * @param hasFocus
     * @param nodes
     * @return 
     */
    public List<Action> getActions(boolean hasFocus, Node[] nodes) {
        if (!hasFocus) {
            return new ArrayList<Action>();
        }
        
        // Get blueprint used from selected panel clicked
        String bp = "";
        if (selectedPanel == null) {
            return null;
        } else if (selectedPanel.equals(indiPanel)) {
            bp = HUSBAND_BP;
        } else if (selectedPanel.equals(spousePanel)) {
            bp = WIFE_BP;
        } else if (selectedPanel.equals(fatherPanel)) {
            bp = PARENT_BP;
        } else if (selectedPanel.equals(motherPanel)) {
            bp = PARENT_BP;
        } else if (selectedPanel.equals(oFamsPanel)) {
            bp = INDILINE_BP;
        } else if (selectedPanel.equals(famSpousePanel)) {
            bp = FAMI_BP;
        } else if (selectedPanel.equals(childrenPanel)) {
            bp = INDILINE_BP;
        } else if (selectedPanel.equals(famParentPanel)) {
            bp = FAMP_BP;
        } else if (selectedPanel.equals(siblingsPanel)) {
            bp = INDILINE_BP;
        } else if (selectedPanel.equals(eventsPanel)) {
            bp = EVENT_BP;
        }
        final String blueprintTag = bp;
        
        // Get property clicked
        Property prop = null;
        if (nodes != null && nodes.length > 0 && nodes[0] instanceof PropertyNode) {
            prop = ((PropertyNode) nodes[0]).getProperty();
            if (prop != null) {
                prop = prop.getEntity();
            }
        }
        
        // Generate the action
        List<Action> actions = new ArrayList<Action>();
        if (prop != null && prop instanceof Indi) {
            actions.add(new ChooseBlueprintAction((Entity)prop, getBlueprint(Gedcom.INDI, blueprintTag)) {
                @Override
                protected void commit(Entity recipient, Blueprint blueprint) {
                    tag2blueprint.put(blueprintTag, blueprint.getName());
                    resetBlueprints();
                    REGISTRY.put("blueprint." + blueprintTag, blueprint.getName());
                }
            });
        }
        if (prop != null && prop instanceof Fam) {
            actions.add(new ChooseBlueprintAction((Entity)prop, getBlueprint(Gedcom.FAM, blueprintTag)) {
                @Override
                protected void commit(Entity recipient, Blueprint blueprint) {
                    tag2blueprint.put(blueprintTag, blueprint.getName());
                    resetBlueprints();
                    REGISTRY.put("blueprint." + blueprintTag, blueprint.getName());
                }
            });
        }
        sticky = false;
        return actions;
    }


    

    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fatherPanel = new javax.swing.JPanel();
        husbFather = new ancestris.modules.beans.ABluePrintBeans();
        motherPanel = new javax.swing.JPanel();
        husbMother = new ancestris.modules.beans.ABluePrintBeans();
        otherSpousePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        indiPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        husband = new ancestris.modules.beans.ABluePrintBeans();
        spousePanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        wife = new ancestris.modules.beans.ABluePrintBeans();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        famSpousePanel = new javax.swing.JPanel();
        familySpouse = new ancestris.modules.beans.ABluePrintBeans();
        jScrollPane1 = new javax.swing.JScrollPane();
        famParentPanel = new javax.swing.JPanel();
        familyParent = new ancestris.modules.beans.ABluePrintBeans();
        jScrollPane3 = new javax.swing.JScrollPane();
        eventsTab = new javax.swing.JPanel();
        jsEvents = new javax.swing.JScrollPane();

        setPreferredSize(new java.awt.Dimension(400, 400));
        setRequestFocusEnabled(false);

        fatherPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.fatherPanel.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14))); // NOI18N
        fatherPanel.setPreferredSize(new java.awt.Dimension(145, 121));

        husbFather.setToolTipText(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "GeneralTootlTipText")); // NOI18N
        husbFather.setMinimumSize(new java.awt.Dimension(0, 80));
        husbFather.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout husbFatherLayout = new javax.swing.GroupLayout(husbFather);
        husbFather.setLayout(husbFatherLayout);
        husbFatherLayout.setHorizontalGroup(
            husbFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        husbFatherLayout.setVerticalGroup(
            husbFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout fatherPanelLayout = new javax.swing.GroupLayout(fatherPanel);
        fatherPanel.setLayout(fatherPanelLayout);
        fatherPanelLayout.setHorizontalGroup(
            fatherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbFather, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
        );
        fatherPanelLayout.setVerticalGroup(
            fatherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbFather, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
        );

        motherPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.motherPanel.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14))); // NOI18N
        motherPanel.setPreferredSize(new java.awt.Dimension(145, 121));

        husbMother.setToolTipText(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "GeneralTootlTipText")); // NOI18N
        husbMother.setMinimumSize(new java.awt.Dimension(0, 80));
        husbMother.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout husbMotherLayout = new javax.swing.GroupLayout(husbMother);
        husbMother.setLayout(husbMotherLayout);
        husbMotherLayout.setHorizontalGroup(
            husbMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        husbMotherLayout.setVerticalGroup(
            husbMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout motherPanelLayout = new javax.swing.GroupLayout(motherPanel);
        motherPanel.setLayout(motherPanelLayout);
        motherPanelLayout.setHorizontalGroup(
            motherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbMother, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
        );
        motherPanelLayout.setVerticalGroup(
            motherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbMother, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
        );

        otherSpousePanel.setPreferredSize(new java.awt.Dimension(145, 121));

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jLabel1.text")); // NOI18N

        jScrollPane2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane2.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));

        javax.swing.GroupLayout otherSpousePanelLayout = new javax.swing.GroupLayout(otherSpousePanel);
        otherSpousePanel.setLayout(otherSpousePanelLayout);
        otherSpousePanelLayout.setHorizontalGroup(
            otherSpousePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        otherSpousePanelLayout.setVerticalGroup(
            otherSpousePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(otherSpousePanelLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(2, 2, 2)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
        );

        indiPanel.setBorder(null);
        indiPanel.setPreferredSize(new java.awt.Dimension(256, 150));

        jLabel3.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jLabel3.text")); // NOI18N

        husband.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        husband.setToolTipText(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "GeneralTootlTipText")); // NOI18N

        javax.swing.GroupLayout husbandLayout = new javax.swing.GroupLayout(husband);
        husband.setLayout(husbandLayout);
        husbandLayout.setHorizontalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        husbandLayout.setVerticalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 115, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout indiPanelLayout = new javax.swing.GroupLayout(indiPanel);
        indiPanel.setLayout(indiPanelLayout);
        indiPanelLayout.setHorizontalGroup(
            indiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husband, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        indiPanelLayout.setVerticalGroup(
            indiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(indiPanelLayout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(2, 2, 2)
                .addComponent(husband, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        spousePanel.setBorder(null);
        spousePanel.setPreferredSize(new java.awt.Dimension(165, 150));

        jLabel2.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jLabel2.text")); // NOI18N

        wife.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        wife.setToolTipText(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "GeneralTootlTipText")); // NOI18N
        wife.setMinimumSize(new java.awt.Dimension(0, 40));
        wife.setPreferredSize(new java.awt.Dimension(256, 60));

        javax.swing.GroupLayout wifeLayout = new javax.swing.GroupLayout(wife);
        wife.setLayout(wifeLayout);
        wifeLayout.setHorizontalGroup(
            wifeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        wifeLayout.setVerticalGroup(
            wifeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 115, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout spousePanelLayout = new javax.swing.GroupLayout(spousePanel);
        spousePanel.setLayout(spousePanelLayout);
        spousePanelLayout.setHorizontalGroup(
            spousePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(wife, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        spousePanelLayout.setVerticalGroup(
            spousePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spousePanelLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(2, 2, 2)
                .addComponent(wife, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        jTabbedPane1.setBorder(null);
        jTabbedPane1.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(388, 200));

        familySpouse.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        familySpouse.setToolTipText(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "GeneralTootlTipText")); // NOI18N
        familySpouse.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout familySpouseLayout = new javax.swing.GroupLayout(familySpouse);
        familySpouse.setLayout(familySpouseLayout);
        familySpouseLayout.setHorizontalGroup(
            familySpouseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 373, Short.MAX_VALUE)
        );
        familySpouseLayout.setVerticalGroup(
            familySpouseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 45, Short.MAX_VALUE)
        );

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        javax.swing.GroupLayout famSpousePanelLayout = new javax.swing.GroupLayout(famSpousePanel);
        famSpousePanel.setLayout(famSpousePanelLayout);
        famSpousePanelLayout.setHorizontalGroup(
            famSpousePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
            .addComponent(familySpouse, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
        );
        famSpousePanelLayout.setVerticalGroup(
            famSpousePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(famSpousePanelLayout.createSequentialGroup()
                .addComponent(familySpouse, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.famSpousePanel.TabConstraints.tabTitle"), famSpousePanel); // NOI18N

        familyParent.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        familyParent.setToolTipText(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "GeneralTootlTipText")); // NOI18N
        familyParent.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout familyParentLayout = new javax.swing.GroupLayout(familyParent);
        familyParent.setLayout(familyParentLayout);
        familyParentLayout.setHorizontalGroup(
            familyParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 373, Short.MAX_VALUE)
        );
        familyParentLayout.setVerticalGroup(
            familyParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 45, Short.MAX_VALUE)
        );

        jScrollPane3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        javax.swing.GroupLayout famParentPanelLayout = new javax.swing.GroupLayout(famParentPanel);
        famParentPanel.setLayout(famParentPanelLayout);
        famParentPanelLayout.setHorizontalGroup(
            famParentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(familyParent, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
        );
        famParentPanelLayout.setVerticalGroup(
            famParentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(famParentPanelLayout.createSequentialGroup()
                .addComponent(familyParent, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.famParentPanel.TabConstraints.tabTitle"), famParentPanel); // NOI18N

        eventsTab.setBackground(java.awt.Color.white);

        jsEvents.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jsEvents.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        javax.swing.GroupLayout eventsTabLayout = new javax.swing.GroupLayout(eventsTab);
        eventsTab.setLayout(eventsTabLayout);
        eventsTabLayout.setHorizontalGroup(
            eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 375, Short.MAX_VALUE)
            .addGroup(eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jsEvents, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE))
        );
        eventsTabLayout.setVerticalGroup(
            eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
            .addGroup(eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jsEvents, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.eventsTab.TabConstraints.tabTitle"), eventsTab); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(fatherPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(motherPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                            .addComponent(indiPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(otherSpousePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                            .addComponent(spousePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(motherPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                            .addComponent(fatherPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(otherSpousePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                        .addGap(9, 9, 9)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(indiPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                    .addComponent(spousePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel eventsTab;
    private javax.swing.JPanel famParentPanel;
    private javax.swing.JPanel famSpousePanel;
    private ancestris.modules.beans.ABluePrintBeans familyParent;
    private ancestris.modules.beans.ABluePrintBeans familySpouse;
    private javax.swing.JPanel fatherPanel;
    private ancestris.modules.beans.ABluePrintBeans husbFather;
    private ancestris.modules.beans.ABluePrintBeans husbMother;
    private ancestris.modules.beans.ABluePrintBeans husband;
    private javax.swing.JPanel indiPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JScrollPane jsEvents;
    private javax.swing.JPanel motherPanel;
    private javax.swing.JPanel otherSpousePanel;
    private javax.swing.JPanel spousePanel;
    private ancestris.modules.beans.ABluePrintBeans wife;
    // End of variables declaration//GEN-END:variables

    public static boolean editEvent(PropertyEvent prop, boolean isNew) {
        String title;
//XXX: 0.8 commented temporarily
//        if (isNew) {
//            title = NbBundle.getMessage(FamilyPanel.class, "dialog.indi.new.title", prop);
//        } else {
//            title = NbBundle.getMessage(FamilyPanel.class, "dialog.indi.edit.title", prop);
//        }
//        if (prop == null) {
//            return false;
//        }
//        final EventBean propEditor = new EventBean();
//        propEditor.setContext(prop);
//        NotifyDescriptor nd = new NotifyDescriptor(new JScrollPane(propEditor), title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
//        DialogDisplayer.getDefault().notify(nd);
//        if (!nd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
//            return false;
//        }
//        try {
//            prop.getGedcom().doUnitOfWork(new UnitOfWork() {
//
//                public void perform(Gedcom gedcom) throws GedcomException {
//                    propEditor.commit();
//                }
//            });
//        } catch (GedcomException ex) {
//            Exceptions.printStackTrace(ex);
//            return false;
//        }
        return true;
    }

    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        refresh();
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        refresh();
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        refresh();
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        refresh();
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        refresh();
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    private class ABeanHandler extends FilteredMouseAdapter {

        private boolean editOnClick = false;
        private ActionListener action = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };

        public ABeanHandler(Action action) {
            this.action = action;
        }

        private ABeanHandler() {
            this(false);
        }

        /**
         *
         * @param edit true if single click mouse must launch editor
         */
        private ABeanHandler(boolean editOnClic) {
            this.editOnClick = editOnClic;
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            if (evt != null && evt.getButton() == MouseEvent.BUTTON3) {
                Object src = evt.getSource();
                if (src != null && (src instanceof ABluePrintBeans)) {
                    ABluePrintBeans bean = (ABluePrintBeans) src;
                    Property prop = bean.getProperty();
                    if (prop != null) {
                        sticky = true;
                        selectedPanel = bean.getParent();
                        SelectionDispatcher.fireSelection(evt, new Context(prop));
                    }
                }
            }
        }

        
        @Override
        public void mouseClickedFiltered(java.awt.event.MouseEvent evt) {
            
            // Return is it is not a mouse click or not a normal click (left-click)
            if (evt.getID() != MouseEvent.MOUSE_CLICKED || evt.getButton() != MouseEvent.BUTTON1) {
                return;
            }

            // Return if no source
            Object src = evt.getSource();
            if (src == null) {
                return;
            }
            
            // Get bean clicked
            ABluePrintBeans bean = null;
            if (src instanceof ABluePrintBeans) {
                bean = (ABluePrintBeans) src;
            }
            
            if (editOnClick || MouseUtils.isDoubleClick(evt) || bean == null || bean.getProperty() == null) {
                try {
                    sticky = true;
                    // Double click on someone = edit it (with an AncestrisEditor, not an Editor)
                    if (bean != null && bean.getProperty() != null) {                                       
                        AncestrisEditor editor = AncestrisEditor.findEditor(bean.getProperty());
                        if (editor != null) {
                            editor.edit(bean.getProperty());
                        }
                    } else {
                    // Double click on empty = create person (with an AncestrisEditor, not an Editor)
                        getCreateAction().actionPerformed(new ActionEvent(evt.getSource(), 0, ""));         
                    }
                    refresh();
                } finally {
                    sticky = false;
                }
            // Click on someone = show it     
            } else if (evt.getClickCount() == 1) {
                // FIXME: test click count necessaire?
                Container c = bean.getParent();
                if (c != null) {
                    Property prop = bean.getProperty();
                    if (prop instanceof Entity) {
                        // In case of selection of another spouse, change context back to main indi and only change spouse index
                        if (c.equals(oFamsPanel) && prop instanceof Indi) {
                            Indi spouse = (Indi) prop; // other spouse clicked
                            Fam[] fams = focusIndi.getFamiliesWhereSpouse();
                            for (int idx = 0; idx < fams.length; idx++) {
                                if (fams[idx].getOtherSpouse(focusIndi).equals(spouse)) {
                                    famIndex = idx;
                                    refresh();
                                    return;
                                }
                            }
                        }
                    }
                    sticky = false;
                    SelectionDispatcher.fireSelection(new Context(prop));
                }
            }
        }

        public ActionListener getCreateAction() {
            return action;
        }
    }

    private class SpouseHandler extends ABeanHandler {

        private final ABluePrintBeans otherBean;

        public SpouseHandler(ABluePrintBeans other) {
            super();
            this.otherBean = other;
        }

        @Override
        public ActionListener getCreateAction() {
            Indi indi = null;
            if (otherBean != null) {
                indi = (Indi) otherBean.getProperty();
            }
            AncestrisEditor editor = AncestrisEditor.findEditor(indi);
            if (editor == null){
                // get NoOp editro
                editor = AncestrisEditor.findEditor(null);
            }
            return editor.getCreateSpouseAction(indi);
        }
    }

    private class ParentHandler extends ABeanHandler {

        int sex;
        private final ABluePrintBeans childBean;

        public ParentHandler(ABluePrintBeans indiBean, int sex) {
            super();
            this.childBean = indiBean;
            this.sex = sex;
        }

        @Override
        public ActionListener getCreateAction() {
            Indi child = null;
            if (childBean != null) {
                child = (Indi) childBean.getProperty();
            }
            AncestrisEditor editor = AncestrisEditor.findEditor(child);
            if (editor == null){
                // get NoOp editro
                editor = AncestrisEditor.findEditor(null);
            }

            return editor.getCreateParentAction(child, sex);
        }
    }

    
    
    private abstract class EntitiesPanel extends AListBean {

        public EntitiesPanel(JScrollPane pane) {
            super();
            setBackground(java.awt.Color.white);
            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));
            pane.setViewportView(this);
        }

        public abstract Property[] getEntities(Property rootProperty);

        public void update(Property rootProperty, Property exclude) {
            removeAll();
            // This call should remove all anonymous listeners
            // https://stackoverflow.com/questions/8727752/does-disposing-of-container-remove-all-registered-listeners
            repaint();
            if (rootProperty != null) {
                add(getEntities(rootProperty), exclude, new ABeanHandler());
            }
            revalidate();
        }
    }
}
