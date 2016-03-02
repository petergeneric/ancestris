package ancestris.modules.gedcom.searchduplicates;

import ancestris.core.actions.AbstractAncestrisContextAction;
import static ancestris.modules.gedcom.searchduplicates.Bundle.CTL_CheckDuplicatesAction;
import static ancestris.modules.gedcom.searchduplicates.Bundle.CheckDuplicates_runing;
import ancestris.modules.gedcom.utilities.matchers.MatcherOptions;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.*;

@ActionID(id = "ancestris.modules.gedcom.checkduplicates.CheckDuplicatesWizardAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_CheckDuplicatesAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Tools/Gedcom", name = "SearchDuplicatesWizardAction", position = 400)
@NbBundle.Messages({"CTL_CheckDuplicatesAction=Search duplicates",
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
            SearchDuplicatesWizardIterator searchDuplicatesWizardIterator = new SearchDuplicatesWizardIterator();
            WizardDescriptor wizardDescriptor = new WizardDescriptor(searchDuplicatesWizardIterator);
            searchDuplicatesWizardIterator.initialize(wizardDescriptor);
            // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
            // {1} will be replaced by WizardDescriptor.Iterator.name()
            wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
            wizardDescriptor.setTitle(CTL_CheckDuplicatesAction());
            if (DialogDisplayer.getDefault().notify(wizardDescriptor) == WizardDescriptor.FINISH_OPTION) {

                TreeMap<String, MatcherOptions> selectedOptions = new TreeMap();
                Gedcom myGedcom = contextToOpen.getGedcom();
                List<String> entities2Ckeck = (List<String>) wizardDescriptor.getProperty("selectedEntities");
                selectedOptions.put(Gedcom.INDI, ((MatcherOptions) wizardDescriptor.getProperty("individualSelectedOptions")));
                selectedOptions.put(Gedcom.FAM, ((MatcherOptions) wizardDescriptor.getProperty("familySelectedOptions")));
                theTask = RP.create(new SearchDuplicatesPlugin(myGedcom, entities2Ckeck, selectedOptions));
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

                //
                progressHandle.start();

                //this actually start the task
                theTask.schedule(0);
            }
        }
    }
}
