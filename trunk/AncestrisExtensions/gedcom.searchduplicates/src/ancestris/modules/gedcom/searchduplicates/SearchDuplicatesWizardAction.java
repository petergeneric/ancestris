package ancestris.modules.gedcom.searchduplicates;

import static ancestris.modules.gedcom.searchduplicates.Bundle.*;
import ancestris.modules.gedcom.utilities.matchers.Options;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.*;

@ActionID(id = "ancestris.modules.gedcom.checkduplicates.CheckDuplicatesWizardAction", category = "Tools")
@ActionRegistration(iconInMenu = true,
displayName = "#CTL_CheckDuplicatesAction",
iconBase = "ancestris/modules/gedcom/searchduplicates/CheckDuplicateIcon.png")
@ActionReference(path = "Menu/Tools/Gedcom")
@NbBundle.Messages({"CTL_CheckDuplicatesAction=Search Duplicate",
"CheckDuplicates.runing=Searching duplicates"})
public final class SearchDuplicatesWizardAction implements ActionListener {

    private static final Logger log = Logger.getLogger(SearchDuplicatesPlugin.class.getName());
    private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
    private RequestProcessor.Task theTask = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        Context context;

        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {

            List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
            panels.add(new SearchDuplicatesWizardPanel1());
            panels.add(new SearchDuplicatesWizardPanel2());
            String[] steps = new String[panels.size()];
            for (int i = 0; i < panels.size(); i++) {
                Component c = panels.get(i).getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
            WizardDescriptor wizardDescriptor = new WizardDescriptor(new WizardDescriptor.ArrayIterator<WizardDescriptor>(panels));
            // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
            wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
            wizardDescriptor.setTitle(CTL_CheckDuplicatesAction());
            if (DialogDisplayer.getDefault().notify(wizardDescriptor) == WizardDescriptor.FINISH_OPTION) {
                Gedcom myGedcom = context.getGedcom();
                List<String> entities2Ckeck = (List<String>)wizardDescriptor.getProperty("selectedEntities");
                TreeMap<String, ? extends Options> selectedOptions = (TreeMap<String, ? extends Options>)wizardDescriptor.getProperty("selectedOptions");
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
                theTask.addTaskListener(new TaskListener() {

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
