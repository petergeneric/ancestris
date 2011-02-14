package org.ancestris.FamilyGroups;

import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genjfr.app.App;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.ancestris.FamilyGroups//FamilyGroups//EN",
autostore = false)
public final class FamilyGroupsTopComponent extends TopComponent {

    private static FamilyGroupsTopComponent instance;
    private Context context;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/ancestris/FamilyGroups/FamilyGroups.png";
    private static final String PREFERRED_ID = "FamilyGroupsTopComponent";
    private int minGroupSize = 2;  // Don't print groups with size less than this
    private int maxGroupSize = 20;
    /** alignment options */
    protected final static int ALIGN_LEFT = 0;
    protected final static int ALIGN_CENTER = 1;
    protected final static int ALIGN_RIGHT = 2;

    private void println(String string) {
        jTextArea1.append(string + "\n");
    }

    /**
     * Aligns a simple text for text outputs.
     * @param txt the text to align
     * @param length the length of the result
     * @param alignment one of LEFT,CENTER,RIGHT
     */
    public static String align(String txt, int length, int alignment) {

        // check txt length
        int n = txt.length();
        if (n > length) {
            return txt.substring(0, length);
        }
        n = length - n;

        // prepare result
        StringBuffer buffer = new StringBuffer(length);

        int before, after;
        switch (alignment) {
            default:
            case ALIGN_LEFT:
                before = 0;
                break;
            case ALIGN_CENTER:
                before = (int) (n * 0.5F);
                break;
            case ALIGN_RIGHT:
                before = n;
                break;
        }
        after = n - before;

        // space before
        for (int i = 0; i < before; i++) {
            buffer.append(' ');
        }

        // txt
        buffer.append(txt);

        // space after
        for (int i = 0; i < after; i++) {
            buffer.append(' ');
        }

        // done
        return buffer.toString();
    }

    /**
     * @return the minGroupSize
     */
    public int getMinGroupSize() {
        return minGroupSize;
    }

    /**
     * @param minGroupSize the minGroupSize to set
     */
    public void setMinGroupSize(int minGroupSize) {
        this.minGroupSize = minGroupSize;
    }

    /**
     * @return the maxGroupSize
     */
    public int getMaxGroupSize() {
        return maxGroupSize;
    }

    /**
     * @param maxGroupSize the maxGroupSize to set
     */
    public void setMaxGroupSize(int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }

    /**
     * A sub-tree of people related to each other
     */
    private class Tree extends HashSet implements Comparable {

        private Indi oldestIndividual;

        @Override
        public int compareTo(Object that) {
            return ((Tree) that).size() - ((Tree) this).size();
        }

        @Override
        public String toString() {
            return oldestIndividual.getId()
                    + " " + oldestIndividual.getName()
                    + "(" + oldestIndividual.getBirthAsString() + "-"
                    + oldestIndividual.getDeathAsString() + ")";
        }

        @Override
        public boolean add(Object o) {
            // Individuals expected
            Indi indi = (Indi) o;
            // check if oldest
            if (isOldest(indi)) {
                oldestIndividual = indi;
            }
            // continue
            return super.add(o);
        }

        private boolean isOldest(Indi indi) {
            long jd;
            try {
                jd = oldestIndividual.getBirthDate().getStart().getJulianDay();
            } catch (Throwable t) {
                return true;
            }
            try {
                return indi.getBirthDate().getStart().getJulianDay() < jd;
            } catch (Throwable t) {
                return false;
            }

        }
    } //Tree

