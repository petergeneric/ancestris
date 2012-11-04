package ancestris.modules.gedcom.checkduplicates;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.openide.util.Utilities;

@ActionID(id = "ancestris.modules.gedcom.checkduplicates.CheckDuplicateAction", category = "Tools")
@ActionRegistration(iconInMenu = true,
displayName = "#CTL_CheckDuplicateAction",
iconBase = "ancestris/modules/gedcom/checkduplicates/CheckDuplicateIcon.png")
@ActionReference(path = "Menu/Tools/Gedcom")
public final class CheckDuplicateAction implements ActionListener {

    private static final Logger log = Logger.getLogger(CheckDuplicates.class.getName());
    private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
    private RequestProcessor.Task theTask = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        Context context;

        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Gedcom myGedcom = context.getGedcom();
            theTask = RP.create(new CheckDuplicates(myGedcom, myGedcom));
            final ProgressHandle progressHandle = ProgressHandleFactory.createHandle("task thats shows progress", new Cancellable() {

                @Override
                public boolean cancel() {
                    return handleCancel();
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

    private boolean handleCancel() {
        log.log(Level.INFO, "handleCancel");
        if (null == theTask) {
            return false;
        }

        return theTask.cancel();
    }
}
