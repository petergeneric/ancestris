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
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
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
    private final static String WIFE_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.wife.empty");
    private final static String HUSBAND_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.husband.empty");
    private final static String CHILD_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.child.empty");
    private final static String FATHER_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.father.empty");
    private final static String MOTHER_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.mother.empty");
    private final static String FAMS_EMPTY_BP = org.openide.util.NbBundle.getMessage(FamilyPanel.class, "blueprint.fams.empty");
    private final static String VOID_BP = "";
    private IndiBeans husbandBeans;
    private IndiBeans wifeBeans;
    private Context context;
    private boolean muteContext = false;

    /** Creates new form FamilyPanel */
    public FamilyPanel() {
        initComponents();
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);

        // Add listners
        husband.addMouseListener(new FilteredMouseAdapter() {

            @Override
            public void filteredMouseClicked(java.awt.event.MouseEvent evt) {
                createOrEditSpouse(evt, husbandBeans, wife.getContext());
            }
        });
        husbFather.addMouseListener(new FilteredMouseAdapter() {

            @Override
            public void filteredMouseClicked(java.awt.event.MouseEvent evt) {
                createOrEditParent(evt, husbandBeans, (Indi) husbFather.getContext(), PropertySex.MALE);
            }
        });
        husbMother.addMouseListener(new FilteredMouseAdapter() {

            @Override
            public void filteredMouseClicked(java.awt.event.MouseEvent evt) {
                createOrEditParent(evt, husbandBeans, (Indi) husbMother.getContext(), PropertySex.FEMALE);
            }
        });

        wife.addMouseListener(new FilteredMouseAdapter() {

            @Override
            public void filteredMouseClicked(java.awt.event.MouseEvent evt) {
                createOrEditSpouse(evt, wifeBeans, husband.getContext());
            }
        });
        wifeFather.addMouseListener(new FilteredMouseAdapter() {

            @Override
            public void filteredMouseClicked(java.awt.event.MouseEvent evt) {
                createOrEditParent(evt, wifeBeans, (Indi) wifeFather.getContext(), PropertySex.MALE);
            }
        });
        wifeMother.addMouseListener(new FilteredMouseAdapter() {

            @Override
            public void filteredMouseClicked(java.awt.event.MouseEvent evt) {
                createOrEditParent(evt, wifeBeans, (Indi) wifeMother.getContext(), PropertySex.FEMALE);
            }
        });

        familySpouse.addMouseListener(new FilteredMouseAdapter() {

            @Override
            public void filteredMouseClicked(java.awt.event.MouseEvent evt) {
                createOrEditFam(evt, familySpouse);
            }
        });

        husbandBeans = new IndiBeans(husband, husbFather, husbMother);
        wifeBeans = new IndiBeans(wife, wifeFather, wifeMother);
        husband.setEmptyBluePrint(HUSBAND_EMPTY_BP);
        wife.setEmptyBluePrint(WIFE_EMPTY_BP);
        familySpouse.setEmptyBluePrint(FAMS_EMPTY_BP);
    }

    private void muteContext(boolean b) {
        muteContext=b;
    }

    public void setContext(Context context) {
        if (muteContext)
            return;
        Entity entity = context.getEntity();
        this.context = context;
        Fam f = null;
        Indi h = null;
        Indi w = null;
        if (entity instanceof Fam) {
            f = ((Fam) entity);
            h = f.getHusband();
            w = f.getWife();
        } else if (entity instanceof Indi) {
            Indi i = (Indi) entity;
            if (i.getSex() == PropertySex.FEMALE) {
                w = i;
            } else {
                h = i;
            }
            if (i.getNoOfFams() > 0) {
                f = i.getFamiliesWhereSpouse()[0];
                h = f.getHusband();
                w = f.getWife();
            }
        }

        familySpouse.setContext(f);
        husbandBeans.setIndi(h);
        wifeBeans.setIndi(w);
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

        husband = new ancestris.modules.beans.ABluePrintBeans();
        husbFather = new ancestris.modules.beans.ABluePrintBeans();
        wifeFather = new ancestris.modules.beans.ABluePrintBeans();
        familySpouse = new ancestris.modules.beans.ABluePrintBeans();
        wifeMother = new ancestris.modules.beans.ABluePrintBeans();
        husbMother = new ancestris.modules.beans.ABluePrintBeans();
        wife = new ancestris.modules.beans.ABluePrintBeans();
        jScrollPane1 = new javax.swing.JScrollPane();
        childrenPanel = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(622, 500));
        setRequestFocusEnabled(false);

        javax.swing.GroupLayout husbandLayout = new javax.swing.GroupLayout(husband);
        husband.setLayout(husbandLayout);
        husbandLayout.setHorizontalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 299, Short.MAX_VALUE)
        );
        husbandLayout.setVerticalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 108, Short.MAX_VALUE)
        );

        husbFather.setMinimumSize(new java.awt.Dimension(256, 80));
        husbFather.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout husbFatherLayout = new javax.swing.GroupLayout(husbFather);
        husbFather.setLayout(husbFatherLayout);
        husbFatherLayout.setHorizontalGroup(
            husbFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 299, Short.MAX_VALUE)
        );
        husbFatherLayout.setVerticalGroup(
            husbFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 95, Short.MAX_VALUE)
        );

        wifeFather.setMinimumSize(new java.awt.Dimension(256, 80));
        wifeFather.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout wifeFatherLayout = new javax.swing.GroupLayout(wifeFather);
        wifeFather.setLayout(wifeFatherLayout);
        wifeFatherLayout.setHorizontalGroup(
            wifeFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 293, Short.MAX_VALUE)
        );
        wifeFatherLayout.setVerticalGroup(
            wifeFatherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 95, Short.MAX_VALUE)
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

        wifeMother.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout wifeMotherLayout = new javax.swing.GroupLayout(wifeMother);
        wifeMother.setLayout(wifeMotherLayout);
        wifeMotherLayout.setHorizontalGroup(
            wifeMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 293, Short.MAX_VALUE)
        );
        wifeMotherLayout.setVerticalGroup(
            wifeMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        husbMother.setPreferredSize(new java.awt.Dimension(256, 80));

        javax.swing.GroupLayout husbMotherLayout = new javax.swing.GroupLayout(husbMother);
        husbMother.setLayout(husbMotherLayout);
        husbMotherLayout.setHorizontalGroup(
            husbMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 299, Short.MAX_VALUE)
        );
        husbMotherLayout.setVerticalGroup(
            husbMotherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout wifeLayout = new javax.swing.GroupLayout(wife);
        wife.setLayout(wifeLayout);
        wifeLayout.setHorizontalGroup(
            wifeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 293, Short.MAX_VALUE)
        );
        wifeLayout.setVerticalGroup(
            wifeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 108, Short.MAX_VALUE)
        );

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        childrenPanel.setLayout(new java.awt.GridLayout(0, 3, 5, 5));
        jScrollPane1.setViewportView(childrenPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
                    .addComponent(familySpouse, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(husband, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(husbFather, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(wife, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(wifeFather, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(husbMother, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wifeMother, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(wifeFather, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                    .addComponent(husbFather, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(wifeMother, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(husbMother, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(wife, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(husband, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(familySpouse, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel childrenPanel;
    private ancestris.modules.beans.ABluePrintBeans familySpouse;
    private ancestris.modules.beans.ABluePrintBeans husbFather;
    private ancestris.modules.beans.ABluePrintBeans husbMother;
    private ancestris.modules.beans.ABluePrintBeans husband;
    private javax.swing.JScrollPane jScrollPane1;
    private ancestris.modules.beans.ABluePrintBeans wife;
    private ancestris.modules.beans.ABluePrintBeans wifeFather;
    private ancestris.modules.beans.ABluePrintBeans wifeMother;
    // End of variables declaration//GEN-END:variables

    private void createOrEditParent(MouseEvent evt, IndiBeans destBean, Indi parent, int sex) {
        if (evt.getButton() != MouseEvent.BUTTON1 || evt.getID() != MouseEvent.MOUSE_CLICKED) {
            return;
        }
        if (MouseUtils.isDoubleClick(evt)) {
            Indi indi = (Indi) destBean.getIndi();
            muteContext(true);
            try{
                if (indi == null) {
                    return;
                }
                if (parent != null) {
                    editEntity(parent,false);
                } else {
                    CreateParent cpAction = new CreateParent(indi, sex);
                    cpAction.actionPerformed(new ActionEvent(this, 0, ""));

                    if (cpAction.isNew()) {
                        parent = (Indi) cpAction.getCreated();
                        if (!editEntity(parent,true)) {
                            indi.getGedcom().undoUnitOfWork(false);
                        }
                    }
                }
            } finally{
                muteContext(false);
            }
            destBean.setIndi(indi);
        } else if (evt.getClickCount() == 1) {
            fireSelection(parent);
        }
    }

    private void createOrEditSpouse(MouseEvent evt, IndiBeans destBean, Entity spouse) {
        if (evt.getButton() != MouseEvent.BUTTON1 || evt.getID() != MouseEvent.MOUSE_CLICKED) {
            return;
        }
        if (evt.getClickCount() == 1) {
            fireSelection(destBean.getIndi());
        } else {
            muteContext(true);
            Indi indi = (Indi) destBean.getIndi();
            if (indi != null) {
                editEntity(indi,false);
            } else {
                CreateSpouse csAction = new CreateSpouse((Indi) spouse);
                csAction.actionPerformed(new ActionEvent(this, 0, ""));
                indi = (Indi) csAction.getCreated();
                if (csAction.isNew()) {
                    if (!editEntity(indi,true)) {
                        spouse.getGedcom().undoUnitOfWork(false);
                    }
                }
            }
            destBean.setIndi(indi);
            familySpouse.setContext(getFams(indi, (Indi) spouse));
            muteContext(false);
        }
    }

    private void createOrEditChild(MouseEvent evt, ABluePrintBeans destBean) {
        if (evt.getButton() != MouseEvent.BUTTON1 || evt.getID() != MouseEvent.MOUSE_CLICKED) {
            return;
        }
        if (evt.getClickCount() == 1) {
            fireSelection(destBean.getContext());
        } else {
            Indi indi = (Indi) destBean.getContext();
            muteContext(true);
            try{
                if (indi != null) {
                    editEntity(indi,false);
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
                        if (!editEntity(indi,true)) {
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
            } finally{
                muteContext(false);
            }
            destBean.setContext(indi);
            updatechildrenPanel();
        }
    }

    private void createOrEditFam(MouseEvent evt, ABluePrintBeans destBean) {
        if (evt.getClickCount() != 2) {
            return;
        }
        Fam fam = (Fam) destBean.getContext();
        if (fam == null) {
            return;
        }

        muteContext(true);
        editEntity(fam,false);
        destBean.setContext(fam);
        updatechildrenPanel();
        muteContext(false);
    }

    private void fireSelection(Entity entity) {
        if (entity != null) {
            SelectionSink.Dispatcher.fireSelection(new Context(entity), false);
        }
    }

    private boolean editEntity(Entity entity, boolean isNew) {
        return false;
    }

    private boolean editEntity(Fam fam,boolean isNew) {
        String title;
        if (isNew)
            title = NbBundle.getMessage(FamilyPanel.class, "dialog.fam.new.title", fam);
        else
            title = NbBundle.getMessage(FamilyPanel.class, "dialog.fam.edit.title", fam);
        AFamBean bean = new AFamBean();
        NotifyDescriptor nd = new NotifyDescriptor(bean.setRoot(fam), title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        DialogDisplayer.getDefault().notify(nd);
        if (!nd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
            return false;
        }
        try {
            bean.commit();
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        return true;
    }

    private boolean editEntity(Indi indi,boolean isNew) {
        String title;
        if (isNew)
            title = NbBundle.getMessage(FamilyPanel.class, "dialog.indi.new.title", indi);
        else
            title = NbBundle.getMessage(FamilyPanel.class, "dialog.indi.edit.title", indi);
        if (indi == null) {
            return false;
        }
        AIndiBean bean = new AIndiBean();
        NotifyDescriptor nd = new NotifyDescriptor(new JScrollPane(bean.setRoot(indi)), title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        DialogDisplayer.getDefault().notify(nd);
        if (!nd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
            return false;
        }
        try {
            bean.commit();
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

    private static class IndiBeans {

        ABluePrintBeans indiBean;
        ABluePrintBeans fatherBean;
        ABluePrintBeans motherBean;
        Indi indi;

        public IndiBeans(ABluePrintBeans i, ABluePrintBeans f, ABluePrintBeans m) {
            indiBean = i;
            fatherBean = f;
            motherBean = m;
        }

        public ABluePrintBeans getFatherBean() {
            return fatherBean;
        }

        public ABluePrintBeans getIndiBean() {
            return indiBean;
        }

        public ABluePrintBeans getMotherBean() {
            return motherBean;
        }

        public Indi getIndi() {
            return indi;
        }

        public void setIndi(Indi indi) {
            this.indi = indi;
            if (indiBean == null) {
                return;
            }
            indiBean.setEmptyBluePrint(EMPTY_BP);
            if (indi == null) {
                fatherBean.setEmptyBluePrint(VOID_BP);
                motherBean.setEmptyBluePrint(VOID_BP);
            } else {
                fatherBean.setEmptyBluePrint(FATHER_EMPTY_BP);
                motherBean.setEmptyBluePrint(MOTHER_EMPTY_BP);
            }
            indiBean.setContext(indi);

            if (indi == null) {
                fatherBean.setContext(null);
                motherBean.setContext(null);
            } else {
                fatherBean.setContext(indi.getBiologicalFather());
                motherBean.setContext(indi.getBiologicalMother());
            }

        }
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

    private Fam getFams(Indi indi, Indi spouse) {
        if (indi == null) {
            return null;
        }
        if (indi.getNoOfFams() == 0) {
            return null;
        }
        Fam[] fams = indi.getFamiliesWhereSpouse();
        if (spouse == null) {
            return fams[0];
        }
        for (Fam fam : fams) {
            if (fam.getOtherSpouse(indi) == spouse) {
                return fam;
            }
        }
        return null;
    }

    private class ChildBean extends ABluePrintBeans {

        public ChildBean() {
            this(null);
        }

        public ChildBean(Indi child) {
            super();
            setEmptyBluePrint(CHILD_EMPTY_BP);
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
            return new java.awt.Dimension(150, 35);
        }

        public void addToPanel(JPanel panel) {
            panel.add(this);
        }
    }
}
