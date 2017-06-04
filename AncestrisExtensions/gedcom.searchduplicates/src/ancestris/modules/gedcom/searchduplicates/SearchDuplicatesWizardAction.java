package ancestris.modules.gedcom.searchduplicates;

import ancestris.core.actions.AbstractAncestrisContextAction;
import static ancestris.modules.gedcom.searchduplicates.Bundle.CheckDuplicates_runing;
import ancestris.modules.gedcom.utilities.matchers.MatcherOptions;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.*;

@ActionID(id = "ancestris.modules.gedcom.checkduplicates.CheckDuplicatesWizardAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_CheckDuplicatesAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Tools/Gedcom", name = "SearchDuplicatesWizardAction", position = 600)
@NbBundle.Messages({"CTL_CheckDuplicatesAction=Search and Merge of duplicates",
    "CheckDuplicates.runing=Searching duplicates"})
public final class SearchDuplicatesWizardAction extends AbstractAncestrisContextAction {

    private static final Logger log = Logger.getLogger(SearchDuplicatesPlugin.class.getName());
    private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
    private RequestProcessor.Task theTask = null;

    public SearchDuplicatesWizardAction() {
        super();
        setImage("ancestris/modules/gedcom/searchduplicates/CheckDuplicateIcon.png");
        setText(NbBundle.getMessage(SearchDuplicatesWizardAction.class, "CTL_CheckDuplicatesAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        Context contextToOpen = getContext();

        if (contextToOpen != null) {
            Gedcom gedcom = contextToOpen.getGedcom();

            JButton OKButton = new JButton(NbBundle.getMessage(SearchDuplicatesWizardAction.class, "ok_button"));
            SearchDuplicatesPanel panel = new SearchDuplicatesPanel(gedcom, OKButton);
            Object o = DialogManager.create(NbBundle.getMessage(SearchDuplicatesWizardAction.class, "CTL_CheckDuplicatesAction") + " - " + gedcom.getName(), panel)
                    .setMessageType(DialogManager.PLAIN_MESSAGE)
                    .setOptions(new Object[]{OKButton, DialogManager.CANCEL_OPTION})
                    .show();
            if (o != OKButton) {
                return;
            }

            List<String> entities2Ckeck = panel.getEntitiesToCheck();
            TreeMap<String, MatcherOptions> selectedOptions = panel.getSelectedOptions();

            theTask = RP.create(new SearchDuplicatesPlugin(gedcom, entities2Ckeck, selectedOptions));
            final ProgressHandle progressHandle = ProgressHandleFactory.createHandle(CheckDuplicates_runing(), new Cancellable() {

                @Override
                public boolean cancel() {
                    log.log(Level.INFO, "handleCancel");
                    if (null == theTask) {
                        return false;
                    }
                    return theTask.cancel();
                }
            });

            theTask.addTaskListener(
                    new TaskListener() {
                        @Override
                        public void taskFinished(org.openide.util.Task task) {
                            progressHandle.finish();
                        }
                    });
            progressHandle.start();

            // This actually start the task
            theTask.schedule(0);
        }
    }
}
