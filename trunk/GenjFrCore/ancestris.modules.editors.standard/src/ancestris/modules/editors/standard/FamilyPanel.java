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
package ancestris.modules.editors.standard;

import ancestris.modules.editors.standard.actions.ACreateParent;
import ancestris.modules.editors.standard.actions.ACreateChild;
import ancestris.modules.editors.standard.actions.ACreateSpouse;
import ancestris.util.FilteredMouseAdapter;
import ancestris.modules.beans.ABluePrintBeans;
import ancestris.modules.beans.AFamBean;
import ancestris.modules.beans.AIndiBean;
import ancestris.modules.beans.AListBean;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import genj.gedcom.UnitOfWork;
import genj.view.SelectionSink;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import ancestris.modules.editors.standard.actions.AActions;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import java.util.ArrayList;
import javax.swing.border.EmptyBorder;
import org.openide.awt.MouseUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class FamilyPanel extends JPanel implements IEditorPanel {

    private final static String EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.empty");
    private final static String WIFE_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.wife.empty");
    private final static String HUSBAND_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.husband.empty");
    private final static String CHILD_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.child.empty");
    private final static String FATHER_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.father.empty");
    private final static String MOTHER_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.mother.empty");
    private final static String FAMS_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.fams.empty");
    private final static String VOID_BP = "";
    private Context context;
    private Indi focusIndi;
    private Fam focusFam;
    private int muteContext = 0;
    private final EntitiesPanel childrenPanel;
    private final EntitiesPanel oFamsPanel;
    private final EntitiesPanel siblingsPanel;
    private final EntitiesPanel eventsPanel;
    private ActionListener NoOpAction = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
        }
    };
    private javax.swing.JButton btAddSpouse;
    private javax.swing.JButton btUnlinkSpouse;
    private javax.swing.JButton btAddChild;
    private javax.swing.JButton btUnlinkFamc;
    private javax.swing.JButton btAddSibling;

    /** Creates new form FamilyPanel */
    public FamilyPanel() {
        initComponents();

        // Adds spouse toolbar buttons
        btAddSpouse = new EditorButton();
        toolSpouse.add(btAddSpouse);
        btUnlinkSpouse = new EditorButton();
        toolSpouse.add(btUnlinkSpouse);

        // Adds indi toolbar buttons
        btAddChild = new EditorButton();
        toolIndi.add(btAddChild);
        btAddSibling = new EditorButton();
        toolIndi.add(btAddSibling);
        btUnlinkFamc = new EditorButton();
        toolIndi.add(btUnlinkFamc);

        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);

        // Add listners
        ABeanHandler handler;
        handler = new ABeanHandler();
        handler.setEditOnClick(true);
        husband.addMouseListener(handler);

        wife.addMouseListener(new SpouseHandler(husband));
        husbFather.addMouseListener(new ParentHandler(husband, PropertySex.MALE));
        husbMother.addMouseListener(new ParentHandler(husband, PropertySex.FEMALE));
        handler = new ABeanHandler();
        handler.setEditOnClick(true);
        familySpouse.addMouseListener(handler);

        husband.setEmptyBluePrint(HUSBAND_EMPTY_BP);
        husband.setBlueprint(Gedcom.INDI, "<body bgcolor=#e9e9ff>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));

        wife.setEmptyBluePrint(WIFE_EMPTY_BP);
        wife.setBlueprint(Gedcom.INDI, "<body bgcolor=#f1f1ff>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));

        husbFather.setEmptyBluePrint(FATHER_EMPTY_BP);
        husbFather.setBlueprint(Gedcom.INDI, "<body bgcolor=#f1f1f1>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));

        husbMother.setEmptyBluePrint(MOTHER_EMPTY_BP);
        husbMother.setBlueprint(Gedcom.INDI, "<body bgcolor=#f1f1f1>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));

        familySpouse.setEmptyBluePrint(FAMS_EMPTY_BP);
        familySpouse.setBlueprint(Gedcom.FAM, "<body bgcolor=#f1f1ff>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.FAM"));

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
                    return ((Indi) rootProperty).getFamiliesWhereSpouse();
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
                    for (Property p:rootProperty.getProperties()){
                        if (p instanceof PropertyEvent)
                            result.add(p);
                    }
                    return result.toArray(new Property[] {});
                }
                return null;
            }
        };
        eventsPanel.setBlueprint("", "<i><name path=.></i>&nbsp;:&nbsp;<prop path=.:DATE img=no>&nbsp;(<prop path=.:PLAC>)");
    }

    public void setContext(Context context) {
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

        this.context = context;
        if (entity instanceof Fam) {
            if (((Fam) entity).getNoOfSpouses() == 0) {
                return;
            }
            focusFam = ((Fam) entity);
            focusIndi = focusFam.getHusband();
            if (focusIndi == null) {
                focusIndi = focusFam.getWife();
            }
        } else if (entity instanceof Indi) {
            focusIndi = (Indi) entity;
            focusFam = null;
        } else {
            return;
        }
        refresh();
    }

    private void refresh() {

        if (focusFam == null && focusIndi.getNoOfFams() > 0) {
            focusFam = focusIndi.getFamiliesWhereSpouse()[0];
        }
        husband.setContext(focusIndi);
        husbFather.setContext(focusIndi.getBiologicalFather());
        husbMother.setContext(focusIndi.getBiologicalMother());
        familySpouse.setContext(focusFam);
        if (focusFam == null) {
            wife.setContext(null);
        } else {
            wife.setContext(focusFam.getOtherSpouse(focusIndi));
        }
        childrenPanel.update(
                familySpouse.getContext() == null ? null : (Fam) (familySpouse.getContext().getEntity()),
                null,
                null);
        btAddChild.setAction(getCreateChildAction());

        Fam famChild = ((Indi) husband.getContext()).getFamilyWhereBiologicalChild();
        familyParent.setContext(famChild);
        //siblingsPanel.update(husband.getContext(), null, new ABeanHandler(new ACreateChild(famChild, this)));
        siblingsPanel.update(husband.getContext(), null, null);

        oFamsPanel.update(husband.getContext(), familySpouse == null ? null : familySpouse.getContext(), null);
        btAddSpouse.setAction(getCreateSpouseActions());
        btUnlinkSpouse.setAction(getUnlinkSpouseAction());
        btUnlinkFamc.setAction(getUnlinkFamcAction());
        btAddSibling.setAction(getAddSiblingAction());

        eventsPanel.update(husband.getContext(), null, null);

    }

    @Override
    public String getName() {
        return "Completer le noyau familial";
    }

    private Action getCreateChildAction(){
        return AActions.alwaysEnabled(
                new ACreateChild((Fam) familySpouse.getContext(), this),
                "",
                org.openide.util.NbBundle.getMessage(FamilyPanel.class, "create.child.action.tt",husband.getContext()),
                "ancestris/modules/editors/standard/images/add-child.png",
                true);
    }
    private Action getCreateSpouseActions(){
        return AActions.alwaysEnabled(
                new ACreateSpouse((Indi) husband.getContext(), this),
                "",
                "Ajouter un conjoint",
                "ancestris/modules/editors/standard/images/add-spouse.png",
                true);
    }
    private Action getUnlinkSpouseAction(){
        return AActions.alwaysEnabled(
                NoOpAction,
                "",
                "Supprimer le liens vers un conjoint",
                "ancestris/modules/editors/standard/images/unlink-spouse.png",
                true);
    }
    private Action getUnlinkFamcAction(){
        return AActions.alwaysEnabled(
                NoOpAction,
                "",
                "Supprimer le liens avec les parents",
                "ancestris/modules/editors/standard/images/unlink-famc.png",
                true);
    }

    private Action getAddSiblingAction(){
        return AActions.alwaysEnabled(
                NoOpAction,
                "",
                "Ajouter un frère ou un soeur",
                "ancestris/modules/editors/standard/images/add-sibling.png",
                true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        familySpouse = new ancestris.modules.beans.ABluePrintBeans();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        familyParent = new ancestris.modules.beans.ABluePrintBeans();
        jPanel2 = new javax.swing.JPanel();
        eventsTab = new javax.swing.JPanel();
        jsEvents = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        husbFather = new ancestris.modules.beans.ABluePrintBeans();
        jPanel4 = new javax.swing.JPanel();
        husbMother = new ancestris.modules.beans.ABluePrintBeans();
        jPanel5 = new javax.swing.JPanel();
        husband = new ancestris.modules.beans.ABluePrintBeans();
        jLabel3 = new javax.swing.JLabel();
        toolIndi = new javax.swing.JToolBar();
        jPanel6 = new javax.swing.JPanel();
        wife = new ancestris.modules.beans.ABluePrintBeans();
        jLabel2 = new javax.swing.JLabel();
        toolSpouse = new javax.swing.JToolBar();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(622, 500));
        setRequestFocusEnabled(false);

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTabbedPane1.setFont(new java.awt.Font("Dialog", 3, 14));

        familySpouse.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        familySpouse.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout familySpouseLayout = new javax.swing.GroupLayout(familySpouse);
        familySpouse.setLayout(familySpouseLayout);
        familySpouseLayout.setHorizontalGroup(
            familySpouseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 590, Short.MAX_VALUE)
        );
        familySpouseLayout.setVerticalGroup(
            familySpouseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 31, Short.MAX_VALUE)
        );

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
            .addComponent(familySpouse, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(familySpouse, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        familyParent.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        familyParent.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout familyParentLayout = new javax.swing.GroupLayout(familyParent);
        familyParent.setLayout(familyParentLayout);
        familyParentLayout.setHorizontalGroup(
            familyParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 590, Short.MAX_VALUE)
        );
        familyParentLayout.setVerticalGroup(
            familyParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 31, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(familyParent, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(familyParent, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel8.TabConstraints.tabTitle"), jPanel8); // NOI18N

        jPanel2.setBackground(java.awt.Color.white);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 592, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        eventsTab.setBackground(java.awt.Color.white);

        jsEvents.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jsEvents.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        javax.swing.GroupLayout eventsTabLayout = new javax.swing.GroupLayout(eventsTab);
        eventsTab.setLayout(eventsTabLayout);
        eventsTabLayout.setHorizontalGroup(
            eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 592, Short.MAX_VALUE)
            .addGroup(eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jsEvents, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE))
        );
        eventsTabLayout.setVerticalGroup(
            eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
            .addGroup(eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jsEvents, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.eventsTab.TabConstraints.tabTitle"), eventsTab); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel3.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 3, 14))); // NOI18N

        husbFather.setMinimumSize(new java.awt.Dimension(0, 80));
        husbFather.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout husbFatherLayout = new javax.swing.GroupLayout(husbFather);
        husbFather.setLayout(husbFatherLayout);
        husbFatherLayout.setHorizontalGroup(
            husbFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 284, Short.MAX_VALUE)
        );
        husbFatherLayout.setVerticalGroup(
            husbFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbFather, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbFather, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel4.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 3, 14))); // NOI18N

        husbMother.setMinimumSize(new java.awt.Dimension(0, 80));
        husbMother.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout husbMotherLayout = new javax.swing.GroupLayout(husbMother);
        husbMother.setLayout(husbMotherLayout);
        husbMotherLayout.setHorizontalGroup(
            husbMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 284, Short.MAX_VALUE)
        );
        husbMotherLayout.setVerticalGroup(
            husbMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbMother, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbMother, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
        );

        jPanel5.setBorder(null);

        javax.swing.GroupLayout husbandLayout = new javax.swing.GroupLayout(husband);
        husband.setLayout(husbandLayout);
        husbandLayout.setHorizontalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        husbandLayout.setVerticalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
        );

        jLabel3.setFont(new java.awt.Font("DejaVu Sans", 3, 16));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jLabel3.text")); // NOI18N

        toolIndi.setFloatable(false);
        toolIndi.setRollover(true);
        toolIndi.setPreferredSize(new java.awt.Dimension(100, 18));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 128, Short.MAX_VALUE)
                .addComponent(toolIndi, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(husband, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(toolIndi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(husband, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(null);

        javax.swing.GroupLayout wifeLayout = new javax.swing.GroupLayout(wife);
        wife.setLayout(wifeLayout);
        wifeLayout.setHorizontalGroup(
            wifeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        wifeLayout.setVerticalGroup(
            wifeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );

        jLabel2.setFont(new java.awt.Font("DejaVu Sans", 3, 14));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jLabel2.text")); // NOI18N

        toolSpouse.setFloatable(false);
        toolSpouse.setRollover(true);
        toolSpouse.setPreferredSize(new java.awt.Dimension(100, 18));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 109, Short.MAX_VALUE)
                .addComponent(toolSpouse, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(wife, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(toolSpouse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wife, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 3, 14));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(173, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(7, 7, 7)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel eventsTab;
    private ancestris.modules.beans.ABluePrintBeans familyParent;
    private ancestris.modules.beans.ABluePrintBeans familySpouse;
    private ancestris.modules.beans.ABluePrintBeans husbFather;
    private ancestris.modules.beans.ABluePrintBeans husbMother;
    private ancestris.modules.beans.ABluePrintBeans husband;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JScrollPane jsEvents;
    private javax.swing.JToolBar toolIndi;
    private javax.swing.JToolBar toolSpouse;
    private ancestris.modules.beans.ABluePrintBeans wife;
    // End of variables declaration//GEN-END:variables

    private void fireSelection(Entity entity) {
        if (entity != null) {
            SelectionSink.Dispatcher.fireSelection(new Context(entity), false);
        }
    }

    public boolean editProperty(Property entity, boolean isNew) {
        // FIXME: Horror!
        if (entity instanceof Indi) {
            return editEntity((Indi) entity, isNew);
        }
        if (entity instanceof Fam) {
            return editEntity((Fam) entity, isNew);
        }
        return false;
    }

    public boolean editEntity(Fam fam, boolean isNew) {
        String title;
        if (isNew) {
            title = NbBundle.getMessage(FamilyPanel.class, "dialog.fam.new.title", fam);
        } else {
            title = NbBundle.getMessage(FamilyPanel.class, "dialog.fam.edit.title", fam);
        }
        final AFamBean bean = new AFamBean();
        NotifyDescriptor nd = new NotifyDescriptor(bean.setRoot(fam), title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        DialogDisplayer.getDefault().notify(nd);
        if (!nd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
            return false;
        }
        try {
            fam.getGedcom().doUnitOfWork(new UnitOfWork() {

                public void perform(Gedcom gedcom) throws GedcomException {
                    bean.commit();
                }
            });
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        return true;
    }

    public static boolean editEntity(Indi indi, boolean isNew) {
        String title;
        if (isNew) {
            title = NbBundle.getMessage(FamilyPanel.class, "dialog.indi.new.title", indi);
        } else {
            title = NbBundle.getMessage(FamilyPanel.class, "dialog.indi.edit.title", indi);
        }
        if (indi == null) {
            return false;
        }
        final AIndiBean bean = new AIndiBean();
        NotifyDescriptor nd = new NotifyDescriptor(new JScrollPane(bean.setRoot(indi)), title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        DialogDisplayer.getDefault().notify(nd);
        if (!nd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
            return false;
        }
        try {
            indi.getGedcom().doUnitOfWork(new UnitOfWork() {

                public void perform(Gedcom gedcom) throws GedcomException {
                    bean.commit();
                }
            });
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        return true;
    }

    @Override
    public void commit() {
    }

    private class ABeanHandler extends FilteredMouseAdapter {

        private boolean editOnClick = false;
        private ActionListener action = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        };

        public ABeanHandler(Action action) {
            this.action = action;
        }

        private ABeanHandler() {
        }

        /**
         *
         * @param edit true if single click mouse must launch editor
         */
        void setEditOnClick(boolean edit) {
            editOnClick = edit;
        }

        @Override
        public void filteredMouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getButton() != MouseEvent.BUTTON1 || evt.getID() != MouseEvent.MOUSE_CLICKED) {
                return;
            }
            Object src = evt.getSource();
            if (src == null) {
                return;
            }
            ABluePrintBeans bean = null;
            if (src instanceof ABluePrintBeans) {
                bean = (ABluePrintBeans) src;
            }
            if (editOnClick || MouseUtils.isDoubleClick(evt) || bean == null || bean.getContext() == null) {
                SelectionSink.Dispatcher.muteSelection(true);
                try {
                    if (bean != null && bean.getContext() != null) {
                        editProperty(bean.getContext(), false);
                    } else {
                        getCreateAction().actionPerformed(new ActionEvent(evt.getSource(), 0, ""));
                    }
                    refresh();
                } finally {
                    SelectionSink.Dispatcher.muteSelection(false);
                }
            } else if (evt.getClickCount() == 1) {
                // FIXME: test click count necessaire?
                Property prop = bean.getContext();
                if (prop instanceof Entity)
                    fireSelection((Entity)prop);
            }
        }

        public ActionListener getCreateAction() {
            return action;
        }

        ;
    }

    private class SpouseHandler extends ABeanHandler {

        private final ABluePrintBeans otherBean;

        public SpouseHandler(ABluePrintBeans other) {
            super();
            this.otherBean = other;
        }

        @Override
        public ActionListener getCreateAction() {
            if (otherBean == null || otherBean.getContext() == null) {
                return new ACreateSpouse(null, ancestris.modules.editors.standard.FamilyPanel.this);
            }
            return new ACreateSpouse((Indi) otherBean.getContext(), ancestris.modules.editors.standard.FamilyPanel.this);
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
            if (childBean == null || childBean.getContext() == null) {
                return new ACreateParent(null, PropertySex.MALE);
            }
            return new ACreateParent((Indi) childBean.getContext(), sex);
        }
    }

    private class ChildHandler extends ABeanHandler {

        ABluePrintBeans famcBean;
        private final ABluePrintBeans parentBean;

        public ChildHandler(ABluePrintBeans parentBean, ABluePrintBeans famcBean) {
            super();
            this.parentBean = parentBean;
            this.famcBean = famcBean;
        }

        @Override
        public ActionListener getCreateAction() {
            if (parentBean != null && parentBean.getContext() != null) {
                return new ACreateChild((Indi) parentBean.getContext(), ancestris.modules.editors.standard.FamilyPanel.this);
            }
            if (famcBean != null && famcBean.getContext() != null) {
                return new ACreateChild((Fam) famcBean.getContext(), ancestris.modules.editors.standard.FamilyPanel.this);
            }
            return new ACreateSpouse(null, ancestris.modules.editors.standard.FamilyPanel.this);
        }
    }

    private abstract class EntitiesPanel extends AListBean {

        public EntitiesPanel(JScrollPane pane) {
            super();
            setBlueprint(Gedcom.INDI, "<body bgcolor=#ffffe3>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI.cell"));
            setBlueprint(Gedcom.FAM, "<body bgcolor=#f1f1ff>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.FAM.cell"));
            setBackground(java.awt.Color.white);
            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));
            pane.setViewportView(this);
        }

        public abstract Property[] getEntities(Property rootProperty);

        public void update(Property rootProperty, Property exclude, MouseListener listener) {
            removeAll();
            repaint();
            if (rootProperty != null) {
                add(getEntities(rootProperty), exclude, new ABeanHandler());
            }
            if (listener != null) {
                JButton createBtn = new JButton("Ajouter");
                createBtn.addMouseListener(listener);
                add(createBtn);
            }
            revalidate();
        }
    }

    private static class EditorButton extends JButton {

        public EditorButton() {
            super();
            setBorderPainted(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(0, 0, 0, 0));
        }
    }
}
