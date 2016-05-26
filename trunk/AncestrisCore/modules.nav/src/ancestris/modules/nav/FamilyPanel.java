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
import ancestris.modules.beans.ABluePrintBeans;
import ancestris.modules.beans.AListBean;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
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
import java.awt.Component;
import java.util.ArrayList;
import org.openide.awt.MouseUtils;
import org.openide.util.NbBundle;

public final class FamilyPanel extends JPanel {

    private final static String EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.empty");
    private final static String WIFE_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.wife.empty");
    private final static String HUSBAND_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.husband.empty");
    private final static String CHILD_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.child.empty");
    private final static String FATHER_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.father.empty");
    private final static String MOTHER_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.mother.empty");
    private final static String FAMS_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.fams.empty");
    private Context context;
    private Indi focusIndi;
    private Fam focusFam;
    private final EntitiesPanel childrenPanel;
    private final EntitiesPanel oFamsPanel;
    private final EntitiesPanel siblingsPanel;
    private final EntitiesPanel eventsPanel;

    /** Creates new form FamilyPanel */
    public FamilyPanel() {
        initComponents();

        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);

        husband.addMouseListener(new ABeanHandler(false));
        wife.addMouseListener(new SpouseHandler(husband));

        husbFather.addMouseListener(new ParentHandler(husband, PropertySex.MALE));
        husbMother.addMouseListener(new ParentHandler(husband, PropertySex.FEMALE));

        familySpouse.addMouseListener(new ABeanHandler(false));

