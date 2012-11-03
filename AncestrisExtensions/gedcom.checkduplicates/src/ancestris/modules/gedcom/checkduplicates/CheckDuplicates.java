package ancestris.modules.gedcom.checkduplicates;

import ancestris.modules.gedcom.utilities.*;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author lemovice left and right entities could be the same.
 */
public class CheckDuplicates implements Runnable {

    private static final Logger log = Logger.getLogger(CheckDuplicates.class.getName());
    private Gedcom leftGedcom;
    private Gedcom rightGedcom;
    private TreeMap<String, EntityMatcher> entitiesMatchers = new TreeMap<String, EntityMatcher>() {

        {
            put(Gedcom.INDI, new IndiMatcher());
            put(Gedcom.FAM, new FamMatcher());
            put(Gedcom.NOTE, new NoteMatcher());
            put(Gedcom.SOUR, new SourceMatcher());
            put(Gedcom.REPO, new RepositoryMatcher());
            put(Gedcom.SUBM, new SubmitterMatcher());
        }
    };

    public CheckDuplicates(Gedcom leftGedcom, Gedcom rightGedcom) {
        this.leftGedcom = leftGedcom;
        this.rightGedcom = rightGedcom;
    }

    @Override
    public void run() {
        final LinkedList <PotentialMatch<? extends Entity>> matchesLinkedList = new LinkedList <PotentialMatch<? extends Entity>>();
        try {
            for (String tag : entitiesMatchers.keySet()) {
                List<? extends Entity> leftEntity = new ArrayList(leftGedcom.getEntities(tag));
                List<? extends Entity> rightEntity = new ArrayList(rightGedcom.getEntities(tag));

                log.log(Level.INFO, "Checking: {0}", tag);

                if (leftEntity != null && rightEntity != null) {
                    List<PotentialMatch<? extends Entity>> potentialMatches = (entitiesMatchers.get(tag)).getPotentialMatches(leftEntity, rightEntity);
                    matchesLinkedList.addAll(potentialMatches);
                }
            }

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    CheckDuplicatesPanel entityViewPanel = new CheckDuplicatesPanel(matchesLinkedList);
                    DialogDescriptor checkDuplicatePanelDescriptor = new DialogDescriptor(
                            entityViewPanel,
                            NbBundle.getMessage(CheckDuplicates.class, "CheckDuplicatePanelDescriptor.title"),
                            true,
                            new Object[]{DialogDescriptor.CLOSED_OPTION},
                            DialogDescriptor.CLOSED_OPTION,
                            DialogDescriptor.DEFAULT_ALIGN,
                            null,
                            null);

                    Dialog dialog = DialogDisplayer.getDefault().createDialog(checkDuplicatePanelDescriptor);
                    dialog.setVisible(true);
                    dialog.setModal(false);
                    dialog.toFront();
                }
            });
        } catch (InterruptedException ex) {
            log.log(Level.INFO, "the task was CANCELLED");
        }
    }
}