    public FamilyGroupsTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(FamilyGroupsTopComponent.class, "CTL_FamilyGroupsTopComponent"));
        setToolTipText(NbBundle.getMessage(FamilyGroupsTopComponent.class, "HINT_FamilyGroupsTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        jFormattedTextField1.setValue(minGroupSize);
        jFormattedTextField2.setValue(maxGroupSize);
    }

    public void start(Entity[] indis, HashSet allIndis) {
        HashSet unvisited = new HashSet(Arrays.asList(indis));
        List trees = new ArrayList();

        jTextArea1.setText("");
//        println(String.format(NbBundle.getMessage(FamilyGroupsTopComponent.class, "FamilyGroupsTopComponent.fileheader"), myGedcom.getName()));
        while (!unvisited.isEmpty()) {
            Indi indi = (Indi) unvisited.iterator().next();

            // start a new sub-tree
            Tree tree = new Tree();

            // indi has been visited now
            unvisited.remove(indi);

            // collect all relatives
            iterate(indi, tree, allIndis);

            // remember
            trees.add(tree);
        }

        // Report about groups
        if (!trees.isEmpty()) {

            // Sort in descending order by count
            Collections.sort(trees);

            // Print sorted list of groups
            println(align(NbBundle.getMessage(FamilyGroupsTopComponent.class, "FamilyGroupsTopComponent.count"), 7, ALIGN_RIGHT) + "  " + NbBundle.getMessage(FamilyGroupsTopComponent.class, "FamilyGroupsTopComponent.indi_name"));
            println("-------  ----------------------------------------------");

            int grandtotal = 0;
            int loners = 0;
            for (int i = 0; i < trees.size(); i++) {

                Tree tree = (Tree) trees.get(i);

                // sort group entities by birth date
                grandtotal += tree.size();
                if (tree.size() < getMinGroupSize()) {
                    loners += tree.size();
                } else if (tree.size() < getMaxGroupSize()) {
                    if (i != 0) {
                        println("");
                    }
                    String prefix = "" + tree.size();
                    Iterator it = tree.iterator();
                    while (it.hasNext()) {
                        Indi indi = (Indi) it.next();
                        println(align(prefix, 7, ALIGN_RIGHT) + "  " + indi.getId()
                                + " " + indi.getName()
                                + " " + "(" + indi.getBirthAsString() + " - "
                                + indi.getDeathAsString() + ")");
                        prefix = "";
                    }
                } else {
                    println(align("" + tree.size(), 7, ALIGN_RIGHT) + "  " + tree);
                }
            }

            println("");
            println(String.format(NbBundle.getMessage(FamilyGroupsTopComponent.class, "FamilyGroupsTopComponent.grandtotal"), grandtotal));

            if (loners > 0) {
                println("\n" + String.format(NbBundle.getMessage(FamilyGroupsTopComponent.class, "FamilyGroupsTopComponent.loners"), loners, getMinGroupSize()));
            }

        }


        // Done
        return;
    }

    /**
     * Iterate over an individual who's part of a sub-tree
     */
    private void iterate(Indi indi, Tree tree, Set unvisited) {

        // individuals we need to check
        Stack todos = new Stack();
        if (unvisited.remove(indi)) {
            todos.add(indi);
        }

        // loop
        while (!todos.isEmpty()) {

            Indi todo = (Indi) todos.pop();

            // belongs to group
            tree.add(todo);

            // check the ancestors
            Fam famc = todo.getFamilyWhereBiologicalChild();
            if (famc != null) {
                Indi mother = famc.getWife();
                if (mother != null && unvisited.remove(mother)) {
                    todos.push(mother);
                }

                Indi father = famc.getHusband();
                if (father != null && unvisited.remove(father)) {
                    todos.push(father);
                }
            }

            // check descendants
            Fam[] fams = todo.getFamiliesWhereSpouse();
            for (int f = 0; f < fams.length; f++) {

                // Get the family & process the spouse
                Fam fam = fams[f];
                Indi spouse = fam.getOtherSpouse(todo);
                if (spouse != null && unvisited.remove(spouse)) {
                    todos.push(spouse);
                }

                // .. and all the kids
                Indi[] children = fam.getChildren();
                for (int c = 0; c < children.length; c++) {
                    if (unvisited.remove(children[c])) {
                        todos.push(children[c]);
                    }
                }

                // next family
            }

            // continue with to-dos
        }

        // done
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new JPanel();
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jFormattedTextField1 = new JFormattedTextField();
        jFormattedTextField2 = new JFormattedTextField();
        jScrollPane1 = new JScrollPane();
        jTextArea1 = new JTextArea();

        FormListener formListener = new FormListener();

        setName("Form"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(FamilyGroupsTopComponent.class, "FamilyGroupsTopComponent.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(FamilyGroupsTopComponent.class, "FamilyGroupsTopComponent.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jFormattedTextField1.setColumns(3);
        jFormattedTextField1.setToolTipText(NbBundle.getMessage(FamilyGroupsTopComponent.class, "FamilyGroupsTopComponent.minGroupSize")); // NOI18N
        jFormattedTextField1.setName("jFormattedTextField1"); // NOI18N
        jFormattedTextField1.addActionListener(formListener);

        jFormattedTextField2.setColumns(3);
        jFormattedTextField2.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));
        jFormattedTextField2.setToolTipText(NbBundle.getMessage(FamilyGroupsTopComponent.class, "FamilyGroupsTopComponent.maxGroupSize")); // NOI18N
        jFormattedTextField2.setName("jFormattedTextField2"); // NOI18N
        jFormattedTextField2.addActionListener(formListener);

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING, false)
                    .addComponent(jFormattedTextField2, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(103, 103, 103))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jFormattedTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jFormattedTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(jPanel1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                .addContainerGap())
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements ActionListener {
        FormListener() {}
        public void actionPerformed(ActionEvent evt) {
            if (evt.getSource() == jFormattedTextField1) {
                FamilyGroupsTopComponent.this.jFormattedTextField1ActionPerformed(evt);
            }
            else if (evt.getSource() == jFormattedTextField2) {
                FamilyGroupsTopComponent.this.jFormattedTextField2ActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void jFormattedTextField1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField1ActionPerformed
        setMinGroupSize((Integer) jFormattedTextField1.getValue());
        if (context != null) {
            Gedcom myGedcom = context.getGedcom();
            Entity[] indis = myGedcom.getEntities(Gedcom.INDI, "INDI:NAME");
            HashSet unvisited = new HashSet(Arrays.asList(indis));
            start(indis, unvisited);
        }
    }//GEN-LAST:event_jFormattedTextField1ActionPerformed

    private void jFormattedTextField2ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jFormattedTextField2ActionPerformed
        setMaxGroupSize((Integer) jFormattedTextField1.getValue());
        if (context != null) {
            Gedcom myGedcom = context.getGedcom();
            Entity[] indis = myGedcom.getEntities(Gedcom.INDI, "INDI:NAME");
            HashSet unvisited = new HashSet(Arrays.asList(indis));
            start(indis, unvisited);
        }
    }//GEN-LAST:event_jFormattedTextField2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JFormattedTextField jFormattedTextField1;
    private JFormattedTextField jFormattedTextField2;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JPanel jPanel1;
    private JScrollPane jScrollPane1;
    private JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized FamilyGroupsTopComponent getDefault() {
        if (instance == null) {
            instance = new FamilyGroupsTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the FamilyGroupsTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized FamilyGroupsTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(FamilyGroupsTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof FamilyGroupsTopComponent) {
            return (FamilyGroupsTopComponent) win;
        }
        Logger.getLogger(FamilyGroupsTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        context = App.center.getSelectedContext(true);
        if (context != null) {
            Gedcom myGedcom = context.getGedcom();
            Entity[] indis = myGedcom.getEntities(Gedcom.INDI, "INDI:NAME");
            HashSet unvisited = new HashSet(Arrays.asList(indis));
            start(indis, unvisited);
        }
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
