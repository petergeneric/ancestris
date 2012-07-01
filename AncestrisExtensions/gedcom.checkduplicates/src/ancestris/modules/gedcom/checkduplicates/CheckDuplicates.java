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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
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
    private TreeMap<String, EntityMatcher> entitiesMatchers = new TreeMap<String, EntityMatcher>();

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
        TreeMap<String, List<PotentialMatch<? extends Entity>>> MatchesMap = new TreeMap<String, List<PotentialMatch<? extends Entity>>>();
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(CheckDuplicates.class, "CheckDuplicates.Check-In-Progress"));
        EntityViewPanel entityViewPanel = null;
        DialogDescriptor checkDuplicatePanelDescriptor = null;
        progressHandle.start();
        for (String tag : entitiesMatchers.keySet()) {
            List<? extends Entity> leftEntity = new ArrayList(leftGedcom.getEntities(tag));
            List<? extends Entity> rightEntity = new ArrayList(rightGedcom.getEntities(tag));

            log.log(Level.INFO, "Checking: {0}", tag);

            progressHandle.progress(MessageFormat.format(NbBundle.getMessage(CheckDuplicates.class, "CheckDuplicates.Checking"), tag));
            if (leftEntity != null && rightEntity != null) {
                List<PotentialMatch<? extends Entity>> potentialMatches = new ArrayList();
                potentialMatches.addAll((entitiesMatchers.get(tag)).getPotentialMatches(leftEntity, rightEntity));
                MatchesMap.put(tag, potentialMatches);
            }
        }
        progressHandle.finish();

        for (String tag : MatchesMap.keySet()) {
            List<PotentialMatch<? extends Entity>> potentialMatches = MatchesMap.get(tag);
            for (PotentialMatch<? extends Entity>match : potentialMatches) {
                entityViewPanel = new EntityViewPanel(match);
                checkDuplicatePanelDescriptor = new DialogDescriptor(
                        entityViewPanel,
                        NbBundle.getMessage(CheckDuplicates.class, "CheckDuplicatePanelDescriptor.title", tag),
                        true,
                        DialogDescriptor.YES_NO_OPTION,
                        null,
                        null);

                Dialog dialog = DialogDisplayer.getDefault().createDialog(checkDuplicatePanelDescriptor);
                dialog.setVisible(true);
                dialog.setModal(false);
                dialog.toFront();
            }
        }
    }
}
