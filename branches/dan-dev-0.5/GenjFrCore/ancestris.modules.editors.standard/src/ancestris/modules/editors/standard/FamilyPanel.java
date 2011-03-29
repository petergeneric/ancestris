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
import ancestris.modules.beans.AListBean;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
    private boolean muteContext = false;
    private final EntitiesPanel childrenPanel;
    private final EntitiesPanel oFamsPanel;
    private final EntitiesPanel siblingsPanel;

    /** Creates new form FamilyPanel */
    public FamilyPanel() {
        initComponents();
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
            public Entity[] getEntities(Entity entity) {
                if (entity != null && entity instanceof Fam){
                    return ((Fam)entity).getChildren();
                }
                return null;
            }
        };

        // other families
        oFamsPanel = new EntitiesPanel(jScrollPane2) {

            @Override
            public Entity[] getEntities(Entity entity) {
                if (entity != null && entity instanceof Indi){
                    return ((Indi)entity).getFamiliesWhereSpouse();
                }
                return null;
            }
        };

        // Siblings
        siblingsPanel = new EntitiesPanel(jScrollPane3) {

            @Override
            public Entity[] getEntities(Entity entity) {
                if (entity != null && entity instanceof Indi){
                    return ((Indi)entity).getSiblings(false);
                }
                return null;
            }
        };

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
        childrenPanel.update(
                familySpouse.getContext() == null ? null : (Fam) (familySpouse.getContext().getEntity()),
                null,
                new ChildHandler(husband, familySpouse));

        Fam famChild = ((Indi)husband.getContext()).getFamilyWhereBiologicalChild();
        siblingsPanel.update(husband.getContext(),null, new ABeanHandler(new ACreateChild(famChild)));

        oFamsPanel.update(husband.getContext(),familySpouse == null ? null : familySpouse.getContext(), new SpouseHandler(husband));
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
        jScrollPane3 = new javax.swing.JScrollPane();
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

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jScrollPane2.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 3, 14))); // NOI18N

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
            .addGap(0, 28, Short.MAX_VALUE)
        );

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jScrollPane3.TabConstraints.tabTitle"), jScrollPane3); // NOI18N

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

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel3.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 3, 14))); // NOI18N

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

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel4.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 3, 14))); // NOI18N

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
            .addGap(0, 126, Short.MAX_VALUE)
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

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), org.openide.util.NbBundle.getMessage(FamilyPanel.class, "FamilyPanel.jPanel6.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 3, 14))); // NOI18N

        javax.swing.GroupLayout wifeLayout = new javax.swing.GroupLayout(wife);
        wife.setLayout(wifeLayout);
        wifeLayout.setHorizontalGroup(
            wifeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 284, Short.MAX_VALUE)
        );
        wifeLayout.setVerticalGroup(
            wifeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
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
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
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
        if (entity instanceof Indi) {
            return editEntity((Indi) entity, isNew);
        }
        if (entity instanceof Fam) {
            return editEntity((Fam) entity, isNew);
        }
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

    static boolean editEntity(Indi indi, boolean isNew) {
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
                muteContext(true);
                try {
                    if (bean != null && bean.getContext() != null) {
                        editEntity(bean.getContext(), false);
                    } else {
                        getCreateAction().actionPerformed(new ActionEvent(evt.getSource(), 0, ""));
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
                return new ACreateSpouse(null);
            }
            return new ACreateSpouse((Indi) otherBean.getContext());
        }
    }

    /*
     * Special create actions for ancestris editor
     */
    private static class ACreateSpouse extends AbstractAction {

        private Indi other;

        ACreateSpouse(Indi indi) {
            super();
            other = indi;
        }

        public void actionPerformed(ActionEvent e) {
            if (other == null) {
                return;
            }
            CreateSpouse csAction = new CreateSpouse(other);
            csAction.actionPerformed(e);
            Indi indi = (Indi) csAction.getCreated();
            if (csAction.isNew()) {
                if (!editEntity(indi, true)) {
                    other.getGedcom().undoUnitOfWork(false);
                }
            }
        }
    }

    private static class ACreateParent extends AbstractAction {

        private Indi child;
        private int sex;

        ACreateParent(Indi child, int sex) {
            super();
            this.child = child;
            this.sex = sex;
        }

        public void actionPerformed(ActionEvent e) {
            if (child == null) {
                return;
            }
            CreateParent cpAction = new CreateParent(child, sex);
            cpAction.actionPerformed(e);
            Indi parent = (Indi) cpAction.getCreated();

            if (cpAction.isNew()) {
                if (!editEntity(parent, true)) {
                    child.getGedcom().undoUnitOfWork(false);
                }
            }
        }
    }

    private static class ACreateChild extends AbstractAction {

        private Indi parent;
        private Fam famc;
        private int sex;

        ACreateChild(Indi parent) {
            super();
            this.parent = parent;
            this.famc = null;
        }

        ACreateChild(Fam famc) {
            super();
            this.parent = null;
            this.famc = famc;
        }

        public void actionPerformed(ActionEvent e) {
            if (parent == null && famc == null) {
                return;
            }
            Gedcom gedcom;
            CreateChild ccAction;
            // tries to guess entity to attach new child to
            // Familly knows?
            if (famc != null) {
                gedcom = famc.getGedcom();
                ccAction = new CreateChild(famc, true);
                ccAction.actionPerformed(e);
            } else if (parent != null) {
                gedcom = parent.getGedcom();
                ccAction = new CreateChild(parent, true);
                ccAction.actionPerformed(e);
            } else {
                return;
            }
            Indi indi = (Indi) ccAction.getCreated();
            if (ccAction.isNew()) {
                if (!editEntity(indi, true)) {
                    if (gedcom != null) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            }
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
                return new ACreateChild((Indi) parentBean.getContext());
            }
            if (famcBean != null && famcBean.getContext() != null) {
                return new ACreateChild((Fam) famcBean.getContext());
            }
            return new ACreateSpouse(null);
        }
    }

    private abstract class EntitiesPanel extends AListBean{

        public EntitiesPanel(JScrollPane pane) {
            super();
            setBlueprint(Gedcom.INDI, "<body bgcolor=#ffffe3>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI.cell"));
            setBlueprint(Gedcom.FAM, "<body bgcolor=#f1f1ff>" + NbBundle.getMessage(FamilyPanel.class, "blueprint.FAM.cell"));
            setBackground(java.awt.Color.white);
            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));
            pane.setViewportView(this);
        }

        public abstract Entity[] getEntities(Entity entity);

        public void update(Entity entity,Entity exclude,MouseListener listener){
            removeAll();
            repaint();
            if (entity != null) {
                add(getEntities(entity),exclude, new ABeanHandler());
            }
            if (listener != null) {
                JButton createBtn = new JButton("Ajouter");
                createBtn.addMouseListener(listener);
                add(createBtn);
            }
            revalidate();
        }
    }
}
