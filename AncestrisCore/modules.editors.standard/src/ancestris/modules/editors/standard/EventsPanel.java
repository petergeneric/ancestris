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
import ancestris.util.FilteredMouseAdapter;
import ancestris.modules.beans.ABluePrintBeans;
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
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import java.awt.ComponentOrientation;
import java.util.ArrayList;
import javax.swing.border.EmptyBorder;
import org.openide.awt.MouseUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class EventsPanel extends EditorPanel {

    private final static String EMPTY_BP = org.openide.util.NbBundle.getMessage(EventsPanel.class, "blueprint.empty");
    private final static String WIFE_EMPTY_BP = org.openide.util.NbBundle.getMessage(EventsPanel.class, "blueprint.wife.empty");
    private final static String HUSBAND_EMPTY_BP = org.openide.util.NbBundle.getMessage(EventsPanel.class, "blueprint.husband.empty");
    private final static String CHILD_EMPTY_BP = org.openide.util.NbBundle.getMessage(EventsPanel.class, "blueprint.child.empty");
    private final static String FATHER_EMPTY_BP = org.openide.util.NbBundle.getMessage(EventsPanel.class, "blueprint.father.empty");
    private final static String MOTHER_EMPTY_BP = org.openide.util.NbBundle.getMessage(EventsPanel.class, "blueprint.mother.empty");
    private final static String FAMS_EMPTY_BP = org.openide.util.NbBundle.getMessage(EventsPanel.class, "blueprint.fams.empty");
    private Context context;
    private Indi focusIndi;
    private final EntitiesPanel eventsPanel;


    /** Creates new form FamilyPanel */
    public EventsPanel() {
        initComponents();

        // Add listners
        ABeanHandler handler;
        handler = new ABeanHandler();
        handler.setEditOnClick(true);
        husband.addMouseListener(handler);

        handler = new ABeanHandler();
        handler.setEditOnClick(true);

        husband.setEmptyBluePrint(HUSBAND_EMPTY_BP);
        husband.setBlueprint(Gedcom.INDI, "<body bgcolor=#e9e9ff>" + NbBundle.getMessage(EventsPanel.class, "blueprint.INDI")); // NOI18N

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
            if (((Fam) entity).getNoOfSpouses() == 0) {
                return;
            }
            Fam focusFam = ((Fam) entity);
            focusIndi = focusFam.getHusband();
            if (focusIndi == null) {
                focusIndi = focusFam.getWife();
            }
        } else if (entity instanceof Indi) {
            focusIndi = (Indi) entity;
        } else {
            return;
        }
        refresh();
    }

    private void refresh() {
        husband.setContext(focusIndi);
        eventsPanel.update(husband.getProperty(), null, null);

    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(EventsPanel.class, "events.panel.title");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jsEvents = new javax.swing.JScrollPane();
        husband = new ancestris.modules.beans.ABluePrintBeans();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(622, 500));
        setRequestFocusEnabled(false);

        jsEvents.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jsEvents.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jsEvents.getVerticalScrollBar().setUnitIncrement(16);

        javax.swing.GroupLayout husbandLayout = new javax.swing.GroupLayout(husband);
        husband.setLayout(husbandLayout);
        husbandLayout.setHorizontalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 641, Short.MAX_VALUE)
        );
        husbandLayout.setVerticalGroup(
            husbandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(81, 81, 81)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(452, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(EventsPanel.class, "EventsPanel.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 633, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 142, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(EventsPanel.class, "EventsPanel.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 633, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 142, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(EventsPanel.class, "EventsPanel.jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jsEvents, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                    .addComponent(husband, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(husband, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jsEvents, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.beans.ABluePrintBeans husband;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JScrollPane jsEvents;
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
            title = NbBundle.getMessage(EventsPanel.class, "dialog.fam.new.title", fam);
        } else {
            title = NbBundle.getMessage(EventsPanel.class, "dialog.fam.edit.title", fam);
        }
        final FamPanel bean = new FamPanel();
        bean.setContext(new Context(fam));
        NotifyDescriptor nd = new NotifyDescriptor(bean, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
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
            title = NbBundle.getMessage(EventsPanel.class, "dialog.indi.new.title", indi);
        } else {
            title = NbBundle.getMessage(EventsPanel.class, "dialog.indi.edit.title", indi);
        }
        if (indi == null) {
            return false;
        }
        final IndiPanel bean = new IndiPanel();
        bean.setContext(new Context(indi));
        NotifyDescriptor nd = new NotifyDescriptor(new JScrollPane(bean), title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
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
            if (editOnClick || MouseUtils.isDoubleClick(evt) || bean == null || bean.getProperty() == null) {
                SelectionSink.Dispatcher.muteSelection(true);
                try {
                    if (bean != null && bean.getProperty() != null) {
                        editProperty(bean.getProperty(), false);
                    } else {
                        getCreateAction().actionPerformed(new ActionEvent(evt.getSource(), 0, ""));
                    }
                    refresh();
                } finally {
                    SelectionSink.Dispatcher.muteSelection(false);
                }
            } else if (evt.getClickCount() == 1) {
                // FIXME: test click count necessaire?
                Property prop = bean.getProperty();
                if (prop instanceof Entity)
                    fireSelection((Entity)prop);
            }
        }

        public ActionListener getCreateAction() {
            return action;
        }

        ;
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
            if (childBean == null || childBean.getProperty() == null) {
                return new ACreateParent(null, PropertySex.MALE);
            }
            return new ACreateParent((Indi) childBean.getProperty(), sex);
        }
    }


    private abstract class EntitiesPanel extends AListBean {

        public EntitiesPanel(JScrollPane pane) {
            super();
            setBlueprint(Gedcom.INDI, "<body bgcolor=#ffffe3>" + NbBundle.getMessage(EventsPanel.class, "blueprint.INDI.cell"));  // NOI18N
            setBlueprint(Gedcom.FAM, "<body bgcolor=#f1f1ff>" + NbBundle.getMessage(EventsPanel.class, "blueprint.FAM.cell"));  // NOI18N
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
                JButton createBtn = new JButton(org.openide.util.NbBundle.getMessage(EventsPanel.class, "button.add.title"));
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
