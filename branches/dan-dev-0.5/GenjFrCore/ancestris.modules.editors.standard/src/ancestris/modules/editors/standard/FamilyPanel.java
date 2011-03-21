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
        wife.setBlueprint(Gedcom.INDI,
                NbBundle.getMessage(FamilyPanel.class, "blueprint.spouse.title")+
                NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));

        husbFather.setEmptyBluePrint(FATHER_EMPTY_BP);
        husbFather.setBlueprint(Gedcom.INDI,
                NbBundle.getMessage(FamilyPanel.class, "blueprint.father.title")+
                NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));

        husbMother.setEmptyBluePrint(MOTHER_EMPTY_BP);
        husbMother.setBlueprint(Gedcom.INDI,
                NbBundle.getMessage(FamilyPanel.class, "blueprint.mother.title")+
                NbBundle.getMessage(FamilyPanel.class, "blueprint.INDI"));

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

        wifeMother = new ancestris.modules.beans.ABluePrintBeans();
        aBluePrintBeans1 = new ancestris.modules.beans.ABluePrintBeans();
        husband = new ancestris.modules.beans.ABluePrintBeans();
        husbFather = new ancestris.modules.beans.ABluePrintBeans();
        wifeFather = new ancestris.modules.beans.ABluePrintBeans();
        familySpouse = new ancestris.modules.beans.ABluePrintBeans();
        wife = new ancestris.modules.beans.ABluePrintBeans();
        jScrollPane1 = new javax.swing.JScrollPane();
        childrenPanel = new javax.swing.JPanel();
        wifeFather1 = new ancestris.modules.beans.ABluePrintBeans();
        jScrollPane2 = new javax.swing.JScrollPane();
        husbMother = new ancestris.modules.beans.ABluePrintBeans();

        setPreferredSize(new java.awt.Dimension(622, 500));
        setRequestFocusEnabled(false);

        wifeMother.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout wifeMotherLayout = new javax.swing.GroupLayout(wifeMother);
        wifeMother.setLayout(wifeMotherLayout);
        wifeMotherLayout.setHorizontalGroup(
            wifeMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        wifeMotherLayout.setVerticalGroup(
            wifeMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        aBluePrintBeans1.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout aBluePrintBeans1Layout = new javax.swing.GroupLayout(aBluePrintBeans1);
        aBluePrintBeans1.setLayout(aBluePrintBeans1Layout);
        aBluePrintBeans1Layout.setHorizontalGroup(
            aBluePrintBeans1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        aBluePrintBeans1Layout.setVerticalGroup(
            aBluePrintBeans1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout husbandLayout = new javax.swing.GroupLayout(husband);
        husband.setLayout(husbandLayout);
        husbandLayout.setHorizontalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        husbandLayout.setVerticalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 143, Short.MAX_VALUE)
        );

        husbFather.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout husbFatherLayout = new javax.swing.GroupLayout(husbFather);
        husbFather.setLayout(husbFatherLayout);
        husbFatherLayout.setHorizontalGroup(
            husbFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        husbFatherLayout.setVerticalGroup(
            husbFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        wifeFather.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout wifeFatherLayout = new javax.swing.GroupLayout(wifeFather);
        wifeFather.setLayout(wifeFatherLayout);
        wifeFatherLayout.setHorizontalGroup(
            wifeFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        wifeFatherLayout.setVerticalGroup(
            wifeFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        familySpouse.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout familySpouseLayout = new javax.swing.GroupLayout(familySpouse);
        familySpouse.setLayout(familySpouseLayout);
        familySpouseLayout.setHorizontalGroup(
            familySpouseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 598, Short.MAX_VALUE)
        );
        familySpouseLayout.setVerticalGroup(
            familySpouseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 45, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout wifeLayout = new javax.swing.GroupLayout(wife);
        wife.setLayout(wifeLayout);
        wifeLayout.setHorizontalGroup(
            wifeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        wifeLayout.setVerticalGroup(
            wifeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        childrenPanel.setLayout(new javax.swing.BoxLayout(childrenPanel, javax.swing.BoxLayout.PAGE_AXIS));
        jScrollPane1.setViewportView(childrenPanel);

        wifeFather1.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout wifeFather1Layout = new javax.swing.GroupLayout(wifeFather1);
        wifeFather1.setLayout(wifeFather1Layout);
        wifeFather1Layout.setHorizontalGroup(
            wifeFather1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        wifeFather1Layout.setVerticalGroup(
            wifeFather1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        husbMother.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout husbMotherLayout = new javax.swing.GroupLayout(husbMother);
        husbMother.setLayout(husbMotherLayout);
        husbMotherLayout.setHorizontalGroup(
            husbMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        husbMotherLayout.setVerticalGroup(
            husbMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(familySpouse, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(wifeMother, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                            .addComponent(aBluePrintBeans1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                            .addComponent(husband, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(wife, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                            .addComponent(wifeFather1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                            .addComponent(wifeFather, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(husbFather, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(husbMother, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(wifeMother, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(wifeFather, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(aBluePrintBeans1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wifeFather1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(husbMother, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                    .addComponent(husbFather, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(wife, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(husband, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(familySpouse, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.beans.ABluePrintBeans aBluePrintBeans1;
    private javax.swing.JPanel childrenPanel;
    private ancestris.modules.beans.ABluePrintBeans familySpouse;
    private ancestris.modules.beans.ABluePrintBeans husbFather;
    private ancestris.modules.beans.ABluePrintBeans husbMother;
    private ancestris.modules.beans.ABluePrintBeans husband;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private ancestris.modules.beans.ABluePrintBeans wife;
    private ancestris.modules.beans.ABluePrintBeans wifeFather;
    private ancestris.modules.beans.ABluePrintBeans wifeFather1;
    private ancestris.modules.beans.ABluePrintBeans wifeMother;
    // End of variables declaration//GEN-END:variables

    private void createOrEditChild(MouseEvent evt, ABluePrintBeans destBean) {
        if (evt.getButton() != MouseEvent.BUTTON1 || evt.getID() != MouseEvent.MOUSE_CLICKED) {
            return;
        }
        if (evt.getClickCount() == 1) {
            fireSelection(destBean.getContext());
        } else {
            Indi indi = (Indi) destBean.getContext();
            muteContext(true);
            try {
                if (indi != null) {
                    editEntity(indi, false);
                } else {
                    CreateChild ccAction;
                    // tries to guess entity to attach new child to
                    // Familly knows?
                    if (familySpouse.getContext() != null) {
                        ccAction = new CreateChild((Fam) (familySpouse.getContext().getEntity()), true);
                        ccAction.actionPerformed(new ActionEvent(this, 0, ""));
                    } else {
                        Indi parent = getWifeOrHusband();
                        // must not be null
                        if (parent == null) {
                            throw new UnsupportedOperationException("no entity to attach new child to");
                        }
                        ccAction = new CreateChild(parent, true);
                        ccAction.actionPerformed(new ActionEvent(this, 0, ""));
                    }
                    indi = (Indi) ccAction.getCreated();
                    if (ccAction.isNew()) {
                        if (!editEntity(indi, true)) {
                            if (context != null) {
                                context.getGedcom().undoUnitOfWork(false);
                            }
                            return;
                        }
                    }
                    if (indi == null) {
                        return;
                    }
                    familySpouse.setContext(indi.getFamilyWhereBiologicalChild());
                }
            } finally {
                muteContext(false);
            }
            destBean.setContext(indi);
            updatechildrenPanel();
        }
    }

    private void fireSelection(Entity entity) {
        if (entity != null) {
            SelectionSink.Dispatcher.fireSelection(new Context(entity), false);
        }
    }

    static boolean editEntity(Entity entity, boolean isNew) {
        return false;
    }

    static boolean editEntity(Fam fam, boolean isNew) {
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

//    private Fam getFams(Indi indi, Indi spouse) {
//        if (indi == null) {
//            return null;
//        }
//        if (indi.getNoOfFams() == 0) {
//            return null;
//        }
//        Fam[] fams = indi.getFamiliesWhereSpouse();
//        if (spouse == null) {
//            return fams[0];
//        }
//        for (Fam fam : fams) {
//            if (fam.getOtherSpouse(indi) == spouse) {
//                return fam;
//            }
//        }
//        return null;
//    }
    private abstract class EntityHandler {

        private ABluePrintBeans bean;
        private ABluePrintBeans beanRelated;
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
                Indi indi = (Indi) bean.getContext();
                try {
                    if (indi != null) {
                        editEntity(indi, false);
                    } else {
                        create(beanRelated.getContext());
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

        public abstract Entity create(Entity from);
    }

    private class SpouseHandler extends EntityHandler {

        public SpouseHandler(ABluePrintBeans bean, ABluePrintBeans beanRelated) {
            super(bean, beanRelated);
        }

        @Override
        public Entity create(Entity from) {
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

        public Entity create(Entity from, int sex) {
            CreateParent cpAction = new CreateParent((Indi) from, sex);
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

        @Override
        public Entity create(Entity from) {
            return create(from, sex);
        }
    }

    private class FamHandler extends EntityHandler {

        public FamHandler(ABluePrintBeans bean, ABluePrintBeans beanRelated) {
            super(bean, beanRelated);
        }

        @Override
        public Entity create(Entity from) {
            return null;
        }
    }

    private class ChildBean extends ABluePrintBeans {

        public ChildBean() {
            this(null);
        }

        public ChildBean(Indi child) {
            super();
            setEmptyBluePrint(CHILD_EMPTY_BP);
            setBlueprint(Gedcom.INDI,NbBundle.getMessage(FamilyPanel.class, "blueprint.CHILD"));
            this.setContext(child);
            addMouseListener(new FilteredMouseAdapter() {

                @Override
                public void filteredMouseClicked(java.awt.event.MouseEvent evt) {
                    createOrEditChild(evt, ChildBean.this);
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new java.awt.Dimension(150, 16);
        }

        public void addToPanel(JPanel panel) {
            panel.add(this);
        }
    }
}
