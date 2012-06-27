package ancestris.modules.gedcom.checkduplicates;

import ancestris.modules.gedcom.utilities.NoteMatcher;
import ancestris.modules.gedcom.utilities.SubmitterMatcher;
import ancestris.modules.gedcom.utilities.RepositoryMatcher;
import ancestris.modules.gedcom.utilities.FamMatcher;
import ancestris.modules.gedcom.utilities.IndiMatcher;
import ancestris.modules.gedcom.utilities.SourceMatcher;
import ancestris.modules.gedcom.utilities.EntityMatcher;
import ancestris.modules.gedcom.utilities.PotentialMatch;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author lemovice left and right entities could be the same.
 */
public class CheckDuplicates implements Runnable {

    private static final Logger log = Logger.getLogger(CheckDuplicates.class.getName());
    EntityMatcher entityMatcher = null;
    private Gedcom leftGedcom;
    private Gedcom rightGedcom;
    private HashMap<String, EntityMatcher> entitiesMatchers = new HashMap();

    {
        entitiesMatchers.put(Gedcom.INDI, new IndiMatcher());
        entitiesMatchers.put(Gedcom.FAM, new FamMatcher());
        entitiesMatchers.put(Gedcom.NOTE, new NoteMatcher());
        entitiesMatchers.put(Gedcom.SOUR, new SourceMatcher());
        entitiesMatchers.put(Gedcom.REPO, new RepositoryMatcher());
        entitiesMatchers.put(Gedcom.SUBM, new SubmitterMatcher());
    }

    public CheckDuplicates(Gedcom leftGedcom, Gedcom rightGedcom) {
        this.leftGedcom = leftGedcom;
        this.rightGedcom = rightGedcom;
    }

    @Override
    public void run() {
        List<PotentialMatch<? extends Entity>> potentialMatches = new ArrayList();
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(CheckDuplicates.class, "CheckDuplicates.Check-In-Progress"));
        CheckDuplicatePanel openZipBundlePanel = new CheckDuplicatePanel();
        DialogDescriptor checkDuplicatePanelDescriptor = new DialogDescriptor(
                openZipBundlePanel,
                NbBundle.getMessage(CheckDuplicatePanel.class, "CTL_CheckDuplicateAction"),
                true,
                DialogDescriptor.INFORMATION_MESSAGE,
                null,
                null);
        progressHandle.start();
        for (String tag : entitiesMatchers.keySet()) {
            List<? extends Entity> leftEntity = new ArrayList(leftGedcom.getEntities(tag));
            List<? extends Entity> rightEntity = new ArrayList(rightGedcom.getEntities(tag));

            log.log(Level.INFO, "Checking: {0}", tag);

            if (leftEntity != null && rightEntity != null) {
                potentialMatches.addAll((entitiesMatchers.get(tag)).getPotentialMatches(leftEntity, rightEntity));
            }
        }
        progressHandle.finish();

        for (PotentialMatch<? extends Entity> match : potentialMatches) {
            String tmp = match.getLeft().getTag() + " left " + match.getLeft().getId() + " right " + match.getRight().getId() + " %" + match.getCertainty();
            openZipBundlePanel.addElement(tmp);
        }
        Dialog dialog = DialogDisplayer.getDefault().createDialog(checkDuplicatePanelDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
    }
}
