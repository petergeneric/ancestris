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

import ancestris.util.FilteredMouseAdapter;
import ancestris.modules.beans.ABluePrintBeans;
import ancestris.modules.beans.AFamBean;
import ancestris.modules.beans.AIndiBean;
import genj.edit.actions.CreateChild;
import genj.edit.actions.CreateParent;
import genj.edit.actions.CreateSpouse;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import genj.gedcom.UnitOfWork;
import genj.view.SelectionSink;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class FamilyPanel extends JPanel implements IEditorPanel {

    private final static String EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.empty");
    private final static String WIFE_EMPTY_BP= org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.wife.empty");
    private final static String HUSBAND_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.husband.empty");
    private final static String CHILD_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.child.empty");
    private final static String FATHER_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.father.empty");
    private final static String MOTHER_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.mother.empty");
    private final static String FAMS_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.fams.empty");
    private final static String VOID_BP = "";
//    private IndiBeans husbandBeans;
//    private IndiBeans wifeBeans;
    private Context context;
    private Indi focusIndi;
    private Fam focusFam;
    private boolean muteContext = false;
    private EntityHandler indiHandler;
    private EntityHandler spouseHandler;
    private EntityHandler fatherHandler;
    private EntityHandler motherHandler;
    private EntityHandler famHandler;

    /** Creates new form FamilyPanel */
    public FamilyPanel() {
        initComponents();
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);

        // Add listners
        indiHandler = new SpouseHandler(husband, null);
        indiHandler.setEditOnClick(true);
        spouseHandler = new SpouseHandler(wife, husband);
        fatherHandler = new ParentHandler(husbFather, husband, PropertySex.MALE);
        motherHandler = new ParentHandler(husbMother, husband, PropertySex.FEMALE);
        famHandler = new FamHandler(familySpouse, husband);
        famHandler.setEditOnClick(true);

        husband.setEmptyBluePrint(HUSBAND_EMPTY_BP);
        husband.setBlueprint(Gedcom.INDI,NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));

        wife.setEmptyBluePrint(WIFE_EMPTY_BP);
        wife.setBlueprint(Gedcom.INDI,NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));

        husbFather.setEmptyBluePrint(FATHER_EMPTY_BP);
        husbFather.setBlueprint(Gedcom.INDI,NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));

        husbMother.setEmptyBluePrint(MOTHER_EMPTY_BP);
        husbMother.setBlueprint(Gedcom.INDI,NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));

        familySpouse.setEmptyBluePrint(FAMS_EMPTY_BP);
        familySpouse.setBlueprint(Gedcom.FAM,NbBundle.getMessage(FamilyPanel.class, "blueprint.FAM"));
    }

    private void muteContext(boolean b) {
        muteContext = b;
    }

    public void setContext(Context context) {
        if (muteContext) {
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
        updatechildrenPanel();
    }

    @Override
    public String getName() {
        return "Completer le noyau familial";
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        familySpouse = new ancestris.modules.beans.ABluePrintBeans();
        jScrollPane1 = new javax.swing.JScrollPane();
        childrenPanel = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        husbFather = new ancestris.modules.beans.ABluePrintBeans();
        jPanel4 = new javax.swing.JPanel();
        husbMother = new ancestris.modules.beans.ABluePrintBeans();
        jPanel5 = new javax.swing.JPanel();
        husband = new ancestris.modules.beans.ABluePrintBeans();
        jPanel6 = new javax.swing.JPanel();
        wife = new ancestris.modules.beans.ABluePrintBeans();

        setPreferredSize(new java.awt.Dimension(622, 500));
        setRequestFocusEnabled(false);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jScrollPane2.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Dialog", 3, 16))); // NOI18N

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTabbedPane1.setFont(new java.awt.Font("Dialog", 3, 16));

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
            .addGap(0, 28, Short.MAX_VALUE)
        );

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        childrenPanel.setLayout(new javax.swing.BoxLayout(childrenPanel, javax.swing.BoxLayout.PAGE_AXIS));
        jScrollPane1.setViewportView(childrenPanel);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(familySpouse, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(familySpouse, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 592, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 157, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel7.TabConstraints.tabTitle"), jPanel7); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 592, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 157, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel3.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 3, 16))); // NOI18N

        husbFather.setMinimumSize(new java.awt.Dimension(0, 80));
        husbFather.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout husbFatherLayout = new javax.swing.GroupLayout(husbFather);
        husbFather.setLayout(husbFatherLayout);
        husbFatherLayout.setHorizontalGroup(
            husbFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 286, Short.MAX_VALUE)
        );
        husbFatherLayout.setVerticalGroup(
            husbFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbFather, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbFather, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel4.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 3, 16))); // NOI18N

        husbMother.setMinimumSize(new java.awt.Dimension(0, 80));
        husbMother.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout husbMotherLayout = new javax.swing.GroupLayout(husbMother);
        husbMother.setLayout(husbMotherLayout);
        husbMotherLayout.setHorizontalGroup(
            husbMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 282, Short.MAX_VALUE)
        );
        husbMotherLayout.setVerticalGroup(
            husbMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbMother, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husbMother, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel5.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 3, 16))); // NOI18N

        javax.swing.GroupLayout husbandLayout = new javax.swing.GroupLayout(husband);
        husband.setLayout(husbandLayout);
        husbandLayout.setHorizontalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 284, Short.MAX_VALUE)
        );
        husbandLayout.setVerticalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 249, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husband, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(husband, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel6.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 3, 16))); // NOI18N

        javax.swing.GroupLayout wifeLayout = new javax.swing.GroupLayout(wife);
        wife.setLayout(wifeLayout);
        wifeLayout.setHorizontalGroup(
            wifeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 284, Short.MAX_VALUE)
        );
        wifeLayout.setVerticalGroup(
            wifeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(wife, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(wife, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(61, 61, 61))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel childrenPanel;
    private ancestris.modules.beans.ABluePrintBeans familySpouse;
    private ancestris.modules.beans.ABluePrintBeans husbFather;
    private ancestris.modules.beans.ABluePrintBeans husbMother;
    private ancestris.modules.beans.ABluePrintBeans husband;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private ancestris.modules.beans.ABluePrintBeans wife;
    // End of variables declaration//GEN-END:variables


    private void fireSelection(Entity entity) {
        if (entity != null) {
            SelectionSink.Dispatcher.fireSelection(new Context(entity), false);
        }
    }

    boolean editEntity(Entity entity, boolean isNew) {
        // FIXME: Horror!
        if (entity instanceof Indi)
            return editEntity((Indi)entity,isNew);
        if (entity instanceof Fam)
            return editEntity((Fam)entity,isNew);
        return false;
    }

    boolean editEntity(Fam fam, boolean isNew) {
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

    boolean editEntity(Indi indi, boolean isNew) {
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

    private void updatechildrenPanel() {
        childrenPanel.removeAll();
        childrenPanel.repaint();
        Fam f = familySpouse.getContext() == null ? null : (Fam) (familySpouse.getContext().getEntity());
        if (f != null) {
            for (Indi child : f.getChildren()) {
                childrenPanel.add(new ChildBean(child));
            }
        }
        childrenPanel.add(new ChildBean());
        childrenPanel.revalidate();
    }

    @Override
    public void commit() {
    }

    /**
     * return wife if wifeBean's indi is not null, husband otherwise
     */
    private Indi getWifeOrHusband() {
        // return wife
        Indi parent = (Indi) wife.getContext();
        // or husband
        if (parent == null) {
            parent = (Indi) husband.getContext();
        }
        return parent;
    }

    private abstract class EntityHandler {

        ABluePrintBeans bean;
        ABluePrintBeans beanRelated;
        private boolean editOnClick=false;

        public EntityHandler(ABluePrintBeans bean, ABluePrintBeans beanRelated) {
            this.bean = bean;
            this.beanRelated = beanRelated;
            bean.addMouseListener(new FilteredMouseAdapter() {

                @Override
                public void filteredMouseClicked(java.awt.event.MouseEvent evt) {
                    createOrEdit(evt);
                }
            });
        }

        /**
         *
         * @param edit true if single click mouse must launch editor
         */

        void setEditOnClick(boolean edit){
            editOnClick = edit;
        }

        private void createOrEdit(MouseEvent evt) {
            if (evt.getButton() != MouseEvent.BUTTON1 || evt.getID() != MouseEvent.MOUSE_CLICKED) {
                return;
            }
            if (editOnClick || MouseUtils.isDoubleClick(evt) || bean.getContext() == null) {
                muteContext(true);
                Entity entity = bean.getContext();
                try {
                    if (entity != null) {
                        editEntity(entity, false);
                    } else {
                        create();
                    }
                    refresh();
                } finally {
                    muteContext(false);
                }
            } else if (evt.getClickCount() == 1) {
                // FIXME: test click count necessaire?
                fireSelection(bean.getContext());
            }
        }

        ABluePrintBeans getBean() {
            return bean;
        }

        public abstract Entity create();
    }

    private class SpouseHandler extends EntityHandler {

        public SpouseHandler(ABluePrintBeans bean, ABluePrintBeans beanRelated) {
            super(bean, beanRelated);
        }

        @Override
        public Entity create() {
            Indi from = (Indi) beanRelated.getContext();
            CreateSpouse csAction = new CreateSpouse((Indi) from);
            csAction.actionPerformed(new ActionEvent(getBean(), 0, ""));
            Indi indi = (Indi) csAction.getCreated();
            if (csAction.isNew()) {
                if (!editEntity(indi, true)) {
                    from.getGedcom().undoUnitOfWork(false);
                    indi = null;
                }
            }
            return indi;
        }
    }

    private class ParentHandler extends EntityHandler {

        int sex;

        public ParentHandler(ABluePrintBeans bean, ABluePrintBeans related, int sex) {
            super(bean, related);
            this.sex = sex;
        }

        public Entity create() {
            Indi from = (Indi) beanRelated.getContext();
            CreateParent cpAction = new CreateParent(from, sex);
            cpAction.actionPerformed(new ActionEvent(getBean(), 0, ""));
            Indi parent = (Indi) cpAction.getCreated();

            if (cpAction.isNew()) {
                if (!editEntity(parent, true)) {
                    from.getGedcom().undoUnitOfWork(false);
                    return null;
                }
            }
            return parent;
        }
    }

    private class ChildHandler extends EntityHandler {

        ABluePrintBeans famc;

        public ChildHandler(ABluePrintBeans bean, ABluePrintBeans parent, ABluePrintBeans famc) {
            super(bean, parent);
            this.famc = famc;
        }

        public Entity create() {
            CreateChild ccAction;
                    // tries to guess entity to attach new child to
                    // Familly knows?
                    if (famc.getContext() != null) {
                        ccAction = new CreateChild((Fam) (famc.getContext().getEntity()), true);
                        ccAction.actionPerformed(new ActionEvent(getBean(), 0, ""));
                    } else {
                        Indi parent = (Indi)beanRelated.getContext();
                        // must not be null
                        if (parent == null) {
                            throw new UnsupportedOperationException("no entity to attach new child to");
                        }
                        ccAction = new CreateChild(parent, true);
                        ccAction.actionPerformed(new ActionEvent(getBean(), 0, ""));
                    }
                    Indi indi = (Indi) ccAction.getCreated();
                    if (ccAction.isNew()) {
                        if (!editEntity(indi, true)) {
                            if (context != null) {
                                context.getGedcom().undoUnitOfWork(false);
                            }
                            return null;
                        }
                    }
                    return indi;
    }
    }

    private class FamHandler extends EntityHandler {

        public FamHandler(ABluePrintBeans bean, ABluePrintBeans beanRelated) {
            super(bean, beanRelated);
        }

        @Override
        public Entity create() {
            return null;
        }
    }

    private class ChildBean extends ABluePrintBeans {
        private EntityHandler handler;

        public ChildBean() {
            this(null);
        }

        public ChildBean(Indi child) {
            super();
            setEmptyBluePrint(CHILD_EMPTY_BP);
            setBlueprint(Gedcom.INDI,NbBundle.getMessage(FamilyPanel.class, "blueprint.CHILD"));
            this.setContext(child);
            handler = new ChildHandler(this,husband, familySpouse);
        }

        @Override
        public Dimension getMinimumSize() {
            return new java.awt.Dimension(10, 32);
        }

        @Override
        public Dimension getPreferredSize() {
            return new java.awt.Dimension(150, 32);
        }
        public void addToPanel(JPanel panel) {
            panel.add(this);
        }
    }
}
