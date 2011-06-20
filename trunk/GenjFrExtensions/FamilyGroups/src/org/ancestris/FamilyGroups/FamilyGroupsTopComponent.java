package org.ancestris.FamilyGroups;

import genj.gedcom.Context;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.view.SelectionSink;
import genjfr.app.App;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.ancestris.FamilyGroups//FamilyGroups//EN",
autostore = false)
public final class FamilyGroupsTopComponent extends TopComponent {

    private static FamilyGroupsTopComponent instance;
    /** alignment options */
    protected final static int ALIGN_LEFT = 0;
    protected final static int ALIGN_CENTER = 1;
    protected final static int ALIGN_RIGHT = 2;
    private Context context;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/ancestris/FamilyGroups/FamilyGroups.png";
    private static final String PREFERRED_ID = "FamilyGroupsTopComponent";
    private int minGroupSize = 0;  // Don't print groups with size less than this
    private int maxGroupSize = 0;
    private String CurrentId = null;

    private class myMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            Gedcom myGedcom = null;
            if (context != null) {
                myGedcom = context.getGedcom();
                if (CurrentId != null && myGedcom != null) {
                    Entity entity = myGedcom.getEntity(CurrentId);
                    if (entity != null) {
                        SelectionSink.Dispatcher.fireSelection(e, new Context(entity));
                    }
                }
            }

        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

    private class MyMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Document doc = familyGroupsTextArea.getDocument();
            Gedcom myGedcom = null;
            String NewId = null;

            if (context != null) {

                myGedcom = context.getGedcom();

                if (myGedcom != null) {
                    try {
                        // do we get a position in the model?
                        int pos = familyGroupsTextArea.viewToModel(e.getPoint());
                        if (pos >= 0) {

                            // scan doc
                            // find ' ' to the left
                            for (int i = 0;; i++) {
                                // stop looking after 10
                                if (i == 10) {
                                    return;
                                }

                                // check for starting line or non digit/character
                                if (pos == 0 || !Character.isLetterOrDigit(doc.getText(pos - 1, 1).charAt(0))) {
                                    break;
                                }

                                // continue
                                pos--;
                            }

                            // find ' ' to the right
                            int len = 0;
                            while (true) {
                                // stop looking after 10
                                if (len == 10) {
                                    return;
                                }
                                // stop at end of doc
                                if (pos + len == doc.getLength()) {
                                    break;
                                }
                                // or non digit/character
                                if (!Character.isLetterOrDigit(doc.getText(pos + len, 1).charAt(0))) {
                                    break;
                                }
                                // continue
                                len++;
                            }

                            // check if it's an ID
                            if (len < 2) {
                                return;
                            }
                            NewId = doc.getText(pos, len);
                            if (myGedcom.getEntity(NewId) == null) {
                                return;
                            }
                            CurrentId = NewId;

                            // mark it
                            // requestFocusInWindow();
                            familyGroupsTextArea.setCaretPosition(pos);
                            familyGroupsTextArea.moveCaretPosition(pos + len);

                            // done
                        }
                    } catch (BadLocationException ble) {
                    }
                }
            }
        }
    }

    private void println(String string) {
        familyGroupsTextArea.append(string + "\n");
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
        StringBuilder buffer = new StringBuilder(length);

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
    private class Tree extends HashSet<Indi> implements Comparable<Tree> {

        private Indi oldestIndividual;

        @Override
        public int compareTo(Tree that) {
            return (that).size() - (this).size();
        }

        @Override
        public String toString() {
            return oldestIndividual.getId()
                    + " " + oldestIndividual.getName()
                    + "(" + oldestIndividual.getBirthAsString() + "-"
                    + oldestIndividual.getDeathAsString() + ")";
        }

        @Override
        public boolean add(Indi indi) {
            // check if oldest
            if (isOldest(indi)) {
                oldestIndividual = indi;
            }
            // continue
            return super.add(indi);
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
        setName(NbBundle.getMessage(FamilyGroupsTopComponent.class, "CTL_FamilyGroupsAction"));
        setToolTipText(NbBundle.getMessage(FamilyGroupsTopComponent.class, "HINT_FamilyGroupsTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        familyGroupsTextArea.addMouseMotionListener(new MyMouseMotionListener());
        familyGroupsTextArea.addMouseListener(new myMouseListener());
    }

    public void start(Indi[] indis, HashSet allIndis) {
        HashSet<Indi> unvisited = new HashSet<Indi>(Arrays.asList(indis));
        List<Tree> trees = new ArrayList<Tree>();

        familyGroupsTextArea.setText("");
//        println(String.format(NbBundle.getMessage(FamilyGroupsTopComponent.class, "FamilyGroupsTopComponent.fileheader"), myGedcom.getName()));
        while (!unvisited.isEmpty()) {
            Indi indi = unvisited.iterator().next();

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

                Tree tree = trees.get(i);

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
                println("\n" + String.format(NbBundle.getMessage(FamilyGroupsTopComponent.class, "CTL_FamilyGroupsAction"), loners, getMinGroupSize()));
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
        Stack<Indi> todos = new Stack<Indi>();
        if (unvisited.remove(indi)) {
            todos.add(indi);
        }

        // loop
        while (!todos.isEmpty()) {

            Indi todo = todos.pop();

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

        familyGroupsScrollPane = new JScrollPane();
        familyGroupsTextArea = new JTextArea();

        setName("Form"); // NOI18N

        familyGroupsScrollPane.setName("familyGroupsScrollPane"); // NOI18N

        familyGroupsTextArea.setColumns(20);
        familyGroupsTextArea.setEditable(false);
        familyGroupsTextArea.setFont(new Font("Monospaced", 0, 12));
        familyGroupsTextArea.setRows(5);
        familyGroupsTextArea.setName("familyGroupsTextArea"); // NOI18N
        familyGroupsScrollPane.setViewportView(familyGroupsTextArea);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(familyGroupsScrollPane, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(familyGroupsScrollPane, GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JScrollPane familyGroupsScrollPane;
    private JTextArea familyGroupsTextArea;
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
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        context = App.center.getSelectedContext(true);
        if (context != null) {
            Gedcom myGedcom = context.getGedcom();
            Indi[] indis = (Indi[] )myGedcom.getEntities(Gedcom.INDI, "INDI:NAME");
            HashSet<Indi> unvisited = new HashSet<Indi>(Arrays.asList(indis));
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
