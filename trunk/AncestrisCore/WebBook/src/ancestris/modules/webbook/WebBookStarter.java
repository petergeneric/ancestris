/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook;

import genj.gedcom.Gedcom;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author frederic
 */
public class WebBookStarter {

    private Gedcom gedcom;
    private Log log;
    private static RequestProcessor RP = null;
    private RequestProcessor.Task theTask = null;

    // Constructor
    public WebBookStarter(Gedcom gedcom) {
        this.gedcom = gedcom;
        String logname = gedcom.getRegistry().get("webbook.logFile", "");
        log = new Log(logname, NbBundle.getMessage(WebBookStarter.class, "OpenIDE-Module-Name") + " " + gedcom.getName());
    }

    // Starter
    public synchronized void start() {
        final ProgressHandle ph = ProgressHandle.createHandle(NbBundle.getMessage(WebBookStarter.class, "TASK_WebBookExecution"), new Cancellable() {

            public boolean cancel() {
                return handleCancel();
            }
        });

        Runnable runnable = new Runnable() {

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
                } catch (Exception ex) {
                    log.printStackTrace(ex);
                    //Exceptions.printStackTrace(ex);
                    Thread.currentThread().interrupt();
                }
            }
        };

        if (RP == null) {
            RP = new RequestProcessor("WebBookStarter", 1, true);
        }
        theTask = RP.create(runnable); //the task is not started yet
        theTask.addTaskListener(new TaskListener() {

            public void taskFinished(Task task) {
                ph.finish();
                log.close();
            }
        });

        theTask.schedule(0); //start the task

        return;
    }

    private boolean handleCancel() {
        if (null == theTask) {
            return false;
        }
        log.write(log.ERROR, NbBundle.getMessage(WebBookStarter.class, "TASK_WebBookExecutionStopped"));
        return theTask.cancel();

    }
}