        husband.setEmptyBluePrint(HUSBAND_EMPTY_BP);
        husband.setBlueprint(Gedcom.INDI, "<body bgcolor=#e9e9ff>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));  // NOI18N

        wife.setEmptyBluePrint(WIFE_EMPTY_BP);
        wife.setBlueprint(Gedcom.INDI, "<body bgcolor=#f1f1ff>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));  // NOI18N

        husbFather.setEmptyBluePrint(FATHER_EMPTY_BP);
        husbFather.setBlueprint(Gedcom.INDI, "<body bgcolor=#f1f1f1>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));  // NOI18N

        husbMother.setEmptyBluePrint(MOTHER_EMPTY_BP);
        husbMother.setBlueprint(Gedcom.INDI, "<body bgcolor=#f1f1f1>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));  // NOI18N

        familySpouse.setEmptyBluePrint(FAMS_EMPTY_BP);
        familySpouse.setBlueprint(Gedcom.FAM, "<body bgcolor=#f1f1ff>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.FAM"));  // NOI18N

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
        eventsPanel.setBlueprint("", "<i><name path=.></i>&nbsp;:&nbsp;<prop path=.:DATE img=no>&nbsp;(<prop path=.:PLAC>)");  // NOI18N
        
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

        if (focusFam == null && focusIndi != null && focusIndi.getNoOfFams() > 0) {
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
        childrenPanel.update(familySpouse.getProperty() == null ? null : (Fam) (familySpouse.getProperty().getEntity()),null);
        setTooltip(childrenPanel);

        Fam famChild = ((Indi) husband.getProperty()).getFamilyWhereBiologicalChild();
        familyParent.setContext(famChild);

        siblingsPanel.update(husband.getProperty(), null);
        setTooltip(siblingsPanel);

        oFamsPanel.update(husband.getProperty(), familySpouse == null ? null : familySpouse.getProperty());
        setTooltip(oFamsPanel);

        eventsPanel.update(husband.getProperty(), null);

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
        indiPanel = new javax.swing.JPanel();
        husband = new ancestris.modules.beans.ABluePrintBeans();
        jLabel3 = new javax.swing.JLabel();
        spousePanel = new javax.swing.JPanel();
        wife = new ancestris.modules.beans.ABluePrintBeans();
        jLabel2 = new javax.swing.JLabel();
        otherSpousePanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        familySpouse = new ancestris.modules.beans.ABluePrintBeans();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        familyParent = new ancestris.modules.beans.ABluePrintBeans();
        relativesPanel = new javax.swing.JPanel();
        eventsTab = new javax.swing.JPanel();
        jsEvents = new javax.swing.JScrollPane();

        setPreferredSize(new java.awt.Dimension(622, 500));
        setRequestFocusEnabled(false);

        fatherPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.fatherPanel.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14))); // NOI18N

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
            .addComponent(husbFather, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
        );
        fatherPanelLayout.setVerticalGroup(
            fatherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbFather, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
        );

        motherPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.motherPanel.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14))); // NOI18N

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
            .addGap(0, 94, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout motherPanelLayout = new javax.swing.GroupLayout(motherPanel);
        motherPanel.setLayout(motherPanelLayout);
        motherPanelLayout.setHorizontalGroup(
            motherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbMother, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
        );
        motherPanelLayout.setVerticalGroup(
            motherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbMother, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
        );

        indiPanel.setBorder(null);

        husband.setBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(51, 102, 255), null));
        husband.setToolTipText(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "GeneralTootlTipText")); // NOI18N

        javax.swing.GroupLayout husbandLayout = new javax.swing.GroupLayout(husband);
        husband.setLayout(husbandLayout);
        husbandLayout.setHorizontalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        husbandLayout.setVerticalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jLabel3.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jLabel3.text")); // NOI18N

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

        wife.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
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
            .addGap(0, 87, Short.MAX_VALUE)
        );

        jLabel2.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout spousePanelLayout = new javax.swing.GroupLayout(spousePanel);
        spousePanel.setLayout(spousePanelLayout);
        spousePanelLayout.setHorizontalGroup(
            spousePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(wife, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        spousePanelLayout.setVerticalGroup(
            spousePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spousePanelLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(2, 2, 2)
                .addComponent(wife, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        jScrollPane2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane2.setToolTipText("");

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jLabel1.text")); // NOI18N

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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.setBorder(null);
        jTabbedPane1.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N

        familySpouse.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        familySpouse.setToolTipText(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "ClicEntTootlTipText")); // NOI18N
        familySpouse.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout familySpouseLayout = new javax.swing.GroupLayout(familySpouse);
        familySpouse.setLayout(familySpouseLayout);
        familySpouseLayout.setHorizontalGroup(
            familySpouseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 608, Short.MAX_VALUE)
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
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
            .addComponent(familySpouse, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(familySpouse, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        familyParent.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        familyParent.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout familyParentLayout = new javax.swing.GroupLayout(familyParent);
        familyParent.setLayout(familyParentLayout);
        familyParentLayout.setHorizontalGroup(
            familyParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 608, Short.MAX_VALUE)
        );
        familyParentLayout.setVerticalGroup(
            familyParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 31, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(familyParent, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(familyParent, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel8.TabConstraints.tabTitle"), jPanel8); // NOI18N

        relativesPanel.setBackground(java.awt.Color.white);
        relativesPanel.setToolTipText(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "ClicTootlTipText")); // NOI18N

        javax.swing.GroupLayout relativesPanelLayout = new javax.swing.GroupLayout(relativesPanel);
        relativesPanel.setLayout(relativesPanelLayout);
        relativesPanelLayout.setHorizontalGroup(
            relativesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 610, Short.MAX_VALUE)
        );
        relativesPanelLayout.setVerticalGroup(
            relativesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.relativesPanel.TabConstraints.tabTitle"), relativesPanel); // NOI18N

        eventsTab.setBackground(java.awt.Color.white);

        jsEvents.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jsEvents.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        javax.swing.GroupLayout eventsTabLayout = new javax.swing.GroupLayout(eventsTab);
        eventsTab.setLayout(eventsTabLayout);
        eventsTabLayout.setHorizontalGroup(
            eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 610, Short.MAX_VALUE)
            .addGroup(eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jsEvents, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE))
        );
        eventsTabLayout.setVerticalGroup(
            eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
            .addGroup(eventsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jsEvents, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.eventsTab.TabConstraints.tabTitle"), eventsTab); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fatherPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(indiPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(motherPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(spousePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(otherSpousePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(motherPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fatherPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(spousePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(otherSpousePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(indiPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel eventsTab;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JScrollPane jsEvents;
    private javax.swing.JPanel motherPanel;
    private javax.swing.JPanel otherSpousePanel;
    private javax.swing.JPanel relativesPanel;
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

    private void setTooltip(EntitiesPanel panel) {
        for (Component c : panel.getComponents()) {
            if (c instanceof ABluePrintBeans) {
                ABluePrintBeans bean = (ABluePrintBeans) c;
                bean.setToolTipText(NbBundle.getMessage(FamilyPanel.class, "ClicTootlTipText"));
            }
        }
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
        public void mouseClickedFiltered(java.awt.event.MouseEvent evt) {
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
            if (editOnClick || MouseUtils.isDoubleClick(evt) || bean == null || bean.getProperty() == null) {
                SelectionDispatcher.muteSelection(true);
                try {
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
                    SelectionDispatcher.muteSelection(false);
                }
            // Click on someone = show it     
            } else if (evt.getClickCount() == 1) {                                                          
                // FIXME: test click count necessaire?
                Property prop = bean.getProperty();
                if (prop instanceof Entity) {
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

//    private class ChildHandler extends ABeanHandler {
//
//        private final ABluePrintBeans parentBean;
//
//        /**
//         *
//         * @param parentBean May be an amC Bean or one of the parents IndiBean
//         */
//        public ChildHandler(ABluePrintBeans parentBean) {
//            super();
//            this.parentBean = parentBean;
//        }
//
//        @Override
//        public ActionListener getCreateAction() {
//            Indi property = null;
//            if (parentBean != null) {
//                property = (Indi) parentBean.getProperty();
//            }
//            return AncestrisEditor.findEditor(property).getCreateChildAction(property);
//        }
//    }
    
    
    
    
    private abstract class EntitiesPanel extends AListBean {

        public EntitiesPanel(JScrollPane pane) {
            super();
            setBlueprint(Gedcom.INDI, "<body bgcolor=#ffffe3>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI.cell"));  // NOI18N
            setBlueprint(Gedcom.FAM, "<body bgcolor=#f1f1ff>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.FAM.cell"));  // NOI18N
            setBackground(java.awt.Color.white);
            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));
            pane.setViewportView(this);
        }

        public abstract Property[] getEntities(Property rootProperty);

        public void update(Property rootProperty, Property exclude) {
            removeAll();
            repaint();
            if (rootProperty != null) {
                add(getEntities(rootProperty), exclude, new ABeanHandler());
            }
            revalidate();
        }
    }
}
