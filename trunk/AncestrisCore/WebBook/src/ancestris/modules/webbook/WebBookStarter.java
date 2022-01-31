/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook;

import genj.gedcom.Gedcom;
import java.util.MissingResourceException;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author frederic
 */
public class WebBookStarter {

    private final Gedcom gedcom;
    private final Log log;
    private static RequestProcessor RP = null;
    private RequestProcessor.Task theTask = null;

    // Constructor
    public WebBookStarter(Gedcom gedcom) {
        this.gedcom = gedcom;
        String logname = gedcom.getRegistry().get("webbook.logFile", "");
        log = new Log(logname, NbBundle.getMessage(WebBookStarter.class, "OpenIDE-Module-Name") + " " + gedcom.getDisplayName());
    }

    // Starter
    public synchronized void start() {
        final ProgressHandle ph = ProgressHandle.createHandle(NbBundle.getMessage(WebBookStarter.class, "TASK_WebBookExecution"), () -> handleCancel());

        Runnable runnable = new Runnable() {
            @Override
            public synchronized void run() {
                ph.start();
                execute();
            }

            private void execute() {
                log.write(NbBundle.getMessage(WebBookStarter.class, "TASK_WebBookExecutionStart"));
                try {
                    new WebBook(gedcom, log);
                    if (log.endSuccessful) {
                        log.write(log.NORMAL, NbBundle.getMessage(WebBookStarter.class, "TASK_WebBookExecutionSuccess"));
                    } else {
                        log.write(log.ERROR, NbBundle.getMessage(WebBookStarter.class, "TASK_WebBookExecutionFailed"));
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (MissingResourceException ex) {
                    log.printStackTrace(ex);
                    Thread.currentThread().interrupt();
                }
            }
        };

        if (RP == null) {
            RP = new RequestProcessor("WebBookStarter", 1, true);
        }
        theTask = RP.create(runnable); //the task is not started yet
        theTask.addTaskListener((Task task) -> {
            ph.finish();
            log.close();
        });

        theTask.schedule(0); //start the task
    }

    private boolean handleCancel() {
        if (null == theTask) {
            return false;
        }
        log.write(log.ERROR, NbBundle.getMessage(WebBookStarter.class, "TASK_WebBookExecutionStopped"));
        return theTask.cancel();

    }
}

